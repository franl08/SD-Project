package Exceptions;

/**
 * Exception that happens when a user tries to register with an email that already exists
 */
public class EmailAlreadyExistsException extends Exception{

    /**
     * Empty exception constructor
     */
    public EmailAlreadyExistsException(){
        super();
    }
}
