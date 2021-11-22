package Exceptions;

public class UsernameAlreadyExistsException extends Exception{
    public UsernameAlreadyExistsException(){
        super();
    }

    public UsernameAlreadyExistsException(String m){
        super(m);
    }
}
