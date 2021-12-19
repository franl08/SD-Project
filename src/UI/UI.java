package UI;

import Client.*;
import Model.Model;
import Model.Flight;
import Model.Reservation;
import Utils.City;

import java.time.LocalDate;
import java.util.*;

public class UI {

    private final Scanner scanner = new Scanner(System.in);
    private Model model;

    public UI(Model model){
        this.model = model;
    }

    public void run() {
        mainMenu();
    }

    public void mainMenu() {
        Menu menu = new Menu("** Welcome to Receeding Airline **", new String[] {
                "Client",
                "Admin"
        });

        menu.setHandler(1, this::clientMenu);
        menu.setHandler(2, () -> this.login(false));

        menu.run();
    }

    public void clientMenu() {
        Menu menu = new Menu("Receeding Airline", new String[] {
                "Login",
                "Registration"
        });

        menu.setHandler(1, () -> this.login(true));
        menu.setHandler(2, this::registration);

        menu.run();
    }

    public void login(boolean isClient) {
        System.out.print("Insert username: ");
        String username = scanner.nextLine();
        System.out.print("Insert password: ");
        String password = scanner.nextLine();

        if (this.model.checkAutentication(username,password)) {
            if (isClient) {
                clientLoggedInMenu();

            } else {

                adminLoggedInMenu();

            }
        } else
            System.out.println("Wrong credentials.");
    }

    public void registration() {
        System.out.print("Insert username: ");
        String username = scanner.nextLine();
        System.out.print("Insert email: ");
        String email = scanner.nextLine();
        System.out.print("Insert full name: ");
        String fullName = scanner.nextLine();
        System.out.print("Insert password: ");
        String password = scanner.nextLine();

        this.model.addUser(new Client(username,email,fullName,password));
    }

    public void clientLoggedInMenu() {
        Menu menu = new Menu("Receeding Airline", new String[] {
                "Get available flights",
                "Make reservation",
                "Cancel reservation"
        });

        menu.setHandler(1, this::getAvailableFlights);
        menu.setHandler(2, this::makeReservation);
        menu.setHandler(3, this::cancelReservation);

        menu.run();
    }

    public void getAvailableFlights() {
        Map<String,Flight> flights = this.model.getFlights();
        List<Map.Entry<String,Flight>> flightsList = new ArrayList<>(flights.entrySet());

        for (int i = 0; i < flightsList.size();) {

            for (int j = i; j < i+10 && j < flightsList.size(); j++) {
                Map.Entry<String,Flight> f = flightsList.get(j);
                Flight flight = f.getValue();
                System.out.println(f.getKey() + " | " + flight.getDate().toString() + " | " + flight.getOrigin() + " -> "
                        + flight.getDestination());
            }
            i = paginationOptions(i, flightsList.size(), 10);
            if (i == -1) break;
        }
    }

    public int paginationOptions(int i, int max, int variation) {

        System.out.println("Options: B -> Go back");
        System.out.println("         N -> Next page");
        System.out.println("         P -> Previous page");
        System.out.println("         Page number -> Go to a specific page");
        System.out.println("Insert option: ");
        String option = scanner.nextLine();

        if (option.equals("B"))
            i = -1;
        else if (option.equals("N"))
            i = (i < max - 1) ? i+variation : max - 1;
        else if (option.equals("P"))
            i = (i > 0) ? i-variation : 0;
        else {
            int op = Integer.parseInt(option) - 1;
            if (op < 0 || op > max - 1)
                i = op;
        }

        return i;
    }

    // TODO
    public void makeReservation() {

    }

    public void cancelReservation() {
        System.out.print("Insert reservation code: ");
        String code = scanner.nextLine();
        if (this.model.removeReservation(code))
            System.out.println("Reservation successfully removed.");
        else
            System.out.println("No reservation matches the code inserted.");
    }

    public void adminLoggedInMenu() {
        Menu menu = new Menu("Receeding Airline", new String[] {
                "Add flight information",
                "Close day"
        });

        menu.setHandler(1, this::addFlightInformation);
        menu.setHandler(2, this::closeDay);

        menu.run();
    }

    // TODO Convert String to City
    public void addFlightInformation() {
        System.out.print("Insert id");
        String id = scanner.nextLine();
        System.out.print("Insert the maximum number of passagers: ");
        String nMaxP = scanner.nextLine();
        System.out.print("Insert number of reservation: ");
        String nReserv = scanner.nextLine();
        System.out.print("Insert origin: ");
        String origS = scanner.nextLine();
        City orig;
        System.out.print("Insert destination: ");
        String destS = scanner.nextLine();
        City dest;
        System.out.print("Insert ?? :");
        String toGo = scanner.nextLine();
        System.out.print("Insert date: ");
        String date = scanner.nextLine();
        //this.model.addFlight(new Flight(id, Integer.parseInt(nMaxP), Integer.parseInt(nReserv), orig, dest, toGo.equals("true"), LocalDate.parse(date)));
    }

    public void closeDay() {
        System.out.print("Insert date: ");
        String date = scanner.nextLine();
        if (this.model.addClosedDay(LocalDate.parse(date)))
            System.out.println("Day succesfully closed.");
        else
            System.out.println("Error closing day.");
    }
}
