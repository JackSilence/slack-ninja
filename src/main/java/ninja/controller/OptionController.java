package ninja.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import ninja.consts.Dialog;
import ninja.consts.Filter;
import ninja.service.Bus;
import ninja.slack.Payload;
import ninja.util.Gson;

@RestController
public class OptionController extends BaseController {
	@Autowired
	private Bus bus;

	@PostMapping( "/options" )
	public Map<String, List<?>> options( String payload ) {
		Payload message = Gson.from( payload, Payload.class );

		String id = message.getId(), value = message.getValue();

		check( "dialog_suggestion", message.getType(), payload );

		if ( Dialog.BUS.name().equals( id ) ) {
			if ( !bus.check( value ) ) {
				return options( Stream.empty() );
			}

			List<Map<String, ?>> info = bus.call( "DisplayStopOfRoute", Filter.and( Filter.ROUTE.eq( value ), Filter.DIRECTION.eq( "0" ) ) );

			return options( info.isEmpty() ? Stream.empty() : bus.stops( info.get( 0 ), bus::stop ).map( i -> option( i, text( value, i ) ) ) );

		} else if ( Dialog.STATION.name().equals( id ) ) {
			return options( bus.call( "Station", Filter.STATION.contains( value ), "$select=StationName" ).stream().map( bus::station ).distinct().map( super::option ) );

		} else {
			throw new IllegalArgumentException( payload );
		}
	}

	private Map<String, List<?>> options( Stream<?> options ) {
		return ImmutableMap.of( "options", options.collect( Collectors.toList() ) );
	}
}