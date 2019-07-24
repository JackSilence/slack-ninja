package ninja.ex;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ninja.controller.DialogController;

@RestControllerAdvice( assignableTypes = DialogController.class )
public class Advice {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	@ExceptionHandler( RuntimeException.class )
	public String ex( RuntimeException ex ) {
		log.error( StringUtils.EMPTY, ex );

		return ex.getMessage();
	}
}