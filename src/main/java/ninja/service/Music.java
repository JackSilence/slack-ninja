package ninja.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import magic.service.IService;
import magic.service.Selenium;

@Service
public class Music extends Data<List<List<String>>> {
	@Autowired
	private IService apple;

	@Value( "${playlist.url:}" )
	private String url;

	@Override
	void init( Map<String, List<List<String>>> data ) {
		data.put( id(), Collections.emptyList() );

		apple.exec();
	}

	private String id() {
		return StringUtils.substringBefore( StringUtils.substringAfterLast( url, "/" ), "?" );
	}

	@Service( "apple" )
	private class Apple extends Selenium {
		@Async
		@Override
		public void exec() {
			run( "--start-maximized" );
		}

		@Override
		protected synchronized void run( WebDriver driver ) {
			driver.get( url );

			sleep();

			long last = height( driver ), recent;

			for ( int i = 0; i < 20; i++ ) {
				script( driver, "window.scrollTo(0, document.body.scrollHeight);" );

				sleep();

				if ( ( recent = height( driver ) ) == last ) {
					break;
				}

				last = recent;
			}

			data().put( id(), list( driver, "div.songs-list > div.song" ).stream().map( e -> {
				List<WebElement> song = list( e, "div.song-name-wrapper > div" );

				return Arrays.asList( song.get( 1 ).getText(), find( e, "div.col-album > a" ).getAttribute( "href" ), song.get( 0 ).getText() );
			} ).collect( Collectors.toList() ) );
		}

		private long height( WebDriver driver ) {
			return ( long ) ( ( JavascriptExecutor ) driver ).executeScript( "return document.body.scrollHeight" );
		}
	}
}