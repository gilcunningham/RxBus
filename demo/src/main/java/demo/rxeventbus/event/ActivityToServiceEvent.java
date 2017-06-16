package demo.rxeventbus.event;

/**
 * Created by gil.cunningham on 6/7/2017.
 */

public class ActivityToServiceEvent extends Event {

    public ActivityToServiceEvent(String message) {
        super(message);
    }
}

