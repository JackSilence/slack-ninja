package ninja.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import net.gpedro.integrations.slack.SlackActionType;
import net.gpedro.integrations.slack.SlackAttachment;
import ninja.consts.Act;
import ninja.slack.Action;
import ninja.slack.Confirm;
import ninja.util.Check;
import ninja.util.Jsoup;
import ninja.util.Slack;
import ninja.util.Utils;

@RestController
public class MovieController extends DialogController {
	private static final String URL = "http://www.atmovies.com.tw", PATH = "/showtime/a02/";

	private static final String MOVIE_PATH = "/movie", IMG = "img", RATING_REGEX = "/images/cer_(.+?).gif";

	private static final Map<String, Map<String, String>> THEATERS = new LinkedHashMap<>();

	private static final Map<String, String> RATINGS = new HashMap<>();

	static {
		RATINGS.put( "G", "普遍級" );
		RATINGS.put( "P", "保護級" );
		RATINGS.put( "PG", "輔導級" );
		RATINGS.put( "F2", "輔12級" );
		RATINGS.put( "F5", "輔15級" );
		RATINGS.put( "R", "限制級" );
	}

	@Value( "${movie.icon.url:}" )
	private String url;

	@Override
	protected String[] skip() {
		return ArrayUtils.toArray( MOVIE_PATH );
	}

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( json( THEATERS.entrySet().stream().map( i -> {
			return ImmutableMap.of( LABEL, i.getKey(), OPTIONS, list( i.getValue().keySet().stream().map( super::option ) ) );
		} ) ) );
	}

	@PostMapping( "/theater" )
	public String theater( @RequestParam String text ) {
		Action action = new Action( Act.MOVIE, "請選擇要觀看的電影", SlackActionType.SELECT, null ).setConfirm( new Confirm() );

		SlackAttachment attach = Slack.attachment( Act.MOVIE ).addAction( action );

		films( text, attach ).forEach( i -> action.addOption( option( title( i ), text ) ) );

		return message( Slack.message().addAttachments( attach ) );
	}

	@PostMapping( MOVIE_PATH )
	public String movie( @RequestParam String command, @RequestParam String text ) {
		String[] params = Check.params( text );

		String theater = params[ 0 ], film = params[ 1 ];

		SlackAttachment attach = Slack.attachment().setTitle( film );

		List<Element> films = list( films( theater, attach ).stream().filter( i -> film.equals( title( i ).text() ) ) );

		Assert.notEmpty( films, "查無影片: " + film );

		Element movie = films.get( 0 ), info = movie.child( 1 ).child( 0 ).child( 0 );

		attach.setTitleLink( Jsoup.href( link( info ) ) ).setImageUrl( src( info ) ).setColor( star( title( movie ) ) ? "good" : null );

		String rating = RATINGS.get( Utils.find( RATING_REGEX, src( info = info.nextElementSibling() ) ) );

		attach.setText( tag( rating, StringUtils.remove( info.text(), "片長：" ) ) );

		int number = films.size() == 1 ? 6 : 3;

		films.forEach( i -> {
			String[] times = StringUtils.split( i.child( 1 ).child( 1 ).select( "li:not(.filmVersion,.theaterElse)" ).text().replace( "：", ":" ) );

			attach.addFields( field( i.select( "li.filmVersion" ).text(), IntStream.range( 0, times.length ).boxed().map( j -> {
				return String.format( j % number == 0 ? "%s" : j % number == number - 1 ? "|%s\n" : "|%s", times[ j ] );

			} ).collect( Collectors.joining() ) ) );
		} );

		return message( attach, command, text );
	}

	private Elements films( String theater, SlackAttachment attach ) {
		String url = Check.first( THEATERS.values().stream().map( i -> i.get( theater ) ).filter( Objects::nonNull ), "查無影院: " + theater );

		attach.setAuthorName( theater ).setAuthorLink( url = URL + url ).setAuthorIcon( this.url );

		return Jsoup.select( url, "ul#theaterShowtimeTable" );
	}

	private Map<String, String> option( Element title, String theater ) {
		String film = title.text(), star = star( title ) ? "★" : StringUtils.EMPTY;

		return ImmutableMap.of( TEXT, star + film, VALUE, Utils.spacer( theater, film ) );
	}

	private boolean star( Element title ) {
		return title.child( 0 ).is( IMG );
	}

	private String src( Element element ) {
		return element.selectFirst( IMG ).attr( "src" );
	}

	private Element link( Element element ) {
		return element.selectFirst( "a[href]" );
	}

	private Element title( Element element ) {
		return element.selectFirst( "li.filmTitle" );
	}

	@PostConstruct
	public void init() {
		Jsoup.select( URL + PATH, "ul#theaterList > li", i -> {
			if ( i.hasClass( "type0" ) ) {
				THEATERS.put( StringUtils.remove( i.text(), "▼" ), new LinkedHashMap<>() );
			} else {
				Element link = link( i );

				THEATERS.get( Iterables.getLast( THEATERS.keySet() ) ).put( link.text(), Jsoup.href( link ) );
			}
		} );
	}
}