package ninja.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.controller.AQIController;

@Service
public class AQI extends Task {
	@Autowired
	private AQIController aqi;

	@Scheduled( cron = "0 0 0-16 * * *" )
	@Override
	public void exec() {
		exec( aqi.aqi( COMMAND, StringUtils.EMPTY ) );
	}
}