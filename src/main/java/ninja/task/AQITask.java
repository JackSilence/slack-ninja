package ninja.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.consts.Zone;
import ninja.controller.AQIController;

@Service
public class AQITask extends Task {
	@Autowired
	private AQIController aqi;

	@Scheduled( cron = "0 30 7-23 * * *", zone = Zone.TAIPEI )
	@Retryable( value = RuntimeException.class, backoff = @Backoff( 30000 ) )
	@Override
	public void exec() {
		aqi.aqi( COMMAND, url );
	}
}