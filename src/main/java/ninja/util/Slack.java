package ninja.util;

import org.apache.commons.lang3.StringUtils;

import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;

public class Slack {
	public static SlackMessage message() {
		return new SlackMessage( StringUtils.EMPTY );
	}

	public static SlackAttachment attachment() {
		return new SlackAttachment( StringUtils.EMPTY );
	}
}