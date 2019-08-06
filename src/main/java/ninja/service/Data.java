package ninja.service;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Data<T> {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	private final Map<String, T> data = new LinkedHashMap<>();

	public Map<String, T> data() {
		if ( data.isEmpty() ) {
			log.error( "資料初始化: {}", getClass() );

			init();
		}

		return data;
	}

	abstract void init( Map<String, T> data );

	@PostConstruct
	private void init() {
		init( data );
	};
}