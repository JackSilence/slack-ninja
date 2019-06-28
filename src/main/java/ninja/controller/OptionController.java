package ninja.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import ninja.consts.Dialog;
import ninja.service.Transport;
import ninja.slack.Payload;
import ninja.util.Gson;

@RestController
public class OptionController extends BaseController {
	private static final String TYPE = "dialog_suggestion", STOP = "stop";

	@Autowired
	private Transport transport;

	@PostMapping( "/option" )
	public Map<String, List<?>> option( @RequestAttribute( REQ_BODY ) String body, String payload ) {
		log.info( "REQ_BODY: " + body );
		log.info( "payload: " + payload );

		Payload message = Gson.from( payload, Payload.class );

		check( TYPE, message.getType(), payload );

		check( Dialog.BUS.name(), message.getId(), payload );

		check( STOP, message.getName(), payload );

		List<Map<String, ?>> elements = transport.call( "DisplayStopOfRoute", message.getValue(), "$filter=Direction%20eq%20%270%27" );
		System.out.println( "with filter: " + elements.size() );
		System.out.println( "no filter: " + transport.call( "DisplayStopOfRoute", message.getValue() ).size() );

		Map<String, ?> stop = elements.stream().findFirst().orElseGet( () -> options( Collections.EMPTY_LIST ) );
		return ImmutableMap.of( "options", Collections.EMPTY_LIST );
	}

	private Map<String, List<?>> options( List<?> options ) {
		return ImmutableMap.of( "options", options );
	}
}