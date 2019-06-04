package ninja.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController extends BaseController {
	@PostMapping( "/event" )
	public String event( @RequestBody Map<String, String> body ) {
		return body.get( "challenge" );
	}
}