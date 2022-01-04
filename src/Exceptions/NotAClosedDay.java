package Exceptions;

/**
 * Exception that happens when trying to remove a closed day that isn't a closed day
 */
public class NotAClosedDay extends Exception{

    /**
     * Empty exception constructor
     */
    public NotAClosedDay(){
        super();
    }

}
