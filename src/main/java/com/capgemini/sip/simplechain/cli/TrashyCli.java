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

  public TrashyCli(Blockchain blockchain) {
    this.exchange = new Exchange(blockchain);
    this.explorer = new BlockchainExplorer(blockchain);
    isInit = true;
  }

  public TrashyCli() {
    isInit = false;
  }


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
  private void checkInit() {
    if (!isInit) {
      throw new BlockchainNotInitializedException();
    }
  }
  private void sendNow(String from, String to, long amount) {
    checkInit();
    // TODO: Maybe create multi-transactional blocks?
    if (exchange.sendNow(from, to, amount)) {
      System.out.printf("Sent %d from %s to %s\n", amount, from, to);
    } else {
      System.out.println("Cannot perform transaction");
    }
  }

  private void send(String from, String to, long amount) {
    checkInit();
    if (exchange.send(from, to, amount)) {
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
  private void save(String dest) {
    checkInit();
    try {
      JsonUtils.saveToFile(this.exchange.getBlockchain(), dest);
      isInit = true;
    } catch (IOException ex) {
      System.out.printf("Cannot save to %s\n", dest);
    }
  }

  private void init() {
    Blockchain blockchain = Blockchain.createSampleBlockchain();
    this.explorer = new BlockchainExplorer(blockchain);
    this.exchange = new Exchange(blockchain);
    System.out.println("Initialized new blockchain");
    isInit = true;
  }





  private void tx(String addr) {
    checkInit();
    System.out.println(explorer.history(addr));
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
            if (args.length != 2) {
              System.out.println("usage: load [path to file]");
              continue;
            }
            String fromWhere = args[1];
            load(fromWhere);
            break;
          case "save":
            if (args.length != 2) {
              System.out.println("usage: save [path to file]");
              continue;
            }
            String dest = args[1];
            save(dest);
            break;
          case "init":
            init();
            break;
          case "sendnow":
            if (args.length != 4) {
              System.out.println("usage: sendnow [from] [to] [amount]");
              continue;
            }
            String from = args[1];
            String to = args[2];
            try {
              long amount = Long.parseLong(args[3]);
              sendNow(from, to, amount);
            } catch (NumberFormatException ex) {
              System.out.println("Specify correct number!");
            }
            break;
          case "send":
            if (args.length != 4) {
              System.out.println("usage: send [from] [to] [amount]");
              continue;
            }
            String fromProperly = args[1];
            String toProperly = args[2];
            try {
              long amountProperly = Long.parseLong(args[3]);
              send(fromProperly, toProperly, amountProperly);
            } catch (NumberFormatException ex) {
              System.out.println("Specify correct number!");
            }
            break;
          case "commit":
            exchange.commit();
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
            if (args.length != 2) {
              System.out.println("usage: balance [address]");
              continue;
            }
            String balanceAddr = args[1];
            balance(balanceAddr);
            break;
          case "tx":
            if (args.length != 2) {
              System.out.println("usage: tx [address]");
            }
            String txAddr = args[1];
            tx(txAddr);
            break;
          case "exit":
          case "quit":
            System.exit(0);
            break;
        }
      } catch (BlockchainNotInitializedException ex) {
        System.out.println("Blokchain not initalized!");
      }
    }
  }
}
