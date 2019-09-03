package ninja.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.service.Data;

@RestController
public class ClearController extends BaseController {
	@Autowired
	private ApplicationContext context;

	@PostMapping( "/clear" )
	public String refresh( @RequestParam String text ) {
		( ( Data<?> ) context.getBean( text ) ).data().clear();

		return "*OK*";
	}
}