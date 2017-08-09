package com.adaptris.core.transform.json;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.ServiceException;
import com.adaptris.core.common.FileDataInputParameter;
import com.adaptris.core.util.LifecycleHelper;

/**
 * Unit tests for {@link JsonSchemaService}.
 *
 * @author Ashley Anderson
 */
@SuppressWarnings("deprecation")
public class JsonSchemaServiceTest extends BaseCase {

	/**
	 * Default constructor.
	 */
	public JsonSchemaServiceTest() {
		super("JSON schema validation service.");
	}

	public JsonSchemaServiceTest(final String name) {
		super(name);
	}

  private static final String SCHEMA_URL = "file:///com/adaptris/core/json/schema/test_schema.json";

	private static final String VALID_JSON = "{ \"rectangle\" : { \"a\" : 5, \"b\" : 5 } }";
  private static final String INVALID_JSON = "{ \"rectangle\" : { \"a\" : -5, \"b\" : -5 } }";
  private static final String JSON_ARRAY = "[{ \"rectangle\" : { \"a\" : -5, \"b\" : -5 } }]";

  public void testInit() throws Exception {
    JsonSchemaService service = new JsonSchemaService();
    try {
      assertNull(service.getSchemaUrl());
      LifecycleHelper.prepare(service);
      LifecycleHelper.init(service);
      fail();
    }
    catch (CoreException expected) {

    }
  }

	public void testSuccess() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(VALID_JSON);
    ServiceCase.execute(createService(), message);
	}

  public void testFailure() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(INVALID_JSON);
    try {
      ServiceCase.execute(createService(), message);
      fail();
    }
    catch (ServiceException expected) {

    }
  }

  public void testArray() throws Exception {
    AdaptrisMessage message = AdaptrisMessageFactory.getDefaultInstance().newMessage(JSON_ARRAY);
    try {
      ServiceCase.execute(createService(), message);
      fail();
    }
    catch (ServiceException expected) {

    }
  }
  private JsonSchemaService createService() {
    final FileDataInputParameter schemaUrl = new FileDataInputParameter();
    schemaUrl.setDestination(new ConfiguredProduceDestination(SCHEMA_URL));
    return new JsonSchemaService(schemaUrl);
  }
}
