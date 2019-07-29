package ninja.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

	static {
		Jsoup.select( URL + PATH, "ul#theaterList > li", i -> {
			if ( i.hasClass( "type0" ) ) {
				THEATERS.put( StringUtils.remove( i.text(), "▼" ), new LinkedHashMap<>() );
			} else {
				Element element = i.selectFirst( "a" );

				THEATERS.get( Iterables.getLast( THEATERS.keySet() ) ).put( element.text(), element.attr( "href" ) );
			}
		} );
	}

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( json( THEATERS.entrySet().stream().map( i -> {
			return ImmutableMap.of( LABEL, i.getKey(), OPTIONS, i.getValue().keySet().stream().map( super::option ).collect( Collectors.toList() ) );
		} ) ) );
	}

	@PostMapping( "/theater" )
	public String theater( @RequestParam String command, @RequestParam String text, @RequestParam( TRIGGER_ID ) String id ) {
		Action action = new Action( Act.MOVIE, "請選擇要觀看的電影", SlackActionType.SELECT, null );

		action.setConfirm( new Confirm( "確認送出嗎", "選擇以查詢時刻", "確認", "取消" ) );

		theater( text, i -> action.addOption( option( i.selectFirst( "li.filmTitle" ).text(), text ) ) );

		SlackAttachment attach = Slack.attachment( "#3AA3E3" ).setCallbackId( Act.MOVIE.name() ).addAction( action );

		return message( Slack.message( attach, command, text ) );
	}

	private Map<String, String> option( String film, String theater ) {
		return ImmutableMap.of( TEXT, film, VALUE, Utils.spacer( theater, film ) );
	}

	private void theater( String theater, Consumer<? super Element> action ) {
		Jsoup.select( URL + checkNull( THEATERS.values().stream().flatMap( i -> i.entrySet().stream() ).filter( i -> {
			return i.getKey().equals( theater );

		} ).map( Entry::getValue ).findFirst().orElse( null ), "查無影院: " + theater ), "ul#theaterShowtimeTable", action );
	}
}