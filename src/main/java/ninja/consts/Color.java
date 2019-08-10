package ninja.consts;

public enum Color {
	G( "good" ), Y( "warning" ), R( "danger" ), B( "#3AA3E3" );

	private final String value;

	private Color( String value ) {
		this.value = value;
	}

	public String value() {
		return value;
	}
}