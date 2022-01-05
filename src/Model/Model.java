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
     * Stores the reservations in a date
     */
    private final Map<LocalDate, Set<String>> reservationsInDate;

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
        this.reservationsInDate = new HashMap<>();
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
                string.append("-> Date: ").append(date).append("\n");
            }

            if(this.closedDays.size() != 0)
                string.append("---------------------------").append("\n\n");
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
     * Creates a flight
     * @param nMaxPassengers Maximum number of passengers
     * @param origin City of origin
     * @param destination City of destination
     * @return ID of the flight generated
     */
    public String createFlight(int nMaxPassengers, City origin, City destination){
        l.writeLock().lock();
        try {
            String fID = this.generateFlightID();
            Flight f = new Flight(fID, nMaxPassengers, origin, destination);
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
     * Checks if all the flights are free to be placed in a reservation
     * @param flightsID List of flights ID's
     * @param date Date of reservation
     * @throws UnavailableFlightException Flight is full
     * @throws FlightDoesntExistException Flight doesn't exist
     */
    public void checkSetOfFlightsToReservation(Set<String> flightsID, LocalDate date) throws UnavailableFlightException, FlightDoesntExistException{
        l.readLock().lock();
        try {
            for (String fID : flightsID) {
                Flight f = this.flights.getOrDefault(fID, null);
                if (f == null) throw new FlightDoesntExistException();
                int totalReservationsOfFlight = 0;
                Set<String> resIdsOnDate = this.reservationsInDate.get(date);
                for(String rID : resIdsOnDate){
                    Reservation r = this.reservations.get(rID);
                    if(r.getFlightsID().contains(fID)) totalReservationsOfFlight++;
                }
                if(totalReservationsOfFlight > f.getnMaxPassengers()) throw new UnavailableFlightException();
            }
        } finally {
            l.readLock().unlock();
        }
    }

    /*

            Reservations methods

     */

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
                    ans.append("Date: ").append(r.getDate()).append("\n");
                    Set<String> fIDs = r.getFlightsID();
                    for (String fID : fIDs) {
                        Flight f = this.flights.get(fID);
                        ans.append("Flight ").append(ac++).append(":\n")
                                .append("From: ").append(f.getOrigin()).append("\n")
                                .append("To: ").append(f.getDestination()).append("\n")
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
     */
    public void removeReservationByClient(String reservationID, String username) throws DoesntExistReservationFromClient{
        l.writeLock().lock();
        try {
            if (this.reservations.containsKey(reservationID)) {
                Reservation r = this.reservations.get(reservationID);

                LocalDate date = r.getDate();
                Set<String> rIDsOnDate = this.reservationsInDate.get(date);
                rIDsOnDate.remove(reservationID);
                if(rIDsOnDate.size() == 0) this.reservationsInDate.remove(date);
                else this.reservationsInDate.put(date, rIDsOnDate);

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
                Reservation r = this.reservations.get(reservationID);
                String username = r.getClientID();
                LocalDate date = r.getDate();

                Set<String> rIdsInDate = this.reservationsInDate.get(date);
                rIdsInDate.remove(reservationID);
                if(rIdsInDate.isEmpty()) this.reservationsInDate.remove(date);
                else this.reservationsInDate.put(date, rIdsInDate);

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
     * @param date Date of the reservation
     * @return Reservation code
     * @throws FlightDoesntExistException A flight does not exist
     * @throws UnavailableFlightException A flight is unavailable
     */
    public String createReservation(String clientID, Set<String> flightsID, LocalDate date) throws FlightDoesntExistException, UnavailableFlightException {
        l.writeLock().lock();
        try {
            checkSetOfFlightsToReservation(flightsID, date);
            String reservationID = this.generateReservationID();
            Reservation r = new Reservation(reservationID, clientID, flightsID, date);

            Set<String> reservationsByClient;

            if (this.clientReservations == null) this.clientReservations = new HashMap<>();
            if (this.reservations == null) this.reservations = new HashMap<>();

            if (this.clientReservations.containsKey(clientID))
                reservationsByClient = this.clientReservations.get(clientID);
            else
                reservationsByClient = new HashSet<>();

            reservationsByClient.add(reservationID);
            this.clientReservations.put(clientID, reservationsByClient);

            if(this.reservationsInDate.containsKey(date)){
                Set<String> rIdsInDate = this.reservationsInDate.get(date);
                rIdsInDate.add(reservationID);
                this.reservationsInDate.put(date, rIdsInDate);
            }
            else{
                Set<String> toAdd = new HashSet<>();
                toAdd.add(reservationID);
                this.reservationsInDate.put(date, toAdd);
            }

            this.reservations.put(reservationID, r);
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

    public List<Flight> getFlightsWithOriginDestination(City o, City d){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getOrigin().equals(o) && f.getDestination().equals(d)) ans.add(f.clone());
        }
        return ans;
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
                List<Flight> toAdd = new ArrayList<>(possibleT);
                toAdd.add(f);
                ans.add(toAdd);
            }
        }
        return ans;
    }

    public Flight getAvailableFlightOnDate(List<Flight> fs, LocalDate date){
        Set<String> resIdsOnDate = this.reservationsInDate.get(date);
        Flight ans = null;
        for(Flight f : fs){
            int nRes = 0;
            for(String rID : resIdsOnDate)
                if(this.reservations.get(rID).getFlightsID().contains(f.getID())) nRes++;
            if(nRes < f.getnMaxPassengers()) ans = f;
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
    public Map.Entry<LocalDate, Set<String>> getAvailableListOfFlightsInDataRange(List<City> desiredCities, LocalDate begin, LocalDate end) throws OnlyClosedDaysException, UnavailableFlightException{
        Set<String> ans = new HashSet<>();
        LocalDate curDate = begin;
        boolean isOpenDay = false;
        while(!isOpenDay && (curDate.isBefore(end) || curDate.isEqual(end))) {
            if (this.closedDays.contains(curDate))
                curDate = curDate.plusDays(1);
            else isOpenDay = true;
        }
        if(!isOpenDay) throw new OnlyClosedDaysException();
        else
            for(int i = 0; i < desiredCities.size() - 1; i++){
                City o = desiredCities.get(i);
                City d = desiredCities.get(i + 1);
                List<Flight> fs = getFlightsWithOriginDestination(o, d);
                Flight f = getAvailableFlightOnDate(fs, curDate);
                if(f == null) throw new UnavailableFlightException();
                else ans.add(f.getID());
            }
        return new AbstractMap.SimpleEntry<>(curDate, ans);
    }

    /**
     * Creates reservations according to a list of cities
     * @param username ID of the client
     * @param desiredCities Cities to travel through
     * @param begin Beginning of the date range
     * @param end End of the date range
     * @return Reservation rode
     */
    public String createReservationGivenCities(String username, List<City> desiredCities, LocalDate begin, LocalDate end) throws OnlyClosedDaysException, UnavailableFlightException, FlightAlreadyDeparted, FlightDoesntExistException {
        Map.Entry<LocalDate, Set<String>> flightsAndDate = getAvailableListOfFlightsInDataRange(desiredCities, begin, end);
        return createReservation(username, flightsAndDate.getValue(), flightsAndDate.getKey());
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
                Set<String> rIDsOnDate = this.reservationsInDate.get(date);
                for(String id : rIDsOnDate)
                    try{
                        removeReservation(id);
                    } catch (Exception ignored){}
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
