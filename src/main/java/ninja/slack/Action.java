package ninja.slack;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import net.gpedro.integrations.slack.SlackAction;
import net.gpedro.integrations.slack.SlackActionType;
import ninja.consts.Act;
import ninja.util.Gson;

public class Action extends SlackAction {
	private Confirm confirm;

	private Set<Map<String, String>> options = new LinkedHashSet<>();

	@SerializedName( "selected_options" )
	private List<Map<String, String>> selected;

	public Action( Act act, String text, SlackActionType type, String value ) {
		super( act.name(), text, type, value );
	}

	public Confirm getConfirm() {
		return confirm;
	}

	public Action setConfirm( Confirm confirm ) {
		this.confirm = confirm;

		return this;
	}

	public void addOption( Map<String, String> option ) {
		this.options.add( option );
	}

	public List<Map<String, String>> getSelected() {
		return selected;
	}

	public void setSelected( List<Map<String, String>> selected ) {
		this.selected = selected;
	}

	@Override
	public JsonObject toJson() {
		var data = Gson.object( this );

		super.toJson().entrySet().forEach( i -> data.add( i.getKey(), i.getValue() ) );

		return data;
	}
}