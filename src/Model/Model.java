package Model;

import Client.Client;
import Client.User;
import Exceptions.UsernameAlreadyExistsException;

import java.util.HashMap;
import java.util.Map;

public class Model {
    private Map<String, User> users;
    private Map<String, Flight> flights;
    private Map<String, Reservation> reservations;

    public Model() {
        this.users = new HashMap<>();
        this.flights = new HashMap<>();
        this.reservations = new HashMap<>();
    }

    public Model(Map<String, User> users, Map<String, Flight> flights, Map<String, Reservation> reservations){
        this.setUsers(users);
        this.setFlights(flights);
        this.setReservations(reservations);
    }

    public void setUsers(Map<String, User> users){
        this.users = new HashMap<>();
        if(!users.isEmpty())
            for(String username : users.keySet())
                this.users.put(username, users.get(username));
    }

    public void setFlights(Map<String, Flight> flights){
        this.flights = new HashMap<>();
        if(!flights.isEmpty())
            for(String code : flights.keySet())
                this.flights.put(code, flights.get(code));
    }

    public void setReservations(Map<String, Reservation> reservations){
        this.reservations = new HashMap<>();
        if(!reservations.isEmpty())
            for(String code : reservations.keySet())
                this.reservations.put(code, reservations.get(code));
    }

    public Map<String, User> getUsers(){
        Map<String, User> users = new HashMap<>();
        if(!this.users.isEmpty())
            for(String username : this.users.keySet())
                users.put(username, this.users.get(username));
        return users;
    }

    public Map<String, Flight> getFlights(){
        Map<String, Flight> flights = new HashMap<>();
        if(!this.flights.isEmpty())
            for(String code : this.flights.keySet())
                flights.put(code, flights.get(code));
        return flights;
    }

    public Map<String, Reservation> getReservations(){
        Map<String, Reservation> reservations = new HashMap<>();
        if(!this.reservations.isEmpty())
            for(String code : this.reservations.keySet())
                reservations.put(code, reservations.get(code));
        return reservations;
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
                boolean added = addReserve(reservationsFromUser.get(code));
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
        Flight f = this.flights.get(r.getFlightID());
        if(f.hasFreeSpace()){
            f.addOneReservation();
            this.reservations.put(r.getID(), r);
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
                if(r.isToFlight(s)) ans.put(code, r);
            }
        return ans;
    }

    public Map<String, Reservation> getReservationsFromUser(String s){
        Map<String, Reservation> ans = new HashMap<>();
        if(!this.reservations.isEmpty())
            for(String code : this.reservations.keySet()){
                Reservation r = this.reservations.get(code);
                if(r.isFromUser(s)) ans.put(code, r);
            }
        return ans;
    }

    public boolean removeFlight(String s){
        if(this.flights.containsKey(s)){
            Map<String, Reservation> reservationsOfFlight = getReservationsFromFlight(s);
            if(!reservationsOfFlight.isEmpty())
                for(String code : reservationsOfFlight.keySet())
                    removeReserve(code);
            this.flights.remove(s);
            return true;
        }
        return false;
    }

    public boolean removeReserve(String s){
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
                    String flightID = r.getFlightID();
                    Flight f = this.getFlight(flightID);
                    if(f.getToGo()) f.removeOneReservation();
                    removeReserve(code);
                }
            this.users.remove(ID);
            return true;
        }
        return false;
    }

    public boolean createFlight(String ID, int nMaxPassengers, int nReserve, String origin, String destination, boolean toGo){
        if(flights.containsKey(ID)) return false;
        Flight f = new Flight(ID, nMaxPassengers, nReserve, origin, destination, toGo);
        this.flights.put(f.getID(), f);
        return true;
    }

    public boolean createReservation(String ID, String clientID, String flightID){
        Flight f = getFlight(flightID);
        if(reservations.containsKey(ID) || !f.hasFreeSpace()) return false;
        Reservation r = new Reservation(ID, clientID, flightID);
        Client c = (Client) this.users.get(clientID);
        Map<String, Reservation> clientReserves = c.getReservations();
        if (!clientReserves.containsKey(r.getID())) c.addReservation(r);
        f.addOneReservation();
        this.reservations.put(r.getID(), r);
        return true;
    }

}
