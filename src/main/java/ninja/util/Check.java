package ninja.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

public class Check {
	public static void equals( String expected, String actual, String message ) {
		expr( expected.equals( actual ), message );
	}

	public static void expr( boolean expression, String message ) {
		Assert.isTrue( expression, message );
	}

	public static <E extends Enum<E>> E name( Class<E> type, String name, String message ) {
		return nil( EnumUtils.getEnum( type, name ), message );
	}

	public static <T> T first( Stream<T> stream, String message ) {
		return nil( stream.findFirst().orElse( null ), message );
	}

	public static <T> T nil( T value, String message ) {
		return Optional.ofNullable( value ).orElseThrow( () -> new IllegalArgumentException( message ) );
	}

	public static <T> List<T> list( List<T> list, String message ) {
		Assert.notEmpty( list, message );

		return list;
	}

	public static <K, V> Map<K, V> map( Map<K, V> map, String message ) {
		Assert.notEmpty( map, message );

		return map;
	}

	public static String[] params( String text ) {
		return params( text, 2 );
	}

	public static String[] params( String text, int number ) {
		String[] params = StringUtils.split( text );

		expr( params.length == number, "參數個數有誤: " + text );

		return params;
	}
}