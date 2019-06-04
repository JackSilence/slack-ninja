package ninja.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import ninja.util.Gson;

@RestController
public class EventController extends BaseController {
	@PostMapping( "/event" )
	public Object event( @RequestAttribute( REQ_BODY ) String body ) {
		return Gson.map( body ).get( "challenge" );
	}
}