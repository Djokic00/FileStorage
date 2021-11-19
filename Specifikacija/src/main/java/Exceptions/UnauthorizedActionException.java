package Exceptions;

public class UnauthorizedActionException extends Exception{

    String message = "Error: Unautorized Action!";

    public UnauthorizedActionException() {
    }

    @Override
    public String getMessage() {
        return message;
    }
}
