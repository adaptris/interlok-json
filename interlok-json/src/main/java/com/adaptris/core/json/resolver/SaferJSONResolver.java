package com.adaptris.core.json.resolver;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.json.JacksonJsonDeserializer;
import com.adaptris.core.json.JsonDeserializer;
import com.adaptris.interlok.resolver.FileResolver;
import com.adaptris.interlok.resolver.ResolverImp;
import com.adaptris.interlok.resolver.UnresolvableException;
import com.adaptris.interlok.types.InterlokMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolver implementation that resolves and escapes JSON content.
 * <p>
 * This resolver resolves values based on the following:
 * %resolveJson{...}, and will place the result in a a JSON node with
 * the correct escaping, particularly of quotation marks.
 * </p>
 */
public class SaferJSONResolver extends ResolverImp
{
	private static final Logger log = LoggerFactory.getLogger(FileResolver.class);

	private static final String RESOLVE_REGEXP = "^.*%resolveJson\\{(.+)\\}.*$";
	private final transient Pattern resolverPattern;

	@Getter
	@Setter
	@AdvancedConfig(rare = true)
	private JsonDeserializer<JsonNode> jsonDeserializer;

	public SaferJSONResolver()
	{
		resolverPattern = Pattern.compile(RESOLVE_REGEXP, Pattern.DOTALL);
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String resolve(String lookupValue)
	{
		throw new UnresolvableException("Safer XML resolver requires a target message!");
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public String resolve(String lookupValue, InterlokMessage target)
	{
		if (target == null)
		{
			throw new UnresolvableException("Target message cannot be null!");
		}
		if (lookupValue == null)
		{
			lookupValue = target.getContent();
		}
		String result = lookupValue;
		log.trace("Resolving {} from JSON", lookupValue);
		try
		{
			JsonNode json = jsonDeserializer().deserialize(lookupValue);
			checkJsonObject(target, json);
			result = json.toString();
		}
		catch (Exception e)
		{
			log.error("Could not parse JSON!", e);
			throw new UnresolvableException(e);
		}
		return result;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public boolean canHandle(String value)
	{
		return resolverPattern.matcher(value).matches();
	}

	private JsonDeserializer<JsonNode> jsonDeserializer()
	{
		return ObjectUtils.defaultIfNull(jsonDeserializer, new JacksonJsonDeserializer());
	}

	/*
	 * It's done this way because trying to use a regex like
	 * %resolveJson{...} within a JSON document leads to madness: do
	 * you try to match the fewest characters before '}' (in which case
	 * you cannot support embedded resolvers such as
	 * %resolveJson{%message{...}} or do you go greedy and then start
	 * matching any '}' that's actually part of the document? Madness I
	 * tell you.
	 */
	private void checkJsonObject(InterlokMessage target, JsonNode node)
	{
		log.debug("Node {}", node.asText());
		Iterator<String> iter = node.fieldNames();
		List<String> remove = new ArrayList<>();
		while (iter.hasNext())
		{
			String key = iter.next();
			JsonNode item = node.get(key);

			String k2 = replaceMatch(target, key);
			if (!k2.equals(key))
			{
				remove.add(key);
				key = k2;
			}
			if (item.isObject())
			{
				checkJsonObject(target, item);
			}
			else
			{
				String result = item.textValue();
				if (result != null)
				{
					result = replaceMatch(target, result);
					((ObjectNode)node).set(key, new TextNode(result));
				}
			}
		}
		for (String r : remove)
		{
			((ObjectNode)node).remove(r);
		}
		return;
	}

	private String replaceMatch(InterlokMessage target, String search)
	{
		String result = search;
		Matcher matcher = resolverPattern.matcher(result);
		while (matcher.matches())
		{
			String replace = matcher.group(1);
			String value = target.resolve(replace);
			log.trace("Found value {} within target message", value);
			String toReplace = "%resolveJson{" + replace + "}";
			result = result.replace(toReplace, value);
			matcher = resolverPattern.matcher(result);
		}
		return target.resolve(result);
	}
}
