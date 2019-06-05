package ninja.controller;

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
import net.gpedro.integrations.slack.SlackMessage;
import ninja.slack.Callback;
import ninja.slack.Event;
import ninja.util.Gson;
import ninja.util.Heroku;

@RestController

public class EventController extends BaseController {
	private static final String CHALLENGE = "challenge", POST_URL = "https://slack.com/api/chat.postMessage";

	private enum Type {
		APP_MENTION;
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

		if ( "bot_message".equals( event.getSubtype() ) ) {
			return;
		}

		SlackMessage message = new SlackMessage( event.toString() );

		if ( Type.APP_MENTION.equals( EnumUtils.getEnumIgnoreCase( Type.class, event.getType() ) ) && StringUtils.contains( event.getText(), "任務清單" ) ) {
			message = Heroku.task();
		}

		Request request = Request.Post( POST_URL ).setHeader( "Authorization", "Bearer " + token );

		log.info( Utils.getEntityAsString( request.bodyString( Gson.json( message.setChannel( event.getChannel() ).prepare() ), ContentType.APPLICATION_JSON ) ) );
	}
}