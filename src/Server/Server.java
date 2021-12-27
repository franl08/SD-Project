package Server;

import Model.Model;
import Model.Flight;
import Utils.City;
import Utils.TaggedConnection.Frame;
import Utils.TaggedConnection;

import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Server {

    public static void main(String[] args) throws Exception {

        ServerSocket ss = new ServerSocket(12345);
        Model model = new Model();
        // TODO: Se o servidor existir fetch contas

        while(true) {
            Socket s = ss.accept();
            TaggedConnection connection = new TaggedConnection(s);

            Runnable worker = () -> {

                try (connection) {

                    while (true) {

                        Frame f = connection.receive();
                        boolean isClient = f.isClient == '1';

                        if (f.tag == 0) { // Login attempt

                            String answer = "0";

                            if (model.checkAutentication(f.username, new String(f.data)))
                                answer = "1";

                            connection.send(f.tag, f.isClient, "", answer.getBytes());

                        } else if (f.tag == 1) { // Registration attempts

                            String username = f.username;
                            String password = new String(f.data);

                            String answer = "0";

                            if (!model.lookupUser(username) && isClient) {
                                answer = "1"; // Success

                                // Criar cliente

                            } else if (!isClient)
                                answer = "2"; // Admin can't perform registration

                            connection.send(f.tag, f.isClient, "", answer.getBytes());
                        } else if (f.tag == 2) { // Add flight information

                            String flightData = new String(f.data);
                            String[] flightDataParsed = flightData.split(" ");
                            String id = "12345"; // TODO: FIX ID GENERATION
                            // TODO: WHAT IS TOGO?
                            Flight flight = new Flight(id, Integer.parseInt(flightDataParsed[2]), 0, City.valueOf(flightDataParsed[0]),  City.valueOf(flightDataParsed[1]), true,  LocalDate.parse(flightDataParsed[4]));
                            model.addFlight(flight);

                            // TODO: ANSWER WITH SUCCESS OF FAIL
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

                } catch (Exception e) {
                    e.printStackTrace();
                }

            };

            Thread t = new Thread(worker);
            t.start();


        }
    }
}
