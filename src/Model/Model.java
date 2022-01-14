package Model;

import Exceptions.*;
import Utils.Colors;
import Utils.Utilities;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class that manages the data from the airline
 */
public class Model implements Serializable, ModelFacade {

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
    private final Set<LocalDate> closedDays;

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
     * {@inheritDoc}
     */
    public String getClosedDays() {
        l.readLock().lock();
        try {
            StringBuilder string = new StringBuilder();

            for (LocalDate date : this.closedDays) {
                string.append(Colors.ANSI_YELLOW + "-> Date: " + Colors.ANSI_RESET).append(date).append("\n");
            }

            if(this.closedDays.size() != 0)
                string.append(Colors.ANSI_CYAN + "---------------------------" + Colors.ANSI_RESET).append("\n\n");
            return (string.isEmpty() ? (Colors.ANSI_YELLOW + "No closed days to show\n\n" + Colors.ANSI_RESET) : string.toString());
        } finally {
            l.readLock().unlock();
        }
    }

    /*

            Client methods

     */

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public void addClient(String email, String password) throws EmailAlreadyExistsException, NotAnEmailException {
        l.writeLock().lock();
        try {
            if (this.clients.containsKey(email)) throw new EmailAlreadyExistsException();
            if (!Utilities.checkEmail(email)) throw new NotAnEmailException();
            this.clients.put(email, password);
        } finally {
            l.writeLock().unlock();
        }
    }


    /*

            Flights methods

     */

    /**
     * {@inheritDoc}
     */
    public String getFlightsString(){
        l.readLock().lock();
        try {
            StringBuilder sb = new StringBuilder();
            for (String id : this.flights.keySet())
                sb.append(this.flights.get(id).toString());
            return (sb.isEmpty()) ? (Colors.ANSI_YELLOW + "No flights to show" + Colors.ANSI_RESET) : sb.toString();
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
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
     * @throws OnlyClosedDaysException Date is a closed day
     * @throws DayHasPassedException Date is in the past
     */
    public void checkSetOfFlightsToReservation(Set<String> flightsID, LocalDate date) throws UnavailableFlightException, FlightDoesntExistException, OnlyClosedDaysException, DayHasPassedException {

        if(this.closedDays.contains(date)) throw new OnlyClosedDaysException();
        if (date.isBefore(LocalDate.now())) throw new DayHasPassedException();
        for (String fID : flightsID) {
            Flight f = this.flights.getOrDefault(fID, null);
            if (f == null) throw new FlightDoesntExistException();
            int totalReservationsOfFlight = 0;
            Set<String> resIdsOnDate = this.reservationsInDate.get(date);
            if (resIdsOnDate != null) {
                for (String rID : resIdsOnDate) {
                    Reservation r = this.reservations.get(rID);
                    if (r.getFlightsID().contains(fID)) totalReservationsOfFlight++;
                }
            }
            if(totalReservationsOfFlight >= f.getnMaxPassengers()) throw new UnavailableFlightException();
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

         Map<String, Reservation> ans = new HashMap<>();
         if (this.clientReservations == null) this.clientReservations = new HashMap<>();
         if (this.clientReservations.containsKey(s)) {
             Set<String> reservationsIDS = this.clientReservations.get(s);

             for (String id : reservationsIDS) {
                 ans.put(id, this.reservations.get(id));
             }
         }
         return ans;

    }

    /**
     * {@inheritDoc}
     */
    public String getReservationsStringFromUser(String s){

        l.readLock().lock();
        try {
            Map<String, Reservation> reservations = getReservationsFromUser(s);
            StringBuilder ans = new StringBuilder();
            if (reservations != null) {
                for (String id : reservations.keySet()) {
                    int ac = 1;
                    Reservation r = reservations.get(id);
                    ans.append(Colors.ANSI_YELLOW + "Reservation ID: " + Colors.ANSI_RESET).append(r.getID()).append("\n");
                    ans.append(Colors.ANSI_YELLOW + "Date: " + Colors.ANSI_RESET).append(r.getDate()).append("\n");
                    Set<String> fIDs = r.getFlightsID();
                    for (String fID : fIDs) {
                        Flight f = this.flights.get(fID);
                        ans.append(Colors.ANSI_YELLOW + "Flight " + Colors.ANSI_RESET).append(ac++).append(":\n")
                                .append(Colors.ANSI_YELLOW + "From: " + Colors.ANSI_RESET).append(f.getOrigin()).append("\n")
                                .append(Colors.ANSI_YELLOW + "To: " + Colors.ANSI_RESET).append(f.getDestination()).append("\n")
                                .append(Colors.ANSI_YELLOW + "Flight ID: " + Colors.ANSI_RESET).append(f.getID()).append("\n\n");
                    }
                    ans.append(Colors.ANSI_CYAN + "------------------------------------------------------------------\n" + Colors.ANSI_RESET);
                }
            }
            return (ans.isEmpty()) ? (Colors.ANSI_YELLOW + "No reservations to show" + Colors.ANSI_RESET) : ans.toString();
        } finally {
            l.readLock().unlock();
        }

    }

    /**
     * {@inheritDoc}
     */
    public void removeReservationByClient(String reservationID, String username) throws DoesntExistReservationFromClientException {
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
                throw new DoesntExistReservationFromClientException("Doesn't exist any reservation with ID " + reservationID + "from you.");
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

    }

    /**
     * {@inheritDoc}
     */
    public String createReservation(String clientID, Set<String> flightsID, LocalDate date) throws FlightDoesntExistException, UnavailableFlightException, OnlyClosedDaysException, DayHasPassedException {
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


        for (String s : this.reservations.keySet()) {
            lastID = s;
            int current = Integer.parseInt(lastID.split("R")[1]);
            if (current > mostRecent) mostRecent = current;
        }
        if (lastID.equals("")) return "R" + 1;
        return "R" + (mostRecent + 1);
    }

    /**
     * Gets flight with a certain origin and destination
     * @param o Origin
     * @param d Destination
     * @return List of flights
     */
    public List<Flight> getFlightsWithOriginDestination(City o, City d){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getOrigin().equals(o) && f.getDestination().equals(d)) ans.add(f.clone());
        }
        return ans;
    }

    /**
     * Gets a available flights on a date
     * @param fs Flights
     * @param date Date
     * @return Flight
     */
    public Flight getAvailableFlightOnDate(List<Flight> fs, LocalDate date){
        Set<String> resIdsOnDate = this.reservationsInDate.get(date);
        Flight ans = null;
        for(Flight f : fs){
            int nRes = 0;
            if (resIdsOnDate != null) {
                for (String rID : resIdsOnDate)
                    if (this.reservations.get(rID).getFlightsID().contains(f.getID())) nRes++;
            }
            if (nRes < f.getnMaxPassengers()) {
                ans = f;
                break;
            }
        }
        return ans;
    }

    /**
     * Get the available routes between a data range
     * @param desiredCities Cities desired
     * @param begin Begin date
     * @param end Maximum end date
     * @throws OnlyClosedDaysException Exception to prevent the case of Only Closed days on date range
     * @throws UnavailableFlightException Exception to prevent unavailable flights to return the answer
     * @return Routes
     */
    public Map.Entry<LocalDate, Set<String>> getAvailableListOfFlightsInDataRange(List<City> desiredCities, LocalDate begin, LocalDate end) throws OnlyClosedDaysException, UnavailableFlightException{
        l.readLock().lock();
        try {
            Set<String> ans = new HashSet<>();
            LocalDate curDate = begin;
            boolean isOpenDay = false;
            while (!isOpenDay && (curDate.isBefore(end) || curDate.isEqual(end))) {
                if (this.closedDays.contains(curDate))
                    curDate = curDate.plusDays(1);
                else isOpenDay = true;
            }
            if (!isOpenDay) throw new OnlyClosedDaysException();
            else
                for (int i = 0; i < desiredCities.size() - 1; i++) {
                    City o = desiredCities.get(i);
                    City d = desiredCities.get(i + 1);
                    List<Flight> fs = getFlightsWithOriginDestination(o, d);
                    Flight f = getAvailableFlightOnDate(fs, curDate);
                    if (f == null) throw new UnavailableFlightException();
                    else ans.add(f.getID());
                }
            return new AbstractMap.SimpleEntry<>(curDate, ans);
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public String createReservationGivenCities(String username, List<City> desiredCities, LocalDate begin, LocalDate end) throws OnlyClosedDaysException, UnavailableFlightException, FlightDoesntExistException, DayHasPassedException{
        Map.Entry<LocalDate, Set<String>> flightsAndDate = getAvailableListOfFlightsInDataRange(desiredCities, begin, end);
        return createReservation(username, flightsAndDate.getValue(), flightsAndDate.getKey());
    }

    /*

            Closed days methods

     */

    /**
     * {@inheritDoc}
     */
    public void addClosedDay(LocalDate date) throws AlreadyIsAClosedDayException, DayHasPassedException{
        l.writeLock().lock();
        try {
            if(date.isBefore(LocalDate.now())) throw new DayHasPassedException();
            if (!this.closedDays.contains(date)) {
                this.closedDays.add(date);
                if (this.reservationsInDate.get(date) != null) {
                    Set<String> rIDsOnDate = new HashSet<>(this.reservationsInDate.get(date));
                    for (String id : rIDsOnDate)
                        try {
                            removeReservation(id);
                        } catch (Exception ignored) {}
                    this.reservationsInDate.remove(date);
                }
            } else throw new AlreadyIsAClosedDayException("The selected day is already closed.");
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeClosedDay(LocalDate date) throws NotAClosedDayException {
        l.writeLock().lock();
        try {
            if (!this.closedDays.contains(date)) throw new NotAClosedDayException();
            this.closedDays.remove(date);
        } finally {
            l.writeLock().unlock();
        }
    }

    /**
     * Get the flights from one city
     * @param c City
     * @return List of flights
     */
    public List<Flight> getFlightsFromCity(City c){
        l.readLock().lock();
        try {
            List<Flight> ans = new ArrayList<>();
            for (String id : this.flights.keySet()) {
                Flight f = this.flights.get(id);
                if (f.getOrigin().equals(c)) ans.add(f.clone());
            }
            return ans;
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Get flight to a city
     * @param c City
     * @return List of flights
     */
    public List<Flight> getFlightsToCity(City c){
        l.readLock().lock();
        try {
            List<Flight> ans = new ArrayList<>();
            for (String id : this.flights.keySet()) {
                Flight f = this.flights.get(id);
                if (f.getDestination().equals(c)) ans.add(f.clone());
            }
            return ans;
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Get flights without the cities mentioned
     * @param c1 City
     * @param c2 City
     * @return Flights
     */
    public Set<Flight> getFlightsWithoutCities(City c1, City c2){
        l.readLock().lock();
        try {
            Set<Flight> ans = new HashSet<>();
            for (String id : this.flights.keySet()) {
                Flight f = this.flights.get(id);
                if (f.getDestination() != c1 && f.getDestination() != c2 && f.getOrigin() != c1 && f.getOrigin() != c2)
                    ans.add(f.clone());
            }
            return ans;
        } finally {
            l.readLock().unlock();
        }
    }

    /**
     * Get flights with one stop
     * @param origin Origin
     * @param destination Destination
     * @return Flights with one stop
     */
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
                            toAdd.add(fl.clone());
                            toAdd.add(f.clone());
                            ans.add(toAdd);
                        }
                }
        }
        return ans;
    }

    /**
     * Get flights with two stops
     * @param origin Origin
     * @param destination Destination
     * @return Flights
     */
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

    /**
     * Get routes with maximum of 2 stops
     * @param origin Origin
     * @param destination Destination
     * @return Flights
     */
    public List<List<Flight>> getRoutesWithMaximum2Stops(City origin, City destination){
        List<List<Flight>> ans = new ArrayList<>();

        List<Flight> flights = getFlightsWithOriginDestination(origin, destination);
        if(flights != null && !flights.isEmpty())
            for(Flight f : flights){
                List<Flight> toAdd = new ArrayList<>();
                toAdd.add(f.clone());
                ans.add(toAdd);
            }

        List<List<Flight>> flights1stop = getFlightsWithOneStop(origin, destination);
        if(flights1stop != null && !flights1stop.isEmpty())
            ans.addAll(flights1stop);

        List<List<Flight>> flights2stop = getFlightsWithTwoStops(origin, destination);
        if(flights2stop != null && !flights2stop.isEmpty())
            ans.addAll(flights2stop);

        return ans;
    }

    /**
     * {@inheritDoc}
     */
    public String getRoutes(City origin, City destination){
        List<List<Flight>> routes = getRoutesWithMaximum2Stops(origin, destination);
        StringBuilder ans = new StringBuilder();
        if(routes != null) {
            for (List<Flight> fls : routes) {
                for (Flight f : fls)
                    ans.append("<-> Flight ").append(f.getID()).append(": ").append(f.getOrigin()).append("->").append(f.getDestination()).append("\n");
                ans.append("-------------------------------------------------------\n");
            }
        }
        return (!ans.isEmpty()) ? ans.toString() : "No flights to show";
    }

    /*

            Serializing

     */

    /**
     * {@inheritDoc}
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


}
