package ninja.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class TyphoonController extends BaseController {
	private static final String JS_URL = "https://www.cwb.gov.tw/Data/js/typhoon/TY_NEWS-Data.js", TIME_REGEX = "var TY_DataTime = '(.+?)';";

	private static final String WEB_URL = "https://www.cwb.gov.tw/V8/C/P/Typhoon/TY_NEWS.html", TITLE = "氣象局颱風消息";

	private static final String IMG_URL = "https://www.cwb.gov.tw/Data/typhoon/TY_NEWS/PTA_%s-72_zhtw.png";

	@PostMapping( "/typhoon" )
	@Async
	public void typhoon( @RequestParam String command, @RequestParam( RESPONSE_URL ) String url ) {
		String time = Utils.find( TIME_REGEX, Utils.call( JS_URL ) );

		if ( time == null ) {
			log.info( "查無颱風消息" );
		}

		message( Slack.attachment( TITLE, WEB_URL ).setImageUrl( String.format( IMG_URL, time ) ), command, StringUtils.EMPTY, url );
	}
}