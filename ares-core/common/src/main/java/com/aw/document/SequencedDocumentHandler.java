package com.aw.document;

import com.aw.document.action.Operation;

/**
 * A document handler that performs its modifications as sequenced operations and can apply those
 * operations from other document handlers that do the same.
 *
 *
 *
 */
public interface SequencedDocumentHandler extends DocumentHandler {

	/**
	 *
	 * Method to apply a mirrored operation from a peer node
	 *
	 * @param doc
	 * @return Document Envelope
	 * @throws Exception
	 *
	 */
	public void acceptMirrorUpdate(Document doc, Operation op) throws Exception;

	/**
	 * Retrieve a document by op sequence for sync purposes
	 * @param opSequenceKey
	 * @param opSequence
	 * @return
	 * @throws Exception
	 */
	public Document getDocumentBySequence(String opSequenceKey, long opSequence) throws Exception;

	/**
	 * write an op sequence
	 *
	 * @param doc the document whose op sequence should be written
	 * @throws Exception if anything goes wrong
	 */
	public void writeOpSequence(DocumentEnvelope doc) throws Exception;

	/**
	 * get an op sequence
	 *
	 * @param opSequenceKey the op sequence key to get
	 * @return the op sequence
	 * @throws Exception if anything goes wrong
	 */
	public long getOpSequence(String opSequenceKey) throws Exception;

	/**
	 * get the id for the sequence key
	 *
	 * @param opSequenceKey the key whose op sequence id is needed
	 * @param opSequence the op sequence value
	 * @return the op sequence id
	 * @throws Exception if anything goes wrong
	 */
	public String getIdForOpSequence(String opSequenceKey, long opSequence) throws Exception;

}
