package Exceptions;

public class NotConnectedException extends Exception{

    String message = "Error: Connection failed";

    public NotConnectedException() {
    }

    @Override
    public String getMessage() {
        return message;
    }

}
