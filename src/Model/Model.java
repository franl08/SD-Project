package Model;

import Exceptions.*;
import Utils.City;
import Utils.Utilities;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class that manages the data from the airline
 */
public class Model implements Serializable {

    /**
     * Stores the clients credentials
     */
    private final Map<String, String> clients;
    /**
     * Stores the existent flights
     */
    private final Map<String, Flight> flights;
    /**
     * Stores the existent reservation
     */
    private Map<String, Reservation> reservations;
    /**
     * Stores the client reservations
     */
    private Map<String, Set<String>> clientReservations;
    /**
     * Stores the closed days
     */
    private final Set<LocalDate> closedDays; // Point 4. of basic functionalities in utterance

    /**
     * Read write lock
     */
    public ReadWriteLock l;

    /**
     * Empty constructor
     */
    public Model() {
        this.clients = new HashMap<>();
        this.flights = new HashMap<>();
        this.reservations = new HashMap<>();
        this.clientReservations = new HashMap<>();
        this.closedDays = new HashSet<>();
        this.l = new ReentrantReadWriteLock();
    }

    /**
     * Gets the closed days
     * @return Closed days in formatted string
     */
    public String getClosedDays() {
        l.readLock().lock();
        try {
            StringBuilder string = new StringBuilder();

            for (LocalDate date : this.closedDays) {
                string.append(" * ").append(date).append("\n");
            }

            string.append("-------------------").append("\n\n");
            return (string.isEmpty() ? "No closed days to show\n\n" :string.toString());
        } finally {
            l.readLock().unlock();
        }
    }

    /*

            Client methods

     */

    /**
     * Validates the credentials inserted
     * @param username Email of the user
     * @param password Password
     * @return True if the credentials are valid, otherwise False
     */
    public boolean checkAuthentication(String username, String password){
        l.readLock().lock();
        try {
            if (!this.clients.containsKey(username)) return false;
            return this.clients.get(username).equals(password);
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Adds a client to the clients map
     * @param email Email
     * @param password Password
     * @throws EmailAlreadyExistsException There already is a client registered under that email
     */
    public void addClient(String email, String password) throws EmailAlreadyExistsException {
        l.writeLock().lock();
        try {
            if (this.clients.containsKey(email)) throw new EmailAlreadyExistsException();
            this.clients.put(email, password);
        } finally {
            l.writeLock().unlock();
        }
    }


    /*

            Flights methods

     */

    /**
     * Gets the flights existent in a formatted string
     * @return String that holds the flights information
     */
    public String getFlightsString(){
        l.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder();
            for (String id : this.flights.keySet())
                sb.append(this.flights.get(id).toString());
            return (!sb.isEmpty()) ? sb.toString() : "No flights to show";
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Gets the flights existent in a formatted string for a specific date
     * @param date Date
     * @return String that holds the flights' information from that date
     */
    public String getFlightsStringInDate(LocalDate date){
        l.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder();
            for (String id : this.flights.keySet())
                if (this.flights.get(id).getDate().equals(date)) sb.append(this.flights.get(id).toString());
            return (!sb.isEmpty()) ? sb.toString() : "No flights to show";
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Creates a flight
     * @param nMaxPassengers Maximum number of passengers
     * @param nReserve Number of reservations already made (normally 0)
     * @param origin City of origin
     * @param destination City of destination
     * @param toGo True if the flight hasn't left yet, false otherwise
     * @param date Date
     * @return ID of the flight generated
     * @throws UnavailableFlightException Can't add flight because the date is closed
     */
    public String createFlight(int nMaxPassengers, int nReserve, City origin, City destination, boolean toGo, LocalDate date) throws UnavailableFlightException{
        l.writeLock().lock();
        try {
            if (closedDays.contains(date)) throw new UnavailableFlightException("The flight is in a closed day.");
            String fID = this.generateFlightID();
            Flight f = new Flight(fID, nMaxPassengers, nReserve, origin, destination, toGo, date);
            this.flights.put(fID, f);
            return fID;
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * Generates a flight ID
     * @return Flight ID
     */
    public String generateFlightID(){
        String lastID = "";
        int mostRecent = 1;

        l.readLock().lock();
        try {
            for (String s : this.flights.keySet()) {
                lastID = s;
                int current = Integer.parseInt(lastID.split("F")[1]);
                if (current > mostRecent) mostRecent = current;
            }
            if (lastID.equals("")) return "F" + 1;
            return "F" + (mostRecent + 1);
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Removes a flight
     * @param s ID of the flight
     * @throws FlightDoesntExistException No flight with that ID is registered
     */
    public void removeFlight(String s) throws FlightDoesntExistException{
        l.writeLock().lock();
        try {
            if (this.flights.containsKey(s)) {
                Map<String, Reservation> reservationsOfFlight = getReservationsFromFlight(s);
                if (!reservationsOfFlight.isEmpty())
                    for (String code : reservationsOfFlight.keySet())
                        try {
                            removeReservation(code);
                        } catch (Exception ignored) {
                        }
                this.flights.remove(s);
            } else throw new FlightDoesntExistException("There isn't any flight with ID " + s);
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * Checks if all the flights are free to be placed in a reservation
     * @param flightsID List of flights ID's
     * @throws UnavailableFlightException Flight is full
     * @throws FlightAlreadyDeparted Flight already left
     * @throws FlightDoesntExistException Flight doesn't exist
     */
    public void checkSetOfFlightsToReservation(Set<String> flightsID) throws UnavailableFlightException, FlightAlreadyDeparted, FlightDoesntExistException{
        l.readLock().lock();
        try {
            for (String fID : flightsID) {
                Flight f = this.flights.getOrDefault(fID, null);
                if (f == null) throw new FlightDoesntExistException();
                else if (!f.hasFreeSpace()) throw new UnavailableFlightException();
                else if (!f.getToGo()) throw new FlightAlreadyDeparted();
            }
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Collects all the flights with a certain origin and destination, between a date range
     * @param origin Origin
     * @param destination Destination
     * @param begin Inferior limit
     * @param end Superior limit
     * @return List of flight IDS
     */
    public List<Flight> getFlightsWithOriginDestinationAndDateRange(City origin, City destination, LocalDate begin, LocalDate end){
        l.readLock().lock();
        try {
            List<Flight> flights = new ArrayList<>();
            for (String key : this.flights.keySet()) {
                Flight f = this.flights.get(key);
                if (f.getOrigin().equals(origin) && f.getDestination().equals(destination) && Utilities.isInRange(begin, end, f.getDate()))
                    flights.add(f.clone());
            }
            return flights;

        } finally {
            l.readLock().unlock();
        }

    }



    /*

            Reservations methods

     */

    /**
     * Gets the reservations that contain a certain flight
     * @param s Flight ID
     * @return Map with the reservations
     */
    public Map<String, Reservation> getReservationsFromFlight(String s){
        l.readLock().lock();
        try {
            Map<String, Reservation> ans = new HashMap<>();
            if (!this.reservations.isEmpty())
                for (String code : this.reservations.keySet()) {
                    Reservation r = this.reservations.get(code);
                    Set<String> fIDs = r.getFlightsID();
                    if (fIDs.contains(s)) ans.put(code, r.clone());
                }
            return ans;
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Gets the reservations made by a user
     * @param s User ID
     * @return Map with the reservations
     */
    public Map<String, Reservation> getReservationsFromUser(String s){

        l.readLock().lock();
        try {
            Map<String, Reservation> ans = new HashMap<>();
            if (this.clientReservations == null) this.clientReservations = new HashMap<>();
            if (this.clientReservations.containsKey(s)) {
                Set<String> reservationsIDS = this.clientReservations.get(s);

                for (String id : reservationsIDS) {
                    ans.put(id, this.reservations.get(id));
                }
            }
            return ans;
        } finally {
            l.readLock().unlock();
        }

    }

    /**
     * Formats the reservations by a user to a string
     * @param s User ID
     * @return Formatted string with the reservations
     */
    public String getReservationsStringFromUser(String s){

        l.readLock().lock();
        try {
            Map<String, Reservation> reservs = getReservationsFromUser(s);
            StringBuilder ans = new StringBuilder();
            if (reservs != null) {
                for (String id : reservs.keySet()) {
                    int ac = 1;
                    Reservation r = reservs.get(id);
                    ans.append("Reservation ID: ").append(r.getID()).append("\n");
                    Set<String> fIDs = r.getFlightsID();
                    for (String fID : fIDs) {
                        Flight f = this.flights.get(fID);
                        ans.append("Flight ").append(ac++).append(":\n")
                                .append("From: ").append(f.getOrigin()).append("\n")
                                .append("To: ").append(f.getDestination()).append("\n")
                                .append("On: ").append(f.getDate()).append("\n")
                                .append("Flight ID: ").append(f.getID()).append("\n\n");
                    }
                    ans.append("------------------------------------------------------------------\n");
                }
            }
            return (!ans.isEmpty()) ? ans.toString() : "No reservations to show";
        } finally {
            l.readLock().unlock();
        }

    }



    /**
     * Removes a reservation, by request of a client
     * @param reservationID ID of the reservation
     * @param username Email of the client
     * @throws DoesntExistReservationFromClient Client doesn't have a reservation with that ID
     * @throws FlightAlreadyDeparted Flight has already departed
     */
    public void removeReservationByClient(String reservationID, String username) throws DoesntExistReservationFromClient, FlightAlreadyDeparted{
        l.writeLock().lock();
        try {
            if (this.reservations.containsKey(reservationID)) {
                Set<String> fs = this.reservations.get(reservationID).getFlightsID();
                for (String id : fs)
                    if (!this.flights.get(id).getToGo())
                        throw new FlightAlreadyDeparted("The flight with ID " + id + " already departed.");

                for (String id : fs)
                    this.flights.get(id).removeOneReservation();

                Set<String> reservationsMadeByClient = this.clientReservations.get(username);
                reservationsMadeByClient.remove(reservationID);

                if (reservationsMadeByClient.size() == 0) this.clientReservations.remove(username);
                else this.clientReservations.put(username, reservationsMadeByClient);

            } else
                throw new DoesntExistReservationFromClient("Doesn't exist any reservation with ID " + reservationID + "from you.");
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * Removes a reservation
     * @param reservationID ID
     * @throws ReservationDoesntExistException No reservation is registered under that ID
     */
    public void removeReservation(String reservationID) throws ReservationDoesntExistException {
        l.writeLock().lock();
        try {
            if (this.reservations.containsKey(reservationID)) {
                String username = this.reservations.get(reservationID).getClientID();

                Set<String> reservationsMadeByClient = this.clientReservations.get(username);
                reservationsMadeByClient.remove(reservationID);

                if (reservationsMadeByClient.size() == 0) this.clientReservations.remove(username);
                else this.clientReservations.put(username, reservationsMadeByClient);

                this.reservations.remove(reservationID);
            } else throw new ReservationDoesntExistException("There isn't any reservation with ID " + reservationID);
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * Creates a reservation
     * @param clientID ID of the client
     * @param flightsID List of flight IDS
     * @return Reservation code
     * @throws FlightDoesntExistException A flight does not exist
     * @throws UnavailableFlightException A flight is unavailable
     * @throws FlightAlreadyDeparted A flight has already left
     */
    public String createReservation(String clientID, Set<String> flightsID) throws FlightDoesntExistException, UnavailableFlightException, FlightAlreadyDeparted {
        l.writeLock().lock();
        try {
            checkSetOfFlightsToReservation(flightsID);
            String reservationID = this.generateReservationID();
            Reservation r = new Reservation(reservationID, clientID, flightsID);

            Set<String> reservationsByClient;

            if (this.clientReservations == null) this.clientReservations = new HashMap<>();
            if (this.reservations == null) this.reservations = new HashMap<>();

            if (this.clientReservations.containsKey(clientID))
                reservationsByClient = this.clientReservations.get(clientID);
            else
                reservationsByClient = new HashSet<>();

            reservationsByClient.add(reservationID);
            this.clientReservations.put(clientID, reservationsByClient);

            for (String fId : flightsID) {
                Flight f = this.flights.get(fId);
                f.addOneReservation();
            }
            this.reservations.put(r.getID(), r);
            return reservationID;
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * Generates a reservation ID
     * @return Reservation ID
     */
    public String generateReservationID(){
        String lastID = "";
        int mostRecent = 1;

        l.readLock().lock();
        try {
            for (String s : this.reservations.keySet()) {
                lastID = s;
                int current = Integer.parseInt(lastID.split("R")[1]);
                if (current > mostRecent) mostRecent = current;
            }
            if (lastID.equals("")) return "R" + 1;
            return "R" + (mostRecent + 1);
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Gets a list of possible trips
     * @param fs Flights ID
     * @param compared Flights to check if it's possible to make a route
     * @param end Future limit date
     * @return Updated compare
     */
    public List<List<Flight>> getPossibleTrip(List<Flight> fs, List<List<Flight>> compared, LocalDate end){
        List<List<Flight>> ans = new ArrayList<>();
        for (Flight f : fs) {
            for (List<Flight> possibleT : compared) {
                int size = possibleT.size();
                if (Utilities.isInRange(possibleT.get(size - 1).getDate(), end, f.getDate())) {
                    List<Flight> toAdd = new ArrayList<>(possibleT);
                    toAdd.add(f);
                    ans.add(toAdd);
                }
            }
        }
        return ans;
    }

    /**
     * Get the available routes between a data range
     * @param desiredCities Cities desired
     * @param begin Begin date
     * @param end Maximum end date
     * @return Routes
     */
    public List<Route> getAvailableRoutesInDataRange(List<City> desiredCities, LocalDate begin, LocalDate end){
        List<List<Flight>> compared = new ArrayList<>();
        List<Route> ans = new ArrayList<>();
        for(int i = 0; i < desiredCities.size() - 1; i++){
            City o = desiredCities.get(i);
            City d = desiredCities.get(i + 1);
            List<Flight> fs = getFlightsWithOriginDestinationAndDateRange(o, d, begin, end);
            if(i != 0)
                compared = getPossibleTrip(fs, compared, end);
            else
                compared.add(fs);
        }
        if(compared.size() == 0) return ans; // não existe nenhuma opção viável
        for(List<Flight> fs : compared){
            Route r = new Route(fs); // passa de lista de listas para lista de rotas
            ans.add(r);
        }
        return ans;
    }

    /**
     * Creates a reservation according to a list of routes
     * @param username Client ID
     * @param routes Routes possibles
     * @return Reservation ID
     */
    public String createReservationGivenListRoutes(String username, List<Route> routes){
        String reservationID = "";
        for(Route r : routes){
            Set<String> fIDs = r.getFlightsIDs();

            for (String id : fIDs) System.out.println(id);
            try{
                reservationID = createReservation(username, fIDs);
                break;
            } catch (Exception ignored){}
        }
        return reservationID;
    }

    /**
     * Creates reservations according to a list of cities
     * @param username ID of the client
     * @param desiredCities Cities to travel through
     * @param begin Beginning of the date range
     * @param end End of the date range
     * @return Reservation rode
     */
    public String createReservationGivenCities(String username, List<City> desiredCities, LocalDate begin, LocalDate end){
        return createReservationGivenListRoutes(username, getAvailableRoutesInDataRange(desiredCities, begin, end));
    }

    public List<Flight> getFlightsAvailableForReservationFromList(List<Flight> fs){
        List<Flight> ans = new ArrayList<>();
        for(Flight f : fs){
            if(this.flights.get(f.getID()).hasFreeSpace() && this.flights.get(f.getID()).getToGo()) ans.add(f);
        }
        return ans;
    }

    /*

            Closed days methods

     */

    /**
     * Adds a closed day, removing the flights on that day
     * @param date Date
     * @throws AlreadyIsAClosedDay Already is a closed day
     */
    public void addClosedDay(LocalDate date) throws AlreadyIsAClosedDay {
        l.writeLock().lock();
        try {
            if (!this.closedDays.contains(date)) {
                this.closedDays.add(date);
                for (String key : flights.keySet()) {
                    Flight f = flights.get(key);
                    if (f.getDate().equals(date)) {
                        try {
                            removeFlight(key);
                        } catch (Exception ignored) {}
                    }
                }
            } else throw new AlreadyIsAClosedDay("The selected day is already closed.");
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * Removes a closed day
     * @param date Date
     * @throws NotAClosedDay The date is not closed
     */
    public void removeClosedDay(LocalDate date) throws NotAClosedDay {
        l.writeLock().lock();
        try {
            if (!this.closedDays.contains(date)) throw new NotAClosedDay();
            this.closedDays.remove(date);
        } finally {
            l.writeLock().unlock();
        }
    }


    /*

    public List<List<Flight>> getFlightsWithOneStopOnSameDateWithDateRange(City origin, City destination, LocalDate begin, LocalDate end){
        List<List<Flight>> ans = new ArrayList<>();
        List<Flight> fromOrigin = getFlightsFromCityWithDateRange(origin, begin, end);
        if(!fromOrigin.isEmpty()){
            List<Flight> toDestination = getFlightsToCityWithDateRange(destination, begin, end);
            if(!toDestination.isEmpty())
                for(Flight f : toDestination){
                    City o = f.getOrigin();
                    LocalDate d = f.getDate();
                    for(Flight fl : fromOrigin)
                        if(fl.getDestination().equals(o) && fl.getDate().equals(d)){
                            List<Flight> toAdd = new ArrayList<>();
                            toAdd.add(f.clone());
                            toAdd.add(fl.clone());
                            ans.add(toAdd);
                        }
                }
        }
        return ans;
    }

    public List<List<Flight>> getFlightsWithTwoStopsOnSameDateWithDateRange(City origin, City destination, LocalDate begin, LocalDate end){
        List<List<Flight>> ans = new ArrayList<>();
        List<Flight> fromOrigin = getFlightsFromCityWithDateRange(origin, begin, end);
        if(!fromOrigin.isEmpty()){
            List<Flight> toDestination = getFlightsToCityWithDateRange(destination, begin, end);
            if(!toDestination.isEmpty()) {
                Set<Flight> withoutCities = getFlightsWithoutCities(origin, destination);
                for (Flight f : withoutCities) {
                    City o = f.getOrigin();
                    City d = f.getDestination();
                    LocalDate dF = f.getDate();
                    for (Flight flO : fromOrigin)
                        if (flO.getDestination().equals(o) && flO.getDate().equals(dF)) {
                            for(Flight flD : toDestination)
                                if(flD.getOrigin().equals(d) && flD.getDate().equals(dF)){
                                    List<Flight> toAdd = new ArrayList<>();
                                    toAdd.add(flO.clone());
                                    toAdd.add(f.clone());
                                    toAdd.add(flD.clone());
                                    ans.add(toAdd);
                                }
                        }
                }
            }
        }
        return ans;
    }

    public List<Flight> getFlightsFromCity(City c){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getOrigin().equals(c)) ans.add(f.clone());
        }
        return ans;
    }

    public List<Flight> getFlightsToCity(City c){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getDestination().equals(c)) ans.add(f.clone());
        }
        return ans;
    }

    public List<Flight> getFlightsToCityWithDateRange(City c, LocalDate begin, LocalDate end){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getDestination().equals(c) && Utilities.isInRange(begin, end, f.getDate())) ans.add(f.clone());
        }
        return ans;
    }

    public List<Flight> getFlightsFromCityWithDateRange(City c, LocalDate begin, LocalDate end){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getOrigin().equals(c) && Utilities.isInRange(begin, end, f.getDate())) ans.add(f.clone());
        }
        return ans;
    }


    public Set<Flight> getFlightsWithoutCities(City c1, City c2){
        Set<Flight> ans = new HashSet<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getDestination() != c1 && f.getDestination() != c2 && f.getOrigin() != c1 && f.getOrigin() != c2) ans.add(f.clone());
        }
        return ans;
    }

    public List<List<Flight>> getFlightsWithOneStop(City origin, City destination){
        List<List<Flight>> ans = new ArrayList<>();
        List<Flight> fromOrigin = getFlightsFromCity(origin);
        if(!fromOrigin.isEmpty()){
            List<Flight> toDestination = getFlightsToCity(destination);
            if(!toDestination.isEmpty())
                for(Flight f : toDestination){
                    City o = f.getOrigin();
                    for(Flight fl : fromOrigin)
                        if(fl.getDestination().equals(o)){
                            List<Flight> toAdd = new ArrayList<>();
                            toAdd.add(f.clone());
                            toAdd.add(fl.clone());
                            ans.add(toAdd);
                        }
                }
        }
        return ans;
    }

    public List<List<Flight>> getFlightsWithTwoStops(City origin, City destination){
        List<List<Flight>> ans = new ArrayList<>();
        List<Flight> fromOrigin = getFlightsFromCity(origin);
        if(!fromOrigin.isEmpty()){
            List<Flight> toDestination = getFlightsToCity(destination);
            if(!toDestination.isEmpty()) {
                Set<Flight> withoutCities = getFlightsWithoutCities(origin, destination);
                for (Flight f : withoutCities) {
                    City o = f.getOrigin();
                    City d = f.getDestination();
                    for (Flight flO : fromOrigin)
                        if (flO.getDestination().equals(o)) {
                            for(Flight flD : toDestination)
                                if(flD.getOrigin().equals(d)){
                                    List<Flight> toAdd = new ArrayList<>();
                                    toAdd.add(flO.clone());
                                    toAdd.add(f.clone());
                                    toAdd.add(flD.clone());
                                    ans.add(toAdd);
                                }
                        }
                }
            }
        }
        return ans;
    }

    // Provavelmente dá pra otimizar isto, aliás, não faz sentido estar sempre a adicionar a cidade de origem e destino, deve dar pra fazer algo com Map.Entry, mas pra já, é o que é
    public List<Route> getRoutes(City origin, City destination){
        List<Route> ans = new ArrayList<>();

        List<Flight> flights = getFlightsWithOriginAndDestination(origin, destination);
        if(flights != null && !flights.isEmpty())
            for(Flight f : flights){
                List<Flight> toAdd = new ArrayList<>();
                toAdd.add(f.clone());
                ans.add(new Route(origin, destination, toAdd));
            }

        List<List<Flight>> flights1stop = getFlightsWithOneStop(origin, destination);
        if(flights1stop != null && !flights1stop.isEmpty())
            for(List<Flight> fls : flights1stop)
                ans.add(new Route(origin, destination, fls));

        List<List<Flight>> flights2stop = getFlightsWithTwoStops(origin, destination);
        if(flights2stop != null && !flights2stop.isEmpty())
            for(List<Flight> fls : flights2stop)
                ans.add(new Route(origin, destination, fls));

        return ans;
    }
    */

    /*

            Serializing and deserializing methods

     */

    /**
     * Serializes the model to a filepath
     * @param filepath Filepath
     * @throws IOException File manipulation
     */
    public void serialize(String filepath) throws IOException {
        FileOutputStream fos = new FileOutputStream(filepath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this);
        oos.flush();
        oos.close();
        fos.flush();
        fos.close();
    }

    /**
     * Deserializes a model from a filepath
     * @param filepath Filepath
     * @return Model deserialized
     * @throws IOException I/O errors from file manipulation
     * @throws ClassNotFoundException The class of the serialized object cannot be found
     */
    public static Model deserialize(String filepath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Model accounts = (Model) ois.readObject();
        ois.close();
        fis.close();
        return accounts;
    }
}
