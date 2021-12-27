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

    public boolean lookupUser(String username) {
        return users.containsKey(username);
    }

    public void addClient(String username, String password, String email, String fullName) {
        this.users.put(username, new Client(username, email, fullName, password));
    }

    public void addUser(User u){
        if(u.getClass().getName().equals("Client.Admin")) this.users.put(u.getUsername(), u);
        else{
            Client c = (Client) u;
            Map<String, Reservation> reservationsFromUser = c.getReservations();
            for(String code : reservationsFromUser.keySet()){
                boolean added = addReservation(reservationsFromUser.get(code).clone());
                if(!added)
                    c.removeReservation(code);
            }
            this.users.put(c.getUsername(), c);
        }
    }

    public void addFlight(Flight f){
        this.flights.put(f.getID(), f);
    }

    public boolean addReservation(Reservation r){
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

    public Reservation getReservation(String s){
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

    public boolean removeReservationByClient(String s, String currentUser){
        if(this.reservations.containsKey(s)) {
            String username = this.reservations.get(s).getClientID();
            if (this.users.containsKey(username) && currentUser.equals(username)) {
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

    public boolean removeReservation(String s) {
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

    // QUESTION 5 feita à padeiro -> pra já, dá para até 2 escalas, mas isto tá muito ineficiente (estupidamente), penso que tbm devia-se evitar os cases, mas não tou a ver como fazer

    public List<Flight> getFlightsAvailableForReservationFromList(List<Flight> fs){
        List<Flight> ans = new ArrayList<>();
        for(Flight f : fs){
            if(this.flights.get(f.getID()).hasFreeSpace() && this.flights.get(f.getID()).getToGo()) ans.add(f);
        }
        return ans;
    }

    public List<Route> getAvailableRoutesInDataRange(List<City> desiredCities, LocalDate begin, LocalDate end){
        List<Route> routes = new ArrayList<>();
        switch(desiredCities.size() - 2){
            case 0:{
                List<Flight> fs = getFlightsWithOriginDestinationAndDateRange(desiredCities.get(0), desiredCities.get(1), begin, end);
                List<Flight> availables = getFlightsAvailableForReservationFromList(fs);
                routes.add(new Route(desiredCities.get(0), desiredCities.get(1), availables));
            }
            case 1:{
                List<List<Flight>> fs = getFlightsWithOneStopOnSameDateWithDateRange(desiredCities.get(0), desiredCities.get(2), begin, end);
                for(List<Flight> list : fs){
                    List<Flight> availables = getFlightsAvailableForReservationFromList(list);
                    Route r = new Route(desiredCities.get(0), desiredCities.get(1), availables);
                    routes.add(r);
                }
            }
            case 2:{
                List<List<Flight>> fs = getFlightsWithTwoStopsOnSameDateWithDateRange(desiredCities.get(0), desiredCities.get(2), begin, end);
                for(List<Flight> list : fs){
                    List<Flight> availables = getFlightsAvailableForReservationFromList(list);
                    Route r = new Route(desiredCities.get(0), desiredCities.get(1), availables);
                    routes.add(r);
                }
            }

            // Vale a pena fazer para mais paragens? PLS ALGUÉM SAIBA UMA MANEIRA DE FAZER ISTO DE FORMA + EFICIENTE!!!!!
        }
        return routes;
    }

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
            if(f.getOrigin().equals(c)) ans.add(f);
        }
        return ans;
    }

    public List<Flight> getFlightsToCity(City c){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getDestination().equals(c)) ans.add(f);
        }
        return ans;
    }

    public List<Flight> getFlightsToCityWithDateRange(City c, LocalDate begin, LocalDate end){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getDestination().equals(c) && Utilities.isInRange(begin, end, f.getDate())) ans.add(f);
        }
        return ans;
    }

    public List<Flight> getFlightsFromCityWithDateRange(City c, LocalDate begin, LocalDate end){
        List<Flight> ans = new ArrayList<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getOrigin().equals(c) && Utilities.isInRange(begin, end, f.getDate())) ans.add(f);
        }
        return ans;
    }


    public Set<Flight> getFlightsWithoutCities(City c1, City c2){
        Set<Flight> ans = new HashSet<>();
        for(String id : this.flights.keySet()){
            Flight f = this.flights.get(id);
            if(f.getDestination() != c1 && f.getDestination() != c2 && f.getOrigin() != c1 && f.getOrigin() != c2) ans.add(f);
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

}
