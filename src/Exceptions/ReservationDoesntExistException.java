package Exceptions;

/**
 * Exception that happens when a reservation doesn't exist
 */
public class ReservationDoesntExistException extends Exception{

    /**
     * Exception constructor
     * @param s String that explains an exception
     */
    public ReservationDoesntExistException(String s){
        super(s);
    }
}
