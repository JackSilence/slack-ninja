package ninja.task;

import java.util.Arrays;
import java.util.Date;

import com.google.common.net.UrlEscapers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import magic.service.IService;
import ninja.consts.Zone;
import ninja.util.Utils;

@Service
public class TestTask implements IService {
    private static final String COMMAND_URL = "https://slack.com/api/chat.command?token=%s&channel=%s&command=/%s&text=%s";

    @Value( "${slack.legacy.token:}" )
    private String token;

    @Value( "${slack.channel:}" )
    private String channel;

    @Scheduled( cron = "0 0 18 * * *", zone = Zone.TAIPEI )
    @Override
    public void exec() {
        String thsr = String.format( "thsr 南港 台中 %tF 10:00 出發", new Date() );

        Arrays.asList( "bus 內湖幹線 \"康寧醫院\"", "movie 哈拉影城", "mrt 中山 松山", "station 康寧醫院 捷運內湖站", "theater 哈拉影城", "dict pig", thsr ).forEach( i -> {
            String[] params = i.split( " ", 2 );

            Utils.call( String.format( COMMAND_URL, token, channel, params[ 0 ], UrlEscapers.urlFragmentEscaper().escape( params[ 1 ] ) ) );
        } );
    }
}