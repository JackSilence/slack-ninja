package ninja.slack;

import com.google.gson.annotations.SerializedName;

public class Confirm {
	private String title, text;

	@SerializedName( "ok_text" )
	private String ok;

	@SerializedName( "dismiss_text" )
	private String dismiss;

	public Confirm() {
	}

	public Confirm( String title, String text, String ok, String dismiss ) {
		this.title = title;
		this.text = text;
		this.ok = ok;
		this.dismiss = dismiss;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle( String title ) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText( String text ) {
		this.text = text;
	}

	public String getOk() {
		return ok;
	}

	public void setOk( String ok ) {
		this.ok = ok;
	}

	public String getDismiss() {
		return dismiss;
	}

	public void setDismiss( String dismiss ) {
		this.dismiss = dismiss;
	}
}