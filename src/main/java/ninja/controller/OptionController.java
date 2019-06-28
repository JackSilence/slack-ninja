package ninja.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OptionController extends BaseController {
	private static final String TYPE = "dialog_suggestion";

	@PostMapping( "/option" )
	public String option( @RequestAttribute( REQ_BODY ) String body, String payload ) {
		log.info( "REQ_BODY: " + REQ_BODY );
		log.info( "payload: " + payload );

		return null;
	}
}