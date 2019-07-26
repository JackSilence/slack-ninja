package ninja.util;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class Gson {
	private static final com.google.gson.Gson GSON = new com.google.gson.Gson();

	public static <T> T from( String json, Class<T> clazz ) {
		return GSON.fromJson( json, clazz );
	}

	public static <T> T from( String json, Type type ) {
		return GSON.fromJson( json, type );
	}

	public static <T> List<T> list( String json ) {
		return Gson.from( json, new TypeToken<List<T>>() {
		}.getType() );
	}

	public static String json( Object src ) {
		return GSON.toJson( src );
	}

	public static JsonElement element( Object src ) {
		return GSON.toJsonTree( src );
	}
}