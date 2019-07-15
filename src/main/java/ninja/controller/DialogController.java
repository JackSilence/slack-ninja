package ninja.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.google.common.collect.ImmutableMap;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackException;
import ninja.consts.Dialog;

public abstract class DialogController extends BaseController {
	private static final String DIALOG_TEMPLATE = "/template/dialog/%s.json";

	@ExceptionHandler( SlackException.class )
	public void ex() {
	}

	@Override
	protected void preHandle( HttpServletRequest request ) {
		if ( request.getParameter( "text" ).isEmpty() ) {
			dialog( request.getParameter( TRIGGER_ID ), EnumUtils.getEnumIgnoreCase( Dialog.class, StringUtils.remove( request.getRequestURI(), "/" ) ) );

			throw new SlackException( null );
		}
	}

	protected void dialog( String id, Dialog dialog ) {
		String template = Utils.getResourceAsString( String.format( DIALOG_TEMPLATE, dialog.name().toLowerCase() ) );

		log.info( post( "dialog.open", ImmutableMap.of( TRIGGER_ID, id, "dialog", String.format( template, args() ) ) ) );
	}

	protected Object[] args() {
		return ArrayUtils.EMPTY_OBJECT_ARRAY;
	}
}