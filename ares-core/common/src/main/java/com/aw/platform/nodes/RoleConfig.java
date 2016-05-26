package com.aw.platform.nodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.system.EnvironmentSettings;
import com.aw.document.Document;
import com.aw.document.DocumentHandler;
import com.aw.document.LocalDocumentHandler;
import com.aw.document.DocumentType;

/**
 * Retrieves and writes configuration for 3p roles
 */
public class RoleConfig {
 public static final Logger logger = LoggerFactory.getLogger(RoleConfig.class);

    public static final String HASHTAG = "#";
	public static final String FILE_CONTENTS = "file_contents";
	public static final String TEMPLATES_DIR = "templates";

    protected HashMap<String, String> m_overlays;

    private Provider<DocumentHandler> docs;

    public RoleConfig(Provider<DocumentHandler> docs) {
    	this.docs = docs;
	}

    public String getConfigTemplatePath(String docName) throws Exception {

    	Document doc = getTemplateDoc(docName);
    	return EnvironmentSettings.getConfDirectory() + "/" + TEMPLATES_DIR + "/" + doc.getBodyAsObject(ConfigTemplate.class).getFilePath();

    }

    public  String getConfigTemplateContent(String docName) throws Exception{

    	Document d = getTemplateDoc(docName);
		ConfigTemplate template = d.getBodyAsObject(ConfigTemplate.class);
		return template.getFileContents();

    }

    private Document getTemplateDoc(String docName) throws Exception {

    	Document d = null;

		try {
			d = docs.get().getDocument(DocumentType.CONFIG_3P, docName);
		}
		catch (Exception ex) {

			logger.info("connot retrieve config template from document server...trying locally" + ex.getMessage());

			LocalDocumentHandler local = new LocalDocumentHandler(EnvironmentSettings.getConfDirectory() +
			File.separatorChar + "defaults");
			d = local.getDocument(DocumentType.CONFIG_3P, docName);

		}

		return d;

    }

    public  String applyConfig (String template, Map<String, String> lineReplacements, String commentChar) throws Exception {

		StringBuffer ret = new StringBuffer();

		BufferedReader bufReader = new BufferedReader(new StringReader(template));
		String line = null;
		while ((line = bufReader.readLine()) != null) {
			line = line.trim();

			boolean replaced = false;
			if (!line.startsWith(commentChar)) { //TODO: all templates need to have a default setting for replaced items

				for (String contains : lineReplacements.keySet()) {
					if (line.contains(contains)) {
						ret.append("\n");
						ret.append(commentChar + " following line set by DG Node Manager");
						ret.append("\n");
						ret.append(lineReplacements.get(contains));
						ret.append("\n");
						ret.append(commentChar + " above line set by DG Node Manager");
						ret.append("\n");
						replaced = true;
					}
				}
			}

			if (!replaced) {
				ret.append(line);
			}

			ret.append("\n");
		}

		return ret.toString();

    }


    public  String appendZKConfig(String existingConfig, Map<String, String> linesToAdd, String commentChar) throws Exception{

        StringBuffer ret = new StringBuffer();
        ret.append(existingConfig);

        ret.append("\n");
        ret.append(commentChar + " following lines added by DG Node Manager");
        for (String add : linesToAdd.keySet()) {
            ret.append("\n");
            ret.append(add + "=" + linesToAdd.get(add));
            ret.append("\n");
        }
        ret.append("\n");
        ret.append(commentChar + " above lines added by DG Node Manager");


        return ret.toString();
    }

    public  void saveConfig(String path, String content) throws  Exception{
        File f = new File(path);
        //System.out.println(" ############################### saving config to " + path);
        if (!f.exists()) {
            FileUtils.touch(f);
        }
        FileWriter w = new FileWriter(f, false); //overwrite
        w.write(content);
        w.close();

    }

}
