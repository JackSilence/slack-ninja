package ninja.util;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class Gson {
	private static final com.google.gson.Gson GSON = new com.google.gson.Gson();

	public static <T> T from( String json, Class<T> clazz ) {
		return GSON.fromJson( json, clazz );
	}

	public static <T> T from( String json, Type type ) {
		return GSON.fromJson( json, type );
	}

	@SuppressWarnings( "unchecked" )
	public static <T> List<T> listOfMaps( String json ) {
		return ( List<T> ) Gson.from( json, new TypeToken<List<Map<String, Object>>>() {
		}.getType() );
	}

	public static List<List<String>> listOfLists( String json ) {
		return Gson.from( json, new TypeToken<List<List<String>>>() {
		}.getType() );
	}

	public static String json( Object src ) {
		return GSON.toJson( src );
	}

	public static JsonObject object( Object src ) {
		return GSON.toJsonTree( src ).getAsJsonObject();
	}
}