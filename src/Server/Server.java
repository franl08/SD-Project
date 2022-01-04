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
import java.time.LocalDateTime;
import java.util.*;

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

                            model.l.readLock().lock();
                            try {
                                if (model.checkAuthentication(f.username, new String(f.data)))
                                    answer = "Success";
                            } finally {
                                model.l.readLock().unlock();
                            }

                            connection.send(f.tag, "", answer.getBytes());

                        } else if (f.tag == 1) { // Registration attempts

                            System.out.println("Registration attempt.");
                            String email = f.username;
                            String password = new String(f.data);

                            String answer;

                            model.l.writeLock().lock();
                            try {
                                model.addClient(email, password);
                                answer = "Success";
                                System.out.println("Serializing...");
                                model.serialize("model.ser");
                                System.out.println("Serialized.");
                            } catch (EmailAlreadyExistsException e){
                                answer = "Error";
                            } finally {
                                model.l.writeLock().unlock();
                            }
                            connection.send(f.tag, "", answer.getBytes());


                        } else if (f.tag == 2) { // Add flight information

                            System.out.println("Adding flight attempt.");

                            String answer = "No admin privileges.";

                            if (f.username.equals("admin")) {

                                String flightData = new String(f.data);
                                String[] flightDataParsed = flightData.split(" ");

                                model.l.writeLock().lock();
                                try {
                                    answer = model.createFlight(Integer.parseInt(flightDataParsed[2]), 0, City.valueOf(flightDataParsed[0]), City.valueOf(flightDataParsed[1]), true, LocalDate.parse(flightDataParsed[3]));
                                    System.out.println("Serializing...");
                                    model.serialize("model.ser");
                                    System.out.println("Serialized.");
                                } catch (UnavailableFlightException | IllegalArgumentException e) {
                                    answer = "Error";
                                } finally {
                                    model.l.writeLock().unlock();
                                }
                            }

                            connection.send(f.tag, f.username, answer.getBytes());

                        } else if (f.tag == 3) { // Close a day

                            System.out.println("Closing a day attempt.");

                            String answer;

                            String date = new String(f.data);

                            model.l.writeLock().lock();
                            try {
                                model.addClosedDay(LocalDate.parse(date));
                                answer = "Success";
                                System.out.println("Serializing...");
                                model.serialize("model.ser");
                                System.out.println("Serialized.");
                            } catch (Exception e) {
                                answer = "Error";
                            } finally {
                                model.l.writeLock().unlock();
                            }

                            connection.send(f.tag, f.username, answer.getBytes());

                        } else if (f.tag == 4) { // Make reservation for a trip by flight ID

                            System.out.println("Reservation attempt.");

                            String path = new String(f.data);
                            String[] pathParsed = path.split(" ");
                            Set<String> pathSet = new HashSet<>(Arrays.asList(pathParsed));
                            String answer;

                            model.l.writeLock().lock();
                            try {
                                answer = model.createReservation(f.username, pathSet);
                                System.out.println("Serializing...");
                                model.serialize("model.ser");
                                System.out.println("Serialized.");
                            } catch (Exception e) {
                                answer = "Error";
                                e.printStackTrace();
                            } finally {
                                model.l.writeLock().unlock();
                            }

                            connection.send(4, f.username, answer.getBytes());

                        } else if (f.tag == 5) { // Make reservation by cities

                            System.out.println("Reservation attempt by cities.");

                            String pathAndDates = new String(f.data);
                            String[] pathAndDatesParsed = pathAndDates.split(";");
                            String[] dates = pathAndDatesParsed[1].split(" ");
                            String[] pathParsed = pathAndDatesParsed[0].split(" ");

                            boolean validReservation = true;

                            List<City> cities = new ArrayList<>();
                            for (String city : pathParsed) {
                                try {
                                    City c = City.valueOf(city);
                                    cities.add(c);
                                } catch (Exception e) {
                                    validReservation = false;
                                }
                            }

                            LocalDate beginDate = null, endDate = null;
                            try {
                                beginDate = LocalDate.parse(dates[0]);
                                endDate = LocalDate.parse(dates[1]);
                            } catch(Exception e) {
                                validReservation = false;
                            }

                            String answer = "Error";

                            if (validReservation) {

                                answer = model.createReservationGivenCities(f.username, cities, beginDate, endDate);
                                if (answer.equals("")) answer = "Error";
                            }

                            connection.send(5, f.username, answer.getBytes());


                        } else if (f.tag == 6) { // Cancel reservation

                            System.out.println("Cancellation attempt.");

                            String username = f.username;
                            String id = new String(f.data);
                            String answer;

                            model.l.writeLock().lock();
                            try {
                                model.removeReservationByClient(id, username);
                                answer = "Success";
                                System.out.println("Serializing...");
                                model.serialize("model.ser");
                                System.out.println("Serialized.");
                            } catch (DoesntExistReservationFromClient e) {
                                answer = "Error";
                            } finally {
                                model.l.writeLock().unlock();
                            }

                            connection.send(6, f.username, answer.getBytes());

                        } else if (f.tag == 7) { // List all flights

                            System.out.println("Listing all flights attempt.");

                            model.l.readLock().lock();
                            try {
                                connection.send(7, f.username, model.getFlightsString().getBytes());
                            } finally {
                                model.l.readLock().unlock();
                            }


                        } else if (f.tag == 8) { // List flights in a date

                            System.out.println("Listing all flights in a date attempt.");

                            String date = new String(f.data);
                            model.l.readLock().lock();
                            try {
                                String flightsListing = model.getFlightsStringInDate(LocalDate.parse(date));
                                connection.send(8, f.username, flightsListing.getBytes());
                            } catch (Exception e) {
                                connection.send(8, f.username, "Error".getBytes());
                            } finally {
                                model.l.readLock().unlock();
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
