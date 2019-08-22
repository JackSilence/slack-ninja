package ninja.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ninja.consts.Zone;
import ninja.controller.TyphoonController;

@Service
public class TyphoonTask extends Task {
	@Autowired
	private TyphoonController typhoon;

	@Scheduled( cron = "0 0 0,12 * * *", zone = Zone.TAIPEI )
	@Override
	public void exec() {
		typhoon.typhoon( COMMAND, url );
	}
}