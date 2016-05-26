package com.aw.utils.kafka;

import java.util.List;

public class TopicMessageList {

	public List<String> getMessages() { return this.messages;  }
	public void setMessages(List<String> messages) { this.messages = messages; }
	private List<String> messages;

	public void addAll(TopicMessageList messageSet) {
		this.messages.addAll(messageSet.messages);
	}
}
