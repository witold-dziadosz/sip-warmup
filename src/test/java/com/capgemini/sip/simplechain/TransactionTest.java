package com.capgemini.sip.simplechain;

import com.capgemini.sip.simplechain.core.Transaction;
import org.junit.Assert;
import org.junit.Test;

public class TransactionTest {
  @Test
  public void givenValidTransaction_thenOk() {
    var t = new Transaction("ewa", "adam", 100);
    Assert.assertTrue(t.isValid());
  }

  @Test
  public void givenNegativeAmountTransaction_thenNotOk() {
    var t = new Transaction("ewa", "adam", -100);
    Assert.assertFalse(t.isValid());
  }

  @Test
  public void givenSendingToYourselfTransaction_thenNotOk() {
    var t = new Transaction("ewa", "ewa", 200);
    Assert.assertFalse(t.isValid());
  }
}
