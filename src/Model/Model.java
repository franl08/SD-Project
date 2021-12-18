package Model;

import Client.Client;
import Client.User;
import Utils.City;
import Utils.Utilities;

import java.time.LocalDate;
import java.util.*;

public class Model {
    private Map<String, User> users;
    private Map<String, Flight> flights;
    private Map<String, Reservation> reservations;
    private Set<LocalDate> closedDays; // Point 4. of basic functionalities in utterance

    public Model() {
        this.users = new HashMap<>();
        this.flights = new HashMap<>();
        this.reservations = new HashMap<>();
        this.closedDays = new HashSet<>();
    }

    public Model(Map<String, User> users, Map<String, Flight> flights, Map<String, Reservation> reservations, Set<LocalDate> closedDays){
        this.setUsers(users);
        this.setFlights(flights);
        this.setReservations(reservations);
    }

    public void setUsers(Map<String, User> users){
        this.users = new HashMap<>();
        if(!users.isEmpty())
            for(String username : users.keySet())
                this.users.put(username, users.get(username).clone());
    }

    public void setFlights(Map<String, Flight> flights){
        this.flights = new HashMap<>();
        if(!flights.isEmpty())
            for(String code : flights.keySet())
                this.flights.put(code, flights.get(code).clone());
    }

    public void setReservations(Map<String, Reservation> reservations){
        this.reservations = new HashMap<>();
        if(!reservations.isEmpty())
            for(String code : reservations.keySet())
                this.reservations.put(code, reservations.get(code).clone());
    }

    public Map<String, User> getUsers(){
        Map<String, User> users = new HashMap<>();
        if(!this.users.isEmpty())
            for(String username : this.users.keySet())
                users.put(username, this.users.get(username).clone());
        return users;
    }

    public Map<String, Flight> getFlights(){
        Map<String, Flight> flights = new HashMap<>();
        if(!this.flights.isEmpty())
            for(String code : this.flights.keySet())
                flights.put(code, flights.get(code).clone());
        return flights;
    }

    public Map<String, Reservation> getReservations(){
        Map<String, Reservation> reservations = new HashMap<>();
        if(!this.reservations.isEmpty())
            for(String code : this.reservations.keySet())
                reservations.put(code, reservations.get(code).clone());
        return reservations;
    }

    public Set<LocalDate> getClosedDays(){
        return new HashSet<>(this.closedDays);
    }

    public void setClosedDays(Set<LocalDate> dates){
        this.closedDays = new HashSet<>();
        this.closedDays.addAll(dates);
    }

    public boolean checkAutentication(String username, String password){
        if(!users.containsKey(username)) return false;
        User u = users.get(username);
        return u.getPassword().equals(password);
    }

    public void addUser(User u){
        if(u.getClass().getName().equals("Client.Admin")) this.users.put(u.getUsername(), u);
        else{
            Client c = (Client) u;
            Map<String, Reservation> reservationsFromUser = c.getReservations();
            for(String code : reservationsFromUser.keySet()){
                boolean added = addReserve(reservationsFromUser.get(code).clone());
                if(!added)
                    c.removeReservation(code);
            }
            this.users.put(c.getUsername(), c);
        }
    }

    public void addFlight(Flight f){
        this.flights.put(f.getID(), f);
    }

    public boolean addReserve(Reservation r){
        Client c = (Client) this.users.get(r.getClientID());
        if(!c.getReservations().containsKey(r.getID())) c.addReservation(r);
        Set<String> flightsIDs = r.getFlightsID();
        if(checkSetOfFlightsToReservation(flightsIDs)){
            for(String fID : flightsIDs){
                this.flights.get(fID).addOneReservation();

            }
            this.reservations.put(r.getID(), r.clone());
            return true;
        }
        return false;
    }

    public Reservation getReserve(String s){
        return reservations.get(s);
    }

    public Flight getFlight(String s){
        return flights.get(s);
    }

    public Map<String, Reservation> getReservationsFromFlight(String s){
        Map<String, Reservation> ans = new HashMap<>();
        if(!this.reservations.isEmpty())
            for(String code : this.reservations.keySet()){
                Reservation r = this.reservations.get(code);
                if(r.isToFlight(s)) ans.put(code, r.clone());
            }
        return ans;
    }

    public Map<String, Reservation> getReservationsFromUser(String s){
        Map<String, Reservation> ans = new HashMap<>();
        if(!this.reservations.isEmpty())
            for(String code : this.reservations.keySet()){
                Reservation r = this.reservations.get(code);
                if(r.isFromUser(s)) ans.put(code, r.clone());
            }
        return ans;
    }

    public boolean removeFlight(String s){
        if(this.flights.containsKey(s)){
            Map<String, Reservation> reservationsOfFlight = getReservationsFromFlight(s);
            if(!reservationsOfFlight.isEmpty())
                for(String code : reservationsOfFlight.keySet())
                    removeReservation(code);
            this.flights.remove(s);
            return true;
        }
        return false;
    }

    public boolean removeReservation(String s){
        if(this.reservations.containsKey(s)) {
            String username = this.reservations.get(s).getClientID();
            if (this.users.containsKey(username)) {
                Client c = (Client) this.users.get(username);
                boolean removed = c.removeReservation(s);
                if (removed) {
                    this.reservations.remove(s);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteUser(String ID){
        if(this.users.containsKey(ID)) {
            Map<String, Reservation> reservationsOfUser = getReservationsFromUser(ID);
            if(!reservationsOfUser.isEmpty())
                for(String code : reservationsOfUser.keySet()){
                    Reservation r = reservationsOfUser.get(code);
                    Set<String> flightsID = r.getFlightsID();
                    boolean flag = true;
                    for(String fID : flightsID) {
                        Flight f = this.getFlight(fID);
                        if (!f.getToGo())
                            flag = false;
                            break;
                    }
                    if(flag){
                        for(String fID : flightsID){
                            Flight f = this.getFlight(fID);
                            f.removeOneReservation();
                        }
                        removeReservation(code);
                    }
                }
            this.users.remove(ID);
            return true;
        }
        return false;
    }

    public boolean createFlight(String ID, int nMaxPassengers, int nReserve, City origin, City destination, boolean toGo, LocalDate date){
        if(flights.containsKey(ID) || closedDays.contains(date)) return false;
        Flight f = new Flight(ID, nMaxPassengers, nReserve, origin, destination, toGo, date);
        this.flights.put(f.getID(), f);
        return true;
    }

    public boolean checkSetOfFlightsToReservation(Set<String> flightsID){
        for(String fID : flightsID){
            Flight f = this.flights.get(fID);
            if(!f.hasFreeSpace()) return false;
        }
        return true;
    }

    public boolean createReservation(String ID, String clientID, Set<String> flightsID){
        if(checkSetOfFlightsToReservation(flightsID) || reservations.containsKey(ID)) return false;
        Reservation r = new Reservation(ID, clientID, flightsID);
        Client c = (Client) this.users.get(clientID);
        Map<String, Reservation> clientReserves = c.getReservations();
        if (!clientReserves.containsKey(r.getID())) c.addReservation(r);
        for(String fId : flightsID){
            Flight f = this.flights.get(fId);
            f.addOneReservation();
        }
        this.reservations.put(r.getID(), r);
        return true;
    }

    public boolean addClosedDay(LocalDate date){
        if(!this.closedDays.contains(date)){
            this.closedDays.add(date);
            for(String key : flights.keySet()){
                Flight f = flights.get(key);
                if(f.getDate().equals(date)) removeFlight(key);
            }
            return true;
        }
        return false;
    }

    public void removeClosedDay(LocalDate date){
        this.closedDays.remove(date);
    }

    public List<Flight> getFlightsWithOriginAndDestination(City origin, City destination){
        List<Flight> flights = new ArrayList<>();
        for(String key : this.flights.keySet()){
            Flight f = this.flights.get(key);
            if(f.getOrigin().equals(origin) && f.getDestination().equals(destination)) flights.add(f);
        }
        return flights;
    }

    public List<Flight> getFlightsWithOriginDestinationAndDate(City origin, City destination, LocalDate date){
        List<Flight> flights = new ArrayList<>();
        for(String key : this.flights.keySet()){
            Flight f = this.flights.get(key);
            if(f.getOrigin().equals(origin) && f.getDestination().equals(destination) && f.getDate().equals(date)) flights.add(f);
        }
        return flights;
    }

    public List<Flight> getFlightsWithOriginDestinationAndDateRange(City origin, City destination, LocalDate begin, LocalDate end){
        List<Flight> flights = new ArrayList<>();
        for(String key : this.flights.keySet()){
            Flight f = this.flights.get(key);
            if(f.getOrigin().equals(origin) && f.getDestination().equals(destination) && Utilities.isInRange(begin, end, f.getDate())) flights.add(f);
        }
        return flights;
    }

    /*
    public Set<List<Flight>> getPossibleOptionsInList(List<List<Flight>> availableFlights){
        Set<List<Flight>> ans = new HashSet<>();
        for(List<Flight> listFlights : availableFlights){
            for(Flight f : listFlights){

            }
        }
    }

    public List<Flight> getAvailableFlightsInDataRange(List<City> desiredCities, LocalDate begin, LocalDate end){
        List<Flight> flights = new ArrayList<>();
        List<List<Flight>> listOfFlights = new ArrayList<>();
        City origin = desiredCities.get(0);
        for(int i = 0; i < desiredCities.size(); i++) {
            City destination = desiredCities.get(i + 1);
            List<Flight> flightsBetweenCities = getFlightsWithOriginDestinationAndDateRange(origin, destination, begin, end);
            origin = destination;
            listOfFlights.add(flightsBetweenCities);
        }

    }

     */

}
