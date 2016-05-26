package com.aw.compute.referencedata;

import java.util.List;

import com.aw.common.exceptions.ProcessingException;
import com.aw.common.rest.security.TenantAware;
import com.aw.compute.inject.Dependent;
import com.aw.document.Document;
import com.aw.document.DocumentEnvelope;
import com.aw.document.DocumentHandler;
import com.aw.document.DocumentType;

/**
 * Platform document reference data. This framework class will take care of detecting changes in the documents using envelopes. If
 * changes have occurred, the full document list will be retrieved and handed to the subclass for refreshing via the refresh method.
 *
 *
 *
 */
public abstract class AbstractPlatformDocumentData extends AbstractReferenceData implements TenantAware, Dependent {

	/**
	 * @return The type of documents we are looking for
	 */
	protected abstract DocumentType getType();

	@Override
	protected void onTTLExpired() throws ProcessingException {

		try {

			//check if we have changed
			DocumentHandler docs = getDocs();

			List<DocumentEnvelope> envelopes = docs.getEnvelopesOfType(getType());

			if (m_currentList == null || !m_currentList.equals(envelopes)) {

				//in this case do a refresh
				refresh(docs.getDocumentsOfType(getType()));

			}

			m_currentList = envelopes;

		} catch (Exception e) {
			throw new ProcessingException("error updating reference data for tenant " + getTenantID() + ", document type " + getType(), e);
		}

	}

	/**
	 * For testability we make the doc handler a get method
	 *
	 * @return The document handler
	 */
	protected DocumentHandler getDocs() {
		return getDependency(DocumentHandler.class);
	}

	/**
	 * Update reference data given a new set of documents
	 *
	 * @param documents
	 * @throws ProcessingException
	 */
	protected abstract void refresh(List<Document> documents) throws Exception;

	//the currently active list of documents in this reference data
	private List<DocumentEnvelope> m_currentList;

}
