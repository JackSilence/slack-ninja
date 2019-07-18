package ninja.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import ninja.util.Jsoup;

@RestController
public class MetroController extends BaseController {
	private static final String URL = "https://m.metro.taipei/pda_ticket_price_time.asp";

	private static final String QUERY = "?s1elect=%s&s2elect=%s&action=query", TITLE = "捷運票價及乘車時間";

	private static final Map<String, String> STATIONS = new HashMap<>();

	@PostMapping( "/mrt" )
	public String mrt( @RequestParam String command, @RequestParam String text ) {
		log.info( "Text: {}", text );

		if ( STATIONS.isEmpty() ) {
			init();
		}

		try {
			String[] params = StringUtils.split( text );

			check( params.length == 2, "起訖站皆須輸入" );

			String start = find( params[ 0 ] ), end = find( params[ 1 ] ), url, txt;

			check( !start.equals( end ), "起訖站不得相同: " + text );

			log.info( "Start: {}, end: {}", start, end );

			Elements tables = Jsoup.get( url = URL.concat( String.format( QUERY, start, end ) ) ).select( "form table" );

			Element table = tables.first(), row = row( table, 2 );

			SlackAttachment attach = new SlackAttachment().setTitle( TITLE ).setTitleLink( url );

			row( table, 1 ).select( "td:lt(3)" ).forEach( i -> attach.addFields( field( i.text(), row.child( i.siblingIndex() ).text() ) ) );

			attach.setText( txt = String.format( "%s（%s）", row( table = tables.get( 1 ), 2 ).text(), row( table, 1 ).text() ) );

			return message( attach.setFallback( txt ), command, text );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	@Override
	protected SlackField field( String title, String value ) {
		return super.field( title.contains( "愛心" ) ? "優待票" : title, value );
	}

	private String find( String name ) {
		return checkNull( STATIONS.get( StringUtils.removeEnd( name, "站" ) ), "查無此站: " + name );
	}

	private Element row( Element table, int index ) {
		return table.select( String.format( "tr:eq(%d)", index ) ).first();
	}

	@PostConstruct
	private void init() {
		Jsoup.select( URL, "select#sstation option", i -> STATIONS.put( StringUtils.split( i.text() )[ 1 ], i.val() ) );
	}
}