package ninja.slack;

import com.google.gson.JsonObject;

import net.gpedro.integrations.slack.SlackAction;
import net.gpedro.integrations.slack.SlackActionType;

public class Action extends SlackAction {
	private static final String CONFIRM = "confirm";

	private Confirm confirm;

	public Action( String name, String text, SlackActionType type, String value ) {
		super( name, text, type, value );
	}

	public Action setConfirm( Confirm confirm ) {
		this.confirm = confirm;

		return this;
	}

	@Override
	public JsonObject toJson() {
		final JsonObject data = super.toJson();

		if ( confirm != null ) {
			data.add( CONFIRM, confirm.toJson() );
		}

		return data;
	}
}