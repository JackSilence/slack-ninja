package ninja.ex;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import magic.util.Utils;
import ninja.controller.DelController;
import ninja.controller.DialogController;
import ninja.util.Gson;

@RestControllerAdvice( assignableTypes = { DialogController.class, DelController.class } )
public class Advice implements ResponseBodyAdvice<String> {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	@Value( "${slack.user.token:}" )
	private String token;

	@ExceptionHandler( RuntimeException.class )
	public String ex( RuntimeException ex ) {
		log.error( StringUtils.EMPTY, ex );

		return ex.getMessage();
	}

	@Override
	public boolean supports( MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType ) {
		return true;
	}

	@Override
	public String beforeBodyWrite( String body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response ) {
		HttpServletRequest req = ( ( ServletServerHttpRequest ) request ).getServletRequest();

		Map<String, Object> map = Gson.from( body, Map.class );

		map.put( "channel", req.getParameter( "channel_id" ) );

		map.put( "user", req.getParameter( "user_id" ) );

		log.info( Utils.getEntityAsString( Request.Post( "https://slack.com/api/chat.postEphemeral" ).setHeader( "Authorization", "Bearer " + token ).bodyString( Gson.json( map ), ContentType.APPLICATION_JSON ) ) );

		return null;
	}
}