package ninja.slack;

import java.util.Map;

import com.google.gson.JsonObject;

import net.gpedro.integrations.slack.SlackAction;
import net.gpedro.integrations.slack.SlackActionType;
import ninja.util.Gson;

public class Action extends SlackAction {
	private static final String CONFIRM = "confirm";

	private Map<?, ?> confirm;

	public Action( String name, String text, SlackActionType type, String value ) {
		super( name, text, type, value );
	}

	public Action setConfirm( Map<?, ?> confirm ) {
		this.confirm = confirm;

		return this;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject data = super.toJson();

		if ( confirm != null ) {
			data.add( CONFIRM, Gson.element( confirm ).getAsJsonObject() );
		}

		return data;
	}
}