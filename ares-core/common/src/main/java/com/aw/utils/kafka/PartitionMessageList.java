package com.aw.utils.kafka;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.aw.util.Statics;

import kafka.message.MessageAndOffset;

public class PartitionMessageList {

	public List<String> getData() { return this.data;  }
	public void setData(List<String> data) { this.data = data; }
	private List<String> data = new ArrayList<String>();

	public long getNextOffset() { return this.nextOffset;  }
	public void setNextOffset(long lastOffset) { this.nextOffset = lastOffset; }
	private long nextOffset;

	public void add(MessageAndOffset msgAndOffset) {

		//get the data as a string
		ByteBuffer payload = msgAndOffset.message().payload();
		byte[] bytes = new byte[payload.limit()];
		msgAndOffset.message().payload().get(bytes);
		data.add(new String(bytes, Statics.CHARSET));

		//save the next offset
		nextOffset = msgAndOffset.nextOffset();

	}

	public boolean isEmpty() {
		return data.isEmpty();
	}

	public int size() {
		return data.size();
	}

}
