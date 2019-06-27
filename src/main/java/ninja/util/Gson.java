package ninja.util;

import java.lang.reflect.Type;

public class Gson {
	private static final com.google.gson.Gson GSON = new com.google.gson.Gson();

	public static <T> T from( String json, Class<T> clazz ) {
		return GSON.fromJson( json, clazz );
	}

	public static <T> T from( String json, Type type ) {
		return GSON.fromJson( json, type );
	}

	public static String json( Object src ) {
		return GSON.toJson( src );
	}
}