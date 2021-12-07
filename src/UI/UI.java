package UI;

import java.util.Scanner;

public class UI {

    Scanner scanner = new Scanner(System.in);

    public static void clientMenu() {
        System.out.println("----------------------------------------------------------------");
        System.out.println("|               Welcome to [NAME] Airline!                     |");
        System.out.println("| 1. User login.                                               |");
        System.out.println("| 2. User registration                                         |");
        System.out.println("| 3. Admin login.                                              |");
        System.out.println("|---------------------------------------------------------------");
    }

    public static void wrongOption() {
        System.out.println("Unavailable option. Try again.");
    }

    public int readOption(int maxOption) {
        int option;
        try {
            String input = scanner.nextLine();
            option = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            option = -1;
        }

        return option;
    }
}
