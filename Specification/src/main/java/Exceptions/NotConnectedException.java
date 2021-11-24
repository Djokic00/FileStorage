package Exceptions;

public class NotConnectedException extends Exception{

    private String message = "Error: Connection failed";

    @Override
    public String getMessage() {
        return message;
    }

}
