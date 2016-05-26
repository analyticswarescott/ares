package com.aw.incident;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aw.common.auth.DefaultUser;

public class DefaultCommentTest {

	@Test
	public void test() {

		DefaultComment comment1 = new DefaultComment(new DefaultUser("test_user"), "test comment 1");
		DefaultComment comment2 = new DefaultComment(new DefaultUser("test_user"), "test comment 2");

		//should not be equal
		assertNotEquals("different comments should not be equal", comment1, comment2);

	}

}
