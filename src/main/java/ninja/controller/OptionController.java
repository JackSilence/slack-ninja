package ninja.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

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
	public String option( @RequestAttribute( REQ_BODY ) String body, String payload ) {
		log.info( "REQ_BODY: " + body );
		log.info( "payload: " + payload );

		Payload message = Gson.from( payload, Payload.class );

		check( TYPE, message.getType(), payload );

		check( Dialog.BUS.name(), message.getId(), payload );

		check( STOP, message.getName(), payload );

		System.out.println( transport.call( "DisplayStopOfRoute", message.getValue(), "$filter=Direction%20eq%20%270%27" ) );

		return null;
	}
}