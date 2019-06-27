package ninja.slack;

import java.util.List;


@SuppressWarnings( "unused" )
public class Element {
	public enum Type {
		TEXT, SELECT
	}

	public Element( Type type ) {
		this.type = type.name().toLowerCase();
	}

	private final String type;

	private String label, name, placeholder, value;

	private List<Option> options;

	public Element setLabel( String label ) {
		this.label = label;

		return this;
	}

	public Element setName( String name ) {
		this.name = name;

		return this;
	}

	public Element setPlaceholder( String placeholder ) {
		this.placeholder = placeholder;

		return this;
	}

	public Element setValue( String value ) {
		this.value = value;

		return this;
	}

	public Element setOptions( List<Option> options ) {
		this.options = options;

		return this;
	}
}