package ninja.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import ninja.slack.Callback;
import ninja.slack.Event;
import ninja.util.Gson;
import ninja.util.Heroku;
import ninja.util.Slack;

@RestController
public class EventController extends BaseController {
	private static final String CHALLENGE = "challenge", MENTION_KEYWORD = "查詢可用任務", METHOD = "chat.postMessage";

	private static final String DICT_URL = "https://tw.dictionary.search.yahoo.com/search?p=";

	private static final List<String> REJECT_SUB_TYPES = Arrays.asList( "bot_message", "message_deleted" );

	private enum Type {
		APP_MENTION, MESSAGE;
	}

	@Value( "${slack.bot.token:}" )
	private String token;

	@PostMapping( "/event" )
	public void event( @RequestAttribute( REQ_BODY ) String body, Model model ) {
		Callback callback = Gson.from( body, Callback.class );

		Event event = callback.getEvent();

		String challenge = callback.getChallenge(), text = event.getText(), channel = event.getChannel();

		if ( challenge != null ) {
			model.addAttribute( CHALLENGE, challenge );

			return;
		}

		if ( REJECT_SUB_TYPES.contains( event.getSubtype() ) ) {
			return; // 不處理小心會變成無窮迴圈
		}

		log.info( "Body: {}", body );

		Type type = EnumUtils.getEnumIgnoreCase( Type.class, event.getType() );

		if ( Type.APP_MENTION.equals( type ) && StringUtils.contains( text, MENTION_KEYWORD ) ) {
			post( Heroku.task( "您可選擇任務並於確認後執行", channel ) );

		} else if ( Type.MESSAGE.equals( type ) && StringUtils.defaultString( text ).matches( "[a-zA-Z]+" ) ) {
			post( Slack.message( DICT_URL + text, channel ) ); // text可能為null, 例如subtype: message_changed
		}
	}

	private void post( Object src ) {
		log.info( post( METHOD, token, src ) );
	}
}