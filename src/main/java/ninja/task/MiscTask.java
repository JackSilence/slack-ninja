package ninja.task;

import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import net.gpedro.integrations.slack.SlackMessage;
import ninja.consts.Zone;
import ninja.util.Cast;
import ninja.util.Utils;

@Service
public class MiscTask extends Task {
	private static final String TEMPLATE = "*_Cloudinary_*\nMonthly plan: %.0f credits\nUsed in last 30 days: %s credits\n";

	@Scheduled( cron = "0 30 23 * * *", zone = Zone.TAIPEI )
	@Override
	public void exec() {
		try {
			Map<?, ?> credits = Cast.map( new Cloudinary().api().usage( ObjectUtils.emptyMap() ), "credits" );

			Utils.call( url, new SlackMessage( String.format( TEMPLATE, Cast.dble( credits, "limit" ), Cast.dble( credits, "usage" ) ) ) );

		} catch ( Exception e ) {
			throw new RuntimeException( e );

		}
	}
}