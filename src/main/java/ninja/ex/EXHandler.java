package ninja.ex;

import java.lang.reflect.Method;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Utils;

public class EXHandler implements AsyncUncaughtExceptionHandler {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private static final String RESPONSE_URL_REGEX = "https://hooks.slack.com/(commands|actions|app).+"; // 排除services (incoming-webhook)

	@Override
	public void handleUncaughtException( Throwable ex, Method method, Object... params ) {
		log.error( StringUtils.EMPTY, ex );

		if ( ArrayUtils.isEmpty( params ) ) {
			return;
		}

		var uri = params[ params.length - 1 ].toString();

		if ( uri.matches( RESPONSE_URL_REGEX ) ) {
			log.info( Utils.call( uri, new SlackMessage( message( ex ) ) ) );
		}
	}

	private String message( Throwable ex ) {
		if ( ex instanceof IllegalArgumentException iae ) {
			return iae.getMessage();

		} else if ( ex instanceof DateTimeParseException dtpe ) {
			return "時間格式有誤: " + dtpe.getParsedString();
		}

		return "系統忙碌中";
	}
}