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
import java.util.Arrays;
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
  public enum Type {
    BYTE(Byte::valueOf),
    BOOLEAN(Boolean::valueOf),
    SHORT(Short::valueOf),
    INT(Integer::valueOf),
    LONG(Long::valueOf),
    FLOAT(Float::valueOf),
    DOUBLE(Double::valueOf),
    DATE(new DateConverter()),
    DATETIME(new DateConverter()),
    STRING(v -> v),

    // Dashbase specific type
    META(v -> v),

    // Dashbase specific type
    ID(v -> v);

    public final IConverter<?> converter;

    Type(IConverter<?> converter) {
      this.converter = converter;
    }
  }

  private static final CharMatcher DELIMITER = CharMatcher.anyOf(";:");

  private static final Splitter SPLITTER = Splitter.on(DELIMITER).limit(3);

  private static final Map<String, Type> TYPES =
      Arrays.asList(Type.values()).stream()
          .collect(Collectors.toMap(t -> t.name().toLowerCase(), t -> t));

  private static Type getType(String key) {
    Type type = TYPES.get(key.toLowerCase());
    if (type == null) {
      throw new IllegalArgumentException("Invalid data type :" + key);
    }
    return type;
  }

  public static Map<String, IConverter> getConverters(Collection<String> groupNames) {
    return groupNames.stream()
        .filter(group -> Converter.DELIMITER.matchesAnyOf(group))
        .collect(Collectors.toMap(Function.identity(), key -> {
          List<String> list = SPLITTER.splitToList(key);
          IConverter converter = getType(list.get(1)).converter;
          if (list.size() == 3) {
            converter = converter.newConverter(list.get(2));
          }
          return converter;
        }));
  }

  public static Map<String, Type> getGroupTypes(Collection<String> groupNames) {
    return groupNames.stream()
        .filter(group -> Converter.DELIMITER.matchesAnyOf(group))
        .collect(Collectors.toMap(Function.identity(), key -> {
          List<String> list = SPLITTER.splitToList(key);
          return getType(list.get(1));
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


