package ninja.util;

import org.apache.commons.lang3.StringUtils;

import net.gpedro.integrations.slack.SlackActionType;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Act;
import ninja.consts.Color;
import ninja.slack.Action;
import ninja.slack.Confirm;

public class Slack {
	private static final String ICON = "https://platform.slack-edge.com/img/default_application_icon.png";

	public static SlackMessage message( SlackAttachment attach, String command, String text ) {
		return message().addAttachments( attach.setFooter( String.format( "%s %s", command, text ) ).setFooterIcon( ICON ) );
	}

	public static SlackMessage message( String text, String channel ) {
		return new SlackMessage( text ).setChannel( channel );
	}

	public static SlackMessage message() {
		return new SlackMessage( StringUtils.EMPTY );
	}

	public static SlackAttachment attachment( Act act ) {
		return attachment( Color.B ).setCallbackId( act.name() );
	}

	public static SlackAttachment attachment( Color color ) {
		return new SlackAttachment( StringUtils.EMPTY ).setColor( color.value() );
	}

	public static SlackAttachment attachment( String title, String link ) {
		return new SlackAttachment( title ).setTitle( title ).setTitleLink( link );
	}

	public static SlackAttachment author( SlackAttachment attach, String name, String link, String icon ) {
		return attach.setAuthorName( name ).setAuthorLink( link ).setAuthorIcon( icon );
	}

	public static Action action( Act act, String text ) {
		return new Action( act, text, SlackActionType.SELECT, null ).setConfirm( new Confirm() );
	}
}