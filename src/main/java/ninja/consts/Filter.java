package ninja.consts;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

public enum Filter {
	ROUTE( true ), STOP( true ), STATION( true ), DIRECTION( false );

	private static final String AND = " and ", OR = " or ";

	private boolean name;

	private Filter( boolean name ) {
		this.name = name;
	}

	public String eq( String keyword ) {
		return format( "%s eq '%s'", keyword );
	}

	public String le( String keyword ) {
		return format( "%s le '%s'", keyword );
	}

	public String contains( String keyword ) {
		return format( "contains(%s,'%s')", keyword );
	}

	public static String and( String... filter ) {
		return String.join( AND, filter );
	}

	public static String or( String... filter ) {
		return String.join( OR, filter );
	}

	@Override
	public String toString() {
		return CaseFormat.UPPER_UNDERSCORE.to( CaseFormat.UPPER_CAMEL, name() ).concat( this.name ? "Name/Zh_tw" : StringUtils.EMPTY );
	}

	private String format( String format, String keyword ) {
		return String.format( format, this, keyword );
	}
}