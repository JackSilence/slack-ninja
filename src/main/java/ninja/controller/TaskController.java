package ninja.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cloudinary.utils.StringUtils;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import net.gpedro.integrations.slack.SlackAction;
import net.gpedro.integrations.slack.SlackActionType;
import net.gpedro.integrations.slack.SlackAttachment;

@RestController
@RequestMapping( "/task" )
public class TaskController {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String ATTACHMENTS = "attachments", ACTIONS = "actions", CONFIRM = "confirm";

	private static final String TITLE = "title", OK_TEXT = "ok_text", DISMISS_TEXT = "dismiss_text";

	private static final Gson GSON = new Gson();

	private enum Task {
		SHOPEE, HEROKU, POINT, NBA;
	}

	@PostMapping
	public Map<?, ?> task() {
		Map<String, Object> attach = map( new SlackAttachment( StringUtils.EMPTY ).setText( "請選擇要執行的任務" ).setColor( "good" ).toJson() );

		attach.put( ACTIONS, Stream.of( Task.values() ).map( this::action ).collect( Collectors.toList() ) );

		return ImmutableMap.of( ATTACHMENTS, Arrays.asList( attach ) );
	}

	@PostMapping( "/execute" )
	public Map<?, ?> execute( String payload ) {
		log.info( payload );

		return null;
	}

	private Map<String, Object> action( Task task ) {
		Map<String, Object> action = map( new SlackAction( "task", task.name(), SlackActionType.BUTTON, task.name() ).toJson() );

		action.put( CONFIRM, ImmutableMap.of( TITLE, "確認執行", OK_TEXT, "是", DISMISS_TEXT, "否" ) );

		return action;
	}

	private Map<String, Object> map( JsonObject object ) {
		return GSON.fromJson( GSON.toJson( object ), new TypeToken<Map<String, Object>>() {
		}.getType() );
	}
}