package ninja.controller;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import ninja.slack.Callback;
import ninja.slack.Event;
import ninja.util.Gson;

@RestController
public class EventController extends BaseController {
	private static final String LIST_TASKS = "list tasks";

	private enum Type {
		APP_MENTION;
	}

	@Autowired
	private TaskController controller;

	@PostMapping( "/event" )
	public Object event( @RequestAttribute( REQ_BODY ) String body ) {
		Callback callback = Gson.from( body, Callback.class );

		Event event = callback.getEvent();

		String challenge = callback.getChallenge(), text = event.getText();

		if ( challenge != null ) {
			return challenge;

		}

		log.info( "Body: {}", body );
		Type type = EnumUtils.getEnumIgnoreCase( Type.class, event.getType() );
		log.info( text );
		log.info( type.toString() );
		if ( Type.APP_MENTION.equals( type ) && StringUtils.containsIgnoreCase( text, LIST_TASKS ) ) {
			return controller.task();

		} else {
			return "OK";

		}
	}
}