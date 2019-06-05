package ninja.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAction;
import ninja.consts.Task;
import ninja.slack.Payload;
import ninja.util.Gson;
import ninja.util.Heroku;

@RestController
@RequestMapping( "/task" )
public class TaskController extends BaseController {
	private static final String TYPE = "interactive_message";

	private static final String COMMAND_URL = "https://slack.com/api/chat.command?token=%s&channel=%s&command=/%s";

	@Value( "${slack.legacy.token:}" )
	private String token;

	@PostMapping
	public Map<String, Object> task() {
		return Gson.map( Heroku.task() );
	}

	@PostMapping( "/execute" )
	public void execute( String payload ) {
		Payload message = Gson.from( payload, Payload.class );

		Assert.isTrue( TYPE.equals( message.getType() ) && Heroku.TASK_ID.equals( message.getId() ), payload );

		List<SlackAction> actions = message.getActions();

		Assert.isTrue( CollectionUtils.isNotEmpty( actions ) && actions.size() == 1, payload );

		SlackAction action = actions.get( 0 );

		Assert.isTrue( Heroku.TASK_ID.equals( action.getName() ), payload );

		Task task = Task.valueOf( action.getValue() );

		// 使用legacy token執行command, 只有對應的帳號才會看到return message
		String uri = String.format( COMMAND_URL, token, message.getChannel().getId(), task.name().toLowerCase() );

		log.info( Utils.getEntityAsString( Request.Get( uri ) ) );
	}
}