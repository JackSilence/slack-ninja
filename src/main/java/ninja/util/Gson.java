package ninja.util;

import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class Gson {
	private static final com.google.gson.Gson GSON = new com.google.gson.Gson();

	public static Map<String, Object> map( JsonObject object ) {
		return GSON.fromJson( GSON.toJson( object ), new TypeToken<Map<String, Object>>() {
		}.getType() );
	}

	public static <T> T from( String json, Class<T> clazz ) {
		return GSON.fromJson( json, clazz );
	}

	public static String json( Object src ) {
		return GSON.toJson( src );
	}
}