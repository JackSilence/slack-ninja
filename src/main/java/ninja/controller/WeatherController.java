package ninja.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import magic.util.Utils;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;
import ninja.util.Gson;

@RestController
public class WeatherController extends BaseController {
	private static final String URL = "https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-D0047-061";

	private static final String QUERY = "?Authorization=%s&locationName=%s", DELIMITER = "。";

	@Value( "${cwb.api.key:}" )
	private String key;

	@PostMapping( "/weather" )
	public String weather( @RequestParam String command, @RequestParam String text ) {
		String district = StringUtils.appendIfMissing( StringUtils.defaultIfEmpty( text, "內湖區" ), "區" );

		try {
			Map<?, ?> result = Gson.from( Utils.getEntityAsString( Request.Get( URL + String.format( QUERY, key, district ) ) ), Map.class );

			Assert.notEmpty( result, "查無此區域: " + text );

			SlackMessage message = new SlackMessage( "天氣預報綜合描述" );

			List<?> elements = list( first( first( map( result, "records" ), "locations" ), "location" ), "weatherElement" );

			elements.stream().map( this::map ).filter( i -> "WeatherDescription".equals( i.get( "elementName" ) ) ).forEach( i -> {
				list( i, "time" ).stream().map( this::map ).forEach( j -> {
					SlackAttachment attach = new SlackAttachment( StringUtils.EMPTY ).setTitle( string( j, "startTime" ) );

					String[] data = string( first( j, "elementValue" ), "value" ).split( DELIMITER );

					attach.setText( data[ 0 ] + DELIMITER + data[ 4 ].replace( StringUtils.SPACE, "，" ) + DELIMITER );

					attach.addFields( field( data[ 2 ], 2 ) ).addFields( super.field( "舒適度", data[ 3 ] ) );

					attach.addFields( field( data[ 1 ], 4 ) ).addFields( field( data[ 5 ], 4 ) );

					message.addAttachments( attach );
				} );
			} );

			return message.prepare().toString();

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return e.getMessage();

		}
	}

	private Map<?, ?> map( Map<?, ?> map, String key ) {
		return map( map.get( key ) );
	}

	private Map<?, ?> map( Object object ) {
		return ( Map<?, ?> ) object;
	}

	private Map<?, ?> first( Map<?, ?> map, String key ) {
		return map( list( map, key ).get( 0 ) );
	}

	private List<?> list( Map<?, ?> map, String key ) {
		return ( List<?> ) map.get( key );
	}

	private String string( Map<?, ?> map, String key ) {
		return ( String ) map.get( key );
	}

	private SlackField field( String data, int index ) {
		return super.field( data.substring( 0, index ), data.substring( index ).trim() );
	}
}