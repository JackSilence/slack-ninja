package ninja.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class ErrorController extends BasicErrorController {
	public ErrorController( ServerProperties properties ) {
		super( new DefaultErrorAttributes(), properties.getError() );
	}

	@Override
	public ResponseEntity<Map<String, Object>> error( HttpServletRequest request ) {
		return new ResponseEntity<>( getStatus( request ) );
	}
}