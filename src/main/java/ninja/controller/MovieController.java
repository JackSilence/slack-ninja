package ninja.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import net.gpedro.integrations.slack.SlackAttachment;
import ninja.consts.Act;
import ninja.consts.Color;
import ninja.service.Movie;
import ninja.slack.Action;
import ninja.util.Check;
import ninja.util.Jsoup;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class MovieController extends DialogController {
	private static final String MOVIE_PATH = "/movie", IMG = "img", RATING_REGEX = "/images/cer_(.+?).gif";

	private static final Map<String, String> RATINGS = new HashMap<>();

	static {
		RATINGS.put( "G", "普遍級" );
		RATINGS.put( "P", "保護級" );
		RATINGS.put( "PG", "輔導級" );
		RATINGS.put( "F2", "輔12級" );
		RATINGS.put( "F5", "輔15級" );
		RATINGS.put( "R", "限制級" );
	}

	@Autowired
	private Movie movie;

	@Value( "${movie.icon.url:}" )
	private String url;

	@Override
	protected String[] skip() {
		return ArrayUtils.toArray( MOVIE_PATH );
	}

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( json( movie.data().entrySet().stream().map( i -> {
			return ImmutableMap.of( LABEL, i.getKey(), OPTIONS, list( i.getValue().keySet().stream().map( super::option ) ) );
		} ) ) );
	}

	@PostMapping( "/theater" )
	@Async
	public void theater( @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		Action action = Slack.action( Act.MOVIE, "請選擇要觀看的電影" );

		SlackAttachment attach = Slack.attachment( Act.MOVIE ).addAction( action );

		films( text, attach ).forEach( i -> action.addOption( option( title( i ), text ) ) );

		message( Slack.message().addAttachments( attach.setFallback( text + "上映影片" ) ), url );
	}

	@PostMapping( MOVIE_PATH )
	@Async
	public void movie( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		String[] params = StringUtils.split( text, null, 2 ); // 考慮電影名稱可能會有空白

		String theater = params[ 0 ], film = params[ 1 ];

		SlackAttachment attach = new SlackAttachment( text + "時刻表" ).setTitle( film );

		List<Element> films = Check.list( list( films( theater, attach ).stream().filter( i -> film.equals( title( i ).text() ) ) ), "查無影片: " + film );

		Element movie = films.get( 0 ), info = movie.child( 1 ).child( 0 ).child( 0 );

		attach.setTitleLink( Jsoup.href( this.movie.link( info ) ) ).setImageUrl( src( info ) ).setColor( star( title( movie ) ) ? Color.G.value() : null );

		String rating = RATINGS.get( Utils.find( RATING_REGEX, src( info = info.nextElementSibling() ) ) );

		attach.setText( tag( rating, StringUtils.remove( info.text(), "片長：" ) ) );

		int number = films.size() == 1 ? 6 : 3;

		films.forEach( i -> {
			String[] times = StringUtils.split( i.child( 1 ).child( 1 ).select( "li:not(.filmVersion,.theaterElse)" ).text().replace( "：", ":" ) );

			attach.addFields( field( i.select( "li.filmVersion" ).text(), IntStream.range( 0, times.length ).boxed().map( j -> {
				return String.format( j % number == 0 ? "%s" : j % number == number - 1 ? " %s\n" : " %s", times[ j ] );

			} ).collect( Collectors.joining() ) ) );
		} );

		message( attach, command, text, url );
	}

	private Elements films( String theater, SlackAttachment attach ) {
		String url = Check.first( movie.data().values().stream().map( i -> i.get( theater ) ).filter( Objects::nonNull ), "查無影院: " + theater );

		Slack.author( attach, theater, url = Movie.URL + url, this.url );

		return Jsoup.select( url, "ul#theaterShowtimeTable" );
	}

	private Map<String, String> option( Element title, String theater ) {
		String film = title.text(), star = star( title ) ? "★" : StringUtils.EMPTY;

		return option2( star + film, Utils.spacer( theater, film ) );
	}

	private boolean star( Element title ) {
		return title.child( 0 ).is( IMG );
	}

	private String src( Element element ) {
		return element.selectFirst( IMG ).attr( "src" );
	}

	private Element title( Element element ) {
		return element.selectFirst( "li.filmTitle" );
	}
}