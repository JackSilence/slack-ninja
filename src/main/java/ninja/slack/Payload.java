package ninja.slack;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import net.gpedro.integrations.slack.SlackAction;

public class Payload {
	private String type, token;

	@SerializedName( "callback_id" )
	private String id;

	private List<SlackAction> actions;

	public String getType() {
		return type;
	}

	public void setType( String type ) {
		this.type = type;
	}

	public String getToken() {
		return token;
	}

	public void setToken( String token ) {
		this.token = token;
	}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public List<SlackAction> getActions() {
		return actions;
	}

	public void setActions( List<SlackAction> actions ) {
		this.actions = actions;
	}
}