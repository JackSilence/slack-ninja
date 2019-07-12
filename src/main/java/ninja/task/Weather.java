package ninja.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.controller.WeatherController;

@Service
public class Weather extends Task {
	@Autowired
	private WeatherController weather;

	@Scheduled( cron = "0 0 4,10,16,22 * * *" )
	@Override
	public void exec() {
		exec( weather.weather( COMMAND, StringUtils.EMPTY ) );
	}
}