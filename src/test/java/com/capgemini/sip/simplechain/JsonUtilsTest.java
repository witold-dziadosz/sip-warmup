package com.capgemini.sip.simplechain;

import com.capgemini.sip.simplechain.core.Blockchain;
import com.capgemini.sip.simplechain.json.JsonUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

public class JsonUtilsTest {
  static final String BLOCKCHAIN_FILEPATH = "blockchain.json";

  static void cleanUpBlockchain() throws IOException {
    Files.deleteIfExists(Path.of(BLOCKCHAIN_FILEPATH));
  }

  @Test
  public void givenDefaultBlockchain_thenCanBeSavedAndRead() throws Exception {
    cleanUpBlockchain();
    final Blockchain b = Blockchain.createSampleBlockchain();
    JsonUtils.saveToFile(b, BLOCKCHAIN_FILEPATH);
    final Blockchain readBlockchain = JsonUtils.readFromFile(BLOCKCHAIN_FILEPATH, Blockchain.class);

    Assert.assertEquals(b, readBlockchain);

    cleanUpBlockchain();
  }
}
