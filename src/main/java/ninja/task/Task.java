package ninja.task;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import magic.service.IService;
import ninja.util.Utils;

public abstract class Task implements IService {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	protected static final String COMMAND = "scheduled-task";

	@Value( "${slack.webhook.url:}" )
	private String url;

	protected void exec( String body ) {
		log.info( Utils.call( Request.Post( url ).bodyString( body, ContentType.APPLICATION_JSON ) ) );
	}
}