package demo.rxbus;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.logging.Logger;

import demo.rxbus.event.ActivityEvent;
import demo.rxbus.event.ActivityToServiceEvent;
import demo.rxbus.event.ServiceToActivityEvent;
import rxbus.RxBus;
import rxbus.annotation.Subscribe;

public class MainActivity extends AppCompatActivity {

    private static Logger log = Logger.getGlobal();

    private Button launchActivityBtn;
    private Button startServiceBtn;
    private Button stopServiceBtn;
    private Button pingServiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        RxBus.subscribe(this);

        launchActivityBtn = (Button)findViewById(R.id.launchActivityBtn);
        launchActivityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AnotherActivity.class);
                startActivity(i);
            }
        });
        startServiceBtn = (Button)findViewById(R.id.startServceBtn);
        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });

        stopServiceBtn = (Button)findViewById(R.id.stopServceBtn);
        stopServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });

        pingServiceBtn = (Button)findViewById(R.id.pingServceBtn);
        pingServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxBus.publish(new ActivityToServiceEvent("*** Hello from " + getClass().getName()));
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    private void startService() {
        Intent i = new Intent(this, DemoService.class);
        startService(i);
    }

    private void stopService() {
        Intent i = new Intent(this, DemoService.class);
        stopService(i);
    }

    @Subscribe
    public void onActivityEvent(ActivityEvent ae) {
        log.info("In " + getClass().getName() + " onActivityEvent() ActivityEvent.message = " + ae.getMessage());
    }

    @Subscribe
    public void onServiceEvent(ServiceToActivityEvent fse) {
        log.info("In " + getClass().getName() + " onServiceEvent() ServiceToActivityEvent.message = " + fse.getMessage());
    }
}
