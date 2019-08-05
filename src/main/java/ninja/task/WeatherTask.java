package ninja.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.consts.Zone;
import ninja.controller.WeatherController;

@Service
public class WeatherTask extends Task {
	@Autowired
	private WeatherController weather;

	@Scheduled( cron = "0 0 0,6,12,18 * * *", zone = Zone.TAIPEI )
	@Override
	public void exec() {
		exec( weather.weather( COMMAND, StringUtils.EMPTY ) );
	}
}