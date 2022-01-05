package Server;

import Exceptions.*;
import Model.Model;
import Utils.City;
import Utils.TaggedConnection.Frame;
import Utils.TaggedConnection;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
                model.addClient("admin", "admin");
            } catch (EmailAlreadyExistsException ignored) {}
        }

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

                            }
                            case 1 -> { // Registration attempts

                                System.out.println("Registration attempt.");
                                String email = f.username;
                                String password = new String(f.data);

                                String answer;

                                try {
                                    model.addClient(email, password);
                                    answer = "Success";

                                } catch (EmailAlreadyExistsException e) {
                                    answer = "Error";
                                }

                                connection.send(f.tag, "", answer.getBytes());

                                if (answer.equals("Success")) serialize(model);

                            }
                            case 2 -> { // Add flight information

                                System.out.println("Adding flight attempt.");

                                String answer = "Error";

                                if (f.username.equals("admin")) {

                                    String flightData = new String(f.data);
                                    String[] flightDataParsed = flightData.split(" ");

                                    try {
                                        answer = model.createFlight(Integer.parseInt(flightDataParsed[2]), 0, City.valueOf(flightDataParsed[0].toUpperCase()), City.valueOf(flightDataParsed[1].toUpperCase()), true, LocalDate.parse(flightDataParsed[3]));
                                    } catch (UnavailableFlightException | IllegalArgumentException e) {
                                        answer = "Error";
                                    }

                                }

                                connection.send(f.tag, f.username, answer.getBytes());

                                if (!answer.equals("Error")) serialize(model);

                            }
                            case 3 -> { // Close a day

                                System.out.println("Closing a day attempt.");

                                String answer;
                                String date = new String(f.data);

                                try {
                                    model.addClosedDay(LocalDate.parse(date));
                                    System.out.println(date);
                                    answer = "Success";
                                } catch (DateTimeParseException | AlreadyIsAClosedDay e) {
                                    answer = "Error";
                                }

                                connection.send(f.tag, f.username, answer.getBytes());

                                if (answer.equals("Success")) serialize(model);

                            }
                            case 4 -> { // Make reservation for a trip by flight ID

                                System.out.println("Reservation attempt by ids.");

                                String path = new String(f.data);
                                String[] pathParsed = path.split(" ");
                                Set<String> pathSet = new HashSet<>(Arrays.asList(pathParsed));
                                String answer;

                                try {
                                    answer = model.createReservation(f.username, pathSet);
                                } catch (FlightDoesntExistException | UnavailableFlightException | FlightAlreadyDeparted e) {
                                    e.printStackTrace();
                                    answer = "Error";
                                }

                                connection.send(4, f.username, answer.getBytes());

                                if (!answer.equals("Error")) serialize(model);

                            }
                            case 5 -> { // Make reservation by cities

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

                                    answer = model.createReservationGivenCities(f.username, cities, beginDate, endDate);
                                    if (answer.equals("")) answer = "Error";

                                }

                                connection.send(5, f.username, answer.getBytes());

                                if (!answer.equals("Error")) serialize(model);

                            }
                            case 6 -> { // Cancel reservation

                                System.out.println("Cancellation attempt.");

                                String username = f.username;
                                String id = new String(f.data);
                                String answer;

                                try {
                                    model.removeReservationByClient(id, username);
                                    answer = "Success";
                                } catch (DoesntExistReservationFromClient e) {
                                    answer = "Error";
                                }

                                connection.send(6, f.username, answer.getBytes());

                                if (answer.equals("Success")) serialize(model);

                            }
                            case 7 -> { // List all flights

                                System.out.println("Listing all flights attempt.");

                                connection.send(7, f.username, model.getFlightsString().getBytes());

                            }
                            case 8 -> { // List flights in a date

                                System.out.println("Listing all flights in a date attempt.");
                                String answer;
                                String date = new String(f.data);
                                try {
                                    answer = model.getFlightsStringInDate(LocalDate.parse(date));
                                } catch (DateTimeParseException e) {
                                    answer = "Error";
                                }

                                connection.send(8, f.username, answer.getBytes());

                            }
                            case 9 -> {

                                System.out.println("Listing all reservation from user.");

                                connection.send(9, f.username, model.getReservationsStringFromUser(f.username).getBytes());

                            }
                            case 10 -> {

                                System.out.println("Removing a closed day.");

                                String answer;
                                String dateS = new String(f.data);
                                try {
                                    LocalDate date = LocalDate.parse(dateS);
                                    model.removeClosedDay(date);
                                    answer = "Success";

                                } catch (NotAClosedDay | DateTimeParseException e) {
                                    answer = "Error";
                                }

                                connection.send(10, f.username, answer.getBytes());

                                if (answer.equals("Success")) serialize(model);
                            }
                            case 11 -> {

                                System.out.println("Listing the closed days attempt.");

                                connection.send(11, f.username, model.getClosedDays().getBytes());

                            }
                            case 12 -> {

                                System.out.println("Marking that flight has left attempt.");

                                String answer;

                                try {
                                    String id = new String(f.data);
                                    model.setFlightAsTakenOff(id);
                                    answer = "Success";
                                } catch (FlightDoesntExistException | FlightAlreadyDeparted e) {
                                    answer = "Error";
                                }

                                connection.send(12, f.username, answer.getBytes());
                                if(answer.equals("Success")) serialize(model);

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
