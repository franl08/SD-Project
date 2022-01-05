package Exceptions;

/**
 * Exception that happens when there is an attempt to add a closed day that already is a closed day
 */
public class AlreadyIsAClosedDayException extends Exception{

    /**
     * Exception constructor
     * @param s Explains the exception
     */
    public AlreadyIsAClosedDayException(String s){
        super(s);
    }
}
