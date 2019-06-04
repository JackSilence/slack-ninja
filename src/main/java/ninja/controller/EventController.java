package ninja.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import ninja.slack.Callback;
import ninja.util.Gson;

@RestController
public class EventController extends BaseController {
	@PostMapping( "/event" )
	public Object event( @RequestAttribute( REQ_BODY ) String body ) {
		Callback callback = Gson.from( body, Callback.class );

		if ( callback.getChallenge() != null ) {
			return callback;

		}

		log.info( "Body: {}", body );
		return ImmutableMap.of( "text", "你剛剛說: " + callback.getEvent().getText() );
	}
}