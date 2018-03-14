package io.thekraken.grok.api;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Convert String argument to the right type.
 *
 * @author anthonyc
 *
 */
public class Converter {

  public static final CharMatcher DELIMITER = CharMatcher.anyOf(";:");

  private static final Splitter SPLITTER = Splitter.on(DELIMITER).limit(3);

  private static Map<String, IConverter<?>> CONVERTERS = ImmutableMap.<String, IConverter<?>>builder()
      .put("byte", Byte::valueOf)
      .put("boolean", Boolean::valueOf)
      .put("short", Short::valueOf)
      .put("int", Integer::valueOf)
      .put("long", Long::valueOf)
      .put("float", Float::valueOf)
      .put("double", Double::valueOf)
      .put("date", new DateConverter())
      .put("datetime", new DateConverter())
      .put("string", v -> v)
      .build();

  private static IConverter getConverter(String key) {
    IConverter converter = CONVERTERS.get(key);
    if (converter == null) {
      throw new IllegalArgumentException("Invalid data type :" + key);
    }
    return converter;
  }

  public static Map<String, IConverter> getConverters(Collection<String> groupNames) {
    return groupNames.stream()
        .filter(group -> Converter.DELIMITER.matchesAnyOf(group))
        .collect(Collectors.toMap(Function.identity(), key -> {
          List<String> list = SPLITTER.splitToList(key);
          IConverter converter = getConverter(list.get(1));
          if (list.size() == 3) {
            converter = converter.newConverter(list.get(2));
          }
          return converter;
        }));
  }

  public static String extractKey(String key) {
    return SPLITTER.split(key).iterator().next();
  }
}

//
// Converters
//
interface IConverter<T> {
  T convert(String value);

  default IConverter<T> newConverter(String param) {
    return this;
  }
}


class DateConverter implements IConverter<Instant> {
  private final DateTimeFormatter formatter;

  public DateConverter() {
    this.formatter = DateTimeFormatter.ISO_DATE_TIME;
  }

  private DateConverter(DateTimeFormatter formatter) {
    this.formatter = formatter;
  }

  @Override
  public Instant convert(String value) {
    TemporalAccessor dt = formatter.parseBest(value.trim(), ZonedDateTime::from, LocalDateTime::from);
    if (dt instanceof ZonedDateTime) {
      return ((ZonedDateTime)dt).toInstant();
    } else {
      return ((LocalDateTime) dt).atZone(ZoneOffset.UTC).toInstant();
    }
  }

  @Override
  public DateConverter newConverter(String param) {
    return new DateConverter(DateTimeFormatter.ofPattern(param));
  }
}


