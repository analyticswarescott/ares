package com.aw.common.hadoop.read;

import com.aw.document.Document;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * Class to allow manipulation of read file input streams
 */
public class FileWrapper {

    protected InputStream _is;
    public void setInputStream(InputStream is) {
      _is = is;
    }
    public InputStream getInputStream() {
        return _is;
    }


	public void writeLocally(String path) throws Exception {
		File targetFile = new File(path);
		FileUtils.copyInputStreamToFile(getInputStream(), targetFile);
	}

    public String getAsString() throws Exception {

        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(_is, writer, StandardCharsets.UTF_8);
            String s = writer.toString();
            return s;
        }
        finally {
            _is.close();
        }
    }


    public Document getAsDocument() throws Exception {

        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(_is, writer, StandardCharsets.UTF_8);
            String s = writer.toString();
            return new Document(s);
        }
        finally {
            _is.close();
        }
    }

    /**
     * @return is this path fully available in the system (no errors getting from the HDFS cluster itself)
     */
    public boolean isFullyAvailable() { return m_fullyAvailable; }
	public void setFullyAvailable(boolean fullyAvailable) { m_fullyAvailable = fullyAvailable; }
	private boolean m_fullyAvailable = false;

	/**
	 * @return If there were errors with HDFS when getting the file, include the exception here
	 */
	public Exception getLastException() { return m_lastException; }
	public void setLastException(Exception lastException) { m_lastException = lastException; }
	private Exception m_lastException;

	/**
	 * @return file size, if available
	 */
	public long getSize() { return this.size;  }
	public void setSize(long size) { this.size = size; }
	private long size;

}
