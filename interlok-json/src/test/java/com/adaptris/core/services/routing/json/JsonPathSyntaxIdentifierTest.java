package com.adaptris.core.services.routing.json;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.routing.AlwaysMatchSyntaxIdentifier;
import com.adaptris.core.services.routing.SyntaxBranchingService;
import com.adaptris.core.services.routing.SyntaxIdentifier;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class JsonPathSyntaxIdentifierTest extends ExampleServiceCase {

  private static final String BASE_DIR_KEY = "JsonPathServiceExamples.baseDir";

  private static final String JSON_PATH_VALID_1 = "$.store.book[0].title";
  private static final String JSON_PATH_VALID_2 = "$..book[?(@.price<10)]";

  public JsonPathSyntaxIdentifierTest() {
    if (PROPERTIES.getProperty(BASE_DIR_KEY) != null) {
      setBaseDir(PROPERTIES.getProperty(BASE_DIR_KEY));
    }
  }


  @Test
  public void testIsThisSyntax_Matches() throws Exception {
    JsonPathSyntaxIdentifier identifier = new JsonPathSyntaxIdentifier();
    identifier.addPattern(JSON_PATH_VALID_1);
    assertTrue("JSONPath matches", identifier.isThisSyntax(sampleJsonContent()));
  }

  @Test
  public void testIsThisSyntax_Matches_Expression() throws Exception {
    JsonPathSyntaxIdentifier identifier = new JsonPathSyntaxIdentifier();
    identifier.addPattern(JSON_PATH_VALID_2);
    assertTrue("JSONPath matches", identifier.isThisSyntax(sampleJsonContent()));
  }

  @Test
  public void testIsThisSyntax_NoMatches() throws Exception {
    JsonPathSyntaxIdentifier identifier = new JsonPathSyntaxIdentifier();
    identifier.addPattern("$.store.book[25].title");
    assertFalse("JSONPath no matches", identifier.isThisSyntax(sampleJsonContent()));
  }

  @Test
  public void testIsThisSyntax_FunctionNoMatch() throws Exception {
    JsonPathSyntaxIdentifier identifier = new JsonPathSyntaxIdentifier();
    identifier.addPattern("$.store.book[?(@.price < 5)]");
    assertFalse("JSONPath no matches", identifier.isThisSyntax(sampleJsonContent()));
  }

  @Test
  public void testIsThisSyntax_ImplicitAND() throws Exception {
    JsonPathSyntaxIdentifier identifier = new JsonPathSyntaxIdentifier();
    identifier.addPattern(JSON_PATH_VALID_1);
    identifier.addPattern(JSON_PATH_VALID_2);
    identifier.addPattern("$.store.book[25].title");
    assertFalse("JSONPath no matches", identifier.isThisSyntax(sampleJsonContent()));
  }

  @Test
  public void testIsThisSyntax_NotJson() throws Exception {
    JsonPathSyntaxIdentifier identifier = new JsonPathSyntaxIdentifier();
    identifier.addPattern(JSON_PATH_VALID_1);
    assertFalse("JSONPath Not Json", identifier.isThisSyntax("<xml />"));
  }

  private List<SyntaxIdentifier> createStandardIdentifiers() {
    return new ArrayList<SyntaxIdentifier>(Arrays.asList(new SyntaxIdentifier[]
        {
            new JsonPathSyntaxIdentifier(Arrays.asList(new String[]
                {
                    JSON_PATH_VALID_1, JSON_PATH_VALID_2
                }), "isJson")

        }));
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    BranchingServiceCollection sl = new BranchingServiceCollection();
    SyntaxBranchingService sbs = new SyntaxBranchingService();
    sbs.setUniqueId("determineDocType");
    sbs.setSyntaxIdentifiers(createStandardIdentifiers());
    sbs.addSyntaxIdentifier(new AlwaysMatchSyntaxIdentifier("alwaysMatches"));
    sl.addService(sbs);
    sl.setFirstServiceId(sbs.getUniqueId());
    sl.addService(new LogMessageService("isJson"));
    sl.addService(new LogMessageService("alwaysMatches"));
    return sl;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return SyntaxBranchingService.class.getName() + "-" + JsonPathSyntaxIdentifier.class.getSimpleName();
  }


  private String sampleJsonContent() {
    return "{"
        + "\"store\": {"
        +    "\"book\": ["
        +        "{"
        +            "\"category\": \"reference\","
        +            "\"author\": \"Nigel Rees\","
        +            "\"title\": \"Sayings of the Century\","
        +            "\"price\": 8.95"
        +        "},"
        +        "{"
        +            "\"category\": \"fiction\","
        +            "\"author\": \"Evelyn Waugh\","
        +            "\"title\": \"Sword of Honour\","
        +            "\"price\": 12.99"
        +        "},"
        +        "{"
        +            "\"category\": \"fiction\","
        +            "\"author\": \"Herman Melville\","
        +            "\"title\": \"Moby Dick\","
        +            "\"isbn\": \"0-553-21311-3\","
        +            "\"price\": 8.99"
        +        "},"
        +        "{"
        +            "\"category\": \"fiction\","
        +            "\"author\": \"J. R. R. Tolkien\","
        +            "\"title\": \"The Lord of the Rings\","
        +            "\"isbn\": \"0-395-19395-8\","
        +            "\"price\": 22.99"
        +        "}"
        +    "],"
        +    "\"bicycle\": {"
        +        "\"color\": \"red\","
        +        "\"price\": 19.95"
        +    "}"
        + "},"
        + "\"expensive\": 10"
        + "}";
  }

}