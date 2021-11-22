package Exceptions;

public class EmailAlreadyExistsException extends Exception{
    public EmailAlreadyExistsException(){
        super();
    }

    public EmailAlreadyExistsException(String m){
        super(m);
    }
}
