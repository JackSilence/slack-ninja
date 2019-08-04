package ninja.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.consts.Zone;
import ninja.controller.DelController;

@Service
public class Delete extends Task {
	@Autowired
	private DelController del;

	@Value( "${slack.channel:}" )
	private String channel;

	@Scheduled( cron = "0 0 0 * * *", zone = Zone.TAIPEI )
	@Override
	public void exec() {
		exec( del.delete( channel, COMMAND, "昨天" ) );
	}
}