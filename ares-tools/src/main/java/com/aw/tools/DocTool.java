package com.aw.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aw.common.rest.security.DefaultSecurityContext;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

import com.aw.common.rest.security.AuthenticatedUser;
import com.aw.common.rest.security.Impersonation;
import com.aw.common.rest.security.ThreadLocalStore;
import com.aw.common.tenant.Tenant;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.LocalDocumentHandler;
import com.aw.document.DocumentHandlerRest;
import com.aw.document.DocumentType;
import com.aw.platform.PlatformMgr;

/**
 * Used to make direct requests to the api
 *
 *
 *
 */
@SuppressWarnings("static-access")
public class DocTool {

	static Option HOST_PORT = OptionBuilder
			.withDescription("host:port")
			.hasArg()
			.withArgName("host:port")
			.isRequired()
			.create("host_port");

	static Option TENANT = OptionBuilder
			.withDescription("tenant")
			.hasArg()
			.withArgName("tenantID")
			.isRequired()
			.create("tenant");

	static Option FILE = OptionBuilder
			.withDescription("document file path")
			.hasArg()
			.withArgName("path")
			.create("file");

	static Option OPERATION = OptionBuilder
			.withDescription("get create update or sync")
			.hasArg()
			.withArgName("operation")
			.isRequired()
			.create("op");

	static Option TYPE = OptionBuilder
			.withDescription("document type")
			.hasArg()
			.withArgName("type")
			.create("type");

	static Option NAME = OptionBuilder
			.withDescription("document name")
			.hasArg()
			.withArgName("name")
			.create("name");

	static Option OUT = OptionBuilder
			.withDescription("output path")
			.hasArg()
			.withArgName("path")
			.create("out");

	static Option FROM = OptionBuilder
			.withDescription("directory of doc source to update from")
			.hasArg()
			.withArgName("path")
			.create("from");

	private static Options OPTIONS = new Options() { {
		addOption(HOST_PORT);
		addOption(TENANT);
		addOption(FILE);
		addOption(OPERATION);
		addOption(TYPE);
		addOption(NAME);
		addOption(OUT);
		addOption(FROM);
	} };

	enum Operation {
		GET,
		CREATE,
		UPDATE,
		SYNC, //delete ones that are missing
	}

	//represents all of something, for example all tenants
	private static final String ALL = "all";

	private static void updateFrom(PlatformMgr platformMgr, String tenant, String dir, DocumentType type, DocumentHandler docs, boolean deleteMissing) throws Exception {

		DocumentHandler local = new LocalDocumentHandler(dir);

		//for each name of the type, update it using the local doc
		List<Document> newDocs = local.getDocumentsOfType(type);
		List<Document> existingDocs = docs.getDocumentsOfType(type);

		//hash the existing ones to see what we can update
		Map<String, Document> existingMap = new HashMap<String, Document>();
		for (Document existing : existingDocs) {
			existingMap.put(existing.getName(), existing);
		}

		//for each local doc where there is a remote, update it
		for (Document newDoc : newDocs) {

			Document docToApply = existingMap.remove(newDoc.getName());

			if (docToApply == null) {
				//add new
				docToApply = newDoc;
			}

			else {
				//update the body
				docToApply.setBody(newDoc.getBody());
			}


			if (docToApply == newDoc) {
				//add the doc
				DocumentEnvelope added = docs.createDocument(docToApply);
				System.out.println(tenant + ": added " + type + "/" + newDoc.getName() + "/v" + docToApply.getVersion() + " -> v" + added.getVersion());
			} else {
				//update the doc
				DocumentEnvelope updated = docs.updateDocument(docToApply);
				System.out.println(tenant + ": updated " + type + "/" + newDoc.getName() + "/v" + docToApply.getVersion() + " -> v" + updated.getVersion());
			}

		}

		//anything left, delete
		for (Document existing : existingMap.values()) {
			docs.deleteDocument(existing.getDocumentType(), existing.getName());
			System.out.println(tenant + ": deleted " + existing.getDocumentType() + "/" + existing.getName() + "/v" + existing.getVersion());
		}

	}

	public static void main(String[] args) {

		try {

			PlatformMgr platformMgr = new PlatformMgr();
			platformMgr.initialize();

			CommandLine cli = new BasicParser().parse(OPTIONS, args);

			//get the required options
			String hostPort = cli.getOptionValue(HOST_PORT.getOpt());
			String strTenant = cli.getOptionValue(TENANT.getOpt());
			Operation op = Operation.valueOf(cli.getOptionValue(OPERATION.getOpt()).toUpperCase());

			//load the optional options
			Document doc = cli.hasOption(FILE.getOpt()) ? new Document(FileUtils.readFileToString(new File(cli.getOptionValue(FILE.getOpt())))) : null;
			DocumentType type = cli.hasOption(TYPE.getOpt()) ? DocumentType.valueOf(cli.getOptionValue(TYPE.getOpt()).toUpperCase()) : null;
			String name = cli.hasOption(NAME.getOpt()) ? cli.getOptionValue(NAME.getOpt()) : null;
			String out = cli.hasOption(OUT.getOpt()) ? cli.getOptionValue(OUT.getOpt()) : null;
			String from = cli.hasOption(FROM.getOpt()) ? cli.getOptionValue(FROM.getOpt()) : null;

			//get the document handler
			System.setProperty("DG_REPORTING_SERVICE_BASE_URL", "http://" + hostPort);
			DocumentHandler docs = new DocumentHandlerRest(Tenant.SYSTEM_TENANT_ID, platformMgr.getPlatform());

			//get tenants we are applying this to
			List<String> tenants = new ArrayList<String>();
			if (ALL.equals(strTenant)) {
				for (Document curDoc : docs.getAllTenants()) {
					tenants.add(curDoc.getBody().getString("tid"));
				}
			}

			//else just the one tenant
			else {
				tenants.add(strTenant);
			}

			for (String tenant : tenants) {

				docs = new DocumentHandlerRest(tenant, platformMgr.getPlatform());

				//impersonate as the user we need
				ThreadLocalStore.set(new DefaultSecurityContext(new AuthenticatedUser(tenant, "user", "user", "en-us")));
				Impersonation.impersonateTenant(tenant);

				//perform the requested operation
				switch (op) {
					case CREATE:
						if (doc == null) throw new MissingOptionException("operation " + op.name().toLowerCase() + " requires file option");
						docs.createDocument(doc);
						System.out.println(tenant + ": created " + doc.getDocumentType());
						break;
					case GET:
						if (type == null) throw new MissingOptionException("operation " + op.name().toLowerCase() + " requires type option");
						if (name == null) throw new MissingOptionException("operation " + op.name().toLowerCase() + " requires name option");
						if (out == null) throw new MissingOptionException("operation " + op.name().toLowerCase() + " requires out option");
						FileUtils.write(new File(out), docs.getDocument(type, name).toJSON(), false);
						System.out.println(tenant + ": document written to " + out);
						break;
					case UPDATE:
						if (from == null && doc == null) throw new MissingOptionException("operation " + op.name().toLowerCase() + " requires either from or file option");
						if (from != null) {
							if (type == null) throw new MissingOptionException("operation " + op.name().toLowerCase() + " requires type option");
							updateFrom(platformMgr, tenant, from, type, docs, false);
						} else {
							docs.updateDocument(doc);
							System.out.println(tenant + ": updated " + doc.getDocumentType() + " " + doc.getName());
						}
						break;
					case SYNC:
						if (type == null) throw new MissingOptionException(tenant + ": operation " + op.name().toLowerCase() + " requires type option");
						if (from == null) throw new MissingOptionException(tenant + ": operation " + op.name().toLowerCase() + " requires either from option");
						updateFrom(platformMgr, tenant, from, type, docs, true);
						break;
				}

			}

		} catch (ParseException e) {

			System.out.println(e.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("docTool", OPTIONS);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
