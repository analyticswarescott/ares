package com.aw.unity.es;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.rest.security.SecurityAware;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.HttpMethod;
import com.aw.common.util.JSONHandler;
import com.aw.common.util.JSONStreamUtils;
import com.aw.common.util.TimeSource;
import com.aw.common.util.es.ESClient;
import com.aw.common.util.es.ElasticIndex;
import com.aw.platform.Platform;
import com.aw.unity.Data;
import com.aw.util.ListMap;
import com.aw.util.Statics;

/**
 * A general use elasticsearch client. Uses the platform to determine how to connect. This client is NOT thread safe.
 * Each thread talking to elasticsearch should have its own client. In general this is good practice anyway, as each
 * client will randomly order the elasticsearch nodes to determine which node to connect to for the next operation.
 *
 *
 */
public class UnityESClient extends ESClient implements JSONHandler, SecurityAware {

	private static final Logger logger = Logger.getLogger(UnityESClient.class);

	public UnityESClient(Platform platform) {
		super(platform);
	}

	/**
	 * Equivalent to calling bulkInsert(index, null, data) - with no type override, the elasticsearch type will be set to the unity
	 * data type name.
	 *
	 * @param index The index to bulk insert to
	 * @param data The data to bulk insert
	 * @throws Exception
	 */
	public void bulkInsert(Tenant tenant, ElasticIndex index, Iterable<Data> data) throws Exception {
		bulkInsert(tenant, index, null, data);
	}

	/**
	 * Performs a bulk insert into the ES cluster.
	 *
	 * @param index The index being loaded
	 * @param typeOverride The type to force all data to - if null, the unity data type will be assumed to be the elasticsearch type
	 * @param data The data to bulk insert
	 * @throws Exception If anything goes wrong
	 */
	public void bulkInsert(Tenant tenant, ElasticIndex index, String typeOverride, Iterable<Data> data) throws Exception {

		//keep all output files generated for cleanup
		ListMap<String, OutputFile> files = null;
		try {

			//write data to files
			files = writeData(tenant, index, typeOverride, data);

			//for each generated file, bulk insert it
			for (List<OutputFile> tfList : files.values()) {
				for (OutputFile tf : tfList) {

					doInsert(tf, index);

				}
			}

		} catch (Exception e) {

			//log the error and throw it
			logger.error("error writing to elasticsearch", e);
			throw e;

		} finally {

			//always clean up generated bulk insert files
			cleanup(files);

		}

	}

	/**
	 * write data to bulk insert files
	 *
	 * @param tenant
	 * @param typeOverride
	 * @param data
	 * @return
	 * @throws Exception
	 */
	ListMap<String, OutputFile> writeData(Tenant tenant, ElasticIndex index, String typeOverride, Iterable<Data> data) throws Exception {

		ListMap<String, OutputFile> ret = new ListMap<String, OutputFile>();

		for (Data cur : data) {

			//get index file for the given data
			OutputFile out = getOut(ret, tenant, index, cur);
			String type = typeOverride == null ? cur.getType().getName() : typeOverride;
			out.println("{ \"index\" : { \"_type\" : \"" + type + "\", \"_id\" : \"" + cur.getGuid() + "\" } }");
			out.println(cur.toJsonString(false, true, true)); //single line json, epoch milli timestamps, only unity fields

		}

		//close the output stream now so we can pump it into elasticsearch - catch so we at least try to close each writer
		Exception first = null;
		for (List<OutputFile> outList : ret.values()) {
			for (OutputFile out : outList) {
				try {
					out.getWriter().close();
				} catch (Exception e) {
					if (first != null) {
						first = e;
					}
				}
			}
		}

		//if exception occurred, clean up and throw an exception
		if (first != null) {
			throw first;
		}

		return ret;

	}

	private void doInsert(OutputFile outputFile, ElasticIndex index) throws Exception {

		File file = outputFile.getFile();
		logger.debug(" processing file to ES " + file.getAbsolutePath() + " size is: " + file.length());
		String indexName = file.getName().substring(file.getName().lastIndexOf('.') + 1);

		//post it using our standard charset
		String path = "/" + indexName + "/_bulk";

		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(outputFile.getFile()))) {

			HttpResponse resp = execute(HttpMethod.POST, path, in);

			//scan for error messages - avoid excessive memory use by streaming
			m_error = null; //reset error from any previous operation
			m_error_ordinal = 0;

			JSONStreamUtils.processArrayElements("items", resp.getEntity().getContent(), this, null);

			//TODO: maybe log these to hadoop and include a pointer ?
			if (m_error != null) {

				logger.error( " Bulk insert error offending line " + m_error_ordinal + " was " );

				throw new ProcessingException(" ElasticSearch errors detected on bulk insert, first error: " + m_error);

			}

			EntityUtils.consume(resp.getEntity());
			logger.debug(" inserted " + file.length() + " bytes to index " + index);

		}

	}

	/**
	 * get writer for the given tenant/index/data combination
	 *
	 * @param files the file->PrintWriter mapping of current output writers
	 * @param tenant the tenant
	 * @param index the index
	 * @param data the data being written
	 * @return the PrintWriter to use for this data
	 */
	protected OutputFile getOut(ListMap<String, OutputFile> files, Tenant tenant, ElasticIndex index, Data data) throws Exception {

		String strIndex = index.buildIndexFor(tenant, timeFor(data));

		OutputFile file = files.getLastOrNull(strIndex);

		//if we need a new file, create one
		if (file == null || file.getFile().length() > getMaxInsertSize()) {

			file = new OutputFile(strIndex, File.createTempFile("es_load_" + files.size(strIndex), "." + strIndex));
			files.add(strIndex, file);

		}

		return file;

	}

	/**
	 * attempt to delete all generated bulk insert files
	 *
	 * @param files
	 * @throws Exception
	 */
	protected void cleanup(ListMap<String, OutputFile> files) throws Exception {

		Exception first = null;

		for (List<OutputFile> fileList : files.values()) {
			for (OutputFile file : fileList) {
				try {
					 file.getFile().delete();
				} catch (Exception e) {
					if (first == null) {
						first = e;
					}
				}
			}
		}

		if (first != null) {
			throw first;
		}

	}

	/**
	 * This method determines the way we get the timestamp that will determine which index data will be
	 * inserted into. For now we are using current time, meaning the time the data way inserted.
	 *
	 * @param data the data for which index time must be determined
	 * @return the index time for the data
	 */
	Instant timeFor(Data data) {
		return time.now(); //current time, i.e. insertion time
	}

	//if errors occur, we'll save the first reason for an exception message
	private String m_error = null;
	private long m_error_ordinal = 0;

	public long getMaxInsertSize() { return this.maxInsertSize; }
	public void setMaxInsertSize(long maxInsertSize) { this.maxInsertSize = maxInsertSize; }
	private long maxInsertSize = 100_000_000; //100 million byte bulk insert max size

	public void setTimeSource(TimeSource time) { this.time = time; }
	private TimeSource time = TimeSource.SYSTEM_TIME;

	private class OutputFile {

		public OutputFile(String indexName, File file) throws IOException {
			this.file = file;
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Statics.UTF_8)));
		}


		public void println(String data) {

			writer.println(data);

		}

		public File getFile() { return this.file; }
		private File file;

		private String indexName;

		public PrintWriter getWriter() { return this.writer;  }
		private PrintWriter writer;

	}
}
