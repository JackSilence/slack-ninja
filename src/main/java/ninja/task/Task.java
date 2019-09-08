package ninja.task;

import org.springframework.beans.factory.annotation.Value;

import magic.service.IService;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Utils;

public abstract class Task implements IService {
	protected static final String COMMAND = "scheduled-task";

	@Value( "${slack.webhook.url:}" )
	protected String url;

	protected void call( String text ) {
		Utils.call( url, new SlackMessage( text ) );
	}
}