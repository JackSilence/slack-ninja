package mrt.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import magic.service.Slack;

@RestController
public class QueryController {
	private final Logger log = LoggerFactory.getLogger( this.getClass() );

	@Autowired
	private Slack slack;

	@PostMapping( "/query" )
	public Map<String, String> execute( @RequestParam String text ) {
		log.info( "Text: {}", text );

		try {
			String[] message = StringUtils.split( text );

			Assert.isTrue( message.length == 2, "起訖站皆須輸入" );

			return slack.text( "成功" );

		} catch ( RuntimeException e ) {
			log.error( "", e );

			return slack.text( e.getMessage() );

		}
	}
}