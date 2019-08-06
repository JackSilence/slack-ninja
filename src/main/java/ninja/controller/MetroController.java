package ninja.controller;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import ninja.service.Metro;
import ninja.util.Check;
import ninja.util.Jsoup;

@RestController
public class MetroController extends DialogController {
	private static final String QUERY = "?s1elect=%s&s2elect=%s&action=query", TITLE = "捷運票價及乘車時間";

	@Autowired
	private Metro metro;

	@PostMapping( "/mrt" )
	@Async
	public void mrt( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		String[] params = Check.params( text );

		String start = id( params[ 0 ] ), end = id( params[ 1 ] ), link, txt;

		Check.expr( !start.equals( end ), "起訖站不得相同: " + text );

		log.info( "Start: {}, end: {}", start, end );

		Elements tables = Jsoup.select( link = Metro.URL.concat( String.format( QUERY, start, end ) ), "form table" );

		Element table = tables.first(), row = row( table, 2 );

		SlackAttachment attach = new SlackAttachment().setTitle( TITLE ).setTitleLink( link );

		row( table, 1 ).select( "td:lt(3)" ).forEach( i -> attach.addFields( field( i.text(), row.child( i.siblingIndex() ).text() ) ) );

		attach.setText( txt = String.format( "%s（%s）", row( table = tables.get( 1 ), 2 ).text(), row( table, 1 ).text() ) );

		message( attach.setFallback( txt ), command, text, url );
	}

	@Override
	protected SlackField field( String title, String value ) {
		return super.field( title.contains( "愛心" ) ? "優待票" : title, value );
	}

	private String id( String station ) {
		return Check.nil( metro.data().get( station ), "查無此站: " + station );
	}

	private Element row( Element table, int index ) {
		return table.select( String.format( "tr:eq(%d)", index ) ).first();
	}
}