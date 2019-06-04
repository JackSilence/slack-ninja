package ninja.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.utils.StringUtils;

import magic.util.Utils;
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
public class TaskController extends BaseController {
	private static final String ID = "heroku_task", TYPE = "interactive_message";

	private static final String COMMAND_URL = "https://slack.com/api/chat.command?token=%s&channel=%s&command=/%s";

	@Value( "${slack.legacy.token:}" )
	private String token;

	private enum Task {
		SHOPEE( "百米家新商品通知" ), HEROKU( "Heroku Usage" ), EPOINT( "點數查詢" ), NBA( "NBA BOX" );

		private String desc;

		private Task( String desc ) {
			this.desc = desc;
		}
	}

	@PostMapping
	public Map<String, Object> task() {
		SlackAttachment attach = new SlackAttachment( StringUtils.EMPTY ).setCallbackId( ID ).setColor( "#3AA3E3" );

		Stream.of( Task.values() ).map( this::action ).forEach( i -> attach.addAction( i ) );

		return Gson.map( new SlackMessage( StringUtils.EMPTY ).addAttachments( attach ).prepare() );
	}

	@PostMapping( "/execute" )
	public void execute( String payload ) {
		Payload message = Gson.payload( payload );

		Assert.isTrue( TYPE.equals( message.getType() ) && ID.equals( message.getId() ), payload );

		List<SlackAction> actions = message.getActions();

		Assert.isTrue( CollectionUtils.isNotEmpty( actions ) && actions.size() == 1, payload );

		SlackAction action = actions.get( 0 );

		Assert.isTrue( ID.equals( action.getName() ), payload );

		Task task = Task.valueOf( action.getValue() );

		String uri = String.format( COMMAND_URL, token, message.getChannel().getId(), task.name().toLowerCase() );

		log.info( Utils.getEntityAsString( Request.Get( uri ) ) );
	}

	private Action action( Task task ) { // "confirm": {} -> 會出現預設的確認視窗
		return new Action( ID, task.desc, SlackActionType.BUTTON, task.name() ).setConfirm( new Confirm() );
	}
}