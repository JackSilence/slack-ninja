package ninja.ex;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Utils;

public class EXHandler implements AsyncUncaughtExceptionHandler {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String RESPONSE_URL_PREFIX = "https://hooks.slack.com/commands/";

	@Override
	public void handleUncaughtException( Throwable ex, Method method, Object... params ) {
		log.error( StringUtils.EMPTY, ex );

		String uri = params[ params.length - 1 ].toString();

		if ( uri.startsWith( RESPONSE_URL_PREFIX ) ) {
			log.info( Utils.call( uri, new SlackMessage( ex instanceof IllegalArgumentException ? ex.getMessage() : "系統忙碌中" ) ) );
		}
	}
}