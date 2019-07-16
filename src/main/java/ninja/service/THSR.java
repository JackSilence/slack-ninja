package ninja.service;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;

@Service
public class THSR extends PTX {
	private static final String PATH = "Rail/THSR/";

	public List<Map<String, ?>> call( String path ) {
		return call( path, BooleanUtils.toStringTrueFalse( true ) );
	}

	@Override
	public List<Map<String, ?>> call( String path, String filter, String... query ) {
		return super.call( PATH + path, filter, query );
	}
}