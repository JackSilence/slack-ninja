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

	private static final String ID = "heroku_task";

	private static final Gson GSON = new Gson();

	private enum Task {
		SHOPEE( "百米家新商品通知" ), HEROKU( "Heroku Usage" ), POINT( "點數查詢" ), NBA( "NBA BOX" );

		private String desc;

		private Task( String desc ) {
			this.desc = desc;
		}
	}

	@PostMapping
	public Map<?, ?> task() {
		SlackAttachment attach = new SlackAttachment( StringUtils.EMPTY ).setTitle( "任務清單" ).setCallbackId( ID );

		Stream.of( Task.values() ).map( this::action ).forEach( i -> attach.addAction( i ) );

		return map( new SlackMessage( StringUtils.EMPTY ).addAttachments( attach.setText( "點擊按鈕以選擇，並於確認後執行" ) ).prepare() );
	}

	@PostMapping( "/execute" )
	public Map<?, ?> execute( String payload ) {
		log.info( payload );

		return null;
	}

	private Action action( Task task ) { // "confirm": {} -> 會出現預設的確認視窗
		return new Action( ID, task.desc, SlackActionType.BUTTON, task.name() ).setConfirm( new Confirm( "確認執行任務", task.desc, "是", "否" ) );
	}

	private Map<String, Object> map( JsonObject object ) {
		return GSON.fromJson( GSON.toJson( object ), new TypeToken<Map<String, Object>>() {
		}.getType() );
	}
}