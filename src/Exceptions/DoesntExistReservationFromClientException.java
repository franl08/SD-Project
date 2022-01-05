package Exceptions;

/**
 * Exception that happens when the client does not have the reservation that is being removed
 */
public class DoesntExistReservationFromClientException extends Exception{

    /**
     * Exception constructor
     * @param s Explains the exception
     */
    public DoesntExistReservationFromClientException(String s){
        super(s);
    }
}
