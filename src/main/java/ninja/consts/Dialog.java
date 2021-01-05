package ninja.consts;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import ninja.util.Utils;

public enum Dialog {
	WEATHER( "district", "hours" ), BUS( "stop" ), STATION( "start", "end" ), THSR( "start", "end", "date", "time", "way" ), AQI( "site" ), MRT( "start", "end" ), THEATER( "theater" ), MOVIE( "film" ), TYPHOON( "area" ), CLEAR( "name" ), VAS( "division" );

	private final String[] key;

	private Dialog( String... key ) {
		this.key = key;
	}

	public String text( Map<String, String> submission ) {
		return Utils.join( Arrays.stream( key ).map( submission::get ), StringUtils.SPACE );
	}
}