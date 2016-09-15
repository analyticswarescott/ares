package com.aw.common.disaster;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.Region;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by scott on 15/09/16.
 */
public class DefaultColdStorageProvider {//TODO: extract interface

	private String namespace;


	private S3Broker broker;

	public void init(String namespace) {
		this.namespace = namespace;
		this.broker= new S3Broker();
	}

	public void storeStream(String key, InputStream stream) throws Exception{

		broker.writeStream(namespace, key, stream);

		System.out.println(" Stored data in Cold Storage for DR");
	}

	public List<String> getKeyList() {
		return broker.listKeys(namespace);
	}

	public List<String> getKeyList(String prefix) {
		return broker.listKeys(namespace, prefix);
	}




}
