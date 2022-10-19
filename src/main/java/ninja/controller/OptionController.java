package ninja.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import ninja.consts.Dialog;
import ninja.consts.Filter;
import ninja.service.Bus;
import ninja.service.Metro;
import ninja.slack.Payload;
import ninja.util.Check;
import ninja.util.Gson;

@RestController
public class OptionController extends BaseController {
	@Autowired
	private Bus bus;

	@Autowired
	private Metro metro;

	@PostMapping( "/options" )
	public Map<String, List<?>> options( String payload ) {
		var message = Gson.from( payload, Payload.class );

		String id = message.getId(), value = message.getValue();

		Check.equals( "dialog_suggestion", message.getType(), payload );

		if ( StringUtils.isBlank( value ) ) {
			return options( Stream.empty() );
		}

		switch ( Dialog.valueOf( id ) ) {
			case BUS:
				if ( !bus.check( value ) ) {
					return options( Stream.empty() );
				}

				var info = bus.call( "DisplayStopOfRoute", Filter.and( Filter.ROUTE.eq( value ), Filter.DIRECTION.eq( "0" ) ) );

				return options( info.isEmpty() ? Stream.empty() : bus.stops( info.get( 0 ), bus::stop ).map( i -> option( i, bus.text( value, i ) ) ) );

			case STATION:
				return options( bus.call( "Station", Filter.STATION.contains( value ), "$select=StationName" ).stream().map( bus::station ).distinct().limit( 100 ).map( super::option ) );

			case MRT:
				return options( metro.data().keySet().stream().filter( i -> i.contains( value ) ).map( super::option ) );

			default:
				throw new IllegalArgumentException( payload );
		}
	}

	private Map<String, List<?>> options( Stream<?> options ) {
		return Map.of( OPTIONS, list( options ) );
	}
}