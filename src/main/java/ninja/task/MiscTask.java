package ninja.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import ninja.consts.Zone;
import ninja.util.Cast;

@Service
public class MiscTask extends Task {
	private static final String TEMPLATE = "*Cloudinary*\nMonthly plan: %.0f credits\nUsed in last 30 days: %s credits\n";

	@Scheduled( cron = "0 30 23 * * *", zone = Zone.TAIPEI )
	@Override
	public void exec() {
		try {
			var credits = Cast.map( new Cloudinary().api().usage( ObjectUtils.emptyMap() ), "credits" );

			call( String.format( TEMPLATE, Cast.dble( credits, "limit" ), Cast.dble( credits, "usage" ) ) );

		} catch ( Exception e ) {
			throw new RuntimeException( e );

		}
	}
}