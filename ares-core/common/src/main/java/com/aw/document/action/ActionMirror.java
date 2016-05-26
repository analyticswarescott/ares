package com.aw.document.action;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aw.common.rest.security.TenantAware;
import com.aw.common.system.EnvironmentSettings;
import com.aw.document.Document;
import com.aw.document.DocumentHandlerRest;
import com.aw.document.SequencedDocumentHandler;
import com.aw.platform.NodeRole;
import com.aw.platform.Platform;
import com.aw.platform.PlatformNode;

/**
 * Handler class to formulate path and name then call FileWriter to mirror to Hadoop
 */
public class ActionMirror implements IAction, TenantAware {
    public static final Logger logger = LoggerFactory.getLogger(ActionMirror.class);

    public static final String MIRROR_FILENAME_DELIMITER  = "$";

    @Override
    public void doAction(Platform platform, Document doc, Operation operation) throws Exception {

    	//if we aren't sequencing documents, don't apply the mirror action
    	if (!(doc.getDocHandler() instanceof SequencedDocumentHandler)) {
    		logger.debug("ActionMirror.doAction(): doc handler not sequenced, not mirroring");
    		return;
    	}

		//TODO: testing to see if localhost<>DGHOST is an issue
    	else if (platform.getNode("localhost") != null) {
			logger.debug(" mirroring not valid in single-node mode");
			return;
		}

		String thisHost = EnvironmentSettings.getHost();

		//call each other node in the platform
		List<PlatformNode> restNodes = platform.getNodes(NodeRole.REST);

		int optimal = restNodes.size() -1;
		int successes = 0;


		for (PlatformNode restNode : restNodes) {
			if (!restNode.getHost().equals(thisHost)) {
				logger.debug("current host " + thisHost + ": mirroring sequence " + doc.getOpSequence() + " on document " + doc.getKey() + " to " + restNode.getHost());

				try {
					DocumentHandlerRest target = new DocumentHandlerRest(getTenantID(), restNode);
					target.acceptMirrorUpdate(doc, operation);
					successes++;
				}
				catch (Exception ex) {//TODO: handle expected messages differently
					logger.warn("current host " + thisHost + ": target node " + restNode.getHost() + " mirror attempt failed on doc " + doc.getKey() + " with message " + ex.getMessage());
				}

			}
		}

		//now decide if the results warrant an error TODO -- implement write ack policy

		if (optimal == 0) {
			return; //nothing to worry about
		}
		else {
			if (successes < optimal) {
				logger.warn(" not all defined nodes accepted mirror ");
			}
		}



    }

}
