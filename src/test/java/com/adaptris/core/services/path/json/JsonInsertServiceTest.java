package com.adaptris.core.services.path.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.common.ConstantDataInputParameter;
import com.adaptris.core.common.MetadataDataInputParameter;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.json.JsonInsertExecution;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JsonInsertServiceTest extends ServiceCase
{
	private static final String PATHS[] = { "$.store.book[0].title", "$.store.book[1].title" };
	private static final String TITLES[] = { "Sayings of the Century", "Sword of Honour"};

	public JsonInsertServiceTest(String name)
	{
		super(name);
	}

	@Test
	public void testMetadataToPayload() throws Exception
	{
		AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage(content());
		message.addMetadata("title-1", TITLES[0]);
		message.addMetadata("title-2", TITLES[1]);

		List<JsonInsertExecution> executions = new ArrayList<>();
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter(PATHS[0]),
				new MetadataDataInputParameter("title-1")));
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter(PATHS[1]),
				new MetadataDataInputParameter("title-2")));

		JsonInsertService service = new JsonInsertService(new StringPayloadDataInputParameter(), executions);

		execute(service, message);

		assertEquals(expected(), message.getContent());
	}

	@Test
	public void testException() throws Exception
	{
		try
		{
			AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage();
			message.addMetadata("title-1", TITLES[0]);
			message.addMetadata("title-2", TITLES[1]);

			List<JsonInsertExecution> executions = new ArrayList<>();
			executions.add(new JsonInsertExecution(new ConstantDataInputParameter(PATHS[0]),
					new MetadataDataInputParameter("title-1")));
			executions.add(new JsonInsertExecution(new ConstantDataInputParameter(PATHS[1]),
					new MetadataDataInputParameter("title-2")));

			JsonInsertService service = new JsonInsertService(new StringPayloadDataInputParameter(), executions);

			execute(service, message);

			fail();
		}
		catch (Exception e)
		{
			/* expected */
		}
	}

	public static String content()
	{
		return "{"
				+ "\"store\": {"
					+ "\"book\": ["
						+ "{"
							+ "\"category\": \"reference\","
							+ "\"author\": \"Nigel Rees\","
							+ "\"price\": 8.95"
						+ "},"
						+ "{"
							+ "\"category\": \"fiction\","
							+ "\"author\": \"Evelyn Waugh\","
							+ "\"price\": 12.99"
						+ "},"
						+ "{"
							+ "\"category\": \"fiction\","
							+ "\"author\": \"Herman Melville\","
							+ "\"title\": \"Moby Dick\","
							+ "\"isbn\": \"0-553-21311-3\","
							+ "\"price\": 8.99"
						+ "},"
						+ "{"
							+ "\"category\": \"fiction\","
							+ "\"author\": \"J. R. R. Tolkien\","
							+ "\"title\": \"The Lord of the Rings\","
							+ "\"isbn\": \"0-395-19395-8\","
							+ "\"price\": 22.99"
						+ "}"
					+ "],"
					+ "\"bicycle\": {"
					+ "\"color\": \"red\","
					+ "\"price\": 19.95"
					+ "}"
				+ "},"
				+ "\"expensive\": 10,"
				+ "\"some_integers\" : [1,2,3,4]"
			+ "}";
	}

	private String expected()
	{
		return "{"
				+ "\"store\":{"
					+ "\"book\":["
						+ "{"
							+ "\"category\":\"reference\","
							+ "\"author\":\"Nigel Rees\","
							+ "\"price\":8.95,"
							+ "\"title\":\"Sayings of the Century\""
						+ "},"
						+ "{"
							+ "\"category\":\"fiction\","
							+ "\"author\":\"Evelyn Waugh\","
							+ "\"price\":12.99,"
							+ "\"title\":\"Sword of Honour\""
						+ "},"
						+ "{"
							+ "\"category\":\"fiction\","
							+ "\"author\":\"Herman Melville\","
							+ "\"title\":\"Moby Dick\","
							+ "\"isbn\":\"0-553-21311-3\","
							+ "\"price\":8.99"
						+ "},"
						+ "{"
							+ "\"category\":\"fiction\","
							+ "\"author\":\"J. R. R. Tolkien\","
							+ "\"title\":\"The Lord of the Rings\","
							+ "\"isbn\":\"0-395-19395-8\","
							+ "\"price\":22.99"
						+ "}"
					+ "],"
					+ "\"bicycle\":{"
					+ "\"color\":\"red\","
					+ "\"price\":19.95"
					+ "}"
				+ "},"
				+ "\"expensive\":10,"
				+ "\"some_integers\":[1,2,3,4]"
			+ "}";
	}

	@Override
	protected Object retrieveObjectForSampleConfig()
	{
		JsonInsertExecution exec1 = new JsonInsertExecution(new ConstantDataInputParameter(PATHS[0]),
				new ConstantDataInputParameter(TITLES[0]));
		JsonInsertExecution exec2 = new JsonInsertExecution(new ConstantDataInputParameter(PATHS[1]),
				new ConstantDataInputParameter(TITLES[1]));
		JsonInsertService service = new JsonInsertService(new StringPayloadDataInputParameter(),
				new ArrayList<>(Arrays.asList(new JsonInsertExecution[]{ exec1, exec2 })));
		service.setSource(new StringPayloadDataInputParameter());
		service.setTarget(new StringPayloadDataOutputParameter());
		return service;
	}
}
