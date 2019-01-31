package logicComponents;

import BinanceBot.BinanceBot;
import Tools.StandardOutputChanger;
import com.google.gson.JsonArray;
import com.webcerebrium.binance.api.BinanceApi;
import com.webcerebrium.binance.api.BinanceApiException;
import com.webcerebrium.binance.datatype.*;
import messages.CurrentLanguageFactory;
import messages.Messages;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class TransactionLogic {
    private BigDecimal percentOfDropToBuyBTC;
    private BigDecimal percentOfUpToSellBTC;
    private BinanceApi binanceApi;
    private Scanner input;
    private BigDecimal lastBTCprice;
    private boolean toDrop;
    private BigDecimal minSellPrice;
    private BigDecimal minBuyPrice;
    private File textFile;
    private PrintWriter writer;
    private JsonArray balances;
    private CurrentLanguageFactory languageFabric;
    private Messages messages;
    private String in;
    private BigDecimal lastUSDTprice;
    private boolean firstSellMove;
    //private BigDecimal lastMovePrice;


    public TransactionLogic(BigDecimal percentOfDropToBuyBTC, BigDecimal percentOfUpToSellBTC) {
        this.percentOfDropToBuyBTC = percentOfDropToBuyBTC;
        this.percentOfUpToSellBTC = percentOfUpToSellBTC;

        StandardOutputChanger.closeOutput();
        apiConfiguration();
        logsFileCreation();
        firstMessagesAndSettings();
        writer.println("TRADING_BOT LOGS");
        lastBTCpriceUpdate();

        if ("R".equals(in) || "r".equals(in)) {
            //czekamy
            toDrop = false;

            while (true) {
                try {
                    tradeAction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if ("D".equals(in) || "d".equals(in)) {
            //sprzedajemy
            toDrop = true;
            firstSellMove = true;
            while (true) {
                try {
                    tradeAction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void logsFileCreation() {
        try {
            textFile = new File("TransactionHistory" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date()) + ".txt");
            writer = new PrintWriter(textFile);
            writer.println("Logs of BinanceBot:");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void apiConfiguration() {
        binanceApi = new BinanceApi();

        //binanceApi.secretKey = BinanceBot.BinanceBot.sl.secretKey;

        binanceApi.apiKey = BinanceBot.myApiKey;

        //Tymczasowe - wywal po ukończeniu testów!!!
        binanceApi.secretKey = "Erpd0BxHlqKG01SxAGpDi3NL2xCHkx7ytQdfDmvbHbgemh33fwgxHbjDK5Mdry6i";

        try {
            balances = binanceApi.account().getAsJsonArray("balances");
        } catch (BinanceApiException e) {
            e.printStackTrace();
        }
    }

    private void firstMessagesAndSettings() {
        StandardOutputChanger.openOutput();

        //Language part:
        languageFabric = new CurrentLanguageFactory();
        messages = languageFabric.getCurrentLanguage();

        //Prints trading rules:
        System.out.println(messages.getTradingRules());

        //Prints BTC amount
        System.out.println(messages.getYourFreeBitcoins());
        System.out.println(balances.get(0).getAsJsonObject().get("free"));

        //Prints USDT amount
        System.out.println(messages.getYourFreeUSDT());
        System.out.println(balances.get(11).getAsJsonObject().get("free"));

        //Trading logic
        System.out.println(messages.getFirstMoveQuesion());
        StandardOutputChanger.closeOutput();
        try {
            lastBTCprice = binanceApi.pricesMap().get("BTCUSDT");
            lastBTCpriceUpdate();
        } catch (BinanceApiException e) {
            e.printStackTrace();
        }
        StandardOutputChanger.openOutput();
        System.out.println(messages.getBtcPrice() + " " + lastBTCprice);
        StandardOutputChanger.closeOutput();
        input = new Scanner(System.in);
        in = input.next();
        while (!"R".equals(in) && !"r".equals(in) && !"D".equals(in) && !"d".equals(in)) {
            System.out.println(messages.getIncorrectValue());;
            in = input.next();
        }
    }

    private void lastBTCpriceUpdate() {
        try {
            lastBTCprice = binanceApi.pricesMap().get("BTCUSDT");
        } catch (BinanceApiException e) {
            e.printStackTrace();
        }
    }
//Unnecessary function
//    private void lastUSDTpriceUpdate() {
//        try {
//            lastUSDTprice = binanceApi.pricesMap().get("USDTBTC");
//        } catch (BinanceApiException e) {
//            e.printStackTrace();
//        }
//    }

    private BigDecimal getFreeBitcoins() {
        return balances.get(0).getAsJsonObject().get("free").getAsBigDecimal();
    }

    private BigDecimal getFreeUsdt() {
        try {
            balances = binanceApi.account().getAsJsonArray("balances");
        } catch (BinanceApiException e) {
            e.printStackTrace();
        }
        return balances.get(11).getAsJsonObject().get("free").getAsBigDecimal();

    }

    private void bitcoinBuyOrder() {
        try {
            BinanceSymbol symbol = BinanceSymbol.USDT("BTC");
            BinanceOrderPlacement placement = new BinanceOrderPlacement(symbol, BinanceOrderSide.BUY);
            placement.setType(BinanceOrderType.MARKET);
            BigDecimal price = minBuyPrice.setScale(2, RoundingMode.FLOOR);
            placement.setPrice(price);

            BigDecimal amountToBuy = getFreeUsdt().divide(lastBTCprice, 4, RoundingMode.FLOOR);
            //amountToBuy = amountToBuy.setScale(5,RoundingMode.FLOOR);
            StandardOutputChanger.openOutput();
//            System.out.println("Free USDT:"+getFreeUsdt());
//            System.out.println("lastBTCPRICE"+lastBTCprice);
//            System.out.println("Amount to buy:"+amountToBuy);
            StandardOutputChanger.closeOutput();
            placement.setQuantity(amountToBuy); // buy 10000 of asset for 0.00001 BTC
            BinanceOrder order = binanceApi.getOrderById(symbol, binanceApi.createOrder(placement).get("orderId").getAsLong());
//            StandardOutputChanger.openOutput();
//            System.out.println(order.toString());
//            StandardOutputChanger.closeOutput();
            writer.println("You made an BUY order using " + amountToBuy + " BTC with price:" + price + order.toString());
            writer.flush();
        } catch (BinanceApiException e) {
            StandardOutputChanger.openOutput();
            //e.printStackTrace();
            System.out.println("Something went wrong. ERROR:" + e.getMessage());
            StandardOutputChanger.closeOutput();
        }
    }

    private void bitcoinSellOrder() {
        try {
            BinanceSymbol symbol = BinanceSymbol.USDT("BTC");
            BinanceOrderPlacement placement = new BinanceOrderPlacement(symbol, BinanceOrderSide.SELL);
            placement.setPrice(minSellPrice);
            if (firstSellMove) {
                placement.setPrice(lastBTCprice.setScale(2, RoundingMode.FLOOR));
            }
            BigDecimal amountToSell = getFreeBitcoins();
            amountToSell = amountToSell.setScale(5, RoundingMode.FLOOR);
//            StandardOutputChanger.openOutput();
//            System.out.println(amountToSell);
//            StandardOutputChanger.closeOutput();
            amountToSell = amountToSell.round(new MathContext(4, RoundingMode.FLOOR));
            System.out.println(amountToSell);

            placement.setQuantity(amountToSell.setScale(5, RoundingMode.FLOOR)); // buy 10000 of asset for 0.00001 BTC


            BinanceOrder order = binanceApi.getOrderById(symbol, binanceApi.createOrder(placement).get("orderId").getAsLong());
            System.out.println(order.toString());
            StandardOutputChanger.openOutput();
            writer.println("You made an order using " + amountToSell + " BTC with price: " + placement.getPrice() + " " + order.toString());
            StandardOutputChanger.closeOutput();
            writer.flush();
            //writer.println("You sold " + balances.get(0).getAsJsonObject().get("free") + " bitcoins with price: " + lastBTCprice);

        } catch (BinanceApiException e) {
            StandardOutputChanger.openOutput();
            System.out.println("Something went wrong. Maybe you got your money in USDT, not BTC? I continue. ERROR:" + e.getMessage());
            //e.printStackTrace();
            StandardOutputChanger.closeOutput();
        }
    }

    private void tradeAction() {
        if (toDrop) {
            //trzeba sprzedać i potem odkupić taniej
            dropStrategy();
        } else if (!toDrop) {
            //trzeba kupić i odsprzedać drożej
            riseStrategy();
        }
    }

    private void dropStrategy() {
//        BigDecimal sum = new BigDecimal(100).subtract(percentOfDropToBuyBTC);
//        BigDecimal hundred = new BigDecimal(100);
//        BigDecimal part = sum.divide(hundred);
        minSellPrice = lastBTCprice;//.multiply(part).setScale(2,RoundingMode.FLOOR);
        //minSellPrice = lastBTCprice.multiply((percentOfDropToBuyBTC.add(new BigDecimal(100)).divide(new BigDecimal(100), new MathContext(2,RoundingMode.FLOOR))));
        //minBuyPrice = lastBTCprice.multiply((new BigDecimal(100).add(percentOfUpToSellBTC)).divide(new BigDecimal(100), new MathContext(2,RoundingMode.FLOOR)));
        //minSellPrice = minSellPrice.round(new MathContext(2,RoundingMode.FLOOR));
        BigDecimal sum = new BigDecimal(100).subtract(percentOfDropToBuyBTC);
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal part = sum.divide(hundred);
        minBuyPrice = lastBTCprice.multiply(part).setScale(2, RoundingMode.FLOOR);

        if (firstSellMove) {
            System.out.println("Trying to SELL BTC with price: " + lastBTCprice);
            bitcoinSellOrder();
            firstSellMove = false;
            return;
        }
        firstSellMove = false;
        int i = 0;
        StandardOutputChanger.openOutput();
        System.out.println("Trying to BUY BTC with price: " + minBuyPrice);
        StandardOutputChanger.closeOutput();
        while (true) {
            StandardOutputChanger.openOutput();
            // System.out.println("Trying to BUY BTC with price: "+minBuyPrice);
            System.out.print(".");
            sleepASecond();
            i++;
            if (i % 80 == 0) {
                System.out.println(" ");
                if (i % 800 == 0)
                    System.out.println("Trying to BUY BTC with price: " + minBuyPrice);
            }
            StandardOutputChanger.closeOutput();

            if (lastBTCprice.compareTo(minBuyPrice) <= 0) {

                bitcoinBuyOrder();
                toDrop = false;
                break;
            }
            lastBTCpriceUpdate();
        }
    }

    private void sleepASecond() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void riseStrategy() {
        firstSellMove = false;
        // minBuyPrice = lastBTCprice.multiply((new BigDecimal(100).add(percentOfUpToSellBTC)).divide(new BigDecimal(100), new MathContext(2,RoundingMode.FLOOR)));
        BigDecimal sum = new BigDecimal(100).add(percentOfUpToSellBTC);
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal part = sum.divide(hundred);
        minSellPrice = lastBTCprice.multiply(part).setScale(2, RoundingMode.FLOOR);
        //minSellPrice = minSellPrice.round(new MathContext(2,RoundingMode.FLOOR));
//        if(firstSellMove){
//            bitcoinSellOrder();
//            firstSellMove = false;
//        }
        int i = 0;
        StandardOutputChanger.openOutput();
        System.out.println("Trying to SELL BTC with price: " + minSellPrice);
        StandardOutputChanger.closeOutput();
        while (true) {
            StandardOutputChanger.openOutput();
//            System.out.println("LAST BTC PRICE:"+lastBTCprice);
//            System.out.println("PART: "+part);
//            System.out.println("MINSELLPRICE:"+minSellPrice);
            System.out.print(".");
            sleepASecond();
            i++;
            if (i % 80 == 0) {
                System.out.println(" ");
                if (i % 800 == 0) {
                    System.out.println("Trying to SELL BTC with price: " + minSellPrice);
                }
            }
            //System.out.println("Trying to SELL BTC with price: "+minSellPrice);
            StandardOutputChanger.closeOutput();
            if (lastBTCprice.compareTo(minSellPrice) >= 0) {
                //sprzedajemy
                bitcoinSellOrder();
                toDrop = true;
                break;
            }
            lastBTCpriceUpdate();
        }
        firstSellMove = false;
    }


}
