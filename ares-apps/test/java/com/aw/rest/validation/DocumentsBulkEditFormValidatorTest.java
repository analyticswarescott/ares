package com.aw.rest.validation;

import com.aw.rest.form.DocumentsBulkEditForm;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import java.util.Arrays;

/**
 * Verifies the validation logic in the {@link DocumentsBulkEditFormValidator} class.
 */
public class DocumentsBulkEditFormValidatorTest {

	private final DocumentsBulkEditFormValidator validator = new DocumentsBulkEditFormValidator();

	@Test(expected = BadRequestException.class)
	public void testNull() throws Exception {
		validator.validate(null);
	}

	@Test(expected = BadRequestException.class)
	public void testAtLeastOnePropertyRequired() throws Exception {
		validator.validate(new DocumentsBulkEditForm());
	}

	@Test(expected = BadRequestException.class)
	public void testAtLeastOneDocumentNameRequired() throws Exception {
		final DocumentsBulkEditForm form = new DocumentsBulkEditForm();
		form.setGrouping("testing"); // passes the one property rule
		validator.validate(form);	// fails the 'one document' rule
	}

	@Test
	public void testValidation() throws Exception {
		final DocumentsBulkEditForm form = new DocumentsBulkEditForm();
		form.setGrouping("testing");
		form.setDocumentNames(Arrays.asList("test1", "test2"));
		validator.validate(form);	// no exceptions thrown
	}
}