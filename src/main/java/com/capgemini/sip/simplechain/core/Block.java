package com.capgemini.sip.simplechain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import lombok.*;

@Data
@NoArgsConstructor
public class Block {
  public static final byte MAX_TRANSACTIONS = 10;
  public static final byte MIN_TRANSACTIONS = 1;
  public static final String GENESIS_FROM = "";
  public static final String GENESIS_PREV_HASH = "";

  @NonNull private final List<Transaction> transactions = new ArrayList<>();
  @NonNull private String prevHash; // for Genesis Block prevHash = ""
  private long timestamp;

  public Block(@NonNull String prevHash) {
    this.prevHash = prevHash;
    timestamp = Instant.now().getEpochSecond();
  }

  public void addTransaction(@NonNull Transaction tx) {
    transactions.add(tx);
  }

  public void addAllTransactions(@NonNull Collection<Transaction> txs) {
    transactions.addAll(txs);
  }

  public List<Transaction> getTransactions() {
    return Collections.unmodifiableList(transactions);
  }

  public String hash() {
    var sb = new StringBuilder();
    for (Transaction tx : transactions) {
      sb.append(tx.hash());
    }

    sb.append(prevHash);
    sb.append(timestamp);

    byte[] hashBytes = null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA256");
      hashBytes = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

    } catch (NoSuchAlgorithmException ex) {
      // TODO: Ask how I should handle this
    }
    return HashUtils.bytesToString(hashBytes);
  }

  @JsonIgnore
  public boolean isValid() {
    final var len = transactions.size();
    if (len > MAX_TRANSACTIONS || len < MIN_TRANSACTIONS) {
      return false;
    }

    for (Transaction tx : transactions) {
      if (!tx.isValid()) {
        return false;
      }
    }

    // TODO: Ask whether I should check prevHash

    return true;
  }

  public static Block genesis(@NonNull String to, long amount) {
    var initialTransaction = new Transaction(GENESIS_FROM, to, amount);
    var genBlock = new Block(GENESIS_PREV_HASH);
    genBlock.addTransaction(initialTransaction);
    return genBlock;
  }

  @JsonIgnore
  public Transaction getLastTransaction() {
    if (transactions.size() == 0) {
      return null;
    }
    return transactions.get(transactions.size() - 1);
  }

  @JsonIgnore
  public boolean isGenesis() {
    return prevHash == GENESIS_PREV_HASH
        && transactions.size() == 1
        && getLastTransaction().getFrom() == GENESIS_FROM;
  }

  public boolean hasTransactions() {
    return this.transactions.size() > 0;
  }
}
