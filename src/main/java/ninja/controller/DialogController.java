package ninja.controller;

import java.util.Collection;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.google.common.collect.ImmutableMap;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackException;
import ninja.consts.Dialog;
import ninja.util.Gson;

public abstract class DialogController extends BaseController {
	private static final String DIALOG_TEMPLATE = "/template/dialog/%s.json";

	@ExceptionHandler( SlackException.class )
	public void ex() {
	}

	@Override
	protected void preHandle( HttpServletRequest request ) {
		if ( request.getParameter( "text" ).isEmpty() ) {
			Dialog dialog = EnumUtils.getEnumIgnoreCase( Dialog.class, StringUtils.remove( request.getRequestURI(), "/" ) );

			String template = Utils.getResourceAsString( String.format( DIALOG_TEMPLATE, dialog.name().toLowerCase() ) );

			log.info( post( "dialog.open", ImmutableMap.of( TRIGGER_ID, request.getParameter( TRIGGER_ID ), "dialog", String.format( template, args() ) ) ) );

			throw new SlackException( null );
		}
	}

	protected String options( Collection<String> collection ) {
		return json( collection.stream().map( super::option ) );
	}

	protected String json( Stream<Map<String, String>> stream ) {
		return Gson.json( stream.collect( Collectors.toList() ) );
	}

	protected <T> Stream<T> iterate( T seed, UnaryOperator<T> f, long size ) {
		return Stream.iterate( seed, f ).limit( size );
	}

	protected Object[] args() {
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}
}