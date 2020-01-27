package com.adaptris.core.resolver;

import com.adaptris.interlok.resolver.ResolverImp;
import com.adaptris.interlok.resolver.UnresolvableException;

import com.adaptris.interlok.types.InterlokMessage;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FromPayloadUsingJSONPath extends ResolverImp
{
  private static final String RESOLVE_PAYLOAD_REGEXP = "^.*%payload\\{jsonpath:([\\w!\\$\"#&%'\\*\\+,\\-\\.:=\\(\\)\\[\\]\\/@\\| ]+)\\}.*$";
  private static final transient Pattern PAYLOAD_RESOLVER = Pattern.compile(RESOLVE_PAYLOAD_REGEXP, Pattern.DOTALL);

  @Override
  public String resolve(String value)
  {
    throw new UnresolvableException();
  }

  /**
   * Attempt to resolve a value externally.
   *
   * @param value The string to resolve.
   * @param target The document to search.
   *
   * @return The resolved value.
   */
  @Override
  public String resolve(String value, InterlokMessage target)
  {
    DocumentContext json;
    try (InputStream inputStream = target.getInputStream())
    {
      json = JsonPath.parse(inputStream);
    }
    catch (IOException e)
    {
      log.error("Could not parse JSON document", e);
      throw new UnresolvableException(e);
    }
    Matcher m = PAYLOAD_RESOLVER.matcher(value);
    while (m.matches())
    {
      String path = m.group(1);
      String replaceWith = extract(path, json);
      log.trace("JSONPath {} found {}", path, replaceWith);
      value = value.replace("%payload{jsonpath:" + path + "}", replaceWith);
      m = PAYLOAD_RESOLVER.matcher(value);
    }
    return value;
  }

  private String extract(String path, DocumentContext json)
  {
    try
    {
      Object o = json.read(path);
      if (o instanceof String)
      {
        return (String)o;
      }
    }
    catch (Exception e)
    {
      log.error("Could not use JSONPath {} to extract data from message payload", path, e);
      throw new UnresolvableException("Could not use JSONPath {} to extract data from message payload");
    }
    log.error("JSONPath payload resolver can only resolve text values nodes at this time");
    throw new UnresolvableException("JSONPath payload resolver can only resolve text values nodes at this time");
  }

  /**
   * Can this resolver handle this type of value.
   *
   * @param value the value e.g. {@code %payload{xpath:…}}
   *
   * @return True if the value will provide matches, false otherwise.
   */
  @Override
  public boolean canHandle(String value)
  {
    return PAYLOAD_RESOLVER.matcher(value).matches();
  }
}
