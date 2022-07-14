package com.capgemini.sip.simplechain.core;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;

/** Exchange provides simplified access to basic operations on blockchain. */
public class Exchange {
  /**
   * I decided that it is the Exchange's job to prevent you from spending more money than you've got
   * *
   */
  private Map<String, Long> balanceCache = new HashMap<String, Long>();

  private final @Getter Blockchain blockchain;
  private @Getter Block currentBlock;

  public Exchange(@NonNull Blockchain blockchain) {
    this.blockchain = blockchain;
    if (!blockchain.isValid()) {
      throw new RuntimeException("Blockchain invalid " + blockchain.toString());
    }
    currentBlock = new Block(blockchain.getHead().hash());
  }

  public boolean canTransact(Transaction tx) {
    String from = tx.getFrom();
    long amount = tx.getAmount();

    return tx.isValid() && hasEnoughCoins(from, amount);
  }

  public boolean send(@NonNull String from, @NonNull String to, long amount) {
    var tx = new Transaction(from, to, amount);
    if (!canTransact(tx)) {
      return false;
    }

    currentBlock.addTransaction(tx);
    updateCache(tx);
    var transactions = currentBlock.getTransactions();
    if (transactions.size() == Block.MAX_TRANSACTIONS) {
      commitCurrentBlock();
      resetCurrentBlock();
    }

    return true;
  }

  public boolean sendNow(@NonNull String from, @NonNull String to, long amount) {

    var tx = new Transaction(from, to, amount);
    if (!canTransact(tx)) {
      return false;
    }
    currentBlock.addTransaction(tx);
    commitCurrentBlock();
    resetCurrentBlock();
    updateCache(tx);
    return true;
  }

  private boolean commitCurrentBlock() {
    if (!currentBlock.isValid()) {
      return false;
    }
    blockchain.addBlock(currentBlock);
    return true;
  }

  private boolean hasEnoughCoins(String from, long amount) {

    return getBalance(from) >= amount;
  }

  private void updateCache(Transaction tx) {
    String to = tx.getTo();
    String from = tx.getFrom();
    long amount = tx.getAmount();

    if (!balanceCache.containsKey(from)) {
      addToCache(from);
    }
    long oldFromBalance = balanceCache.get(from);
    balanceCache.put(from, oldFromBalance - amount);

    if (!balanceCache.containsKey(to)) {
      addToCache(to);
    }

    long oldToBalance = balanceCache.get(to);
    balanceCache.put(to, oldToBalance + amount);
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

  public void commit() {
    commitCurrentBlock();
    resetCurrentBlock();
  }
}
