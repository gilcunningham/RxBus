package rxbus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Gil.Cunningham on 4/28/2017.
 */

public class Subscriber {

    private Object receiver;
    private Method method;
    private Class event;

    protected Subscriber(Object receiver, Method method, Class event) {
        this.receiver = receiver;
        this.method = method;
        this.event = event;
    }

    protected Object getReceiver() {
        return receiver;
    }

    protected Method getMethod() {
        return method;
    }

    protected Class getEvent() { return event; }

    protected Object invoke(Object param) {
        try {
            return method.invoke(receiver, param);
        }
        catch (IllegalAccessException iae) {
            iae.printStackTrace();

            // log this
        }
        catch (InvocationTargetException ite) {
            ite.printStackTrace();

            // log this
        }
        return null;
    }
    @Override
    public boolean equals(Object another) {
        System.out.println("*** CHECK EQUALS " + this + " equals " + another);
        if (another instanceof Subscriber) {
            return (getId().equals(((Subscriber) another).getId()));
        }
        return false;
    }

    public String getId() {
        return receiver.getClass().getName() + ":" + method.getName() + ":" + event.getName();
    }
}
