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

	@Override
	public void handleUncaughtException( Throwable ex, Method method, Object... params ) {
		log.error( StringUtils.EMPTY, ex );

		String message = ex instanceof IllegalArgumentException ? ex.getMessage() : "系統忙碌中";

		log.info( Utils.call( params[ params.length - 1 ].toString(), new SlackMessage( message ) ) );
	}
}