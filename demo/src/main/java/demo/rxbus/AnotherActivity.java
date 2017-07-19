package demo.rxbus;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.logging.Logger;

import demo.rxbus.event.ActivityToActivityEvent;
import demo.rxbus.event.ServiceToActivityEvent;
import demo.rxbus.event.ActivityToServiceEvent;
import rxbus.RxBus;
import rxbus.annotation.Subscribe;

/**
 * Created by gil.cunningham on 6/7/2017.
 */

public class AnotherActivity extends AppCompatActivity {

    private static Logger log = Logger.getGlobal();

    private Button pingActivityBtn;
    private Button pingServiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_another);

        RxBus.subscribe(this);

        pingActivityBtn = (Button)findViewById(R.id.pingActivityBtn);
        pingActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("*** PING ACTIVITY 1");
                RxBus.publish(new ActivityToActivityEvent("*** message from Activity 2"));
            }
        });

        pingServiceBtn = (Button)findViewById(R.id.pingServiceBtn);
        pingServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("*** PING SERVICE");
                RxBus.publish(new ActivityToServiceEvent("*** Hello from " + getClass().getName()));
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        RxBus.unsubscribe(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onServiceEvent(ServiceToActivityEvent fse) {
        log.info("In " + getClass().getName() + " onServiceEvent() ServiceToActivityEvent.message = " + fse.getMessage());
    }

}

