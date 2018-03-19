package io.thekraken.grok.api;

import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
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
    INT(Integer::valueOf, "integer"),
    LONG(Long::valueOf),
    FLOAT(Float::valueOf),
    DOUBLE(Double::valueOf),
    DATETIME(new DateConverter(), "date"),
    STRING(v -> v, "text"),

    // Dashbase specific type
    META(v -> v, "sorted"),

    // Dashbase specific type
    ID(v -> v, "key");

    public final IConverter<?> converter;
    public final List<String> aliases;

    Type(IConverter<?> converter, String... aliases) {
      this.converter = converter;
      this.aliases = Arrays.asList(aliases);
    }
  }

  private static final CharMatcher DELIMITER = CharMatcher.anyOf(";:");

  private static final Splitter SPLITTER = Splitter.on(DELIMITER).limit(3);

  private static final Map<String, Type> TYPES =
      Arrays.asList(Type.values()).stream()
          .collect(Collectors.toMap(t -> t.name().toLowerCase(), t -> t));

  private static final Map<String, Type> TYPE_ALIASES =
      Arrays.asList(Type.values()).stream()
        .flatMap(type -> type.aliases.stream().map(alias -> new AbstractMap.SimpleEntry<>(alias, type)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  private static Type getType(String key) {
    key = key.toLowerCase();
    Type type = TYPES.getOrDefault(key, TYPE_ALIASES.get(key));
    if (type == null) {
      throw new IllegalArgumentException("Invalid data type :" + key);
    }
    return type;
  }

  public static Map<String, IConverter> getConverters(Collection<String> groupNames, Object... params) {
    return groupNames.stream()
        .filter(group -> Converter.DELIMITER.matchesAnyOf(group))
        .collect(Collectors.toMap(Function.identity(), key -> {
          List<String> list = SPLITTER.splitToList(key);
          IConverter converter = getType(list.get(1)).converter;
          if (list.size() == 3) {
            converter = converter.newConverter(list.get(2), params);
          }
          return converter;
        }));
  }

  public static Map<String, Type> getGroupTypes(Collection<String> groupNames) {
    return groupNames.stream()
        .filter(group -> Converter.DELIMITER.matchesAnyOf(group))
        .map(group -> SPLITTER.splitToList(group))
        .collect(Collectors.toMap(
            l -> l.get(0),
            l -> getType(l.get(1))
        ));
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

  default IConverter<T> newConverter(String param, Object... params) {
    return this;
  }
}


class DateConverter implements IConverter<Instant> {
  private final DateTimeFormatter formatter;
  private final ZoneId timeZone;

  public DateConverter() {
    this.formatter = DateTimeFormatter.ISO_DATE_TIME;
    this.timeZone = ZoneOffset.UTC;
  }

  private DateConverter(DateTimeFormatter formatter, ZoneId timeZone) {
    this.formatter = formatter;
    this.timeZone = timeZone;
  }

  @Override
  public Instant convert(String value) {
    TemporalAccessor dt = formatter.parseBest(value.trim(), ZonedDateTime::from, LocalDateTime::from);
    if (dt instanceof ZonedDateTime) {
      return ((ZonedDateTime)dt).toInstant();
    } else {
      return ((LocalDateTime) dt).atZone(timeZone).toInstant();
    }
  }

  @Override
  public DateConverter newConverter(String param, Object... params) {
    Preconditions.checkArgument(params.length == 1 && params[0] instanceof ZoneId);
    return new DateConverter(DateTimeFormatter.ofPattern(param), (ZoneId) params[0]);
  }
}


