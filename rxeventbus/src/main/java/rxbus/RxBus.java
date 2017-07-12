package rxbus;

import android.support.annotation.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import rxbus.annotation.Subscribe;

/**
 * @author gil.cunningham@gmail.com
 *
 * Simple event bus which leverages Rx to subscribe to and publish events.
 * Makes use of annotations to search receivers list of subscribed methods.
 *
 * Note:
 * The term "receiver" throughout refers to any class which includes the @Subscribe annotation
 * above a public method that takes a single parameter.
 */

public final class RxBus {

    // Observable by Class (event)
    private static final Map<Class, PublishSubject<Object>> eventObservables = new ConcurrentHashMap<>();
    // Subscriber by Class (event)
    private static final Map<Class, List<Subscriber>> eventSubscribers = new ConcurrentHashMap<>();
    // Subscription by receiver
    private static final Map<Object, List<Disposable>> receiverSubscriptions = new ConcurrentHashMap<>();

    private static final Logger log = Logger.getGlobal();

    /*
     * Subscribes a receiver
     * @param Object receiver
     * <p>
     * Subscribe to SomeMessage event in a receiver:
     * <b>
     * @Subscribe
     * public void someMethod(SomeMessage msg) {
     * ... handle this msg
     * }
     * </b>
     * </p>
     */
    public static void subscribe(@NonNull Object receiver) {

        List<Subscriber> subscribers = getAllSubscribers(receiver);

        for (Subscriber subscriber : subscribers) {

            System.out.println("*** adding " + subscriber.getMethod().getName() + " " + subscriber.getEvent().getName());

            if (!isSubscribed(subscriber)) {
                Disposable subscription = getSubject(subscriber.getEvent()).subscribe(getConsumer());
                getReceiverSubscription(receiver).add(subscription);
            }
            // add Subscriber
            addSubscriber(subscriber);
        }
    }

    /**
     * Publishes a message
     * @param message
     *
     * Publish a SomeMessage event to receiver(s):
     *
     * <b>
     * RxBus.publish(new SomeMesssage("Here is the message"));
     * </b>
     */
    public static void publish(@NonNull Object message) {
        getSubject(message.getClass()).onNext(message);
    }

    /**
     * Unsubscribes a receiver:
     *
     * <b>
     * RxBus.unsubscribe(this); // where this is the receiver
     * </b>
     */
    public static void unsubscribe(@NonNull Object receiver) {
        // remove all receivers subscribers
        List<Subscriber> receiverSubscribers = getAllSubscribers(receiver);

        for (Subscriber s : receiverSubscribers) {
            Class event = s.getEvent();
            List<Subscriber> subscribers = eventSubscribers.get(event);
            if (subscribers != null) {
                subscribers.remove(s);
                if (subscribers.isEmpty()) {
                    eventSubscribers.remove(event);
                }
            }
        }

        List<Disposable> subscriptions = receiverSubscriptions.get(receiver);

        // notify and move on
        if (subscriptions == null) {
            log.warning("Object " + receiver + " is not subscribed");
        }
        else {
            // unregisters all associated Subscriptions for this receiver
            for (Disposable disposable : subscriptions) {
                disposable.dispose();
            }
            // remove and clean subscription list
            subscriptions = receiverSubscriptions.remove(receiver);
            subscriptions.clear();
        }
    }

    /**
     * Get the subject or create it if it's not already in memory.
     */
    @NonNull
    private static PublishSubject<Object> getSubject(Class event) {
        PublishSubject<Object> subject = eventObservables.get(event);
        if (subject == null) {
            subject = PublishSubject.create();
            eventObservables.put(event, subject);
        }

        return subject;
    }

    private static boolean isSubscribed(Subscriber subscriber) {
        return eventObservables.get(subscriber.getEvent()) != null;
    }

    /**
     * Get the CompositeSubscription or create it if it's not already in memory.
     */
    @NonNull
    private static List<Disposable> getReceiverSubscription(@NonNull Object receiver) {
        List<Disposable> subscriptions = receiverSubscriptions.get(receiver);
        if (subscriptions == null) {
            subscriptions = new ArrayList<>();
            receiverSubscriptions.put(receiver, subscriptions);
        }
        return subscriptions;
    }

    private static List<Subscriber> getAllSubscribers(Object receiver) {

        List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

        for (Method method : receiver.getClass().getDeclaredMethods()) {
            // The compiler sometimes creates synthetic bridge methods as part of the
            // type erasure process. As of JDK8 these methods now include the same
            // annotations as the original declarations. They should be ignored.
            if (method.isBridge()) {
                continue;
            }
            if (method.isAnnotationPresent(Subscribe.class)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation but requires "
                            + parameterTypes.length + " arguments.  Method requires a single argument.");
                }

                Class<?> param = parameterTypes[0];
                if (param.isInterface()) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + param
                            + " which is an interface.  Subscription must be on a concrete class type.");
                }

                if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException("Method " + method + " has @Subscribe annotation on " + param
                            + " but is not 'public'.");
                }

                subscribers.add(new Subscriber(receiver, method, param));
            }
        }

        return subscribers;
    }

    private static void addSubscriber(Subscriber subscriber) {
        List<Subscriber> subscribers = eventSubscribers.get(subscriber.getEvent());

        if (subscribers == null) {
            subscribers = new CopyOnWriteArrayList<>();
            eventSubscribers.put(subscriber.getEvent(), subscribers);
        }

        if (subscribers.contains(subscriber)) {
            throw new RuntimeException("Subscriber " + subscriber.getReceiver().getClass() + " " +
                    "already registered for argument types " + subscriber.getEvent());
        }

        subscribers.add(subscriber);
    }

    // Generic Action to lookup Subscribers by type and call Subscriber's invoke()
    private static Consumer getConsumer() {
        return new Consumer() {
            @Override
            public void accept(Object event) {
                // get {@link List} of {@link Subscriber}s for this event
                List<Subscriber> subscribers = eventSubscribers.get(event.getClass());

                // invoke event for Subscriber
                for (Subscriber s : subscribers) {
                    Object res = s.invoke(event);
                }
            }
        };
    }
}
