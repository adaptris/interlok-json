package com.adaptris.core.json;

import java.util.HashMap;
import java.util.Map;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;

public abstract class DeserializerCase {

  public enum TypeKey {
    Object, Invalid_Object, Array, Invalid_Array, Not_Json
  }

  public static final String NOT_JSON = "hello world";

  public static final String INVALID_JSON_STRICT = "{ \"key\" : value }";
  public static final String VALID_JSON = "{ \"key\" : \"value\" }";

  public static final String JSON_ARRAY = "[" + VALID_JSON + "]";
  public static final String INVALID_JSON_ARRAY_STRICT = "[" + INVALID_JSON_STRICT + "]";


  private final Map<TypeKey, AdaptrisMessage> allMessageFlavours = createMessageFlavours();


  protected AdaptrisMessage jsonObject = allMessageFlavours.get(TypeKey.Object);
  protected AdaptrisMessage invalidJsonObj = allMessageFlavours.get(TypeKey.Invalid_Object);
  protected AdaptrisMessage jsonArray = allMessageFlavours.get(TypeKey.Array);
  protected AdaptrisMessage invalidJsonArray = allMessageFlavours.get(TypeKey.Invalid_Array);
  protected AdaptrisMessage notJson = allMessageFlavours.get(TypeKey.Not_Json);

  public static Map<TypeKey, AdaptrisMessage> createMessageFlavours() {

    AdaptrisMessageFactory fac = AdaptrisMessageFactory.getDefaultInstance();
    Map<TypeKey, AdaptrisMessage> result = new HashMap<>();
    result.put(TypeKey.Object, fac.newMessage(VALID_JSON));
    result.put(TypeKey.Invalid_Object, fac.newMessage(INVALID_JSON_STRICT));
    result.put(TypeKey.Array, fac.newMessage(JSON_ARRAY));
    result.put(TypeKey.Invalid_Array, fac.newMessage(INVALID_JSON_ARRAY_STRICT));
    result.put(TypeKey.Not_Json, fac.newMessage(NOT_JSON));
    return result;
  }

}
