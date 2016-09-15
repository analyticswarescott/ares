package com.aw.common.disaster;

import com.aw.common.rest.security.TenantAware;

import java.io.InputStream;
import java.util.List;

/**
 * Created by scott on 15/09/16.
 */
public class DefaultColdStorageProvider implements TenantAware {//TODO: extract interface if other than s3 is ever needed

	private String namespacePrefix;


	private S3Broker broker;//TODO: extract interface if other than s3 is ever needed

	public void init(String namespacePrefix) {
		this.namespacePrefix = namespacePrefix;
		this.broker= new S3Broker();
	}

	public void storeStream(String key, InputStream stream) throws Exception{

		String fullNamespace = namespacePrefix + "-" + getTenantID();

		//ensure the desired namespace is created
		broker.ensureNamespace(fullNamespace);

		broker.writeStream(fullNamespace, key, stream);
		System.out.println(" Stored data " + key + " in Cold Storage namespace " + fullNamespace + "  for DR");
	}

	public List<String> getKeyList() {
		return broker.listKeys(namespacePrefix);
	}

	public List<String> getKeyList(String prefix) {
		return broker.listKeys(namespacePrefix, prefix);
	}




}
