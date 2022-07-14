package com.capgemini.sip.simplechain.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import lombok.*;

@Data
@NoArgsConstructor
public class Transaction {

  private String from;

  private String to;
  private long amount;
  private long timestamp;

  public Transaction(@NonNull String from, @NonNull String to, long amount) {
    this.from = from;
    this.to = to;
    this.amount = amount;
    timestamp = Instant.now().getEpochSecond();
  }

  public String hash() {
    var s = from + to + Long.toString(amount) + Long.toString(getTimestamp());

    String hashString = null;

    try {
      var md = MessageDigest.getInstance("SHA256");
      hashString = HashUtils.bytesToString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      // TODO: Ask how to handle this
    }

    return hashString;
  }

  @JsonIgnore
  public boolean isValid() {

    return from != to && AddressUtils.isValid(from) && AddressUtils.isValid(to) && amount >= 0;
  }
}
