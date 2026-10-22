package roadsafe;


public class RoadSafeException extends Exception {

    public RoadSafeException(String message) {
        super(message);
    }

    
    public RoadSafeException(String message, Throwable cause) {
        super(message, cause);
    }
}
