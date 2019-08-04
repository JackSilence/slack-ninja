package ninja.ex;

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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.google.gson.JsonObject;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.controller.DelController;
import ninja.controller.DialogController;

@RestControllerAdvice( assignableTypes = { DialogController.class, DelController.class } )
public class Advice implements ResponseBodyAdvice<SlackMessage> {
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
	public SlackMessage beforeBodyWrite( SlackMessage body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response ) {
		HttpServletRequest req = ( HttpServletRequest ) request;

		body.setChannel( req.getParameter( "channel_id" ) );

		JsonObject object = body.prepare();

		object.addProperty( "user", req.getParameter( "user_id" ) );

		log.info( Utils.getEntityAsString( Request.Post( "https://slack.com/api/chat.postEphemeral" ).setHeader( "Authorization", "Bearer " + token ).bodyString( object.toString(), ContentType.APPLICATION_JSON ) ) );

		return null;
	}

}