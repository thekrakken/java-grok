package io.thekraken.grok.api;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Convert String argument to the right type.
 *
 * @author anthonyc
 *
 */
public class Converter {

  public static Locale locale = Locale.ENGLISH;

  public static final CharMatcher DELIMITER = CharMatcher.anyOf(";:");

  private static final Splitter SPLITTER = Splitter.on(DELIMITER).limit(3);

  private static Map<String, IConverter<?>> CONVERTERS = ImmutableMap.<String, IConverter<?>>builder()
      .put("byte", new ByteConverter())
      .put("boolean", new BooleanConverter())
      .put("short", new ShortConverter())
      .put("int", new IntegerConverter())
      .put("long", new LongConverter())
      .put("float", new FloatConverter())
      .put("double", new DoubleConverter())
      .put("date", new DateConverter())
      .put("datetime", new DateConverter())
      .put("string", new StringConverter())
      .build();

  private static IConverter getConverter(String key) throws Exception {
    IConverter converter = CONVERTERS.get(key);
    if (converter == null) {
      throw new Exception("Invalid data type :" + key);
    }
    return converter;
  }

  public static KeyValue convert(String key, String value) {
    List<String> spec = SPLITTER.splitToList(key);
    try {
      switch (spec.size()) {
        case 1:
          return new KeyValue(spec.get(0), value);
        case 2:
          return new KeyValue(spec.get(0), getConverter(spec.get(1)).convert(value));
        case 3:
          return new KeyValue(spec.get(0), getConverter(spec.get(1)).convert(value, spec.get(2)));
        default:
          return new KeyValue(spec.get(0), value, "Unsupported spec :" + key);
      }
    } catch (Exception e) {
      return new KeyValue(spec.get(0), value, e.toString());
    }
  }
}


//
// KeyValue
//

class KeyValue {

  private final String key;
  private final Object value;
  private final String grokFailure;

  public KeyValue(String key, Object value) {
    this.key = key;
    this.value = value;
    grokFailure = null;
  }

  public KeyValue(String key, Object value, String grokFailure) {
    this.key = key;
    this.value = value;
    this.grokFailure = grokFailure;
  }

  public boolean hasGrokFailure() {
    return grokFailure != null;
  }

  public String getGrokFailure() {
    return this.grokFailure;
  }

  public String getKey() {
    return key;
  }

  public Object getValue() {
    return value;
  }
}


//
// Converters
//
abstract class IConverter<T> {

  public T convert(String value, String informat) throws Exception {
    return null;
  }

  public abstract T convert(String value) throws Exception;
}


class ByteConverter extends IConverter<Byte> {
  @Override
  public Byte convert(String value) throws Exception {
    return Byte.parseByte(value);
  }
}


class BooleanConverter extends IConverter<Boolean> {
  @Override
  public Boolean convert(String value) throws Exception {
    return Boolean.parseBoolean(value);
  }
}


class ShortConverter extends IConverter<Short> {
  @Override
  public Short convert(String value) throws Exception {
    return Short.parseShort(value);
  }
}


class IntegerConverter extends IConverter<Integer> {
  @Override
  public Integer convert(String value) throws Exception {
    return Integer.parseInt(value);
  }
}


class LongConverter extends IConverter<Long> {
  @Override
  public Long convert(String value) throws Exception {
    return Long.parseLong(value);
  }
}


class FloatConverter extends IConverter<Float> {
  @Override
  public Float convert(String value) throws Exception {
    return Float.parseFloat(value);
  }
}


class DoubleConverter extends IConverter<Double> {
  @Override
  public Double convert(String value) throws Exception {
    return Double.parseDouble(value);
  }
}


class StringConverter extends IConverter<String> {
  @Override
  public String convert(String value) throws Exception {
    return value;
  }
}


class DateConverter extends IConverter<Date> {
  @Override
  public Date convert(String value) throws Exception {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT,
                                          DateFormat.SHORT,
                                          Converter.locale).parse(value);
  }

  @Override
  public Date convert(String value, String informat) throws Exception {
    SimpleDateFormat formatter = new SimpleDateFormat(informat, Converter.locale);
    return formatter.parse(value);
  }

}


