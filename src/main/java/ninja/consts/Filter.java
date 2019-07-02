package ninja.consts;

import org.apache.commons.lang3.StringUtils;

public enum Filter {
	ROUTE, STATION;

	public String name( String keyword ) {
		return String.format( "$filter=%sName/Zh_tw eq '%s'", StringUtils.capitalize( name().toLowerCase() ), keyword );
	}
}