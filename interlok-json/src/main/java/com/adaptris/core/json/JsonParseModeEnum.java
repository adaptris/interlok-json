package com.adaptris.core.json;

import net.minidev.json.parser.JSONParser;

public enum JsonParseModeEnum {

  Permissive {
    @Override
    public int parserMode() {
      return JSONParser.MODE_PERMISSIVE;
    }
  },
  RFC4627 {
    @Override
    public int parserMode() {
      return JSONParser.MODE_RFC4627;
    }
  },
  Json_Simple {
    @Override
    public int parserMode() {
      return JSONParser.MODE_JSON_SIMPLE;
    }
  },
  Strict {
    @Override
    public int parserMode() {
      return JSONParser.MODE_STRICTEST;
    }
  };
  
  public abstract int parserMode();
    
  
}
