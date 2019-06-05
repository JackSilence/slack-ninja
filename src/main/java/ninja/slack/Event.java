package ninja.slack;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Event {
	private String type, subtype, text, channel;

	public String getType() {
		return type;
	}

	public void setType( String type ) {
		this.type = type;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype( String subtype ) {
		this.subtype = subtype;
	}

	public String getText() {
		return text;
	}

	public void setText( String text ) {
		this.text = text;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel( String channel ) {
		this.channel = channel;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString( this, ToStringStyle.JSON_STYLE );
	}
}