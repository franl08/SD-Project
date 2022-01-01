package Server;

import Exceptions.EmailAlreadyExistsException;
import Model.Model;
import Model.Flight;
import Utils.City;
import Utils.TaggedConnection.Frame;
import Utils.TaggedConnection;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;

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

                        } else if (f.tag == 1) { // Registration attempts TODO: Fazer serialize quando se adicionar 1 conta

                            System.out.println("Registration attempt.");
                            String email = f.username;
                            String password = new String(f.data);

                            String answer = "Error";
                            try {
                                model.addClient(email, password);
                                answer = "Success";
                            } catch (EmailAlreadyExistsException e){
                                answer = "Error";
                            }

                            connection.send(f.tag, "", answer.getBytes());


                        } else if (f.tag == 2) { // Add flight information

                            String answer = "No admin privileges.";

                            if (f.username.equals("admin")) {

                                String flightData = new String(f.data);
                                String[] flightDataParsed = flightData.split(" ");
                                String id = "12345"; // TODO: FIX ID GENERATION
                                // TODO: WHAT IS TOGO?
                                Flight flight = new Flight(id, Integer.parseInt(flightDataParsed[2]), 0, City.valueOf(flightDataParsed[0]), City.valueOf(flightDataParsed[1]), true, LocalDate.parse(flightDataParsed[4]));
                                model.addFlight(flight);
                                answer = "Success";
                            }

                            connection.send(f.tag, f.username, answer.getBytes());

                        } else if (f.tag == 3) { // End a day

                            String date = new String(f.data);
                            model.addClosedDay(LocalDate.parse(date));

                            // TODO: ANSWER WITH SUCCESS OR FAIL
                        } else if (f.tag == 4) { // Make reservation for a trip

                            String path = new String(f.data);
                            String[] pathParsed = path.split(" ");

                            // Convert from Cities to flights IDS

                        } else if (f.tag == 5) { // Cancel reservation

                            String username = f.username;
                            String id = new String(f.data);
                            model.removeReservationByClient(id, username);

                            // TODO: ANSWER WITH FAIL OR SUCCESS

                        } else if (f.tag == 6) { // List all flights

                            // TODO

                        }
                    }

                } catch (Exception ignored) {}

            };

            Thread t = new Thread(worker);
            t.start();

        }
    }
}
