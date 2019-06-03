package ninja.controller;

import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.gpedro.integrations.slack.SlackActionType;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.slack.Action;
import ninja.slack.Confirm;

@RestController
@RequestMapping( "/task" )
public class TaskController {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final Gson GSON = new Gson();

	private enum Task {
		SHOPEE, HEROKU, POINT, NBA;
	}

	@PostMapping
	public Map<?, ?> task() {
		SlackAttachment attach = new SlackAttachment( StringUtils.EMPTY ).setColor( "good" ).setCallbackId( "heroku_task" );

		Stream.of( Task.values() ).map( this::action ).forEach( i -> attach.addAction( i ) );

		return map( new SlackMessage( "請選擇要執行的任務" ).addAttachments( attach ).prepare() );
	}

	@PostMapping( "/execute" )
	public Map<?, ?> execute( String payload ) {
		log.info( payload );

		return null;
	}

	private Action action( Task task ) {
		Action action = new Action( "task", task.name(), SlackActionType.BUTTON, task.name() );

		// slack action如果有confirm: {}則會出現預設的確認視窗
		return action.setConfirm( new Confirm() );
	}

	private Map<String, Object> map( JsonObject object ) {
		return GSON.fromJson( GSON.toJson( object ), new TypeToken<Map<String, Object>>() {
		}.getType() );
	}
}