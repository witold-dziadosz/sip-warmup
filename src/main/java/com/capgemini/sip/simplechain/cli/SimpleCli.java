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
public class SimpleCli {

  private static final String SENDNOW_USAGE = "sendnow [from] [to] [amount]";
  private static final String SEND_USAGE = "send [from] [to] [amount]";

  private static final String LOAD_USAGE = "load [path to file]";
  private static final String SAVE_USAGE = "save [path to file]";

  private static final String BYHASH_USAGE = "byhash [hash]";
  private static final String BYHEIGHT_USAGE = "byheight [height]";

  private static final String BALANCE_USAGE = "balance [address]";
  private static final String TX_USAGE = "tx [address]";

  private static class BlockchainNotInitializedException extends RuntimeException {}

  private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
  private BlockchainExplorer explorer;
  private Exchange exchange;
  private boolean isInit = false;

  public SimpleCli(Blockchain blockchain) {
    this.exchange = new Exchange(blockchain);
    this.explorer = new BlockchainExplorer(blockchain);
    isInit = true;
  }

  public SimpleCli() {
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

  private void help() {
    System.out.println("Simplechain: ");
    System.out.println("init :: create new blockchain");
    System.out.println(SENDNOW_USAGE + " :: send money instantly");
    System.out.println(SEND_USAGE + " :: add tx to block");
    System.out.println("commit :: add current block to blockchain");
    System.out.println(LOAD_USAGE + " load bchain from JSON");
    System.out.println(SAVE_USAGE + " save bchain to JSON");
    System.out.println("length :: length of the blockchain");
    System.out.println("last :: last accepted block");
    System.out.println(BYHASH_USAGE + " :: get info about block");
    System.out.println(BYHEIGHT_USAGE + " :: get info about block");
    System.out.println(BALANCE_USAGE + " :: get account balance");
    System.out.println(TX_USAGE + " :: account's transaction history");
    System.out.println("exit :: terminate program");
    System.out.println("quit :: terminate program");
  }

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

  private void byHeight(int h) {
    checkInit();
    System.out.println(explorer.blockDetailsByHeight(h));
  }

  private void byHash(String hash) {
    checkInit();
    hash = hash.strip();
    var response = explorer.blockDetailsByHash(hash);
    System.out.println(response);
  }

  public void run() {
    System.out.println(logo);
    System.out.println("Stuck? Try typing `help`");
    loop();
  }

  private void loop() {
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
              System.out.println("usage: " + LOAD_USAGE);
              continue;
            }
            String fromWhere = args[1];
            load(fromWhere);
            break;
          case "save":
            if (args.length != 2) {
              System.out.println("usage :" + SAVE_USAGE);
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
              System.out.println("usage: " + SENDNOW_USAGE);
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
              System.out.println("usage: " + SEND_USAGE);
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
            if (args.length != 2) {
              System.out.println("usage: " + BYHASH_USAGE);
              continue;
            }
            String hash = args[1];
            byHash(hash);
            break;
          case "byheight":
            if (args.length != 2) {
              System.out.println("usage: " + BYHEIGHT_USAGE);
            }
            try {
              int h = Integer.parseInt(args[1]);
              byHeight(h);
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
              System.out.println("usage: " + BALANCE_USAGE);
              continue;
            }
            String balanceAddr = args[1];
            balance(balanceAddr);
            break;
          case "tx":
            if (args.length != 2) {
              System.out.println("usage: " + TX_USAGE);
              continue;
            }
            String txAddr = args[1];
            tx(txAddr);
            break;
          case "exit":
          case "quit":
            System.out.println("Bye.");
            System.exit(0);
            break;
        }
      } catch (BlockchainNotInitializedException ex) {
        System.out.println("Blokchain not initalized!");
      }
    }
  }

  static final String logo =
      "\n"
          + "  ______   ______  __       __  _______   __        ________         ______   __   "
          + " __   ______   ______  __    __ \n"
          + " /      \\ |      \\|  \\     /  \\|       \\ |  \\      |        \\       /      \\"
          + " |  \\  |  \\ /      \\ |      \\|  \\  |  \\\n"
          + "|  $$$$$$\\ \\$$$$$$| $$\\   /  $$| $$$$$$$\\| $$      | $$$$$$$$      |  $$$$$$\\|"
          + " $$  | $$|  $$$$$$\\ \\$$$$$$| $$\\ | $$\n"
          + "| $$___\\$$  | $$  | $$$\\ /  $$$| $$__/ $$| $$      | $$__          | $$   \\$$|"
          + " $$__| $$| $$__| $$  | $$  | $$$\\| $$\n"
          + " \\$$    \\   | $$  | $$$$\\  $$$$| $$    $$| $$      | $$  \\         | $$      |"
          + " $$    $$| $$    $$  | $$  | $$$$\\ $$\n"
          + " _\\$$$$$$\\  | $$  | $$\\$$ $$ $$| $$$$$$$ | $$      | $$$$$         | $$   __ |"
          + " $$$$$$$$| $$$$$$$$  | $$  | $$\\$$ $$\n"
          + "|  \\__| $$ _| $$_ | $$ \\$$$| $$| $$      | $$_____ | $$_____       | $$__/  \\| $$"
          + "  | $$| $$  | $$ _| $$_ | $$ \\$$$$\n"
          + " \\$$    $$|   $$ \\| $$  \\$ | $$| $$      | $$     \\| $$     \\       \\$$    $$|"
          + " $$  | $$| $$  | $$|   $$ \\| $$  \\$$$\n"
          + "  \\$$$$$$  \\$$$$$$ \\$$      \\$$ \\$$       \\$$$$$$$$ \\$$$$$$$$        \\$$$$$$"
          + "  \\$$   \\$$ \\$$   \\$$ \\$$$$$$ \\$$   \\$$\n"
          + "                                                                                    "
          + "                               \n"
          + "                                                                                    "
          + "                               \n"
          + "                                                                                    "
          + "                               \n";
}
