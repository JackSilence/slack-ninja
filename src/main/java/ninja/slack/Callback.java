package ninja.slack;

public class Callback {
	private String token, challenge, type;

	private Event event;

	public String getToken() {
		return token;
	}

	public void setToken( String token ) {
		this.token = token;
	}

	public String getChallenge() {
		return challenge;
	}

	public void setChallenge( String challenge ) {
		this.challenge = challenge;
	}

	public String getType() {
		return type;
	}

	public void setType( String type ) {
		this.type = type;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent( Event event ) {
		this.event = event;
	}
}