package ninja.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import ninja.consts.Dialog;
import ninja.consts.Filter;
import ninja.service.Bus;
import ninja.service.Metro;
import ninja.slack.Payload;
import ninja.util.AQI;
import ninja.util.Cast;
import ninja.util.Gson;

@RestController
public class OptionController extends BaseController {
	@Autowired
	private Bus bus;

	@Autowired
	private Metro metro;

	@PostMapping( "/options" )
	public Map<String, List<?>> options( String payload ) {
		Payload message = Gson.from( payload, Payload.class );

		String id = message.getId(), value = message.getValue();

		check( "dialog_suggestion", message.getType(), payload );

		if ( equals( Dialog.BUS, id ) ) {
			if ( !bus.check( value ) ) {
				return options( Stream.empty() );
			}

			List<Map<String, ?>> info = bus.call( "DisplayStopOfRoute", Filter.and( Filter.ROUTE.eq( value ), Filter.DIRECTION.eq( "0" ) ) );

			return options( info.isEmpty() ? Stream.empty() : bus.stops( info.get( 0 ), bus::stop ).map( i -> option( i, text( value, i ) ) ) );

		} else if ( equals( Dialog.STATION, id ) ) {
			return options( bus.call( "Station", Filter.STATION.contains( value ), "$select=StationName" ).stream().map( bus::station ).distinct().map( super::option ) );

		} else if ( equals( Dialog.AQI, id ) ) {
			Filter county = Filter.COUNTY, site = Filter.SITE_NAME;

			return options( AQI.call( county.eq( value ) ).stream().map( i -> {
				return option( String.join( StringUtils.SPACE, Cast.string( i, county.toString() ), Cast.string( i, site.toString() ) ) );
			} ) );

		} else if ( equals( Dialog.MRT, id ) ) {
			return options( metro.find( value ).map( super::option ) );

		} else {
			throw new IllegalArgumentException( payload );
		}
	}

	private boolean equals( Dialog dialog, String id ) {
		return dialog.name().equals( id );
	}

	private Map<String, List<?>> options( Stream<?> options ) {
		return ImmutableMap.of( "options", options.collect( Collectors.toList() ) );
	}
}