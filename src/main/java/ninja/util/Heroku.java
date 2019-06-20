package ninja.util;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

import net.gpedro.integrations.slack.SlackActionType;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Task;
import ninja.slack.Action;
import ninja.slack.Confirm;

public class Heroku {
	public static final String TASK_ID = "heroku_task";

	public static JsonObject task() {
		return task( StringUtils.EMPTY, null );
	}

	public static JsonObject task( String text, String channel ) {
		SlackAttachment attach = Slack.attachment().setCallbackId( TASK_ID ).setColor( "#3AA3E3" );

		Stream.of( Task.values() ).map( Heroku::action ).forEach( i -> attach.addAction( i ) );

		return new SlackMessage( text ).setChannel( channel ).addAttachments( attach ).prepare();
	}

	private static Action action( Task task ) { // "confirm": {} -> 會出現預設的確認視窗
		return new Action( TASK_ID, task.desc(), SlackActionType.BUTTON, task.name() ).setConfirm( new Confirm() );
	}
}