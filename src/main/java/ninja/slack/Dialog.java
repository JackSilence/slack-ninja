package ninja.slack;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Dialog {
	private String title;

	@SerializedName( "callback_id" )
	private String id;

	private List<Element> elements;

	public String getTitle() {
		return title;
	}

	public void setTitle( String title ) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public List<Element> getElements() {
		return elements;
	}

	public void setElements( List<Element> elements ) {
		this.elements = elements;
	}
}