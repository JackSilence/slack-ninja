package ninja.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.consts.Zone;
import ninja.controller.NASAController;

@Service
public class NASATask extends Task {
	@Autowired
	private NASAController nasa;

	@Scheduled( cron = "0 45 0 * * *", zone = Zone.NEW_YORK )
	@Retryable( value = RuntimeException.class, backoff = @Backoff( 30000 ) )
	@Override
	public void exec() {
		nasa.apod( COMMAND, url );
	}
}