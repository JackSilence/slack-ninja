package ninja.util;

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

import net.gpedro.integrations.slack.SlackActionType;
import ninja.consts.Act;
import ninja.consts.Task;
import ninja.slack.Action;
import ninja.slack.Confirm;

public class Ninja {
	public static JsonObject task() {
		return task( StringUtils.EMPTY, null );
	}

	public static JsonObject task( String text, String channel ) {
		var attach = Slack.attachment( Act.NINJA_TASK );

		Stream.of( Task.values() ).map( Ninja::action ).forEach( i -> attach.addAction( i ) );

		return Slack.message( text, channel ).addAttachments( attach ).prepare();
	}

	private static Action action( Task task ) { // "confirm": {} -> 會出現預設的確認視窗
		return new Action( Act.NINJA_TASK, task.desc(), SlackActionType.BUTTON, task.name() ).setConfirm( new Confirm() );
	}
}