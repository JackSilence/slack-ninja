package ninja.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Zone;
import ninja.service.AQI;
import ninja.util.Utils;

@Service
public class SiteTask extends Task {
	private static final String TEMPLATE = "測站數量: %d, 不重複名稱數: %d";

	@Autowired
	private AQI aqi;

	@Scheduled( cron = "0 0 23 * * *", zone = Zone.TAIPEI )
	@Override
	public void exec() {
		List<String> sites = Utils.list( aqi.data().values().stream().flatMap( List::stream ) );

		String text = String.format( TEMPLATE, sites.size(), sites.stream().distinct().count() );

		Utils.call( url, new SlackMessage( text ) );
	}
}