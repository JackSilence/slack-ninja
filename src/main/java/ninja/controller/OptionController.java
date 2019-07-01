package ninja.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import ninja.consts.Dialog;
import ninja.service.Bus;
import ninja.slack.Payload;
import ninja.util.Cast;
import ninja.util.Gson;

@RestController
public class OptionController extends BaseController {
	private static final String QUERY = " and Direction eq Service.Enumeration.Version2.Bus.BusDirectionEnum'去程'";

	@Autowired
	private Bus bus;

	@PostMapping( "/option" )
	public Map<String, List<?>> option( String payload ) {
		Payload message = Gson.from( payload, Payload.class );

		check( "dialog_suggestion", message.getType(), payload );

		check( Dialog.BUS.name(), message.getId(), payload );

		check( "stop", message.getName(), payload );

		String route = message.getValue();

		if ( !bus.check( route ) ) {
			return options( Collections.EMPTY_LIST );
		}

		log.info( "No filter, size: {}", bus.call( "DisplayStopOfRoute", route ).size() );
		log.info( "With filter, size: {}", bus.call( "DisplayStopOfRoute", route, QUERY ).size() );

		Map<String, ?> info = bus.call( "DisplayStopOfRoute", route, QUERY ).stream().findFirst().orElseGet( () -> null );

		return options( info == null ? Collections.EMPTY_LIST : Cast.list( info, "Stops" ).stream().map( Cast::map ).map( bus::stop ).map( i -> {
			return option( i, route + "%20" + i );

		} ).collect( Collectors.toList() ) );
	}

	private Map<String, List<?>> options( List<?> options ) {
		return ImmutableMap.of( "options", options );
	}
}