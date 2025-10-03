package ninja.logging;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;

import com.google.common.net.HttpHeaders;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.status.ErrorStatus;
import ninja.util.Gson;
import ninja.util.Utils;

public class SlackAppender extends AppenderBase<ILoggingEvent> {
    private static final String API_URL = "https://slack.com/api/chat.postMessage";

    private String token, channel, app;

    @Override
    public void start() {
        if ( StringUtils.isBlank( token ) ) {
            addStatus( new ErrorStatus( "Slack token is not configured", this ) );
            return;
        }
        if ( StringUtils.isBlank( channel ) ) {
            addStatus( new ErrorStatus( "Slack channel is not configured", this ) );
            return;
        }
        super.start();
    }

    @Override
    protected void append( ILoggingEvent event ) {
        if ( !isStarted() ) {
            return;
        }

        try {
            send( message( event ) );
        } catch ( Exception e ) {
            addError( "Failed to send ephemeral message to Slack", e );
        }
    }

    private String message( ILoggingEvent event ) {
        StringBuilder sb = new StringBuilder();

        String prefix = StringUtils.isNotBlank( app ) ? app + StringUtils.SPACE : StringUtils.EMPTY;

        sb.append( "🚨 *" ).append( prefix ).append( "Error Alert*\n" ).append( "```\n" );
        sb.append( "Time: " ).append( new java.util.Date( event.getTimeStamp() ) ).append( "\n" );
        sb.append( "Level: " ).append( event.getLevel() ).append( "\n" );
        sb.append( "Logger: " ).append( event.getLoggerName() ).append( "\n" );
        sb.append( "Message: " ).append( event.getFormattedMessage() ).append( "\n" );

        if ( event.getThrowableProxy() != null ) {
            sb.append( "Exception: " ).append( event.getThrowableProxy().getClassName() ).append( "\n" );
            sb.append( "Error: " ).append( event.getThrowableProxy().getMessage() ).append( "\n" );
        }

        sb.append( "```" );

        return sb.toString();
    }

    private void send( String message ) {
        String body = Gson.json( Map.of( "channel", channel, "text", message ) );

        String response = Utils.call( Request.Post( API_URL ).setHeader( HttpHeaders.AUTHORIZATION, "Bearer " + token ), body );

        if ( response.contains( "\"ok\":false" ) ) {
            addError( "Slack API error: " + response );
        }
    }

    public void setToken( String token ) {
        this.token = token;
    }

    public void setChannel( String channel ) {
        this.channel = channel;
    }

    public void setApp( String app ) {
        this.app = app;
    }
}
