package UI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Menu {

    public interface Handler {
        void execute();
    }

    public interface PreCondition {
        boolean validate();
    }

    private static Scanner is = new Scanner(System.in);

    private String title;
    private List<String> options;
    private List<PreCondition> preConditions;
    private List<Handler> handlers;

    public Menu() {
        this.title = "Menu";
        this.options = new ArrayList<>();
        this.preConditions = new ArrayList<>();
        this.handlers = new ArrayList<>();
    }

    public Menu(String title, List<String> options) {
        this.title = title;
        this.options = new ArrayList<>();
        this.options.addAll(options);
        this.preConditions = new ArrayList<>();
        this.handlers = new ArrayList<>();
        this.options.forEach(s -> {
            this.preConditions.add(() -> true);
            this.handlers.add(() -> System.out.println("\nOption not available."));
        });
    }

    public Menu(String title, String[] options) {
        this(title, Arrays.asList(options));
    }

    public void addOption(String name, PreCondition p, Handler h) {
        this.options.add(name);
        this.preConditions.add(p);
        this.handlers.add(h);
    }

    public void setPreConditions(int i, PreCondition p) {
        this.preConditions.set(i-1,p);
    }

    public void setHandler(int i, Handler h) {
        this.handlers.set(i-1,h);
    }

    public int readOption() {
        int op;

        System.out.print("Insert option: ");
        try {
            String line = is.nextLine();
            op = Integer.parseInt(line);

        } catch (NumberFormatException input) {
            op = -1;
        }

        if (op < 0 || op > this.options.size()) {
            op = -1;
            System.out.println("Invalid option.");
        }

        return op;
    }

    public void printMenu() {
        System.out.println(" *** " + this.title + " *** ");
        for(int i = 0; i < this.options.size(); i++) {
            System.out.print(i+1 + ". ");
            System.out.println(this.preConditions.get(i).validate() ? this.options.get(i) : " --- ");
        }
        System.out.println("0. Quit");
    }

    public void run() {
        int op;
        do {
            printMenu();
            op = readOption();
            if (op > 0 && !this.preConditions.get(op-1).validate())
                System.out.println("You don't have permission to access this.");
            else if (op > 0)
                this.handlers.get(op-1).execute();
        } while(op != 0);
    }


}
