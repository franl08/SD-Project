package Exceptions;

/**
 * Exception that happens when trying to remove a closed day that isn't a closed day
 */
public class NotAClosedDayException extends Exception{

    /**
     * Empty exception constructor
     */
    public NotAClosedDayException(){
        super();
    }

}
