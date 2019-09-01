package ninja.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import com.google.common.collect.Iterables;

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
		List<List<String>> songs = Iterables.getOnlyElement( music.data().values() );

		if ( StringUtils.isEmpty( text ) ) {
			String duplicate = text( songs.stream().filter( i -> Collections.frequency( songs, i ) > 1 ).distinct() );

			duplicate = duplicate.isEmpty() ? StringUtils.EMPTY : ", duplicate:\n" + duplicate;

			message( String.format( "Number of songs: *%d*%s", songs.size(), duplicate ), url );

		} else {
			String query = HtmlUtils.htmlUnescape( text );

			message( String.format( "*%s*\n%s", tag( query ), Check.empty( text( songs.stream().filter( i -> {
				String artist = i.get( 0 ), name = i.get( 2 ), feat = StringUtils.defaultString( Utils.find( "\\(feat. (.+?)\\)", name ) );

				return Stream.concat( Stream.of( artist, name ), Stream.of( artist, feat ).map( j -> j.split( "[,&]" ) ).flatMap( Arrays::stream ) ).anyMatch( j -> {
					return query.equalsIgnoreCase( j.trim() );

				} ) || query.equalsIgnoreCase( RegExUtils.removeAll( name, "\\(.+?\\)|\\[.+?\\]" ).trim() );

			} ) ), "查無歌曲: " + query ) ), url );
		}
	}

	private String text( Stream<List<String>> stream ) {
		return Utils.join( stream.map( i -> String.format( "%s - <%s|%s>", i.toArray() ) ), "\n" );
	}

	private void message( String text, String url ) {
		message( new SlackMessage( text ), url );
	}
}