package ninja.task;

import org.springframework.beans.factory.annotation.Value;

import magic.service.IService;

abstract class Task implements IService {
	protected static final String COMMAND = "scheduled-task";

	@Value( "${slack.webhook.url:}" )
	protected String url;
}