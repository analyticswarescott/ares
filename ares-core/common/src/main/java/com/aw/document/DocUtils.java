package com.aw.document;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import com.aw.common.system.EnvironmentSettings;
import com.aw.common.tenant.Tenant;
import com.aw.common.util.AWFileUtils;
import com.aw.platform.PlatformMgr;

/**
 * document handler utilities
 *
 *
 *
 */
public class DocUtils {

	public static final Logger LOGGER = Logger.getLogger(DocUtils.class);

    private static final String DEFAULTS_DIR = "/defaults";

    public static void initForTenant(PlatformMgr platformMgr, DocumentHandler handler, boolean bootstrap) throws Exception {

        //test if we have data somehow - for now get 3P count as a way to tell if we've been bootstrapped
        int docCount = handler.getDocumentsOfType(DocumentType.CONFIG_3P).size();


		System.out.println("initForTenant:  CONF_DIRECTORY is " + EnvironmentSettings.getConfDirectory());

        //bootstrap if the tenant is new
        if (docCount == 0) {
			try {
				if (handler.getTenant().getTenantID().equals(Tenant.SYSTEM_TENANT_ID)) {
					handler.setBootstrapping(true);
				}
				if (bootstrap) {
					bootstrap(platformMgr, handler, EnvironmentSettings.getConfDirectory() + DEFAULTS_DIR);
				}

			}
			finally {
				handler.setBootstrapping(false);
			}
        }

    }

    /**
     * Initialize the tenant database with configuration data from the given directory
     *
     * @param rootDir
     * @throws Exception
     */
	protected static void bootstrap(PlatformMgr platformMgr, DocumentHandler handler, String rootDir) throws Exception {

		//structure root>>type>>name to read into empty repository as Tenant 0 (defaults)
		//only a single level structure is currently supported
			try {
				LOGGER.info(" GenericBootstrapper security info: tid:" + handler.getTenant().getTenantID() + " user: " + Tenant.SYSTEM_TENANT_UID);
			} catch (Exception ex) {
				throw new RuntimeException(" security error: tenant or principal is missing ");
			}

			File root = new File(rootDir);

			System.out.println(" DEBUG doc root is " + root.getAbsolutePath());

			String[] directories = root.list(new FilenameFilter() {
				@Override
				public boolean accept(File current, String name) {
					return new File(current, name).isDirectory();
				}
			});
			if (directories == null) {
				LOGGER.warn(" Nothing to bootstrap. No directories found under: " + rootDir);
			}
			for (String dir : directories) {

				File subdir = new File(root.getAbsolutePath() + File.separator + dir);
				String docType = subdir.getName();

				String[] docs = subdir.list();

				if (docs == null) {
					continue;
				}
				for (String doc : docs) {

					File f = new File(subdir.getAbsolutePath() + File.separator + doc);

					JSONObject docJ = AWFileUtils.getFileToJSONObject(f);
					String basename = FilenameUtils.getBaseName(f.getName());

					String docName = basename;

					Document document = new Document(docJ.toString());

					document.setName(docName);

					try {
						document.setType(DocumentType.forValue(docType));
					} catch (Exception e) {
						//ignore unknown types, just warn in the log
						LOGGER.warn("Unknown document type, ignoring: " + docType);
						break;
					}

					if (!handler.documentExists(DocumentType.forValue(docType), docName)) {
						if (document.getDocumentType().isInScope(handler.getTenant())) {
							DocumentEnvelope env = handler.createDocument(document);
							LOGGER.debug("BOOTSTRAP inserted doc " + env.getName() + " assigned ID " + env.getID());
						}
						else {
							LOGGER.debug("BOOTSTRAP skipped out-of-scope (" + document.getDocumentType().getScope() + ") doc :"
								+ document.getName() + " for tenant " + handler.getTenant().getTenantID());
						}
					} else {
						//TODO: assume there would be a different method for installing updates to exsiting docs
					}


				}

			}
		}

}
