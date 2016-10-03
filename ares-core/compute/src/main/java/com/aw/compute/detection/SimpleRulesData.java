package com.aw.compute.detection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aw.compute.inject.Dependent;
import com.aw.compute.referencedata.AbstractPlatformDocumentData;
import com.aw.compute.referencedata.ReferenceDataList;
import com.aw.document.Document;
import com.aw.document.DocumentType;
import com.aw.platform.NodeRole;
import com.aw.platform.PlatformMgr;

/**
 * Reference data for simple single event match rules
 *
 *
 *
 */
public class SimpleRulesData extends AbstractPlatformDocumentData implements ReferenceDataList<SimpleRule>, Dependent {

	@Override
	public Iterator<SimpleRule> iterator() {

		try {
			check();
		} catch (Exception e) {
			throw new RuntimeException("error checking for simple rule data", e);
		}

		return m_simpleRules.iterator();

	}

	@Override
	public int size() {
		return m_simpleRules == null ? 0 : m_simpleRules.size();
	}

	@Override
	protected DocumentType getType() {
		return DocumentType.SIMPLE_RULE;
	}

	@Override
	protected void refresh(List<Document> documents) throws Exception {

		List<SimpleRule> rules = new ArrayList<SimpleRule>();

		for (Document doc : documents) {

			try {
				rules.add(doc.getBodyAsObject());
			} catch (Exception e) {
				getDependency(PlatformMgr.class).handleException(e, NodeRole.SPARK_WORKER);
			}

		}

		m_simpleRules = rules;

	}

	//our currently active rules
	private List<SimpleRule> m_simpleRules;

}
