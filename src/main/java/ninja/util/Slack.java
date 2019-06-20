package ninja.util;

import org.apache.commons.lang3.StringUtils;

import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;

public class Slack {
	private static final String ICON = "https://platform.slack-edge.com/img/default_application_icon.png";

	public static SlackMessage message( SlackAttachment attach, String command, String text ) {
		return new SlackMessage( StringUtils.EMPTY ).addAttachments( footer( attach, command, text ) );
	}

	public static SlackAttachment attachment() {
		return new SlackAttachment( StringUtils.EMPTY );
	}

	private static SlackAttachment footer( SlackAttachment attach, String command, String text ) {
		return attach.setFooter( String.format( "%s %s", command, text ) ).setFooterIcon( ICON );
	}
}