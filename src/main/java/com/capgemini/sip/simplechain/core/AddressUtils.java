package com.capgemini.sip.simplechain.core;

// TODO: Ask whether should I create more standard addresses, e.g hashes, or any String will be OK

public class AddressUtils {
  private static final String containsWhitespaceRegex = ".*\\s.*";

  static boolean isValid(String address) {

    return address.equals("") || (address != null && !address.matches(containsWhitespaceRegex));
  }
}
