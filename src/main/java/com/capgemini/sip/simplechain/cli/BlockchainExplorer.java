package com.capgemini.sip.simplechain.cli;

import com.capgemini.sip.simplechain.core.Block;
import com.capgemini.sip.simplechain.core.Blockchain;
import com.capgemini.sip.simplechain.core.Transaction;
import java.util.function.Predicate;
import lombok.NonNull;

public class BlockchainExplorer {
  private final Blockchain blockchain;

  public BlockchainExplorer(@NonNull Blockchain blockchain) {
    this.blockchain = blockchain;
  }

  public String length() {
    var len = blockchain.getBlocks().size();
    return String.format("Blockchain length is %d", len);
  }

  public String blockTransactions(Block block) {
    return blockTransactions(block, "  ", "  ");
  }

  public String blockTransactions(Block block, String firstLevelPrefix, String secondLevelPrefix) {
    return blockTransactions(block, firstLevelPrefix, secondLevelPrefix, tx -> true);
  }

  public String blockTransactions(Block block, Predicate<Transaction> filter) {
    return blockTransactions(block, "  ", "  ", filter);
  }

  public String blockTransactions(
      Block block,
      String firstLevelPrefix,
      String secondLevelPrefix,
      Predicate<Transaction> filter) {

    var sb = new StringBuilder();
    secondLevelPrefix = secondLevelPrefix + firstLevelPrefix;
    var transactions = block.getTransactions();

    for (int i = transactions.size() - 1; i >= 0; i--) {
      var tx = transactions.get(i);
      if (!filter.test(tx)) {
        continue;
      }
      sb.append(firstLevelPrefix + String.format("Tx#: %s\n", tx.hash()));
      sb.append(secondLevelPrefix + String.format("From: %s\n", tx.getFrom()));
      sb.append(secondLevelPrefix + String.format("To: %s\n", tx.getTo()));
      sb.append(secondLevelPrefix + String.format("Amount: %d\n", tx.getAmount()));
      sb.append(secondLevelPrefix + String.format("Timestamp: %d\n", tx.getTimestamp()));
    }

    return sb.toString();
  }

  public String blockDetails(Block block) {
    return blockDetails(block, "  ", "  ");
  }

  public String blockDetails(Block block, String firstLevelIndent, String secondLevelIndent) {
    var sb = new StringBuilder();
    var nTx = block.getTransactions().size();

    sb.append(String.format("Block %s\n", block.hash()));
    sb.append(firstLevelIndent + String.format("Prev. hash: %s\n", block.getPrevHash()));
    sb.append(firstLevelIndent + String.format("Timestamp: %d\n", block.getTimestamp()));
    sb.append(firstLevelIndent + String.format("Transactions: %d\n", nTx));

    sb.append(blockTransactions(block, "  " + secondLevelIndent, "    "));

    return sb.toString();
  }

  public String blockDetailsByHash(@NonNull String hash) {
    var b = blockchain.findBlockByHash(hash);
    if (b.isEmpty()) {
      return ("Block with hash " + hash + " does not exist");
    }

    return blockDetails(b.get());
  }

  public String blockDetailsByHeight(int height) {
    var b = blockchain.findBlockByHeight(height);
    if (b.isEmpty()) {
      return ("Block with height " + height + " does not exist");
    }

    return blockDetails(b.get());
  }

  public String lastBlockDetails() {
    var lastBlock = blockchain.getHead();
    return blockDetails(lastBlock);
  }

  public String balance(String addr) {
    var result = 0;
    for (Block b : blockchain.getBlocks()) {
      for (Transaction tx : b.getTransactions()) {
        if (tx.getFrom().equals(addr)) {
          result -= tx.getAmount();
        } else if (tx.getTo().equals(addr)) {
          result += tx.getAmount();
        }
      }
    }

    return addr + " has " + result + " coins";
  }

  public String history(@NonNull String addr) {
    var sb = new StringBuilder();
    for (Block block : blockchain.getBlocks()) {
      String txInfo = "";
      sb.append(
          blockTransactions(block, tx -> tx.getFrom().equals(addr) || tx.getTo().equals(addr)));
    }

    return sb.toString();
  }
}
