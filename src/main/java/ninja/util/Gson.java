package ninja.util;

public class Gson {
	private static final com.google.gson.Gson GSON = new com.google.gson.Gson();

	public static <T> T from( String json, Class<T> clazz ) {
		return GSON.fromJson( json, clazz );
	}

	public static String json( Object src ) {
		return GSON.toJson( src );
	}
}