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
import ninja.service.Transport;
import ninja.slack.Payload;
import ninja.util.Cast;
import ninja.util.Gson;

@RestController
public class OptionController extends BaseController {
	@Autowired
	private Transport transport;

	@PostMapping( "/option" )
	public Map<String, List<?>> option( String payload ) {
		Payload message = Gson.from( payload, Payload.class );

		check( "dialog_suggestion", message.getType(), payload );

		check( Dialog.BUS.name(), message.getId(), payload );

		check( "stop", message.getName(), payload );

		String route = message.getValue();

		Map<String, ?> bus = transport.call( "DisplayStopOfRoute", route, "$filter=Direction%20eq%20%270%27" ).stream().findFirst().orElseGet( () -> null );

		return options( bus == null ? Collections.EMPTY_LIST : Cast.list( bus, "Stops" ).stream().map( Cast::map ).map( transport::stop ).map( i -> {
			return option( i, route + "%20" + i );

		} ).collect( Collectors.toList() ) );
	}

	private Map<String, List<?>> options( List<?> options ) {
		return ImmutableMap.of( "options", options );
	}
}