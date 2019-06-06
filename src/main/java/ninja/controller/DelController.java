package ninja.controller;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import magic.util.Utils;

@RestController
public class DelController {
	private static final String TEMPLATE = "/template/datepicker.json";

	@PostMapping( "/delete" )
	public String delete() {
		return String.format( Utils.getResourceAsString( TEMPLATE ), LocalDate.now() );
	}
}