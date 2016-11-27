package de.nosebrain.mserver2podcast.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author dzo
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "de.nosebrain.mserver2podcast" })
@org.springframework.context.annotation.PropertySource(value = { "classpath:mserver2podcast-service.properties", "file:${catalina.home}/conf/mserver2podcast-service/mserver2podcast-service.properties" }, ignoreResourceNotFound = true)
public class MServerPodcastServiceConfig extends WebMvcConfigurerAdapter {

	// noop
}
