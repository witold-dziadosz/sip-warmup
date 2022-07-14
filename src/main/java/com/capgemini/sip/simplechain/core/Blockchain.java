package com.capgemini.sip.simplechain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Blockchain {
  private static final String SAMPLE_BLOCKCHAIN_OWNER_ADDRESS = "YOU";
  private static final long SAMPLE_BLOCKCHAIN_AMOUNT = 50000;
  private List<Block> blocks = new ArrayList<>();

  @JsonIgnore
  public Block getHead() {
    return blocks.get(blocks.size() - 1);
  }

  public static Blockchain createSampleBlockchain() {
    return new Blockchain(SAMPLE_BLOCKCHAIN_OWNER_ADDRESS, SAMPLE_BLOCKCHAIN_AMOUNT);
  }

  public Blockchain(@NonNull String firstMoneyOwnerAddress, long initialAmount) {
    // create genesis block
    var genBlock = Block.genesis(firstMoneyOwnerAddress, initialAmount);
    blocks.add(genBlock);
  }

  public void addBlock(@NonNull Block block) {
    blocks.add(block);
  }

  public List<Block> getBlocks() {
    return Collections.unmodifiableList(blocks);
  }

  @JsonIgnore
  public Block getGenesis() {
    return blocks.get(0);
  }

  @JsonIgnore
  public boolean isValid() {
    var len = blocks.size();

    for (var i = len - 1; i > 0; i--) {
      Block b = blocks.get(i);
      String prevHash = blocks.get(i - 1).hash();
      if (!b.isValid() || !b.getPrevHash().equals(prevHash)) {
        return false;
      }
    }

    Block genesis = getGenesis();

    return genesis.isGenesis() && genesis.isValid();
  }

  public int size() {
    return blocks.size();
  }

  public Optional<Block> findBlockByHeight(int h) {
    if (h < size() && h >= 0) {
      return Optional.of(blocks.get(h));
    }

    return Optional.empty();
  }

  public Optional<Block> findBlockByHash(@NonNull String hash) {
    for (Block b : blocks) {
      if (b.hash().equals(hash)) {
        return Optional.of(b);
      }
    }

    return Optional.empty();
  }
}
