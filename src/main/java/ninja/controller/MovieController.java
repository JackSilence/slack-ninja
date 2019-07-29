package ninja.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import net.gpedro.integrations.slack.SlackActionType;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Act;
import ninja.slack.Action;
import ninja.slack.Confirm;
import ninja.util.Jsoup;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class MovieController extends DialogController {
	private static final String URL = "http://www.atmovies.com.tw", PATH = "/showtime/a02/";

	private static final Map<String, Map<String, String>> THEATERS = new LinkedHashMap<>();

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( json( THEATERS.entrySet().stream().map( i -> {
			return ImmutableMap.of( LABEL, i.getKey(), OPTIONS, i.getValue().keySet().stream().map( super::option ).collect( Collectors.toList() ) );
		} ) ) );
	}

	@PostMapping( "/theater" )
	public String theater( @RequestParam String text ) {
		Action action = new Action( Act.MOVIE, "請選擇要觀看的電影", SlackActionType.SELECT, null ).setConfirm( new Confirm() );

		theater( text, i -> action.addOption( option( i.selectFirst( "li.filmTitle" ).text(), text ) ) );

		return message( Slack.message().addAttachments( Slack.attachment( Act.MOVIE ).addAction( action ) ) );
	}

	@PostMapping( "/movie" )
	public String movie( @RequestParam String command, @RequestParam String text ) {
		String[] params = StringUtils.split( text );

		check( params.length == 2, "參數個數有誤: " + text );

		String theater = params[ 0 ], film = params[ 1 ];

		SlackMessage message = Slack.message( Slack.attachment().setTitle( theater ).setTitleLink( theater( theater, i -> {
			Element element = link( i );

			if ( film.equals( element.text() ) ) {
				SlackAttachment attach = Slack.attachment( "good" ).setTitle( film ).setTitleLink( Jsoup.href( element ) );

				attach.setAuthorIcon( i.selectFirst( "ul:eq(0)" ).select( "img" ).get( 0 ).attr( "src" ) );
				attach.setThumbUrl( i.selectFirst( "ul:eq(0)" ).select( "img" ).get( 1 ).attr( "src" ) );

			}

		} ) ), command, text );

		return message( message );
	}

	private Map<String, String> option( String film, String theater ) {
		return ImmutableMap.of( TEXT, film, VALUE, Utils.spacer( theater, film ) );
	}

	private String theater( String theater, Consumer<? super Element> action ) {
		String url;

		Jsoup.select( url = URL + checkNull( THEATERS.values().stream().flatMap( i -> i.entrySet().stream() ).filter( i -> {
			return i.getKey().equals( theater );

		} ).map( Entry::getValue ).findFirst().orElse( null ), "查無影院: " + theater ), "ul#theaterShowtimeTable", action );

		return url;
	}

	private Element link( Element element ) {
		return element.selectFirst( "a[href]" );
	}

	@PostConstruct
	private void init() {
		Jsoup.select( URL + PATH, "ul#theaterList > li", i -> {
			if ( i.hasClass( "type0" ) ) {
				THEATERS.put( StringUtils.remove( i.text(), "▼" ), new LinkedHashMap<>() );
			} else {
				Element element = link( i );

				THEATERS.get( Iterables.getLast( THEATERS.keySet() ) ).put( element.text(), Jsoup.href( element ) );
			}
		} );
	}
}