package ninja.util;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Jsoup {
	private static final Logger log = LoggerFactory.getLogger( Jsoup.class );

	public static Elements select( String url, String selector ) {
		try {
			return org.jsoup.Jsoup.connect( url ).get().select( selector );

		} catch ( IOException e ) {
			throw new RuntimeException( e );

		}
	}

	public static String href( Element element ) {
		return element.attr( "href" );
	}

	public static void select( String url, String selector, Consumer<? super Element> action ) {
		try {
			select( url, selector ).forEach( action );

		} catch ( RuntimeException e ) {
			log.error( StringUtils.EMPTY, e );

		}
	}
}