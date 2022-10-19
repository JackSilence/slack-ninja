package ninja.controller;

import java.util.Collection;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackException;
import ninja.consts.Dialog;
import ninja.util.Gson;

public abstract class DialogController extends BaseController {
	private static final String DIALOG_TEMPLATE = "/template/dialog/%s.json", DIALOG_METHOD = "dialog.open";

	@ExceptionHandler( SlackException.class )
	public void ex() {
	}

	@Override
	protected void preHandle( HttpServletRequest request ) {
		if ( request.getParameter( TEXT ).isEmpty() && !ArrayUtils.contains( skip(), request.getRequestURI() ) ) {
			dialog( request.getParameter( TRIGGER_ID ), EnumUtils.getEnumIgnoreCase( Dialog.class, StringUtils.remove( request.getRequestURI(), "/" ) ) );

			throw new SlackException( null );
		}
	}

	protected void dialog( String id, Dialog dialog, Object... args ) {
		var template = Utils.getResourceAsString( String.format( DIALOG_TEMPLATE, dialog.name().toLowerCase() ) );

		log.info( post( DIALOG_METHOD, Map.of( TRIGGER_ID, id, "dialog", String.format( template, args ) ) ) );
	}

	protected String options( Collection<String> collection ) {
		return json( collection.stream().map( super::option ) );
	}

	protected String json( Stream<Map<String, ?>> stream ) {
		return Gson.json( list( stream ) );
	}

	protected <T> Stream<T> iterate( T seed, UnaryOperator<T> f, long size ) {
		return Stream.iterate( seed, f ).limit( size );
	}

	protected String[] skip() {
		return ArrayUtils.EMPTY_STRING_ARRAY;
	}

	protected Object[] args() {
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}

	private void dialog( String id, Dialog dialog ) {
		dialog( id, dialog, args() );
	}
}