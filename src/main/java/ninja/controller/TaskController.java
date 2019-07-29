package ninja.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ninja.consts.Act;
import ninja.consts.Dialog;
import ninja.consts.Task;
import ninja.slack.Action;
import ninja.slack.Payload;
import ninja.util.Gson;
import ninja.util.Heroku;

@RestController
@RequestMapping( "/task" )
public class TaskController extends BaseController {
	private enum Type {
		INTERACTIVE_MESSAGE, DIALOG_SUBMISSION;
	}

	@PostMapping
	public String task() {
		return Heroku.task().toString();
	}

	@PostMapping( "/execute" )
	public void execute( String payload ) {
		Payload message = Gson.from( payload, Payload.class );

		Type type = checkNull( EnumUtils.getEnumIgnoreCase( Type.class, message.getType() ), payload );

		log.info( "State: " + message.getState() ); // 目前是dialog有設state就會收到

		String command = message.getId(), text = StringUtils.EMPTY;

		if ( Type.INTERACTIVE_MESSAGE.equals( type ) ) {
			Act act = check( Act.class, command, payload );

			Action action = check( message.getActions(), payload );

			check( command, action.getName(), payload );

			if ( Act.HEROKU_TASK.equals( act ) ) {
				check( Task.class, command = action.getValue(), payload );

			} else {
				text = check( action.getSelected(), payload ).get( VALUE );

			}

		} else {
			Map<String, String> submission = message.getSubmission();

			Assert.notEmpty( submission, payload );

			text = check( Dialog.class, command, payload ).text( submission );
		}

		command( message.getUser().getName(), message.getChannel().getId(), command.toLowerCase(), text );
	}

	private <T> T check( List<T> list, String payload ) {
		check( CollectionUtils.isNotEmpty( list ) && list.size() == 1, payload );

		return list.get( 0 );
	}
}