package ninja.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import ninja.consts.Act;
import ninja.consts.Dialog;
import ninja.consts.Task;
import ninja.slack.Payload;
import ninja.util.Check;
import ninja.util.Gson;
import ninja.util.Heroku;

@RestController
@RequestMapping( "/task" )
public class TaskController extends BaseController {
	private static final String COMMAND_METHOD = "chat.command", COMMAND_QUERY = "&command=/%s";

	private enum Type {
		INTERACTIVE_MESSAGE, DIALOG_SUBMISSION;
	}

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	private ApplicationContext context;

	@PostMapping
	public String task() {
		return Heroku.task().toString();
	}

	@PostMapping( "/execute" )
	public void execute( String payload ) {
		var message = Gson.from( payload, Payload.class );

		var type = Check.nil( EnumUtils.getEnumIgnoreCase( Type.class, message.getType() ), payload );

		log.info( "State: " + message.getState() ); // 目前是dialog有設state就會收到

		String command = message.getId(), url = message.getUrl(), text = StringUtils.EMPTY, path;

		if ( Type.INTERACTIVE_MESSAGE.equals( type ) ) {
			var act = Check.name( Act.class, command, payload );

			var action = check( message.getActions(), payload );

			Check.equals( command, action.getName(), payload );

			if ( Act.HEROKU_TASK.equals( act ) ) {
				Check.name( Task.class, command = action.getValue(), payload );

				String user = message.getUser().getName(), token = System.getenv( "slack.legacy.token." + user );

				if ( token == null ) {
					log.error( "權限不足: " + user );

					message( "權限不足", url );

				} else {
					log.info( get( COMMAND_METHOD, token, message.getChannel().getId(), String.format( COMMAND_QUERY, command ) ) );
				}

				return;

			} else {
				text = check( action.getSelected(), payload ).get( VALUE );

			}

		} else {
			var submission = Check.map( message.getSubmission(), payload );

			text = Check.name( Dialog.class, command, payload ).text( submission );
		}

		Object[] args = { path = "/" + command.toLowerCase(), text, url };

		requestMappingHandlerMapping.getHandlerMethods().entrySet().stream().filter( i -> {
			return String.format( "[%s]", path ).equals( i.getKey().getActivePatternsCondition().toString() ); // methodsCondition就不比較了, 都是POST

		} ).findFirst().map( Entry::getValue ).ifPresent( i -> { // 不用ParameterNameDiscoverer
			try {
				i.getMethod().invoke( context.getBean( i.getBeanType() ), Arrays.copyOfRange( args, args.length - i.getMethodParameters().length, args.length ) );

			} catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
				throw new RuntimeException( e );
			}
		} );
	}

	private <T> T check( List<T> list, String payload ) {
		Check.expr( !CollectionUtils.isEmpty( list ) && list.size() == 1, payload );

		return list.get( 0 );
	}
}