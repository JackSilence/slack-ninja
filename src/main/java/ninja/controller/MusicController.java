package ninja.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.gpedro.integrations.slack.SlackMessage;
import ninja.service.Music;
import ninja.util.Check;
import ninja.util.Utils;

@RestController
public class MusicController extends BaseController {
	@Autowired
	private Music music;

	@PostMapping( "/music" )
	@Async
	public void music( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		Stream<List<String>> songs = music.data().values().stream().flatMap( List::stream );

		if ( StringUtils.isEmpty( text ) ) {
			message( String.format( "Number of songs: *%d*", songs.count() ), url );

		} else {
			message( String.format( "*%s*\n%s", text, Check.empty( String.join( "\n", Utils.list( songs.filter( i -> {
				return Arrays.stream( i.get( 0 ).split( "[,&]" ) ).anyMatch( j -> {
					return text.equalsIgnoreCase( j.trim() );

				} ) || text.equalsIgnoreCase( StringUtils.substringBefore( i.get( 1 ), "(" ).trim() );

			} ).map( i -> String.format( "%s - <%s|%s>", i.toArray() ) ) ) ), "查無歌曲: " + text ) ), url );
		}
	}

	private void message( String text, String url ) {
		message( new SlackMessage( text ), url );
	}
}