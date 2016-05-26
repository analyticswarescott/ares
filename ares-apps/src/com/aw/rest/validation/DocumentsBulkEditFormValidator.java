package com.aw.rest.validation;

import com.aw.rest.form.DocumentsBulkEditForm;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.BadRequestException;

/**
 * Validates the information submitted with a {@link DocumentsBulkEditForm} request.
 */
public class DocumentsBulkEditFormValidator implements Validator<DocumentsBulkEditForm> {

	@Override
	public void validate(DocumentsBulkEditForm form) throws BadRequestException {
		if (form == null) {
			throw new BadRequestException("Invalid request");
		}

		// Make sure we have at least one document to edit
		if (form.getDocumentNames() == null || form.getDocumentNames().isEmpty()) {
			throw new BadRequestException("At least one document is required for editing");
		}

		// Make sure that there's at least one property to edit
		final boolean hasGrouping = StringUtils.isNotBlank(form.getGrouping());
		final boolean hasRemoveGrouping = form.isRemoveGrouping();
		final boolean hasTagsToAdd = form.getTags() != null && !form.getTags().isEmpty();
		final boolean hasTagsToRemove = form.getTagsToRemove() != null && !form.getTagsToRemove().isEmpty();
		final boolean hasReadPermission = form.getReadPermission() != null;
		final boolean hasWritePermission = form.getWritePermission() != null;
		if (!(hasGrouping || hasRemoveGrouping || hasTagsToAdd || hasTagsToRemove || hasReadPermission || hasWritePermission)) {
			throw new BadRequestException("At least one property is required for editing");
		}

	}
}
