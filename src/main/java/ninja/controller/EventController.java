package ninja.controller;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import magic.util.Utils;
import ninja.slack.Callback;
import ninja.slack.Event;
import ninja.util.Gson;
import ninja.util.Heroku;

@RestController

public class EventController extends BaseController {
	private static final String CHALLENGE = "challenge", MENTION_KEYWORD = "查詢可用任務";

	private static final String POST_URL = "https://slack.com/api/chat.postMessage";

	private static final List<String> REJECT_SUB_TYPES = Arrays.asList( "bot_message", "message_deleted" );

	private enum Type {
		APP_MENTION, MESSAGE;
	}

	@Value( "${slack.bot.token:}" )
	private String token;

	@PostMapping( "/event" )
	public void event( @RequestAttribute( REQ_BODY ) String body, Model model ) {
		Callback callback = Gson.from( body, Callback.class );

		String challenge = callback.getChallenge();

		if ( challenge != null ) {
			model.addAttribute( CHALLENGE, challenge );

			return;
		}

		Event event = callback.getEvent();

		if ( REJECT_SUB_TYPES.contains( event.getSubtype() ) ) {
			return; // 不處理小心會變成無窮迴圈
		}

		log.info( "Body: {}", body );

		Type type = EnumUtils.getEnumIgnoreCase( Type.class, event.getType() );

		if ( Type.APP_MENTION.equals( type ) && StringUtils.contains( event.getText(), MENTION_KEYWORD ) ) {
			Request request = Request.Post( POST_URL ).setHeader( "Authorization", "Bearer " + token );

			String json = Gson.json( Heroku.task( "您可選擇任務並於確認後執行", event.getChannel() ) );

			log.info( Utils.getEntityAsString( request.bodyString( json, ContentType.APPLICATION_JSON ) ) );
		}
	}
}