package UI;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Scanner;

public class UI {

    private Scanner scanner = new Scanner(System.in);

    public UI(){
    }

    public String clientMenu() {
        System.out.println("----------------------------------------------------------------");
        System.out.println("|               Welcome to Receeding Airline!                  |");
        System.out.println("| 1. User login.                                               |");
        System.out.println("| 2. User registration                                         |");
        System.out.println("| 3. Admin login.                                              |");
        System.out.println("----------------------------------------------------------------");
        System.out.println("Insert option: ");

        return scanner.nextLine();
    }

    public Map.Entry<String,String> login() {
        System.out.println("Insert username: ");
        String username = scanner.nextLine();
        System.out.println("Insert password: ");
        String password = scanner.nextLine();

        return new AbstractMap.SimpleEntry<>(username, password);
    }

    public void wrongOption() {
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
