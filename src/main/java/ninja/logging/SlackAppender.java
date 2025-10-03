package ninja.logging;

import org.apache.commons.lang3.StringUtils;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.status.ErrorStatus;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Utils;

public class SlackAppender extends AppenderBase<ILoggingEvent> {
    private String webhook, app;

    @Override
    public void start() {
        if ( StringUtils.isBlank( webhook ) ) {
            addStatus( new ErrorStatus( "Slack webhook is not configured", this ) );
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
            addError( "Failed to send message to Slack webhook", e );
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
        String response = Utils.call( webhook, new SlackMessage( message ) );

        if ( response.contains( "\"ok\":false" ) ) {
            addError( "Slack webhook error: " + response );
        }
    }

    public void setWebhook( String webhook ) {
        this.webhook = webhook;
    }

    public void setApp( String app ) {
        this.app = app;
    }
}
