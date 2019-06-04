package ninja.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController extends BaseController {
	@PostMapping( "/event" )
	public String event( @RequestBody String body ) {
		log.info( body );
		return body;
	}
}