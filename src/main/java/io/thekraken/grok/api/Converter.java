package io.thekraken.grok.api;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
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
    STRING(v -> v, "text");

    public final IConverter<?> converter;
    public final List<String> aliases;

    Type(IConverter<?> converter, String... aliases) {
      this.converter = converter;
      this.aliases = Arrays.asList(aliases);
    }
  }

  private static final Pattern SPLITTER = Pattern.compile("[:;]");

  private static final Map<String, Type> TYPES =
      Arrays.stream(Type.values())
          .collect(Collectors.toMap(t -> t.name().toLowerCase(), t -> t));

  private static final Map<String, Type> TYPE_ALIASES =
      Arrays.stream(Type.values())
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
        .filter(Converter::containsDelimiter)
        .collect(Collectors.toMap(Function.identity(), key -> {
          String[] list = splitGrokPattern(key);
          IConverter converter = getType(list[1]).converter;
          if (list.length == 3) {
            converter = converter.newConverter(list[2], params);
          }
          return converter;
        }));
  }

  public static Map<String, Type> getGroupTypes(Collection<String> groupNames) {
    return groupNames.stream()
        .filter(Converter::containsDelimiter)
        .map(Converter::splitGrokPattern)
        .collect(Collectors.toMap(
            l -> l[0],
            l -> getType(l[1])
        ));
  }

  public static String extractKey(String key) {
    return splitGrokPattern(key)[0];
  }

  private static boolean containsDelimiter(String s) {
    return s.indexOf(':') >= 0 || s.indexOf(';') >= 0;
  }

  private static String[] splitGrokPattern(String s) {
    return SPLITTER.split(s, 3);
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
    TemporalAccessor dt = formatter.parseBest(value.trim(), ZonedDateTime::from, LocalDateTime::from, OffsetDateTime::from, Instant::from, LocalDate::from);
    if (dt instanceof ZonedDateTime) {
      return ((ZonedDateTime)dt).toInstant();
    } else if (dt instanceof LocalDateTime) {
      return ((LocalDateTime) dt).atZone(timeZone).toInstant();
    } else if (dt instanceof OffsetDateTime) {
      return ((OffsetDateTime) dt).atZoneSameInstant(timeZone).toInstant();
    } else if (dt instanceof Instant) {
      return ((Instant) dt);
    } else if (dt instanceof LocalDate) {
      return ((LocalDate) dt).atStartOfDay(timeZone).toInstant();
    } else {
      return null;
    }
  }

  @Override
  public DateConverter newConverter(String param, Object... params) {
    if (!(params.length == 1 && params[0] instanceof ZoneId)) {
      throw new IllegalArgumentException("Invalid parameters");
    }
    return new DateConverter(DateTimeFormatter.ofPattern(param), (ZoneId) params[0]);
  }
}


