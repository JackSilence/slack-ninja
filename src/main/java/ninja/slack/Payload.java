package ninja.slack;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import net.gpedro.integrations.slack.SlackAction;

public class Payload {
	private String type;

	@SerializedName( "callback_id" )
	private String id;

	@SerializedName( "response_url" )
	private String url;

	private Channel channel;

	private List<SlackAction> actions;

	public String getType() {
		return type;
	}

	public void setType( String type ) {
		this.type = type;
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

	public List<SlackAction> getActions() {
		return actions;
	}

	public void setActions( List<SlackAction> actions ) {
		this.actions = actions;
	}
}