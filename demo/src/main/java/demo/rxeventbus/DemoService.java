package demo.rxeventbus;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.logging.Logger;

import demo.rxeventbus.event.ActivityToServiceEvent;
import demo.rxeventbus.event.ServiceToActivityEvent;
import rxeventbus.RxEventBus;
import rxeventbus.annotation.Subscribe;

/**
 * Created by gil.cunningham on 6/7/2017.
 */

public class DemoService extends Service {

    private static Logger log = Logger.getGlobal();

    private DemoThread dt;

    @Override
    public void onCreate() {
        super.onCreate();

        log.info("DemoService started()");

        RxEventBus.subscribe(this);

        dt = new DemoThread();
        dt.start();
    }

    class DemoThread implements Runnable {

        Thread t;
        boolean alive = false;

        void start() {
            RxEventBus.publish(new ServiceToActivityEvent("*** DemoService.starting()"));

            alive = true;
            t = new Thread(this);
            t.start();
        }

        public void run() {
            while (alive) {
                RxEventBus.publish(new ServiceToActivityEvent("*** DemoService.run()"));

                try { Thread.sleep(10000); }
                catch (Exception e) {}
            }
        }

        void stop() {
            RxEventBus.publish(new ServiceToActivityEvent("*** DemoService.stopping()"));
            t.interrupt();
            alive = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        RxEventBus.unsubscribe(this);

        if (dt != null) {
            dt.stop();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Subscribe
    public void onActivityEvent(ActivityToServiceEvent ae) {
        log.info("In " + getClass().getName() + " onActivityEvent() ActivityEvent.message = " + ae.getMessage());

        RxEventBus.publish("*** PING BACK - RECEIVED " + ae.getMessage());
    }

}
