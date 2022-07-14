package com.capgemini.sip.simplechain.core;

public class HashUtils {
  public static String bytesToString(byte[] bytes) {
    var sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }

    return sb.toString();
  }
}
