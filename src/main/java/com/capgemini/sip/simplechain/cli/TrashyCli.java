package com.capgemini.sip.simplechain.cli;

import com.capgemini.sip.simplechain.core.Blockchain;
import com.capgemini.sip.simplechain.core.Exchange;
import com.capgemini.sip.simplechain.json.JsonUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class does not implement any user input validation. This is just a warmup task, so let's
 * skip defensive coding for now.
 */
public class TrashyCli {
  private static class BlockchainNotInitializedException extends RuntimeException {}

  private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  private BlockchainExplorer explorer;
  private Exchange exchange;
  private boolean isInit = false;

  private String prompt() {
    System.out.print("> ");

    try {
      var ret = reader.readLine();
      return ret;
    } catch (IOException e) {
      throw new RuntimeException("Ooops");
    }
  }

  private void help() {}

  private void send(String from, String to, long amount) {
    checkInit();
    // TODO: Maybe create multi-transactional blocks?
    if (exchange.oneTransactionBlock(from, to, amount)) {
      System.out.printf("Sent %d from %s to %s\n", amount, from, to);
    } else {
      System.out.println("Cannot perform transaction");
    }
  }

  private void load(String fromWhere) {

    try {
      Blockchain blockchain = JsonUtils.readFromFile(fromWhere, Blockchain.class);
      this.explorer = new BlockchainExplorer(blockchain);
      this.exchange = new Exchange(blockchain);
      System.out.printf("Loaded blockchain from %s\n", fromWhere);
      isInit = true;
    } catch (IOException ex) {
      System.out.printf("Cannot read from %s\n", fromWhere);
    }
  }

  public void loop() {
    while (true) {
      try {
        String p = prompt();

        String[] args = p.split("\\s+");

        switch (args[0]) {
          default:
          case "help":
            help();
            break;

          case "load":
            String fromWhere = args[1];
            load(fromWhere);
            break;
          case "save":
            String dest = args[1];
            save(dest);
            break;
          case "init":
            init();
            break;
          case "send":
            String from = args[1];
            String to = args[2];
            long amount = Long.parseLong(args[3]);
            send(from, to, amount);
            break;
          case "byhash":
            String hash = args[1];
            byHash(hash);
            break;
          case "byheight":
            try {
              int h = Integer.parseInt(args[1]);
              byheight(h);
            } catch (NumberFormatException e) {
              System.out.println("Specify correct number!");
            }
            break;
          case "length":
            length();
            break;
          case "last":
            last();
            break;
          case "balance":
            String balanceAddr = args[1];
            balance(balanceAddr);
            break;
          case "tx":
            String txAddr = args[1];
            tx(txAddr);
        }
      } catch (BlockchainNotInitializedException ex) {
        System.out.println("Blokchain not initalized!");
      }
    }
  }

  private void tx(String addr) {
    checkInit();
    System.out.println(explorer.history(addr));
  }

  private void checkInit() {
    if (!isInit) {
      throw new BlockchainNotInitializedException();
    }
  }

  private void balance(String addr) {
    checkInit();
    System.out.println(explorer.balance(addr));
  }

  private void last() {
    checkInit();
    System.out.println(explorer.lastBlockDetails());
  }

  private void length() {
    checkInit();
    System.out.println(explorer.length());
  }

  private void byheight(int h) {
    checkInit();
    System.out.println(explorer.blockDetailsByHeight(h));
  }

  private void byHash(String hash) {
    checkInit();
    hash = hash.strip();
    var response = explorer.blockDetailsByHash(hash);
    System.out.println(response);
  }

  private void init() {
    Blockchain blockchain = Blockchain.createSampleBlockchain();
    this.explorer = new BlockchainExplorer(blockchain);
    this.exchange = new Exchange(blockchain);
    System.out.println("Initialized new blockchain");
    isInit = true;
  }

  private void save(String dest) {
    checkInit();
    try {
      JsonUtils.saveToFile(this.exchange.getBlockchain(), dest);
      isInit = true;
    } catch (IOException ex) {
      System.out.printf("Cannot save to %s\n", dest);
    }
  }

  public TrashyCli(Blockchain blockchain) {
    this.exchange = new Exchange(blockchain);
    this.explorer = new BlockchainExplorer(blockchain);
    isInit = true;
  }

  public TrashyCli() {
    isInit = false;
  }
}
