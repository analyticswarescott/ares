package com.aw.document;

/**
 * Listen for document events
 *
 *
 *
 */
public interface DocumentListener {

	/**
	 * @param document The document added
	 */
	public void onDocumentCreated(Document document) throws Exception;

	/**
	 * @param document The document updated
	 */
	public void onDocumentUpdated(Document document) throws Exception;

	/**
	 * @param type The document deleted
	 * @param name
	 */
	public void onDocumentDeleted(Document document) throws Exception;

}
