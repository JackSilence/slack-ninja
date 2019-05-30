package mrt.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
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
import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;

@RestController
public class QueryController {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String URL = "https://web.metro.taipei/c/2stainfo.asp", TITLE = "捷運票價及乘車時間";

	private static final String TITLE_LINK = "https://www.metro.taipei/cp.aspx?n=ECEADC266D7120A7";

	private static final Gson GSON = new Gson();

	private static final Map<String, String> STATIONS = new HashMap<>();

	@Autowired
	private Slack slack;

	@PostMapping( "/query" )
	public Map<?, ?> query( @RequestParam String text ) {
		log.info( "Text: {}", text );

		if ( STATIONS.isEmpty() ) {
			init();
		}

		try {
			String[] params = StringUtils.split( text );

			Assert.isTrue( params.length == 2, "起訖站皆須輸入" );

			String start = find( params[ 0 ] ), end = find( params[ 1 ] );

			Assert.isTrue( !start.equals( end ), "起訖站不得相同: " + text );

			log.info( "Start: {}, end: {}", start, end );

			Form form = Form.form().add( "s1elect", start ).add( "s2elect", end ).add( "action", "query" );

			Elements tables = Jsoup.parse( Utils.getEntityAsString( Request.Post( URL ).bodyForm( form.build() ) ) ).select( "form table" );

			Element table = tables.first(), row = row( table, 1 );

			SlackAttachment attach = new SlackAttachment().setTitle( TITLE ).setTitleLink( TITLE_LINK );

			row( table, 0 ).select( "th:lt(2)" ).forEach( i -> attach.addFields( field( i.text(), row.child( i.siblingIndex() ).text() ) ) );

			attach.setText( text = String.format( "%s（%s）", row( table = tables.get( 1 ), 2 ).text(), row( table, 1 ).text() ) );

			JsonObject object = new SlackMessage( StringUtils.EMPTY ).addAttachments( attach.setFallback( text ) ).prepare();

			return GSON.fromJson( GSON.toJson( object ), Map.class );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return slack.text( e.getMessage() );

		}
	}

	private String find( String name ) {
		List<String> stations = STATIONS.entrySet().stream().filter( i -> i.getKey().contains( name ) ).map( Entry::getValue ).collect( Collectors.toList() );

		Assert.isTrue( stations.size() == 1, "找到多個站名: " + name );

		return stations.get( 0 );
	}

	private Element row( Element table, int index ) {
		return table.select( String.format( "tr:eq(%d)", index ) ).first();
	}

	private SlackField field( String title, String value ) {
		return new SlackField().setShorten( true ).setTitle( title ).setValue( value );
	}

	@PostConstruct
	private void init() {
		try {
			Document doc = Jsoup.connect( URL ).get();

			doc.getElementById( "sstation" ).select( "option" ).forEach( i -> STATIONS.put( i.text(), i.val() ) );

		} catch ( IOException e ) {
			log.error( "", e );

		}
	}
}