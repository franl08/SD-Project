package Exceptions;

/**
 * Exception that happens when a client attempts to schedule a trip to the a date that has passed
 */
public class DayHasPassedException extends Exception {

    /**
     * Exception empty constructor
     */
    public DayHasPassedException() {}
}
