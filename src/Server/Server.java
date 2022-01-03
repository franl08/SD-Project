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
                                model.serialize("model.ser");
                            } catch (EmailAlreadyExistsException e){
                                answer = "Error";
                            }
                            connection.send(f.tag, "", answer.getBytes());


                        } else if (f.tag == 2) { // Add flight information

                            String answer = "No admin privileges.";

                            if (f.username.equals("admin")) {

                                String flightData = new String(f.data);
                                String[] flightDataParsed = flightData.split(" ");
                                try {
                                    answer = model.createFlight(Integer.parseInt(flightDataParsed[2]), 0, City.valueOf(flightDataParsed[0]), City.valueOf(flightDataParsed[1]), true, LocalDate.parse(flightDataParsed[3]));
                                    model.serialize("model.ser");
                                } catch (UnavailableFlightException e) {
                                    answer = "Error";
                                }
                            }

                            connection.send(f.tag, f.username, answer.getBytes());

                        } else if (f.tag == 3) { // Close a day

                            String answer;

                            String date = new String(f.data);
                            try {
                                model.addClosedDay(LocalDate.parse(date));
                                answer = "Success";
                                model.serialize("model.ser");
                            } catch (Exception e) {
                                answer = "Error";
                            }

                            connection.send(f.tag, f.username, answer.getBytes());

                        } else if (f.tag == 4) { // Make reservation for a trip

                            String path = new String(f.data);
                            String[] pathParsed = path.split(" ");

                            Set<String> pathSet = new HashSet<>(Arrays.asList(pathParsed));

                            String answer;

                            try {
                                answer = model.createReservation(f.username, pathSet);
                                model.serialize("model.ser");

                            } catch (FlightDoesntExistException | UnavailableFlightException | FlightAlreadyDeparted e) {
                                answer = "Error";
                            }

                            connection.send(4, f.username, answer.getBytes());

                        } else if (f.tag == 5) { // Cancel reservation

                            String username = f.username;
                            String id = new String(f.data);
                            String answer;
                            try {
                                model.removeReservationByClient(id, username);
                                answer = "Success";
                                model.serialize("model.ser");
                            } catch (DoesntExistReservationFromClient e) {
                                answer = "Error";
                            }

                            connection.send(5, f.username, answer.getBytes());

                        } else if (f.tag == 6) { // List all flights

                            connection.send(6, f.username, model.getFlightsString().getBytes());

                        } else if (f.tag == 7) { // List flights in a date

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
