package com.aw.rest.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import com.aw.action.RootActionFactory;
import com.aw.action.json.ActionModule;
import com.aw.common.json.DefaultModule;
import com.aw.common.util.JSONUtils;
import com.aw.document.DocumentHandler;
import com.aw.document.body.IBodyInitializable;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Custom json serializer to stay consistent with our JSONUtils methods.
 *
 *
 */
@javax.ws.rs.ext.Provider
public class JSONProvider extends JacksonJsonProvider {

	//TODO: define this once, globally
	public static final Charset CHARSET = Charset.forName("UTF-8");

	private SimpleModule actionModule;
	private SimpleModule defaultModule;
	private Provider<DocumentHandler> docs;

	@Inject
	public JSONProvider(RootActionFactory factory, Provider<DocumentHandler> docs) {
		actionModule = new ActionModule(factory);
		defaultModule = new DefaultModule();
		this.docs = docs;
	}

	@Override
	public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException {
		try {
			byte[] out = JSONUtils.objectToString(value, false, false, false, actionModule, defaultModule).getBytes(CHARSET);
			entityStream.write(out);
		} catch (Exception e) {
			throw new IOException("error serializing type " + type, e);
		}
	}

	@Override
	public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		try {

			//read the json
			String strJson = IOUtils.toString(entityStream);

			//accept only unity format dates
			Object ret = JSONUtils.objectFromString(strJson, type, false, false, actionModule);

			//initialize documents if applicable
			if (ret instanceof IBodyInitializable) {
				((IBodyInitializable)ret).initialize(strJson, docs.get());
			}

			return ret;

		} catch(IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

}
