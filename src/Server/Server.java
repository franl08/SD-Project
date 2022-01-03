package Server;

import Exceptions.*;
import Model.Model;
import Utils.City;
import Utils.TaggedConnection.Frame;
import Utils.TaggedConnection;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Server {

    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(12345);
        Model model;

        /*

                Gets data saved from file

         */

        File file = new File("model.ser");
        if (file.exists())
            model = Model.deserialize("model.ser");
        else
            model = new Model();

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

                        if (f.tag == 0) { // Login attempt

                            System.out.println("Login attempt.");
                            String answer = "Error";

                            if (model.checkAuthentication(f.username, new String(f.data)))
                                answer = "Success";

                            connection.send(f.tag, "", answer.getBytes());

                        } else if (f.tag == 1) { // Registration attempts

                            System.out.println("Registration attempt.");
                            String email = f.username;
                            String password = new String(f.data);

                            String answer;
                            try {
                                model.addClient(email, password);
                                answer = "Success";
                                System.out.println("Serializing...");
                                model.serialize("model.ser");
                                System.out.println("Serialized.");
                            } catch (EmailAlreadyExistsException e){
                                answer = "Error";
                            }
                            connection.send(f.tag, "", answer.getBytes());


                        } else if (f.tag == 2) { // Add flight information

                            System.out.println("Adding flight attempt.");

                            String answer = "No admin privileges.";

                            if (f.username.equals("admin")) {

                                String flightData = new String(f.data);
                                String[] flightDataParsed = flightData.split(" ");
                                try {
                                    answer = model.createFlight(Integer.parseInt(flightDataParsed[2]), 0, City.valueOf(flightDataParsed[0]), City.valueOf(flightDataParsed[1]), true, LocalDate.parse(flightDataParsed[3]));
                                    System.out.println("Serializing...");
                                    model.serialize("model.ser");
                                    System.out.println("Serialized.");
                                } catch (UnavailableFlightException | IllegalArgumentException e) {
                                    answer = "Error";
                                }
                            }

                            connection.send(f.tag, f.username, answer.getBytes());

                        } else if (f.tag == 3) { // Close a day

                            System.out.println("Closing a day attempt.");

                            String answer;

                            String date = new String(f.data);
                            try {
                                model.addClosedDay(LocalDate.parse(date));
                                answer = "Success";
                                System.out.println("Serializing...");
                                model.serialize("model.ser");
                                System.out.println("Serialized.");
                            } catch (Exception e) {
                                answer = "Error";
                            }

                            connection.send(f.tag, f.username, answer.getBytes());

                        } else if (f.tag == 4) { // Make reservation for a trip

                            System.out.println("Reservation attempt.");

                            String path = new String(f.data);
                            String[] pathParsed = path.split(" ");
                            Set<String> pathSet = new HashSet<>(Arrays.asList(pathParsed));

                            String answer;
                            try {
                                answer = model.createReservation(f.username, pathSet);
                                System.out.println("Serializing...");
                                model.serialize("model.ser");
                                System.out.println("Serialized.");

                            } catch (FlightDoesntExistException | UnavailableFlightException | FlightAlreadyDeparted e) {
                                answer = "Error";
                            }

                            connection.send(4, f.username, answer.getBytes());

                        } else if (f.tag == 5) { // Cancel reservation

                            System.out.println("Cancellation attempt.");

                            String username = f.username;
                            String id = new String(f.data);
                            String answer;
                            try {
                                model.removeReservationByClient(id, username);
                                answer = "Success";
                                System.out.println("Serializing...");
                                model.serialize("model.ser");
                                System.out.println("Serialized.");
                            } catch (DoesntExistReservationFromClient e) {
                                answer = "Error";
                            }

                            connection.send(5, f.username, answer.getBytes());

                        } else if (f.tag == 6) { // List all flights

                            System.out.println("Listing all flights attempt.");

                            connection.send(6, f.username, model.getFlightsString().getBytes());

                        } else if (f.tag == 7) { // List flights in a date

                            System.out.println("Listing all flights in a date attempt.");

                            String date = new String(f.data);
                            try {
                                String flightsListing = model.getFlightsStringInDate(LocalDate.parse(date));
                                connection.send(7, f.username, flightsListing.getBytes());
                            } catch (Exception e) {
                                connection.send(7, f.username, "Error".getBytes());
                            }
                        }
                    }

                } catch (Exception ignored) {}

            };

            Thread t = new Thread(worker);
            t.start();

        }
    }
}
