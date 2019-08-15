package ninja.ex;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

@Component
public class ErrorAttrs extends DefaultErrorAttributes {
	@Override
	public Map<String, Object> getErrorAttributes( RequestAttributes requestAttributes, boolean includeStackTrace ) {
		Map<String, Object> attrs = super.getErrorAttributes( requestAttributes, includeStackTrace );

		attrs.put( "exception", StringUtils.EMPTY );
		attrs.put( "message", StringUtils.EMPTY );

		return attrs;
	}
}