package ninja.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.slack.Callback;
import ninja.util.Cast;
import ninja.util.Check;
import ninja.util.Gson;
import ninja.util.Heroku;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class EventController extends BaseController {
	private static final String CHALLENGE = "challenge", MENTION_KEYWORD = "查詢可用任務", METHOD = "chat.postMessage";

	private static final String GRAMMAR_URL = "http://api.grammarbot.io/v2/check?api_key=%s&text=%s&language=en-US";

	private static final String DICT_TEMPLATE = "%1$s：<https://tw.dictionary.search.yahoo.com/search?p=%2$s|*%2$s*>\n";

	private static final String QUERY_TITLE = "您查詢的單字是", CHECK_TITLE = "您是不是要查", ENG_REGEX = "[a-zA-Z]+";

	private static final List<String> REJECT_SUB_TYPES = List.of( "bot_message", "message_deleted" );

	private enum Type {
		APP_MENTION, MESSAGE, APP_HOME_OPENED;
	}

	@Value( "${slack.bot.token:}" )
	private String token;

	@Value( "${grammar.api.key:}" )
	private String key;

	@PostMapping( "/event" )
	public void event( @RequestAttribute( REQ_BODY ) String body, Model model ) {
		var callback = Gson.from( body, Callback.class );

		var event = callback.getEvent();

		String challenge = callback.getChallenge(), text = event.getText(), channel = event.getChannel();

		if ( challenge != null ) {
			model.addAttribute( CHALLENGE, challenge );

			return;
		}

		if ( REJECT_SUB_TYPES.contains( event.getSubtype() ) ) {
			return; // 不處理小心會變成無窮迴圈
		}

		log.info( "Body: {}", body );

		var type = EnumUtils.getEnumIgnoreCase( Type.class, event.getType() );

		if ( Type.APP_MENTION.equals( type ) && StringUtils.contains( text, MENTION_KEYWORD ) ) {
			post( Heroku.task( "您可選擇任務並於確認後執行", channel ) );

		} else if ( Type.MESSAGE.equals( type ) && ( text = StringUtils.defaultString( text ).trim() ).matches( ENG_REGEX ) ) {
			post( Slack.message( translate( text ), channel ) ); // text可能為null, 例如subtype: message_changed

		} else if ( Type.APP_HOME_OPENED.equals( type ) ) {
			var view = magic.util.Utils.getResourceAsString( "/template/home.json" );

			log.info( post( "views.publish", token, Map.of( "user_id", event.getUser(), "view", view ) ) );
		}
	}

	@PostMapping( "/dict" )
	@Async
	public void dict( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		Check.expr( text.matches( ENG_REGEX ), "參數有誤: " + text );

		message( translate( text ), url );
	}

	private String translate( String text ) {
		String uri = String.format( GRAMMAR_URL, key, text ), value = StringUtils.EMPTY;

		try {
			value = Utils.join( Cast.list( Gson.from( Utils.call( uri ), Map.class ), "matches" ).stream().flatMap( i -> {
				return Cast.list( Cast.map( i ), "replacements" ).stream();

			} ).limit( 1 ).map( i -> Cast.string( Cast.map( i ), VALUE ) ), StringUtils.SPACE );

		} catch ( RuntimeException e ) {
			log.error( StringUtils.EMPTY, e );
		}

		return ( value.isEmpty() ? StringUtils.EMPTY : dict( CHECK_TITLE, value ) ) + dict( QUERY_TITLE, text );
	}

	private String dict( String title, String text ) {
		return String.format( DICT_TEMPLATE, title, text );
	}

	private void post( Object src ) {
		log.info( post( METHOD, token, src ) );
	}
}