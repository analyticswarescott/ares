package com.aw.compute.streams.processor.framework;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import com.aw.compute.streams.processor.GenericESProcessor;

public class GroupedProcessorFunctionTest {

	@Test
	public void testSerialization() throws Exception {

		GroupedProcessorFunction function = new GroupedProcessorFunction();
		function.setProcessor(new GenericESProcessor());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out  = new ObjectOutputStream(baos);
		out.writeObject(function);

		out.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream in = new ObjectInputStream(bais);
		Object obj = in.readObject();

		assertEquals(GroupedProcessorFunction.class, obj.getClass());

	}

}
