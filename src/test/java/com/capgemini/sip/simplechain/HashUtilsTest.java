package com.capgemini.sip.simplechain;

import com.capgemini.sip.simplechain.core.HashUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.junit.Assert;
import org.junit.Test;

public class HashUtilsTest {
  @Test
  public void givenString_thenGeneratesValidSha256() throws Exception {
    // from: https://emn178.github.io/online-tools/sha256.html
    final String sha = "4a61c4b97aae1272b824f05acbc027a598ddf144b4bde1f53752aaa67be59b56";
    final String str = "napis";

    final MessageDigest md = MessageDigest.getInstance("SHA256");
    final byte[] generatedSha = md.digest(str.getBytes(StandardCharsets.UTF_8));
    final String generatedShaString = HashUtils.bytesToString(generatedSha);

    Assert.assertEquals(generatedShaString, sha);
  }
}
