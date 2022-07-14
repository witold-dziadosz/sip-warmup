package com.capgemini.sip.simplechain.core;

import java.util.HashMap;
import lombok.Getter;
import lombok.NonNull;

/** Exchange provides simplified access to basic operations on blockchain. */
public class Exchange {
  /**
   * I decided that it is the Exchange's job to prevent you from spending more money than you've got
   * *
   */
  private HashMap<String, Long> balanceCache = new HashMap<>();

  private final @Getter Blockchain blockchain;
  private @Getter Block currentBlock;

  public Exchange(@NonNull Blockchain blockchain) {
    this.blockchain = blockchain;
    if (!blockchain.isValid()) {
      throw new RuntimeException("Blockchain invalid " + blockchain.toString());
    }
    currentBlock = new Block(blockchain.getHead().hash());
  }

  public boolean oneTransactionBlock(@NonNull String from, @NonNull String to, long amount) {
    if (currentBlock.hasTransactions()) {
      commitCurrentBlock();
      resetCurrentBlock();
    }

    var tx = new Transaction(from, to, amount);
    if (!tx.isValid() || !hasEnoughCoins(from, amount)) {
      return false;
    }
    currentBlock.addTransaction(tx);
    commitCurrentBlock();
    resetCurrentBlock();
    updateCache(tx);
    return true;
  }

  public boolean commitCurrentBlock() {
    if (!currentBlock.isValid()) {
      return false;
    }
    blockchain.addBlock(currentBlock);
    return true;
  }

  public boolean transact(@NonNull String from, @NonNull String to, long amount) {
    var tx = new Transaction(from, to, amount);

    if (!tx.isValid() || !hasEnoughCoins(from, amount)) {
      return false;
    }

    currentBlock.addTransaction(tx);

    if (currentBlock.getTransactions().size() == Block.MAX_TRANSACTIONS) {
      commitCurrentBlock();
      resetCurrentBlock();
    }

    updateCache(tx);
    return true;
  }

  private boolean hasEnoughCoins(String from, long amount) {

    return getBalance(from) >= amount;
  }

  private void updateCache(Transaction tx) {
    String to = tx.getTo();
    String from = tx.getFrom();
    long amount = tx.getAmount();

    if (balanceCache.containsKey(from)) {
      long oldBalance = balanceCache.get(from);

      balanceCache.put(from, oldBalance - amount);
    } else {
      addToCache(from);
    }

    if (balanceCache.containsKey(to)) {
      long oldBalance = balanceCache.get(to);
      balanceCache.put(to, oldBalance + amount);
    } else {
      addToCache(to);
    }
  }

  private long getBalance(String addr) {
    if (!balanceCache.containsKey(addr)) {
      addToCache(addr);
    }

    return balanceCache.get(addr);
  }

  private void addToCache(String addr) {
    long coins = 0;
    for (Block block : this.blockchain.getBlocks()) {
      for (Transaction tx : block.getTransactions()) {
        if (tx.getFrom().equals(addr)) {
          coins -= tx.getAmount();
        } else if (tx.getTo().equals(addr)) {
          coins += tx.getAmount();
        }
      }
    }

    balanceCache.put(addr, coins);
  }

  private void resetCurrentBlock() {
    currentBlock = new Block(blockchain.getHead().hash());
  }
}
