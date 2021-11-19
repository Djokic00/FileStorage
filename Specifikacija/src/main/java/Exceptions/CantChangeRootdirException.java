package Exceptions;

public class CantChangeRootdirException extends Exception{

    String message = "Error: Cannot change rootDirectory";

    public CantChangeRootdirException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

}
