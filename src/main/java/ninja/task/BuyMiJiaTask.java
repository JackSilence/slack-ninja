package ninja.task;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.net.HttpHeaders;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import magic.service.IMailService;
import magic.service.IService;
import magic.service.Slack;
import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Zone;
import ninja.util.Cast;
import ninja.util.Gson;

@Service
public class BuyMiJiaTask implements IService {
    private static final String TEMPLATE = "/template/shopee/template.html", ITEMS = "/template/shopee/items.html";

    private static final String SEARCH_URL = "https://shopee.tw/api/v4/search/search_items/?";

    private static final String QUERY = "by=ctime&limit=20&match_id=14358222&newest=0&order=desc&page_type=shop";

    private static final String LINK = "https://shopee.tw/%s-i.%.0f.%.0f", IMAGE = "https://cf.shopee.tw/file/%s_tn";

    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36";

    @Autowired
    private IMailService mail;

    @Autowired
    private Slack slack;

    @SuppressWarnings( "unchecked" )
    @Scheduled( cron = "0 0 12,19 * * *", zone = Zone.TAIPEI )
    public void exec() {
        Utils.sleep( RandomUtils.nextInt( 1000, 10000 ) );

        String items = Utils.getResourceAsString( ITEMS ), subject;

        var sb = new StringBuilder();

        List<SlackAttachment> attachments = new ArrayList<>();

        var now = new Date();

        var result = Gson.from( Utils.getEntityAsString( get( SEARCH_URL + QUERY ) ), Map.class );

        ( ( List<Map<String, Object>> ) MoreObjects.firstNonNull( result.get( "items" ), Collections.EMPTY_LIST ) ).stream().map( i -> ( Map<String, Object> ) i.get( "item_basic" ) ).forEach( i -> {
            Double shopId = Cast.dble( i, "shopid" ), itemId = Cast.dble( i, "itemid" );

            int min = price( i.get( "price_min" ) ), max = price( i.get( "price_max" ) );

            String name = Cast.string( i, "name" ), price, link, title;

            i.put( "price", price = min == max ? String.valueOf( min ) : min + "<br>" + max );

            i.put( "link", link = String.format( LINK, name.replaceAll( "\\s", "-" ), shopId, itemId ) );

            Date c = new Date( ( long ) ( Cast.dble( i, "ctime" ) * 1000 ) ), ytd = DateUtils.addDays( now, -1 );

            boolean isNow = DateUtils.isSameDay( c, now ), isYtd = DateUtils.isSameDay( c, ytd );

            i.put( "color", isNow ? "#ffeb3b" : isYtd ? "#EEEEE0" : "#ffffff" );

            sb.append( new StringSubstitutor( i ).replace( items ) );

            if ( isNow || isYtd || attachments.isEmpty() ) {
                SlackAttachment attachment = new SlackAttachment( title = String.format( "%s $%s", name, price.replace( "<br>", " - $" ) ) );

                String color = isNow ? "good" : isYtd ? "warning" : "danger";

                attachments.add( attachment.setTitle( title ).setTitleLink( link ).setColor( color ).setImageUrl( String.format( IMAGE, i.get( "image" ) ) ) );
            }
        } );

        subject = String.format( "%s_%s", "百米家新商品通知", LocalDateTime.now( ZoneId.of( Zone.TAIPEI ) ).format( DateTimeFormatter.ofPattern( "yyyyMMddHH" ) ) );

        mail.send( subject, String.format( Utils.getResourceAsString( TEMPLATE ), sb.toString() ) );

        slack.call( new SlackMessage( subject ).setAttachments( attachments ) );
    }

    private Request get( String uri ) {
        return Request.Get( uri ).userAgent( UA ).setHeader( HttpHeaders.REFERER, "https://shopee.tw" );
    }

    private int price( Object price ) {
        return price == null ? 0 : ( int ) ( ( Double ) price / 100000 );
    }
}