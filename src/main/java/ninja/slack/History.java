package ninja.slack;

import java.util.List;

public class History {
	private List<Event> messages;

	public List<Event> getMessages() {
		return messages;
	}

	public void setMessages( List<Event> messages ) {
		this.messages = messages;
	}
}