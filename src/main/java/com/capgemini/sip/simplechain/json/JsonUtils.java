package com.capgemini.sip.simplechain.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JsonUtils {
  public static <T> void saveToFile(T what, String where) throws IOException {
    var mapper = new ObjectMapper();
    var dest = new File(where);
    mapper.writeValue(dest, what);
  }

  public static <T> T readFromFile(String fromWhere, Class<T> klass) throws IOException {
    var mapper = new ObjectMapper();
    var source = new File(fromWhere);
    return mapper.readValue(source, klass);
  }
}
