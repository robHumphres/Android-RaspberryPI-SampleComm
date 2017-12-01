package com.example.roberthumphres.bluetoothconnecttopie.Bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.example.roberthumphres.bluetoothconnecttopie.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.example.roberthumphres.bluetoothconnecttopie.MainActivity.mBluetoothAdapter;

/**
 * Created by roberthumphres on 10/10/17.
 */

public class BluetoothConnection extends Thread{
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private int numberOfBytes;
    byte[] buffer;

    // Unique UUID for this application, you may use different
    public UUID MY_UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID

    public interface BluetoothListener{
        void connectionMade();
        void msgReceived(String s);
    }

    public BluetoothConnection(BluetoothDevice device, final BluetoothListener callBack) {

        BluetoothSocket tmp = null;

        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        // TODO: 10/22/17 Make a call back saying the device wasn't able to pair.... hit pair button?
        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;

        //now make the socket connection in separate thread to avoid FC
        Thread connectionThread  = new Thread(new Runnable() {

            @Override
            public void run() {
                // Always cancel discovery because it will slow down a connection
                mBluetoothAdapter.cancelDiscovery();

                // Make a connection to the BluetoothSocket
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmSocket.connect();
                    callBack.connectionMade();

                } catch (IOException e) {
                    //connection to device failed so close the socket
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });

        connectionThread.start();

        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = mmSocket.getInputStream();
            tmpOut = mmSocket.getOutputStream();
            buffer = new byte[1024];
        } catch (IOException e) {
            e.printStackTrace();
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run(final BluetoothListener listener) {
        System.out.println("Made it to run");
        Thread temp = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("made it inside the override run");
                int count = 0;
                while (true) {
                    if(count == 0){
                        System.out.println("Made it inside the while true");
                        count++;
                    }

                    if(!MainActivity.endReadThread)
                        break;

                    try {
                        //read the data from socket stream
                        numberOfBytes = mmInStream.read(buffer);
                        Log.d("BufferRead", "Number of bytes read: " + numberOfBytes);
                        String text = getReadSocket();
                        listener.msgReceived(text);
                        Log.d("Listener", "message: " + text);

                    } catch (Exception e) {
                        System.out.println("There was an io exception " + e.getMessage());
                        //an exception here marks connection loss
                        //send message to UI Activity
                        break;
                    }
                }
            }
        });
        temp.start();
        System.out.println("Thread was started");
    }

    public void write(byte[] buffer) {
        try {
            //write the data to socket stream
            mmOutStream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getReadSocket() {

        byte [] b = new byte[numberOfBytes];

        for(int x = 0; x < numberOfBytes; x++)
            b[x] = buffer[x];
        
       return new String(b, StandardCharsets.UTF_8);
    }


    public void closeConnection(){
        try {
            mmSocket.close();
        }catch(Exception e){
            System.out.println("Trouble closing the log");
        }

    }


}//private Inner class BluetoothConnectoin
