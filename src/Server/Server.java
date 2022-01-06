package Server;

import Exceptions.*;
import Model.Model;
import Utils.AESEncrypt;
import Utils.City;
import Utils.Log;
import Utils.TaggedConnection.Frame;
import Utils.TaggedConnection;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Class that allows running an instance of a Server
 */
public class Server {

    /**
     * Allows serializing the model to a file
     * @param model Model
     */
    public static void serialize(Model model) {
        model.l.writeLock().lock();
        try {
            System.out.println("Serializing...");
            model.serialize("model.ser");
            System.out.println("Serialized.");
        } catch (IOException e) {
            System.out.println("Error serializing.");
        } finally {
            model.l.writeLock().unlock();
        }
    }

    /**
     * Allows running the server
     * @param args Program arguments
     * @throws IOException Exception that comes from opening a server socket
     * @throws ClassNotFoundException Exception that comes from deserializing
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        ServerSocket ss = new ServerSocket(12345);
        Model model;

        /*

                Gets data saved from file

         */

        File file = new File("model.ser");
        if (file.exists())
            model = Model.deserialize("model.ser");
        else {
            model = new Model();
            try {
                model.addClient("admin@highfly.pt", AESEncrypt.encrypt("admin"));
            } catch (EmailAlreadyExistsException | NotAnEmailException ignored) {}
        }

        Log log = new Log();
        log.appendSeparator(LocalDateTime.now());

        while(true) {

            /*

                Creates one connection per client and handles it in a thread

             */
            Socket s = ss.accept();
            TaggedConnection connection = new TaggedConnection(s);

            Runnable worker = () -> {

                try (connection) {

                    while (true) {

                        Frame f = connection.receive();

                        switch (f.tag) {

                            case 0 -> { // Login attempt

                                System.out.println("Login attempt.");
                                String answer = "Error";

                                if (model.checkAuthentication(f.username, new String(f.data)))
                                    answer = "Success";

                                connection.send(f.tag, "", answer.getBytes());

                                if (answer.equals("Success")) log.appendMessage("Logged in: " + f.username);

                            }
                            case 1 -> { // Registration attempts

                                System.out.println("Registration attempt.");
                                String email = f.username;
                                String password = new String(f.data);

                                String answer;

                                try {
                                    model.addClient(email, password);
                                    answer = "Success";

                                } catch (EmailAlreadyExistsException | NotAnEmailException e) {
                                    answer = "Error";
                                }

                                connection.send(f.tag, "", answer.getBytes());

                                if (answer.equals("Success")) {
                                    serialize(model);
                                    log.appendMessage("Registered new user: " + f.username);
                                }

                            }
                            case 2 -> { // Add flight information

                                System.out.println("Adding flight attempt.");

                                String answer;

                                String flightData = new String(f.data);
                                String[] flightDataParsed = flightData.split(" ");

                                try {
                                    answer = model.createFlight(Integer.parseInt(flightDataParsed[2]), City.valueOf(flightDataParsed[0].toUpperCase()), City.valueOf(flightDataParsed[1].toUpperCase()));
                                } catch (IllegalArgumentException e) {
                                    answer = "Error";
                                }

                                connection.send(f.tag, f.username, answer.getBytes());

                                if (!answer.equals("Error")) {
                                    serialize(model);
                                    log.appendMessage("Added flight with ID " + answer + " by " + f.username);
                                }

                            }
                            case 3 -> { // Close a day

                                System.out.println("Closing a day attempt.");

                                String answer;
                                String date = new String(f.data);

                                try {
                                    model.addClosedDay(LocalDate.parse(date));
                                    answer = "Success";
                                } catch (AlreadyIsAClosedDayException | DateTimeParseException e) {
                                    answer = "Error";
                                }

                                connection.send(f.tag, f.username, answer.getBytes());

                                if (answer.equals("Success")) {
                                    serialize(model);
                                    log.appendMessage("Closed day: " + date + " by " + f.username);
                                }

                            }
                            case 4 ->  // Make reservation for a trip by flight ID

                                new Thread(()-> {

                                    System.out.println("Reservation attempt by ids.");

                                    String path = new String(f.data);
                                    String[] pathAndDateParsed = path.split(";");
                                    String[] pathParsed = pathAndDateParsed[0].split(" ");
                                    String dateS = pathAndDateParsed[1];
                                    Set<String> pathSet = new HashSet<>(Arrays.asList(pathParsed));
                                    String answer;


                                    try {
                                        LocalDate date = LocalDate.parse(dateS);
                                        answer = model.createReservation(f.username, pathSet, date);
                                    } catch (FlightDoesntExistException | UnavailableFlightException | IllegalArgumentException | OnlyClosedDaysException | DayHasPassedException e) {
                                        answer = "Error";
                                    }

                                    try {
                                        connection.send(4, f.username, answer.getBytes());
                                    } catch(IOException e) {
                                        System.out.println("Error sending.");
                                    }

                                    if (!answer.equals("Error")) {
                                        serialize(model);
                                        log.appendMessage("User " + f.username + "made a reservation. Code: " + answer);
                                    }

                                }).start();

                            case 5 -> // Make reservation by cities
                                new Thread(()-> {
                                    System.out.println("Reservation attempt by cities.");

                                    String pathAndDates = new String(f.data);
                                    String[] pathAndDatesParsed = pathAndDates.split(";");
                                    String[] dates = pathAndDatesParsed[1].split(" ");
                                    String[] pathParsed = pathAndDatesParsed[0].split(" ");

                                    boolean validReservation = true;

                                    List<City> cities = new ArrayList<>();
                                    for (String city : pathParsed) {
                                        try {
                                            City c = City.valueOf(city.toUpperCase());
                                            cities.add(c);
                                        } catch (Exception e) {
                                            validReservation = false;
                                        }
                                    }

                                    LocalDate beginDate = null, endDate = null;
                                    try {
                                        beginDate = LocalDate.parse(dates[0]);
                                        endDate = LocalDate.parse(dates[1]);
                                    } catch (DateTimeParseException e) {
                                        validReservation = false;
                                    }

                                    String answer = "Error";

                                    if (validReservation) {

                                        try {
                                            answer = model.createReservationGivenCities(f.username, cities, beginDate, endDate);
                                        } catch (OnlyClosedDaysException | UnavailableFlightException  | FlightDoesntExistException | DayHasPassedException e) {
                                            answer = "Error";
                                        }

                                    }

                                    try {
                                        connection.send(5, f.username, answer.getBytes());
                                    } catch (IOException e) {
                                        System.out.println("Error sending");
                                    }

                                    if (!answer.equals("Error")) {
                                        serialize(model);
                                        log.appendMessage("User " + f.username + "made a reservation. Code: " + answer);
                                    }

                                }).start();

                            case 6 -> { // Cancel reservation

                                System.out.println("Cancellation attempt.");

                                String username = f.username;
                                String id = new String(f.data);
                                String answer;

                                try {
                                    model.removeReservationByClient(id, username);
                                    answer = "Success";
                                } catch (DoesntExistReservationFromClientException e) {
                                    answer = "Error";
                                }

                                connection.send(6, f.username, answer.getBytes());

                                if (answer.equals("Success")) {
                                    serialize(model);
                                    log.appendMessage("User " + f.username + " cancelled reservation with code " + id);
                                }

                            }
                            case 7 -> { // List all flights

                                System.out.println("Listing all flights attempt.");

                                connection.send(7, f.username, model.getFlightsString().getBytes());

                                log.appendMessage("Sent flights listing to user " + f.username);

                            }
                            case 8 -> { // Get list of routes between 2 cities

                                System.out.println("Getting list of routes.");

                                String cities = new String(f.data);
                                String origin = cities.split(" ")[0];
                                String destination = cities.split(" ")[1];

                                String answer;
                                try {
                                    answer = model.getRoutes(City.valueOf(origin), City.valueOf(destination));
                                } catch (IllegalArgumentException e) {
                                    answer = "Error";
                                }

                                connection.send(8, f.username, answer.getBytes());

                                log.appendMessage("Sent available routes from " + origin + " to " + destination);
                            }
                            case 9 -> {

                                System.out.println("Listing all reservation from user.");

                                connection.send(9, f.username, model.getReservationsStringFromUser(f.username).getBytes());

                                log.appendMessage("Sent list of reservations made by the user to " + f.username);

                            }
                            case 10 -> {

                                System.out.println("Removing a closed day.");

                                String answer;
                                String dateS = new String(f.data);
                                try {
                                    LocalDate date = LocalDate.parse(dateS);
                                    model.removeClosedDay(date);
                                    answer = "Success";

                                } catch (NotAClosedDayException | DateTimeParseException e) {
                                    answer = "Error";
                                }

                                connection.send(10, f.username, answer.getBytes());

                                if (answer.equals("Success")) {
                                    serialize(model);
                                    log.appendMessage("Removed closed day " + dateS + " by " + f.username);
                                }
                            }
                            case 11 -> {

                                System.out.println("Listing the closed days attempt.");

                                connection.send(11, f.username, model.getClosedDays().getBytes());

                                log.appendMessage("Sent list of closed days to " + f.username);

                            }
                            default -> {}
                        }
                    }

                } catch (Exception ignored) {}

            };

            Thread t = new Thread(worker);
            t.start();

        }
    }
}
