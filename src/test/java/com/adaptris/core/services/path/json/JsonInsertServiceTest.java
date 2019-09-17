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
	private static final String NEW_BOOK = "{"
			+ "\"category\": \"fantasy\","
			+ "\"author\": \"Terry Pratchett\","
			+ "\"title\": \"The Colour of Magic\","
			+ "\"isbn\": \"978-1473205321\","
			+ "}";
	private static final String NEW_PRICE = Double.toString(9.99);
	private static final String NEW_AVAILABILITY = Boolean.TRUE.toString();
	private static final String NEW_STOCK = Integer.toString(100);
	private static final String NO_AVAILABILITY = Boolean.FALSE.toString();
	private static final String MORE_INTEGERS = "[ [ 5, 6, 7, 8 ] ]";
	private static final String MANY_INTEGERS = "[ 9, 10, 11, 12 ]";

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
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter(PATHS[0]), new MetadataDataInputParameter("title-1")));
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter(PATHS[1]), new MetadataDataInputParameter("title-2")));

		JsonInsertService service = new JsonInsertService(new StringPayloadDataInputParameter(), executions);
		execute(service, message);

		assertEquals(expected(), message.getContent());
	}

	@Test
	public void testInsertJSONObject() throws Exception
	{
		AdaptrisMessage message = DefaultMessageFactory.getDefaultInstance().newMessage(content());
		message.addMetadata("new-book", NEW_BOOK);
		message.addMetadata("new-price", NEW_PRICE);
		message.addMetadata("new-availability", NEW_AVAILABILITY);
		message.addMetadata("new-stock", NEW_STOCK);
		message.addMetadata("no-availability", NO_AVAILABILITY);
		message.addMetadata("more-integers", MORE_INTEGERS);
		message.addMetadata("many-integers", MANY_INTEGERS);

		List<JsonInsertExecution> executions = new ArrayList<>();
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter("$.store.book[0].availability"), new MetadataDataInputParameter("no-availability")));
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter("$.store.book[1].availability"), new MetadataDataInputParameter("no-availability")));
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter("$.store.book[4]"), new MetadataDataInputParameter("new-book")));
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter("$.store.book[4].price"), new MetadataDataInputParameter("new-price")));
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter("$.store.book[4].availability"), new MetadataDataInputParameter("new-availability")));
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter("$.store.book[4].stock"), new MetadataDataInputParameter("new-stock")));
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter("$.more_integers"), new MetadataDataInputParameter("more-integers")));
		executions.add(new JsonInsertExecution(new ConstantDataInputParameter("$.more_integers[1]"), new MetadataDataInputParameter("many-integers")));

		JsonInsertService service = new JsonInsertService(new StringPayloadDataInputParameter(), executions);
		execute(service, message);

		assertEquals(EXPECTED, message.getContent());
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

	private static final String EXPECTED = "{" +
				"\"store\":{" +
					"\"book\":[" +
						"{" +
							"\"category\":\"reference\"," +
							"\"author\":\"Nigel Rees\"," +
							"\"price\":8.95," +
							"\"availability\":false" +
						"}," +
						"{" +
							"\"category\":\"fiction\"," +
							"\"author\":\"Evelyn Waugh\"," +
							"\"price\":12.99," +
							"\"availability\":false" +
						"}," +
						"{" +
							"\"category\":\"fiction\"," +
							"\"author\":\"Herman Melville\"," +
							"\"title\":\"Moby Dick\"," +
							"\"isbn\":\"0-553-21311-3\"," +
							"\"price\":8.99" +
						"}," +
						"{" +
							"\"category\":\"fiction\"," +
							"\"author\":\"J. R. R. Tolkien\"," +
							"\"title\":\"The Lord of the Rings\"," +
							"\"isbn\":\"0-395-19395-8\"," +
							"\"price\":22.99" +
						"}," +
						"{" +
							"\"author\":\"Terry Pratchett\"," +
							"\"isbn\":\"978-1473205321\"," +
							"\"category\":\"fantasy\"," +
							"\"title\":\"The Colour of Magic\"," +
							"\"price\":9.99," +
							"\"availability\":true," +
							"\"stock\":100" +
						"}" +
					"]," +
					"\"bicycle\":{" +
						"\"color\":\"red\"," +
						"\"price\":19.95" +
					"}" +
				"}," +
				"\"expensive\":10," +
				"\"some_integers\":[" +
					"1," +
					"2," +
					"3," +
					"4" +
				"]," +
				"\"more_integers\":[" +
					"[" +
						"5," +
						"6," +
						"7," +
						"8" +
					"]," +
					"[" +
						"9," +
						"10," +
						"11," +
						"12" +
					"]" +
				"]" +
			"}";

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
