package ninja.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import magic.service.IService;
import magic.util.Utils;
import ninja.controller.WeatherController;

@Service
public class Weather implements IService {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	@Autowired
	private WeatherController weather;

	@Value( "${slack.webhook.url:}" )
	private String url;

	@Scheduled( cron = "0 0 4,10,16,22 * * *" )
	@Override
	public void exec() {
		Request request = Request.Post( url ).bodyString( weather.weather( "scheduled-task", StringUtils.EMPTY ), ContentType.APPLICATION_JSON );

		log.info( Utils.getEntityAsString( request ) );
	}
}