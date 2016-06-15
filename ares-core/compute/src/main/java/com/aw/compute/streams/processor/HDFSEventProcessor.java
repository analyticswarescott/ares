package com.aw.compute.streams.processor;

import com.aw.common.exceptions.InitializationException;
import com.aw.common.messaging.Topic;
import com.aw.common.rest.security.TenantAware;
import com.aw.common.spark.StreamDef;
import com.aw.common.system.FileInputMetadata;
import com.aw.common.util.JSONUtils;
import com.aw.common.util.ResourceManager;
import com.aw.compute.inject.Dependent;
import com.aw.compute.streams.exceptions.StreamProcessingException;
import com.aw.compute.streams.processor.framework.JsonTransformer;
import com.aw.compute.streams.processor.framework.JsonTransformerFactory;
import com.aw.compute.streams.processor.framework.StringTupleProcessor;
import com.aw.platform.Platform;
import com.aw.unity.dg.CommonField;
import com.aw.util.Statics;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by scott on 15/06/16.
 */
public class HDFSEventProcessor  implements StringTupleProcessor, HDFSFileProcessor, Dependent, TenantAware {

	protected JsonTransformerFactory json_transformer_factory;

	@Override
	public void processFile(FileInputMetadata metadata, InputStream in) throws Exception {


		String eventGroupString = IOUtils.toString(in, Statics.CHARSET);
		JSONArray rawJsons = new JSONArray(eventGroupString);

		Platform platform = getDependency(Platform.class);

		JsonTransformer xform = null;
		for (int i = 0; i< rawJsons.length(); i++) {
			JSONObject json = rawJsons.getJSONObject(i);

			if (i == 0) {
				xform = json_transformer_factory.getTransformer(json.getString(CommonField.EVENT_TYPE_FIELD));
			}

			List<JSONObject> processedJSON = xform.transform(json);

			//TODO: type to topic map to allow flexibility -- for now all events to both ES and JDBC
			for (JSONObject j : processedJSON) {

				Producer<String, String> producer = ResourceManager.KafkaProducerSingleton.getInstance(platform);

				KeyedMessage<String, String> msg = new KeyedMessage<>(Topic.toTopicString(getTenantID(), Topic.EVENTS_JDBC), getTenantID()
					, JSONUtils.objectToString(j));
					producer.send(msg);

				msg = new KeyedMessage<>(Topic.toTopicString(getTenantID(), Topic.EVENTS_ES), getTenantID(), JSONUtils.objectToString(j));
				producer.send(msg);

			}

		}




	}


	@Override
	public void init(StreamDef data)  {
		try {
			json_transformer_factory = (JsonTransformerFactory) Class.forName(data.getConfigData().get("json_transformer_factory").toString()).newInstance();

		} catch (Exception ex) {
			throw new RuntimeException(" error initializing transformer factory" , ex);
		}

	}
}
