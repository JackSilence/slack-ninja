package ninja.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.service.Metro;
import ninja.util.Cast;
import ninja.util.Check;
import ninja.util.Gson;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class MetroController extends DialogController {
	private static final String WEB_URL = "https://web.metro.taipei/pages/tw/ticketroutetime/%s/%s", TITLE = "捷運票價及乘車時間";

	private static final String PATH_INFO = "routetimepathinfo", TICKET_INFO = "ticketinfo", payload = "{\"StartSID\":\"%s\",\"EndSID\":\"%s\",\"Lang\":\"tw\"}";

	@Autowired
	private Metro metro;

	@PostMapping( "/mrt" )
	@Async
	public void mrt( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		var params = Check.station( Check.params( text ) );

		String start = id( params[ 0 ] ), end = id( params[ 1 ] );

		log.info( "Start: {}, end: {}", start, end );

		var attach = Slack.attachment( TITLE, String.format( WEB_URL, start, end ) );

		try {
			Map<?, ?> path = call( PATH_INFO, start, end ), ticket = call( TICKET_INFO, start, end );

			end = StringUtils.appendIfMissing( Cast.string( path, "EndStationName" ), "站" );

			attach.setText( String.format( "%s=>%s（約 %s 分鐘）", path.get( "Path" ), end, path.get( "TravelTime" ) ) );

			var discount40 = Cast.string( ticket, "Discount40" );

			attach.addFields( field( "全票", Cast.string( ticket, "DeductedFare" ) ) ).addFields( field( "敬老、愛心", discount40 ) );

			attach.addFields( field( "臺北市兒童", Cast.string( ticket, "Discount60" ) ) ).addFields( field( "新北市兒童", discount40 ) );

		} catch ( RuntimeException e ) {
			log.error( StringUtils.EMPTY, e );
		}

		message( attach, command, text, url );
	}

	private Map<?, ?> call( String path, String start, String end ) {
		return Gson.from( Utils.call( Request.Post( metro.api( path ) ), String.format( payload, start, end ) ), Map.class );
	}

	private String id( String station ) {
		return Check.station( metro, station );
	}
}