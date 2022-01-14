package Client;

import Exceptions.NoNoticesException;
import Utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Class that represents a client. Needs an instance of Server running to work
 */
public class Client {

    /**
     * Runs the client
     * @param args Program arguments
     * @throws IOException I/O errors from socket manipulation
     * @throws InterruptedException Error from receiving through demultiplexer
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        Socket s = new Socket("localhost", 12345);
        Demultiplexer dm = new Demultiplexer(new TaggedConnection(s));

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        Notices notices = new Notices();
        Thread receiveReservationAnswer = null;

        dm.start();

        boolean quit = false;
        while (!quit) {

            /*

                    Initial Menu: Login + Registration

             */
            System.out.println(Colors.ANSI_GREEN + "\n\n\n" +
                    "______  ______        ______       _______________            \n" +
                    "___  / / /__(_)______ ___  /_      ___  ____/__  /____  __    \n" +
                    "__  /_/ /__  /__  __ `/_  __ \\     __  /_   __  /__  / / /    \n" +
                    "_  __  / _  / _  /_/ /_  / / /     _  __/   _  / _  /_/ /     \n" +
                    "/_/ /_/  /_/  _\\__, / /_/ /_/      /_/      /_/  _\\__, /      \n" +
                    "              /____/                             /____/       \n" +
                    "\n" + Colors.ANSI_RESET);

            System.out.println(Colors.ANSI_PURPLE + "**************************** Welcome ****************************\n" + Colors.ANSI_RESET);
            System.out.println(Colors.ANSI_CYAN + "1. " + Colors.ANSI_RESET + "Login.");
            System.out.println(Colors.ANSI_CYAN + "2. " + Colors.ANSI_RESET + "Registration.");
            System.out.println(Colors.ANSI_CYAN + "0. " + Colors.ANSI_RESET + "Quit.");
            System.out.print(Colors.ANSI_YELLOW + "\nInsert option: " + Colors.ANSI_RESET);

            String option = input.readLine();
            switch (option) {
                case "1" -> { // Login

                /*

                        Login Menu

                 */

                    System.out.println(Colors.ANSI_GREEN + "\n********** Login **********\n" + Colors.ANSI_RESET);
                    System.out.print(Colors.ANSI_YELLOW + "Insert email: " + Colors.ANSI_RESET);
                    String username = input.readLine();
                    System.out.print(Colors.ANSI_YELLOW + "Insert password: " + Colors.ANSI_RESET);
                    String password = input.readLine();

                    dm.send(0, username, AESEncrypt.encrypt(password).getBytes());

                    String answerLogin = new String(dm.receive(0));
                    if (answerLogin.equals("Success")) {

                        boolean homeMenuQuit = false;
                        while (!homeMenuQuit) {

                            if (username.equals("admin@highfly.pt")) {

                            /*

                                    Admin home menu

                             */

                                System.out.println(Colors.ANSI_GREEN + "\n********** Admin Home Menu **********\n" + Colors.ANSI_RESET);
                                System.out.println(Colors.ANSI_CYAN + "1. " + Colors.ANSI_RESET + "Add flight.");
                                System.out.println(Colors.ANSI_CYAN + "2. " + Colors.ANSI_RESET + "Add closed day.");
                                System.out.println(Colors.ANSI_CYAN + "3. " + Colors.ANSI_RESET + "Remove closed day.");
                                System.out.println(Colors.ANSI_CYAN + "4. " + Colors.ANSI_RESET + "Listing of the closed days.");
                                System.out.println(Colors.ANSI_CYAN + "5. " + Colors.ANSI_RESET + "Listing of all flights.");
                                System.out.println(Colors.ANSI_CYAN + "0. " + Colors.ANSI_RESET + "Quit.");
                                System.out.print(Colors.ANSI_YELLOW + "\nInsert option: " + Colors.ANSI_RESET);

                                option = input.readLine();
                                switch (option) {
                                    case "1" -> {

                                        System.out.println(Colors.ANSI_GREEN + "\n********** Flight **********\n" + Colors.ANSI_RESET);
                                        System.out.print(Colors.ANSI_YELLOW + "Insert origin: " + Colors.ANSI_RESET);
                                        String origin = input.readLine();
                                        System.out.print(Colors.ANSI_YELLOW + "Insert destination: " + Colors.ANSI_RESET);
                                        String destination = input.readLine();
                                        System.out.print(Colors.ANSI_YELLOW + "Insert max number of passengers: " + Colors.ANSI_RESET);
                                        String max = input.readLine();

                                        byte[] flight = String.format("%s %s %s", origin, destination, max).getBytes();
                                        dm.send(2, username, flight);

                                        String answerFlightAdd = new String(dm.receive(2));
                                        if (!answerFlightAdd.equals("Error"))
                                            System.out.println(Colors.ANSI_PURPLE + "\nFlight successfully added with code " + Colors.ANSI_RESET + answerFlightAdd);
                                        else
                                            System.out.println(Colors.ANSI_RED + "\nError in inputs inserted." + Colors.ANSI_RESET);

                                    }
                                    case "2" -> { // Close day

                                        System.out.println(Colors.ANSI_GREEN + "\n********** Close day **********\n" + Colors.ANSI_RESET);
                                        System.out.print(Colors.ANSI_YELLOW + "Insert date (yyyy-mm-dd): " + Colors.ANSI_RESET);
                                        String date = input.readLine();

                                        if (date.equals("")) {
                                            System.out.println(Colors.ANSI_PURPLE + "Operation canceled." + Colors.ANSI_RESET);
                                        } else {

                                            dm.send(3, username, date.getBytes());

                                            String closingDayAnswer = new String(dm.receive(3));
                                            if (closingDayAnswer.equals("Success"))
                                                System.out.println(Colors.ANSI_PURPLE + "\nDay successfully closed." + Colors.ANSI_RESET);
                                            else
                                                System.out.println(Colors.ANSI_RED + "\nAction could not be performed." + Colors.ANSI_RESET);

                                        }

                                    }
                                    case "3" -> {

                                        System.out.println(Colors.ANSI_GREEN + "\n********** Removing closed day **********\n" + Colors.ANSI_RESET);
                                        System.out.print(Colors.ANSI_YELLOW + "Insert date (yyyy-mm-dd): " + Colors.ANSI_RESET);
                                        String date = input.readLine();

                                        dm.send(10, username, date.getBytes());

                                        String answer = new String(dm.receive(10));
                                        if (answer.equals("Success"))
                                            System.out.println(Colors.ANSI_PURPLE + "\nClosed day removed." + Colors.ANSI_RESET);
                                        else
                                            System.out.println(Colors.ANSI_RED + "\nAction could not be performed." + Colors.ANSI_RESET);
                                    }
                                    case "4" -> {
                                        dm.send(11, username, new byte[0]);

                                        String listingClosedDays = new String(dm.receive(11));

                                        System.out.println(Colors.ANSI_GREEN + "\n********** Close day listing **********\n" + Colors.ANSI_RESET);
                                        System.out.println(listingClosedDays);
                                        System.out.print(Colors.ANSI_PURPLE + "\nEnter any key to proceed. " + Colors.ANSI_RESET);
                                        input.readLine();

                                    }
                                    case "5" -> {
                                        dm.send(7, username, new byte[0]);
                                        String data = new String(dm.receive(7));
                                        System.out.println(Colors.ANSI_GREEN + "\n********** Flights **********\n" + Colors.ANSI_RESET);
                                        System.out.println(data);
                                        System.out.print(Colors.ANSI_PURPLE + "\nPress any key to proceed. " + Colors.ANSI_RESET);
                                        input.readLine();
                                    }
                                    case "0" -> homeMenuQuit = true;
                                    default -> System.out.println(Colors.ANSI_RED + "\nInvalid option." + Colors.ANSI_RESET);
                                }

                            } else {

                            /*

                                    Client home menu

                             */

                                /*
                                    Thread that receives results of reservations
                                 */
                                if (receiveReservationAnswer == null) {

                                    receiveReservationAnswer = new Thread( () -> {
                                        boolean run = true;
                                        while (run) {
                                            try {
                                                String reservationAnswer = new String(dm.receive(4));
                                                notices.addNotice(reservationAnswer);
                                            } catch (InterruptedException e) {
                                                run = false;
                                            } catch (IOException ignored) {}
                                        }
                                    });
                                    receiveReservationAnswer.start();

                                }


                                /*
                                    When the client enter the home menu, checks if there are any notification pending. If so, prints them and deletes them.
                                 */
                                try {
                                    String noticesString = notices.displayPendingNotices();
                                    System.out.println(Colors.ANSI_GREEN + "\n********** Notifications **********\n" + Colors.ANSI_RESET);
                                    System.out.println(noticesString);
                                } catch (NoNoticesException ignored) {}

                                System.out.println(Colors.ANSI_GREEN + "\n********** Home Menu **********\n" + Colors.ANSI_RESET);
                                System.out.println(Colors.ANSI_CYAN + "1. " + Colors.ANSI_RESET + "Make a reservation.");
                                System.out.println(Colors.ANSI_CYAN + "2. " + Colors.ANSI_RESET + "Get existing flights.");
                                System.out.println(Colors.ANSI_CYAN + "3. " + Colors.ANSI_RESET + "Remove reservation.");
                                System.out.println(Colors.ANSI_CYAN + "4. " + Colors.ANSI_RESET + "Get list of reservations made.");
                                System.out.println(Colors.ANSI_CYAN + "5. " + Colors.ANSI_RESET + "Get possible routes between two cities.");
                                System.out.println(Colors.ANSI_CYAN + "0. " + Colors.ANSI_RESET + "Quit.");
                                System.out.print(Colors.ANSI_YELLOW + "\nInsert option: " + Colors.ANSI_RESET);

                                option = input.readLine();
                                switch (option) {
                                    case "1" -> {  // Make a reservation

                                        boolean reservationMenuQuit = false;
                                        while (!reservationMenuQuit) {
                                            System.out.println(Colors.ANSI_GREEN + "\n********** Reservation **********\n" + Colors.ANSI_RESET);
                                            System.out.println(Colors.ANSI_CYAN + "1. " + Colors.ANSI_RESET + "Use flight IDS.");
                                            System.out.println(Colors.ANSI_CYAN + "2. " + Colors.ANSI_RESET + "Use cities.");
                                            System.out.println(Colors.ANSI_CYAN + "0. " + Colors.ANSI_RESET + "Quit.");
                                            System.out.print(Colors.ANSI_YELLOW + "\nInsert option: " + Colors.ANSI_RESET);

                                            String optionReservation = input.readLine();
                                            switch(optionReservation) {
                                                case "1" -> {

                                                    StringBuilder flights = new StringBuilder();
                                                    boolean finishedList = false;
                                                    while (!finishedList) {
                                                        System.out.print(Colors.ANSI_YELLOW + "Insert flight ID (or press enter to stop): " + Colors.ANSI_RESET);
                                                        String flightIDInserted = input.readLine();
                                                        if (flightIDInserted.equals(""))
                                                            finishedList = true;
                                                        else
                                                            flights.append(flightIDInserted).append(" ");
                                                    }

                                                    System.out.print(Colors.ANSI_YELLOW + "Insert date (yyyy-mm-dd): " + Colors.ANSI_RESET);
                                                    String date = input.readLine();

                                                    if (!date.equals("") && !flights.isEmpty()) {

                                                        flights.append(";").append(date);

                                                        dm.send(4, username, flights.toString().getBytes());

                                                    } else
                                                        System.out.println(Colors.ANSI_PURPLE + "\nOperation canceled." + Colors.ANSI_RESET);

                                                }
                                                case "2" -> {

                                                    StringBuilder cities = new StringBuilder();
                                                    boolean finishedList = false;
                                                    while(!finishedList) {
                                                        System.out.print(Colors.ANSI_YELLOW + "Insert city (or press enter to stop): ");
                                                        String city = input.readLine();
                                                        if (city.equals("")) finishedList = true;
                                                        else cities.append(city).append(" ");
                                                    }

                                                    System.out.print(Colors.ANSI_YELLOW + "\nInsert beginning date (yyyy-mm-dd): ");
                                                    String beginDate = input.readLine();
                                                    System.out.print(Colors.ANSI_YELLOW + "Insert ending date (yyyy-mm-dd): ");
                                                    String endDate = input.readLine();

                                                    if (!cities.isEmpty() && !beginDate.equals("") && !endDate.equals("")) {

                                                        cities.append(";").append(beginDate).append(" ").append(endDate);

                                                        dm.send(5, username, cities.toString().getBytes());

                                                    } else
                                                        System.out.println(Colors.ANSI_PURPLE + "\nOperation canceled" + Colors.ANSI_RESET);


                                                }
                                                case "0" -> reservationMenuQuit = true;
                                                default -> System.out.println(Colors.ANSI_RED + "\nInvalid option." + Colors.ANSI_RESET);
                                            }
                                        }




                                    }
                                    case "2" -> {

                                        dm.send(7, username, new byte[0]);
                                        String data = new String(dm.receive(7));
                                        System.out.println(Colors.ANSI_GREEN + "\n********** Flights **********\n" + Colors.ANSI_RESET);
                                        System.out.println(data);
                                        System.out.print(Colors.ANSI_PURPLE + "\nPress enter to proceed:  " + Colors.ANSI_RESET);
                                        input.readLine();

                                    }
                                    case "3" -> {  // Remove reservation

                                        System.out.println(Colors.ANSI_GREEN + "\n********** Removing Reservations **********\n" + Colors.ANSI_RESET);
                                        System.out.print(Colors.ANSI_YELLOW + "Insert reservation code: " + Colors.ANSI_RESET);
                                        String reservationCode = input.readLine();
                                        dm.send(6, username, reservationCode.getBytes());
                                        String answer = new String(dm.receive(6));
                                        if (answer.equals("Success"))
                                            System.out.println(Colors.ANSI_PURPLE + "\nReservation removed with success." + Colors.ANSI_RESET);
                                        else
                                            System.out.println(Colors.ANSI_PURPLE + "\nReservation could not be removed." + Colors.ANSI_RESET);
                                    }
                                    case "4" -> { // Get listing

                                        dm.send(9, username, new byte[0]);
                                        String data = new String(dm.receive(9));
                                        System.out.println(Colors.ANSI_GREEN + "\n********** Reservations made **********\n" + Colors.ANSI_RESET);
                                        System.out.println(data);
                                        System.out.print(Colors.ANSI_PURPLE + "\nPress enter to proceed: " + Colors.ANSI_RESET);
                                        input.readLine();
                                    }
                                    case "5" -> {

                                        System.out.println(Colors.ANSI_GREEN + "\n********** Possible routes **********\n" + Colors.ANSI_RESET);
                                        System.out.print(Colors.ANSI_YELLOW + "Insert origin: " + Colors.ANSI_RESET);
                                        String origin = input.readLine();
                                        System.out.print(Colors.ANSI_YELLOW + "Insert destination: " + Colors.ANSI_RESET);
                                        String destination = input.readLine();

                                        dm.send(8, username, (origin.toUpperCase() + " " + destination.toUpperCase()).getBytes());

                                        String answer = new String(dm.receive(8));
                                        if (answer.equals("Error"))
                                            System.out.println(Colors.ANSI_RED + "\nError occurred." + Colors.ANSI_RESET);
                                        else {
                                            System.out.println("\n" + answer);
                                            System.out.print(Colors.ANSI_PURPLE + "\nPress enter to proceed: " + Colors.ANSI_RESET);
                                            input.readLine();
                                        }

                                    }
                                    case "0" -> homeMenuQuit = true;
                                    default -> System.out.println(Colors.ANSI_RED + "\nInvalid option." + Colors.ANSI_RESET);
                                }
                            }
                        }

                    } else
                        System.out.println(Colors.ANSI_RED + "\nUnknown credentials" + Colors.ANSI_RESET);

                }
                case "2" -> {

                /*

                        Registration Menu

                 */

                    System.out.println(Colors.ANSI_GREEN + "\n********** Registration **********\n" + Colors.ANSI_RESET);
                    System.out.print(Colors.ANSI_YELLOW + "Insert email: " + Colors.ANSI_RESET);
                    String username = input.readLine();
                    System.out.print(Colors.ANSI_YELLOW + "Insert password: " + Colors.ANSI_RESET);
                    String password = input.readLine();

                    dm.send(1, username, AESEncrypt.encrypt(password).getBytes());

                    String answerLogin = new String(dm.receive(1));
                    if (answerLogin.equals("Success"))
                        System.out.println(Colors.ANSI_PURPLE + "\nAccount successfully created." + Colors.ANSI_RESET);
                    else
                        System.out.println(Colors.ANSI_RED + "\nInvalid credentials." + Colors.ANSI_RESET);

                }
                case "0" -> {

                /*

                        Quiting program

                 */

                    quit = true;

                    if (receiveReservationAnswer != null) receiveReservationAnswer.interrupt();

                    System.out.println(Colors.ANSI_PURPLE + "\nExiting app..." + Colors.ANSI_RESET);
                    dm.close();
                }
                default -> System.out.println(Colors.ANSI_RED + "\nInvalid option." + Colors.ANSI_RESET);
            }

        }

    }

}
