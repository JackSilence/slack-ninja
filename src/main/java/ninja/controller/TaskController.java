package ninja.controller;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackAction;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Task;
import ninja.slack.Payload;
import ninja.util.Gson;
import ninja.util.Heroku;

@RestController
@RequestMapping( "/task" )
public class TaskController extends BaseController {
	private static final String TYPE = "interactive_message", METHOD = "chat.command", QUERY = "&command=/";

	@Value( "${slack.legacy.token:}" )
	private String token;

	@PostMapping
	public String task() {
		return Heroku.task().toString();
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
		log.info( get( METHOD, token, message.getChannel().getId(), QUERY + task.name().toLowerCase() ) );

		new SlackApi( message.getUrl() ).call( new SlackMessage( "手動執行: " + task ) );
	}
}