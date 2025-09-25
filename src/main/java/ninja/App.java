package ninja;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import magic.controller.ExecuteController;
import magic.service.AsyncExecutor;
import magic.service.Slack;
import ninja.ex.EXHandler;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@Import( { ExecuteController.class, AsyncExecutor.class, Slack.class } )
public class App implements AsyncConfigurer {
	public static void main( String[] args ) {
		SpringApplication.run( App.class, args );
	}

	@Bean
	@Override
	public Executor getAsyncExecutor() {
		var executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize( 10 );

		return executor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new EXHandler();
	}
}