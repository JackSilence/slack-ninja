package ninja.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Iterables;

import net.gpedro.integrations.slack.SlackAttachment;
import ninja.consts.Color;
import ninja.util.Cast;
import ninja.util.Check;
import ninja.util.Gson;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class TyphoonController extends DialogController {
	private static final String WARN_URL = "https://www.cwb.gov.tw/Data/js/warn/Warning_Content.js", TY_NEWS = "'TY_NEWS'";

	private static final String DATA_URL = "https://www.cwb.gov.tw/Data/js/typhoon/TY_NEWS-Data.js";

	private static final String TIME_REGEX = "var TY_DataTime = '(.+?)';", COUNT_REGEX = "var TY_COUNT = \\[(.+?)];";

	private static final String WEB_URL = "https://www.cwb.gov.tw/V8/C/P/Typhoon/TY_NEWS.html", TITLE = "氣象局颱風消息";

	private static final String IMG_JSON = "PTA_IMGS_%s_zhtw.json", AREA_JSON = "WSP-AREA_%s_WHOLE-DURATION.json";

	private static final String NEWS_URL = "https://www.cwb.gov.tw/Data/typhoon/TY_NEWS/", DEFAULT = "臺北";

	private static final Map<String, String> AREAS = new LinkedHashMap<>();

	static {
		AREAS.put( "基隆市", "Keelung" );
		AREAS.put( "臺北市", "TaipeiCity" );
		AREAS.put( "新北市", "NewTaipei" );
		AREAS.put( "桃園市", "Taoyuan" );
		AREAS.put( "新竹市", "HsinchuCity" );
		AREAS.put( "新竹縣", "HsinchuCounty" );
		AREAS.put( "苗栗縣", "Miaoli" );
		AREAS.put( "臺中市", "Taichung" );
		AREAS.put( "彰化縣", "Changhua" );
		AREAS.put( "南投縣", "Nantou" );
		AREAS.put( "雲林縣", "Yunlin" );
		AREAS.put( "嘉義市", "ChiayiCity" );
		AREAS.put( "嘉義縣", "ChiayiCounty" );
		AREAS.put( "臺南市", "Tainan" );
		AREAS.put( "高雄市", "Kaohsiung" );
		AREAS.put( "屏東縣", "Pingtung" );
		AREAS.put( "恆春", "Hengchun" );
		AREAS.put( "宜蘭縣", "Yilan" );
		AREAS.put( "花蓮縣", "Hualien" );
		AREAS.put( "臺東縣", "Taitung" );
		AREAS.put( "綠島", "Ludao" );
		AREAS.put( "蘭嶼", "Lanyu" );
		AREAS.put( "澎湖縣", "Penghu" );
		AREAS.put( "金門縣", "Kinmen" );
		AREAS.put( "連江縣", "LienChiang" );
	}

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( DEFAULT, options( AREAS.keySet() ) );
	}

	@PostMapping( "/typhoon" )
	@Async
	public void typhoon( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		if ( !Utils.call( WARN_URL ).contains( TY_NEWS ) ) {
			message( "查無颱風消息", url );

			return;
		}

		String area = StringUtils.defaultIfEmpty( text, DEFAULT );

		String data = Utils.call( DATA_URL ), time = Utils.find( TIME_REGEX, data ), count = Utils.find( COUNT_REGEX, data );

		SlackAttachment attach = Slack.attachment( TITLE, WEB_URL ).setImageUrl( url( Iterables.getLast( Cast.list( map( IMG_JSON, time ), "WHOLE" ) ) ) );

		int pr = Cast.dble( Cast.map( map( AREA_JSON, time ), "AREA" ), Check.nil( AREAS.get( area ), "查無區域: " + area ) ).intValue();

		attach.addFields( field( "熱帶低壓 / 颱風", count.replace( ",", " / " ) + "個" ) ).addFields( field( "侵襲" + area + "機率", pr + "%" ) );

		message( attach.setColor( ( pr > 80 ? Color.R : pr > 40 ? Color.Y : Color.G ).value() ), command, text, url );
	}

	private Map<?, ?> map( String path, String time ) {
		return Gson.from( Utils.call( url( String.format( path, time ) ) ), Map.class );
	}

	private String url( Object path ) {
		return NEWS_URL + path;
	}
}