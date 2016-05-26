/*
package com.aw.compute.streams.processor.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.aw.common.TestPlatform;
import com.aw.common.spark.StreamDef;
import com.aw.common.util.HttpMethod;
import com.aw.compute.inject.ComputeInjector;
import com.aw.compute.inject.TestComputeModule;
import com.aw.compute.streams.processor.GenericESProcessor;
import com.aw.unity.es.UnityESClient;
import com.aw.util.Statics;

public class GenericESProcessorTest {

	@Test
	public void test() throws Exception {

		ComputeInjector.init(new TestComputeModule());

		List<String> postData = new ArrayList<String>();

		//disable elasticsearch and platform start for this test
		System.setProperty(Statics.PROP_ELASTICSEARCH_ENABLED, "false");
		System.setProperty(Statics.PROP_PLATFORM_START_ENABLED, "false");

		GenericESProcessor processor = new GenericESProcessor() {

			//set up a test client to trap what we're trying to post
			public com.aw.unity.es.UnityESClient getClient() {
				return new UnityESClient(new TestPlatform()) {
					public org.apache.http.HttpResponse execute(HttpMethod method, String path, InputStream in) throws Exception {
						postData.add(IOUtils.toString(in));
						return new BasicHttpResponse(new StatusLine() {

							@Override
							public int getStatusCode() {
								return 200;
							}

							@Override
							public String getReasonPhrase() {
								return "test reason";
							}

							@Override
							public ProtocolVersion getProtocolVersion() {
								return null;
							}

						}) {
							@Override
							public HttpEntity getEntity() {
								try {
									return new StringEntity("{}");
								} catch (Exception e) {
									throw new RuntimeException("error building test response");
								}
							}
						};
					}

				};

			}

		};

		processor.init(new StreamDef("index_name", "events"));

		processor.process("1", Arrays.asList(new String[] {
			"{\n" +
			"    \"uad_meid\": \"215d49b2-e63a-1035-e909-806e7fbe53d5\",\n" +
			"    \"uad_medid\": \"08e724ed-45f8-11e5-8105-0aca84fb90cb\",\n" +
			"    \"uad_dir\": \"false\",\n" +
			"    \"uad_sir\": \"false\",\n" +
			"    \"uad_dvn\": \"0\",\n" +
			"    \"uad_seq\": \"3\",\n" +
			"    \"uad_ot\": \"1\",\n" +
			"    \"uad_svn\": \"0\",\n" +
			"    \"ua_aa\": \"false\",\n" +
			"    \"pi_cn\": \"google inc.\",\n" +
			"    \"pi_dn\": \"WIN-VL6D9UUAJI2\",\n" +
			"    \"pi_fcl\": 1308385862,\n" +
			"    \"pi_fd\": \"google chrome\",\n" +
			"    \"pi_fml\": 1308385862,\n" +
			"    \"pi_fp\": \"c:\\\\program files (x86)\\\\google\\\\chrome\\\\application\\\\chrome.exe\",\n" +
			"    \"pi_fv\": \"44.0.2403.155\",\n" +
			"    \"pi_lc\": \"copyright 2012 google inc. all rights reserved.\",\n" +
			"    \"pi_md5\": \"1d4020f8-270d-c354-a78c-707927058a41\",\n" +
			"    \"pi_pn\": \"google chrome\",\n" +
			"    \"pi_pv\": \"44.0.2403.155\",\n" +
			"    \"pi_seq\": \"18\",\n" +
			"    \"pi_sha1\": \"BC8D2EA34CE6F5E648C1F267E8C38C4E1B22F2FC\",\n" +
			"    \"pi_sha256\": \"EF7544448D6AD8C8B41F71E767101ABF7A496B84130CFCD9004836F46C1D7D18\",\n" +
			"    \"pi_sid\": \"S-1-5-21-577109202-508972849-2764107374-500\",\n" +
			"    \"pi_un\": \"Administrator\",\n" +
			"    \"pi_v\": \"1024\",\n" +
			"    \"ua_ar\": \"false\",\n" +
			"    \"ua_dc\": \"1\",\n" +
			"    \"ua_dn\": \"WIN-VL6D9UUAJI2\",\n" +
			"    \"ua_eel\": 1308441063,\n" +
			"    \"ua_eeu\": 1308441063,\n" +
			"    \"ua_esl\": 1308441063,\n" +
			"    \"ua_esu\": 1308441063,\n" +
			"    \"ua_hc\": \"false\",\n" +
			"    \"ua_md\": \"false\",\n" +
			"    \"ua_md5\": \"1d4020f8-270d-c354-a78c-707927058a41\",\n" +
			"    \"ua_meid\": \"215d49b2-e63a-1035-e909-806e7fbe53d5\",\n" +
			"    \"ua_ob\": \"false\",\n" +
			"    \"ua_ot\": \"14\",\n" +
			"    \"ua_ph\": \"737fcf6a-d501-01d0-680c-000080b317b3\",\n" +
			"    \"ua_pki\": \"false\",\n" +
			"    \"ua_pt\": \"0\",\n" +
			"    \"ua_seq\": \"2\",\n" +
			"    \"ua_sid\": \"S-1-5-21-577109202-508972849-2764107374-500\",\n" +
			"    \"ua_tfs\": \"247090\",\n" +
			"    \"ua_un\": \"Administrator\",\n" +
			"    \"ua_vs\": \"true\",\n" +
			"    \"ua_wb\": \"false\",\n" +
			"    \"dg_bid\": \"eb010c88-45fa-11e5-8105-0aca84fb90cb\",\n" +
			"    \"dg_mid\": \"b5dc5bc2-fe51-8689-b9d8-47098c04082b\",\n" +
			"    \"dg_utype\": \"user_cd_burn\"\n" +
			"}"
		}));

		//check the posted data
		assertEquals("should only be one POSTed payload from the ES bundle processor", 1, postData.size());
		BufferedReader br = new BufferedReader(new StringReader(postData.get(0)));

		//first line should be index operation
		assertEquals("index operation line wrong", new JSONObject("{\"index\":{\"_type\":\"user_cd_burn\",\"_id\":\"08e724ed-45f8-11e5-8105-0aca84fb90cb\"}}").toString(), new JSONObject(br.readLine()).toString());
		//make sure the next line parses, at least
		assertTrue("payload for event doesn't contain any fields", new JSONObject(br.readLine()).length() > 0);

	}

}
*/
