package ninja.ex;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.google.common.net.HttpHeaders;

@RestControllerAdvice
public class Advice {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String TEMPLATE = "Agent: %s, Address: %s, URI: %s";

	@ExceptionHandler( Exception.class )
	public void ex( HttpServletRequest request, Exception ex ) {
		log.error( message( request ), ex ); // status一律回傳200, 不回傳真實狀態
	}

	private String message( HttpServletRequest request ) {
		return String.format( TEMPLATE, request.getHeader( HttpHeaders.USER_AGENT ), request.getRemoteAddr(), request.getRequestURI() );
	}
}