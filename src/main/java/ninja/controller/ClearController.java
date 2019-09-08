package ninja.controller;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.service.Data;

@RestController
public class ClearController extends DialogController {
	@Autowired
	private ApplicationContext context;

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( options( context.getBeansOfType( Data.class ).keySet() ) );
	}

	@PostMapping( "/clear" )
	public String clear( @RequestParam String text ) {
		( ( Data<?> ) context.getBean( text ) ).data().clear();

		return "*OK*";
	}
}