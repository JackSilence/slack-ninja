package ninja.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackMessage;
import ninja.service.Music;

@RestController
public class MusicController extends BaseController {
	@Autowired
	private Music music;

	@PostMapping( "/music" )
	public void music( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		if ( StringUtils.isEmpty( text ) ) {
			message( String.format( "Number of songs: *%d*", music.data().values().stream().mapToInt( List::size ).sum() ), url );

		} else {
			message( String.format( "*%s*\n%s", text, music.data().entrySet().stream().filter( i -> {
				return text.equalsIgnoreCase( StringUtils.substringBefore( i.getKey(), "(" ) );

			} ).flatMap( i -> i.getValue().stream() ).collect( Collectors.joining( "\n" ) ) ), url );
		}
	}

	private void message( String text, String url ) {
		message( new SlackMessage( text ), url );
	}
}