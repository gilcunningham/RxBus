package demo.rxbus.event;

/**
 * Created by gil.cunningham on 6/14/2017.
 */

public abstract class Event {

    String message;

    public Event(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
