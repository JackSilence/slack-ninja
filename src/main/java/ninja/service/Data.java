package ninja.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

public abstract class Data<T> {
	private final Map<String, T> data = new LinkedHashMap<>();

	public Map<String, T> data() {
		init();

		return data;
	}

	abstract void init( Map<String, T> data );

	@PostConstruct
	private void init() {
		if ( data.isEmpty() ) {
			init( data );
		}
	};
}