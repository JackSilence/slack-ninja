package ninja.slack;

import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

import net.gpedro.integrations.slack.SlackAction;

public class Payload {
	private String type, name, value, state;

	@SerializedName( "callback_id" )
	private String id;

	@SerializedName( "response_url" )
	private String url;

	private Channel channel;

	private User user;

	private Map<String, String> submission;

	private List<SlackAction> actions;

	public String getType() {
		return type;
	}

	public void setType( String type ) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue( String value ) {
		this.value = value;
	}

	public String getState() {
		return state;
	}

	public void setState( String state ) {
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl( String url ) {
		this.url = url;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel( Channel channel ) {
		this.channel = channel;
	}

	public User getUser() {
		return user;
	}

	public void setUser( User user ) {
		this.user = user;
	}

	public Map<String, String> getSubmission() {
		return submission;
	}

	public void setSubmission( Map<String, String> submission ) {
		this.submission = submission;
	}

	public List<SlackAction> getActions() {
		return actions;
	}

	public void setActions( List<SlackAction> actions ) {
		this.actions = actions;
	}
}