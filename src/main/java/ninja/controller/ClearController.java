package ninja.controller;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ninja.service.Data;
import ninja.util.Check;

@RestController
public class ClearController extends DialogController {
	@Autowired
	private List<Data<?>> datas; // 用ApplicationContext用Bean Name會比較合邏輯就是

	@Override
	protected Object[] args() {
		return ArrayUtils.toArray( options( list( datas.stream().map( ClassUtils::getSimpleName ) ) ) );
	}

	@GetMapping( "/clear" )
	@Async
	public void clear( @RequestParam String text, @RequestParam( RESPONSE_URL ) String url ) {
		Data<?> data = Check.first( datas.stream().filter( i -> ClassUtils.getSimpleName( i ).equals( text ) ), "查無此類: " + text );

		data.data().clear();
		data.data();

		message( "*OK*", url );
	}
}