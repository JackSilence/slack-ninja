package ninja.slack;

import com.google.gson.JsonObject;

public class Confirm {
	private static final String TITLE = "title", TEXT = "text", OK = "ok_text", DISMISS = "dismiss_text";

	private String title, text, ok, dismiss;

	public Confirm( String title, String text, String ok, String dismiss ) {
		this.title = title;
		this.text = text;
		this.ok = ok;
		this.dismiss = dismiss;
	}

	public JsonObject toJson() {
		final JsonObject data = new JsonObject();

		data.addProperty( TITLE, title );
		data.addProperty( TEXT, text );
		data.addProperty( OK, ok );
		data.addProperty( DISMISS, dismiss );

		return data;
	}
}