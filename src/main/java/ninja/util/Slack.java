package ninja.util;

import org.apache.commons.lang3.StringUtils;

import net.gpedro.integrations.slack.SlackActionType;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Act;
import ninja.slack.Action;
import ninja.slack.Confirm;

public class Slack {
	private static final String ICON = "https://platform.slack-edge.com/img/default_application_icon.png";

	public static SlackMessage message( SlackAttachment attach, String command, String text ) {
		return message().addAttachments( footer( attach, command, text ) );
	}

	public static SlackMessage message( String text, String channel ) {
		return new SlackMessage( text ).setChannel( channel );
	}

	public static SlackMessage message() {
		return new SlackMessage( StringUtils.EMPTY );
	}

	public static SlackAttachment attachment( Act act ) {
		return Slack.attachment( "#3AA3E3" ).setCallbackId( act.name() );
	}

	public static SlackAttachment attachment( String color ) {
		return attachment().setColor( color );
	}

	public static SlackAttachment attachment() {
		return new SlackAttachment( StringUtils.EMPTY );
	}

	public static Action action( Act act, String text ) {
		return new Action( act, text, SlackActionType.SELECT, null ).setConfirm( new Confirm() );
	}

	private static SlackAttachment footer( SlackAttachment attach, String command, String text ) {
		return attach.setFooter( String.format( "%s %s", command, text ) ).setFooterIcon( ICON );
	}
}