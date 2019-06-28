package ninja.util;

import java.io.IOException;
import java.util.function.Consumer;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Jsoup {
	private static final Logger log = LoggerFactory.getLogger( Jsoup.class );

	public static Document get( String url ) {
		try {
			return org.jsoup.Jsoup.connect( url ).get();

		} catch ( IOException e ) {
			throw new RuntimeException( e );

		}
	}

	public static void select( String url, String css, Consumer<? super Element> action ) {
		try {
			get( url ).select( css ).forEach( action );

		} catch ( RuntimeException e ) {
			log.error( "", e );

		}
	}
}