package ninja.util;

import java.util.List;
import java.util.Map;

public class Cast {
	public static Map<?, ?> map( Map<?, ?> map, String key ) {
		return map( map.get( key ) );
	}

	public static Map<?, ?> map( Object object ) {
		return ( Map<?, ?> ) object;
	}

	public static List<?> list( Map<?, ?> map, String key ) {
		return ( List<?> ) map.get( key );
	}

	public static String string( Map<?, ?> map, String key ) {
		return ( String ) map.get( key );
	}

	public static Double dble( Map<?, ?> map, String key ) {
		return ( Double ) map.get( key );
	}
}