package ninja.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import magic.service.Slack;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.slack.Callback;
import ninja.util.Gson;

@RestController
public class EventController extends BaseController {
	@Autowired
	private Slack slack;

	@PostMapping( "/event" )
	public Object event( @RequestAttribute( REQ_BODY ) String body ) {
		Callback callback = Gson.from( body, Callback.class );

		if ( callback.getChallenge() != null ) {
			return callback;

		}

		// 正常應該要用chat.postMessage
		slack.call( new SlackMessage( "Event type: " + callback.getEvent().getType() ) );

		return null;
	}
}