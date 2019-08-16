package ninja.ex;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class Advice {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String TEMPLATE = "Agent: %s, Referer: %s, Address: %s, URI: %s";

	@ExceptionHandler( Exception.class )
	public void ex( HttpServletRequest request, Exception ex ) {
		log.error( message( request ), ex ); // status一律回傳200, 不回傳真實狀態
	}

	private String message( HttpServletRequest request ) {
		String agent = header( request, "User-Agent" ), referer = header( request, "Referer" );

		return String.format( TEMPLATE, agent, referer, request.getRemoteAddr(), request.getRequestURI() );
	}

	private String header( HttpServletRequest request, String name ) {
		return StringUtils.defaultString( request.getHeader( name ) );
	}
}