package ninja.task;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.consts.Zone;
import ninja.controller.NASAController;

@Service
public class NASATask extends Task {
	@Autowired
	private NASAController nasa;

	@Scheduled( cron = "0 0 0 * * *", zone = Zone.NEW_YORK )
	@Override
	public void exec() {
		nasa.apod( COMMAND, StringUtils.EMPTY, url );
	}
}