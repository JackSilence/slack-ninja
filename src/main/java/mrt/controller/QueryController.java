package mrt.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import magic.service.Slack;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;

@RestController
public class QueryController {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String URL = "https://web.metro.taipei/c/2stainfo.asp";

	private static final String QUERY = "?s1elect=%s&s2elect=%s&action=query", TITLE = "捷運票價及乘車時間";

	private static final Gson GSON = new Gson();

	private static final Map<String, String> STATIONS = new HashMap<>();

	@Autowired
	private Slack slack;

	@PostMapping( "/query" )
	public Map<?, ?> query( String command, @RequestParam String text ) {
		log.info( "Text: {}", text );

		if ( STATIONS.isEmpty() ) {
			init();
		}

		try {
			String[] params = StringUtils.split( text );

			Assert.isTrue( params.length == 2, "起訖站皆須輸入" );

			String start = find( params[ 0 ] ), end = find( params[ 1 ] ), url;

			Assert.isTrue( !start.equals( end ), "起訖站不得相同: " + text );

			log.info( "Start: {}, end: {}", start, end );

			Elements tables = get( url = URL.concat( String.format( QUERY, start, end ) ) ).select( "form table" );

			Element table = tables.first(), row = row( table, 1 );

			SlackAttachment attach = new SlackAttachment().setTitle( TITLE ).setTitleLink( url );

			row( table, 0 ).select( "th:lt(2)" ).forEach( i -> attach.addFields( field( i.text(), row.child( i.siblingIndex() ).text() ) ) );

			attach.setFooter( StringUtils.isEmpty( command ) ? StringUtils.EMPTY : String.format( "%s %s", command, text ) );

			attach.setText( text = String.format( "%s（%s）", row( table = tables.get( 1 ), 2 ).text(), row( table, 1 ).text() ) );

			JsonObject object = new SlackMessage( StringUtils.EMPTY ).addAttachments( attach.setFallback( text ) ).prepare();

			return GSON.fromJson( GSON.toJson( object ), Map.class );

		} catch ( RuntimeException | IOException e ) {
			log.error( "", e );

			return slack.text( e.getMessage() );

		}
	}

	private String find( String name ) {
		Optional<String> station = STATIONS.entrySet().stream().filter( i -> check( i.getKey(), name ) ).map( Entry::getValue ).findFirst();

		Assert.isTrue( station.isPresent(), "查無此站: " + name );

		return station.get();
	}

	private boolean check( String key, String name ) {
		return StringUtils.equalsAny( StringUtils.split( key )[ 1 ], name, StringUtils.removeEnd( name, "站" ) );
	}

	private Element row( Element table, int index ) {
		return table.select( String.format( "tr:eq(%d)", index ) ).first();
	}

	private SlackField field( String title, String value ) {
		return new SlackField().setShorten( true ).setTitle( title ).setValue( value );
	}

	private Document get( String url ) throws IOException {
		return Jsoup.connect( url ).get();
	}

	@PostConstruct
	private void init() {
		try {
			get( URL ).getElementById( "sstation" ).select( "option" ).forEach( i -> STATIONS.put( i.text(), i.val() ) );

		} catch ( IOException e ) {
			log.error( "", e );

		}
	}
}