package ninja.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackAttachment;
import ninja.consts.Color;
import ninja.util.Cast;
import ninja.util.Gson;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class TyphoonController extends BaseController {
	private static final String WARN_URL = "https://www.cwb.gov.tw/Data/js/warn/Warning_Content.js", TY_NEWS = "'TY_NEWS'";

	private static final String DATA_URL = "https://www.cwb.gov.tw/Data/js/typhoon/TY_NEWS-Data.js";

	private static final String TIME_REGEX = "var TY_DataTime = '(.+?)';", COUNT_REGEX = "var TY_COUNT = \\[(.+?)];";

	private static final String WEB_URL = "https://www.cwb.gov.tw/V8/C/P/Typhoon/TY_NEWS.html", TITLE = "氣象局颱風消息";

	private static final String IMG_URL = "https://www.cwb.gov.tw/Data/typhoon/TY_NEWS/PTA_%s-72_zhtw.png";

	private static final String AREA_URL = "https://www.cwb.gov.tw/Data/typhoon/TY_NEWS/WSP-AREA_%s_WHOLE-DURATION.json";

	@PostMapping( "/typhoon" )
	@Async
	public void typhoon( @RequestParam String command, @RequestParam( RESPONSE_URL ) String url ) {
		if ( !Utils.call( WARN_URL ).contains( TY_NEWS ) ) {
			log.info( "查無颱風消息" );

			return;
		}

		String data = Utils.call( DATA_URL ), time = Utils.find( TIME_REGEX, data ), count = Utils.find( COUNT_REGEX, data );

		SlackAttachment attach = Slack.attachment( TITLE, WEB_URL ).setImageUrl( String.format( IMG_URL, time ) );

		int pr = Cast.dble( Cast.map( Gson.from( Utils.call( String.format( AREA_URL, time ) ), Map.class ), "AREA" ), "Taipei" ).intValue();

		attach.addFields( field( "熱帶低壓 / 颱風", count.replace( ",", " / " ) + "個" ) ).addFields( field( "侵襲台北機率", pr + "%" ) );

		message( attach.setColor( ( pr > 80 ? Color.R : pr > 40 ? Color.Y : Color.G ).value() ), command, StringUtils.EMPTY, url );
	}
}