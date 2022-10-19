package ninja.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

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

import net.gpedro.integrations.slack.SlackAttachment;
import ninja.consts.Act;
import ninja.consts.Color;
import ninja.service.Movie;
import ninja.util.Check;
import ninja.util.Jsoup;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class MovieController extends GroupController<Map<String, String>> {
	private static final String MOVIE_PATH = "/movie", IMG = "img", RATING_REGEX = "/images/cer_(.+?).gif";

	private static final String NEW_PATH = MOVIE_PATH + "/new", NEW_URL = Movie.URL + "/movie/new/", TITLE = "本周新片上映%d部";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern( "M/d/yyyy" );

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
		return ArrayUtils.toArray( MOVIE_PATH, NEW_PATH );
	}

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( groups( movie ) );
	}

	@Override
	protected Stream<Map<String, String>> group( Entry<String, Map<String, String>> entry ) {
		return entry.getValue().keySet().stream().map( super::option );
	}

	@PostMapping( "/theater" )
	@Async
	public void theater( @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		var action = Slack.action( Act.MOVIE, "請選擇要觀看的電影" );

		var attach = Slack.attachment( Act.MOVIE ).addAction( action );

		films( text, attach ).forEach( i -> action.addOption( option( title( i ), text ) ) );

		message( Slack.message().addAttachments( attach.setFallback( text + "上映影片" ) ), url );
	}

	@PostMapping( MOVIE_PATH )
	@Async
	public void movie( @RequestParam String command, @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		var params = StringUtils.split( text, null, 2 ); // 考慮電影名稱可能會有空白

		Check.expr( params.length > 0 && params.length <= 2, "參數個數有誤: " + text );

		String theater = params[ 0 ], film;

		var attach = new SlackAttachment();

		var elements = films( theater, attach );

		attach.setTitle( film = params.length == 1 ? title( elements.first() ).text() : params[ 1 ] ).setFallback( String.format( "%s %s時刻表", theater, film ) );

		var films = Check.list( list( elements.stream().filter( i -> film.equals( title( i ).text() ) ) ), "查無影片: " + film );

		Element movie = films.get( 0 ), info = movie.child( 1 ).child( 0 ).child( 0 );

		attach.setTitleLink( Jsoup.href( this.movie.link( info ) ) ).setImageUrl( src( info ) ).setColor( star( title( movie ) ) ? Color.G.value() : null );

		var rating = RATINGS.get( Utils.find( RATING_REGEX, src( info = info.nextElementSibling() ) ) );

		attach.setText( tag( rating, StringUtils.remove( info.text(), "片長：" ) ) );

		var number = films.size() == 1 ? 6 : 3;

		films.forEach( i -> {
			var times = StringUtils.split( i.child( 1 ).child( 1 ).select( "li:not(.filmVersion,.theaterElse)" ).text().replace( "：", ":" ) );

			attach.addFields( field( i.select( "li.filmVersion" ).text(), IntStream.range( 0, times.length ).boxed().map( j -> {
				return String.format( j % number == 0 ? "%s" : j % number == number - 1 ? " %s\n" : " %s", times[ j ] );

			} ).collect( Collectors.joining() ) ) );
		} );

		message( attach, command, text, url );
	}

	@PostMapping( NEW_PATH )
	@Async
	public void mnew( @RequestParam String command, @RequestParam( RESPONSE_URL ) String url ) {
		var films = Jsoup.select( NEW_URL, "article.filmList" );

		var title = String.format( TITLE, films.size() );

		var now = LocalDate.now( ZONE_ID );

		var partition = Lists.partition( films, 5 );

		for ( int i = 0; i < partition.size(); i++ ) {
			var message = i == 0 ? Slack.message( Slack.author( new SlackAttachment( title ), title, NEW_URL, this.url ), command, StringUtils.EMPTY ) : Slack.message();

			partition.get( i ).forEach( j -> {
				Element image = j.selectFirst( "a.image.filmListPoster" ), runtime = j.selectFirst( "div.runtime" );

				var rating = RATINGS.getOrDefault( Utils.find( RATING_REGEX, src( runtime ) ), "無分級" );

				var info = Arrays.stream( runtime.text().split( StringUtils.SPACE ) ).map( k -> StringUtils.substringAfter( k, "：" ) ).toArray( String[]::new );

				var attach = Slack.attachment( title( j ).text(), image.attr( "abs:href" ) );

				try {
					var date = LocalDate.parse( info[ 1 ], DATE_TIME_FORMATTER );

					attach.setColor( ( date.isEqual( now ) ? Color.G : date.isBefore( now ) ? Color.Y : Color.R ).value() );

					attach.setText( tag( rating, info[ 0 ], StringUtils.substringBeforeLast( info[ 1 ], "/" ) ) );

				} catch ( DateTimeParseException e ) {
					attach.setText( tag( rating, info[ 0 ] ) );
				}

				message.addAttachments( attach.setImageUrl( src( image ) ) );
			} );

			message( message, url );
		}
	}

	private Elements films( String theater, SlackAttachment attach ) {
		var url = Check.first( movie.data().values().stream().map( i -> i.get( theater ) ).filter( Objects::nonNull ), "查無影院: " + theater );

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
		var img = element.selectFirst( IMG );

		return img == null ? StringUtils.EMPTY : img.attr( "src" );
	}

	private Element title( Element element ) {
		return element.selectFirst( ".filmTitle" );
	}
}
