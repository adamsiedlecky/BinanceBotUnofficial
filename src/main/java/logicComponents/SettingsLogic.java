package logicComponents;

import messages.Messages;
import messages.CurrentLanguageFactory;

import java.util.Scanner;

public class SettingsLogic {

    private static String secretKey;
    private static String language;
    private Messages messages;
    private Scanner input;
    private CurrentLanguageFactory languageFabric;

    public SettingsLogic() {
        languageFabric = new CurrentLanguageFactory();
        messages = languageFabric.getCurrentLanguage();
    }

    public static String getLanguageParameter() {
        return language;
    }

    public void startSettings() {

        System.out.println(Messages.getBtcLogo());
        System.out.println("If you bought this program, enjoy it. If not, here is my BTC address: 1LuR1RcCuXCgfjeUKEpaNjusmEaembnUXT");
        System.out.println("Please sent even a little part of Bitcoin. I will really appreciate that :)");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Please select your language: \n P - Polish \n E - English");
        input = new Scanner(System.in);
        String in = input.next();
        while (!"p".equals(in) && !"P".equals(in) && !"e".equals(in) && !"E".equals(in)) {
            System.out.println("Incorrect value! Please enter again such value as: e OR p");
            in = input.next();

        }

        // Actually I don't got any better idea to write it better;  it is not good way to call same methods two or more times in code.
        if ("P".equals(in) || "p".equals(in)) {
            language = "Polish";
//             PolishMessages messages = new PolishMessages();
//            System.out.println(messages.getWelcome());
//            System.out.println(messages.getAskForSecretKey());
//            secretKey = input.next();

        } else if ("E".equals(in) || "e".equals(in)) {
            language = "English";
//             EnglishMessages messages = new EnglishMessages();
//            System.out.println(messages.getWelcome());
//            System.out.println(messages.getAskForSecretKey());
//            secretKey = input.next();
        }
        messages = languageFabric.getCurrentLanguage();
        System.out.println(messages.getWelcome());
        askForSecretKey();
    }


     void askForSecretKey() {

        System.out.println(messages.getAskForSecretKey());
        secretKey = input.next();

    }

      String getSecretKey() {
        return secretKey;
    }
}
