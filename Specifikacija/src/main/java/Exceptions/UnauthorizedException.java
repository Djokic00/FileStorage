package Exceptions;

public class UnauthorizedException extends Exception{

    private String message = "Error: Unauthorized Action!";

    @Override
    public String getMessage() {
        return message;
    }
}
