package de.nosebrain.mserver2podcast.server.controller.util.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class LocalTimeConverter implements Converter<String, LocalTime> {

  @Override
  public LocalTime convert(final String source) {
    if (source==null) {
      return null;
    }
    return LocalTime.parse(source);
  }
}