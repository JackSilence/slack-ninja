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

import net.gpedro.integrations.slack.SlackAction;
import ninja.consts.Dialog;
import ninja.consts.Task;
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

		Type type = EnumUtils.getEnumIgnoreCase( Type.class, message.getType() );

		Assert.notNull( type, payload );

		String id = message.getId(), command, text = StringUtils.EMPTY;

		if ( Type.INTERACTIVE_MESSAGE.equals( type ) ) {
			check( Heroku.TASK_ID, id, payload );

			List<SlackAction> actions = message.getActions();

			check( CollectionUtils.isNotEmpty( actions ) && actions.size() == 1, payload );

			SlackAction action = actions.get( 0 );

			check( Heroku.TASK_ID, action.getName(), payload );

			check( Task.class, command = action.getValue(), payload );

		} else {
			check( Dialog.class, id, payload );

			Map<String, String> submission = message.getSubmission();

			Assert.notEmpty( submission, payload );

			text = text( Dialog.valueOf( command = id ).text( submission ), StringUtils.defaultString( message.getState() ) );
		}

		command( message.getUser().getName(), message.getChannel().getId(), command.toLowerCase(), text );
	}

	private <E extends Enum<E>> void check( Class<E> expected, String actual, String payload ) {
		check( EnumUtils.isValidEnum( expected, actual ), payload );
	}
}