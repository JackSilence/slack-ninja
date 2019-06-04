package ninja.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.utils.StringUtils;
import com.google.common.collect.ImmutableMap;

import net.gpedro.integrations.slack.SlackAction;
import net.gpedro.integrations.slack.SlackActionType;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.slack.Action;
import ninja.slack.Confirm;
import ninja.slack.Payload;
import ninja.util.Gson;

@RestController
@RequestMapping( "/task" )
public class TaskController {
	private static final String ID = "heroku_task", TYPE = "interactive_message";

	private static final String URL = "%s.herokuapp.com/execute/%s";

	private enum Task {
		SHOPEE( "百米家新商品通知", "/shopee" ), HEROKU( "Heroku Usage", "" ), POINT( "點數查詢", "" ), NBA( "NBA BOX", "" );

		private String desc, command;

		private Task( String desc, String command ) {
			this.desc = desc;
			this.command = command;
		}
	}

	@PostMapping
	public Map<String, Object> task() {
		SlackAttachment attach = new SlackAttachment( StringUtils.EMPTY ).setCallbackId( ID ).setColor( "#3AA3E3" );

		Stream.of( Task.values() ).map( this::action ).forEach( i -> attach.addAction( i ) );

		return Gson.map( new SlackMessage( StringUtils.EMPTY ).addAttachments( attach ).prepare() );
	}

	@PostMapping( "/execute" )
	public Map<String, String> execute( String payload ) {
		Payload message = Gson.payload( payload );

		Assert.isTrue( TYPE.equals( message.getType() ) && ID.equals( message.getId() ), payload );

		List<SlackAction> actions = message.getActions();

		Assert.isTrue( CollectionUtils.isNotEmpty( actions ) && actions.size() == 1, payload );

		SlackAction action = actions.get( 0 );

		Task task = Task.valueOf( action.getValue() );

		return ImmutableMap.of( "text", task.command );
	}

	private Action action( Task task ) { // "confirm": {} -> 會出現預設的確認視窗
		return new Action( ID, task.desc, SlackActionType.BUTTON, task.name() ).setConfirm( new Confirm() );
	}
}