package ninja.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.consts.Zone;
import ninja.controller.AQIController;

@Service
public class AQITask extends Task {
	@Autowired
	private AQIController aqi;

	@Scheduled( cron = "0 30 7-23 * * *", zone = Zone.TAIPEI )
	@Override
	public void exec() {
		aqi.aqi( COMMAND, StringUtils.EMPTY, url );
	}
}