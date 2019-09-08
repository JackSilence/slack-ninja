package ninja.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.consts.Zone;
import ninja.service.AQI;
import ninja.util.Utils;

@Service
public class SiteTask extends Task {
	private static final String TEMPLATE = "*AQI Site*\nCount: %d, distinct: %d";

	@Autowired
	private AQI aqi;

	@Scheduled( cron = "0 0 23 * * *", zone = Zone.TAIPEI )
	@Override
	public void exec() {
		List<String> sites = Utils.list( aqi.data().values().stream().flatMap( List::stream ) );

		call( String.format( TEMPLATE, sites.size(), sites.stream().distinct().count() ) );
	}
}