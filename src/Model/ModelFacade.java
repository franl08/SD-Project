package Model;

import Exceptions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Interface that allows managing the flights, reservations and accounts data
 */
public interface ModelFacade {

    /**
     * Gets the closed days
     * @return Closed days in formatted string
     */
    String getClosedDays();

    /**
     * Validates the credentials inserted
     * @param username Email of the user
     * @param password Password
     * @return True if the credentials are valid, otherwise False
     */
    boolean checkAuthentication(String username, String password);

    /**
     * Adds a client to the clients map
     * @param email Email
     * @param password Password
     * @throws EmailAlreadyExistsException There already is a client registered under that email
     * @throws NotAnEmailException Invalid email format
     */
    void addClient(String email, String password) throws EmailAlreadyExistsException, NotAnEmailException;

    /**
     * Gets the flights existent in a formatted string
     * @return String that holds the flights information
     */
    String getFlightsString();

    /**
     * Creates a flight
     * @param nMaxPassengers Maximum number of passengers
     * @param origin City of origin
     * @param destination City of destination
     * @return ID of the flight generated
     */
    String createFlight(int nMaxPassengers, City origin, City destination);

    /**
     * Formats the reservations by a user to a string
     * @param s User ID
     * @return Formatted string with the reservations
     */
    String getReservationsStringFromUser(String s);

    /**
     * Removes a reservation, by request of a client
     * @param reservationID ID of the reservation
     * @param username Email of the client
     * @throws DoesntExistReservationFromClientException Client doesn't have a reservation with that ID
     */
    void removeReservationByClient(String reservationID, String username) throws DoesntExistReservationFromClientException;

    /**
     * Creates a reservation
     * @param clientID ID of the client
     * @param flightsID List of flight IDS
     * @param date Date of the reservation
     * @return Reservation code
     * @throws FlightDoesntExistException A flight does not exist
     * @throws UnavailableFlightException A flight is unavailable
     * @throws OnlyClosedDaysException Selected date is a closed day
     * @throws DayHasPassedException Selected date is in the past
     */
    String createReservation(String clientID, Set<String> flightsID, LocalDate date) throws FlightDoesntExistException, UnavailableFlightException, OnlyClosedDaysException, DayHasPassedException;

    /**
     * Creates reservations according to a list of cities
     * @param username ID of the client
     * @param desiredCities Cities to travel through
     * @param begin Beginning of the date range
     * @param end End of the date range
     * @return Reservation rode
     * @throws OnlyClosedDaysException Exception to prevent a reservation on a closed day
     * @throws UnavailableFlightException Exception to prevent a reservation of an unavailable flight
     * @throws FlightDoesntExistException Exception to prevent a reservation of a non-existing flight
     * @throws DayHasPassedException Exception to prevent a reservation on a past date
     */
    String createReservationGivenCities(String username, List<City> desiredCities, LocalDate begin, LocalDate end) throws OnlyClosedDaysException, UnavailableFlightException, FlightDoesntExistException, DayHasPassedException;

    /**
     * Adds a closed day, removing the flights on that day
     * @param date Date
     * @throws AlreadyIsAClosedDayException Already is a closed day
     * @throws DayHasPassedException Exception to prevent a closed day insertion on a past date
     */
    void addClosedDay(LocalDate date) throws AlreadyIsAClosedDayException, DayHasPassedException;

    /**
     * Removes a closed day
     * @param date Date
     * @throws NotAClosedDayException The date is not closed
     */
    void removeClosedDay(LocalDate date) throws NotAClosedDayException;

    /**
     * Gets the possible routes between two cities
     * @param origin Origin
     * @param destination Destination
     * @return Formatted string with the options
     */
    String getRoutes(City origin, City destination);

    /**
     * Serializes the model to a filepath
     * @param filepath Filepath
     * @throws IOException File manipulation
     */
    void serialize(String filepath) throws IOException;

    /**
     * Deserializes a model from a filepath
     * @param filepath Filepath
     * @return Model deserialized
     * @throws IOException I/O errors from file manipulation
     * @throws ClassNotFoundException The class of the serialized object cannot be found
     */
    static ModelFacade deserialize(String filepath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filepath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Model accounts = (Model) ois.readObject();
        ois.close();
        fis.close();
        return accounts;
    }
}
