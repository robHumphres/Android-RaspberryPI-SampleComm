package com.example.roberthumphres.bluetoothconnecttopie;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.roberthumphres.bluetoothconnecttopie.Bluetooth.BluetoothConnection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BluetoothConnection.BluetoothListener {

    public static BluetoothSocket   mmSocket;
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothDevice mBluetoothDevice = null;
    public static boolean bluetoothEstablished = false;
    public static boolean endReadThread = true;
    public BluetoothConnection connection;
    private Set<BluetoothDevice> pairedDevices;
    private Button button_sendMSG, button_turnOn,button_ConnectBt,button_listDevices;
    ListView lv;
    private boolean receiveThreadRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        button_ConnectBt = (Button)findViewById(R.id.button_establishConnection);
        button_sendMSG = (Button)findViewById(R.id.button3);
        button_turnOn = (Button)findViewById(R.id.button_TurnOnBT);
        button_listDevices = (Button) findViewById(R.id.button_findDevice);
        lv = (ListView) findViewById(R.id.listView);

        button_sendMSG.setEnabled(false);


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

    @Override
    protected void onStart() {
        super.onStart();
        if(mBluetoothAdapter == null) {
            Toast.makeText(this, "This device isn't capable of bluetooth.... ", Toast.LENGTH_LONG).show();
            button_listDevices.setEnabled(false);
            button_sendMSG.setEnabled(false);
            button_ConnectBt.setEnabled(false);
            button_turnOn.setEnabled(false);

        }
    }

    public void connectToSocket(View view){
        if (mBluetoothDevice != null) {
            connection = new BluetoothConnection(mBluetoothDevice,this);
        }
    }

    public void turnOnBT(View v){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "It's already on....", Toast.LENGTH_SHORT).show();

        }

    }

    public void listDevices(View v){
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices) {
            list.add(bt.getName());//gets devices name.


            if(bt.getName().contains("raspberry")) {
                mBluetoothDevice = bt;
                Toast.makeText(this,"Found the connection",Toast.LENGTH_LONG).show();
            }

        }
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);
    }

    public void sendMsg(View v){

        //check to see if its enabled
        if(!mBluetoothAdapter.isEnabled())
            turnOnBT(v);


        EditText edit = (EditText) findViewById(R.id.editText_MessageToSend);
        String welcomeMsg = edit.getText().toString();
        byte[] biteMe = welcomeMsg.getBytes(StandardCharsets.UTF_8);
        connection.write(biteMe);
        


    }

    //Callback function inside BluetoothConnection to alert me when it has connected
    @Override
    public void connectionMade() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"Connection Made",Toast.LENGTH_LONG).show();
                button_sendMSG.setEnabled(true);

            }
        });

        connection.run(this);
        System.out.println("Made it out of the isDone() interface call");

    }

    @Override
    public synchronized void msgReceived(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"Received Message " + s,Toast.LENGTH_SHORT).show();
                System.out.println("Message receieved....");
            }
        });
    }
}
