package com.dweis.remotecoffee;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends Activity implements View.OnClickListener{
    private Button buttonOnOff;
    private Button buttonStart;
    private TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonOnOff =(Button) findViewById(R.id.OnOff);
        buttonOnOff.setOnClickListener(this);
        buttonStart =(Button) findViewById(R.id.Start);
        buttonStart.setOnClickListener(this);
        label =(TextView) findViewById(R.id.label);
    }

    @Override
    public void onClick(View v) {
        if (v == buttonOnOff){
            Log.v("", "Button OnOff");
            new AsyncBluetooth('0', buttonOnOff, buttonStart, label).execute();
        } else if (v == buttonStart){
            Log.v("", "Button start");
            new AsyncBluetooth('1', buttonOnOff, buttonStart, label).execute();
        }
    }

    private static class AsyncBluetooth extends AsyncTask<Object, String, String>{
        private Button bt1;
        private Button bt2;
        private TextView label;
        private char message;


        public AsyncBluetooth(char message, Button bt1, Button bt2, TextView label){
            this.message = message;
            this.bt1 = bt1;
            this.bt2 = bt2;
            this.label = label;
        }

        @Override
        protected void onPreExecute (){
            bt1.setClickable(false);
            bt2.setClickable(false);
            label.setText("Processing request");
        }

        @Override
        protected void onProgressUpdate(String... progress){
            label.setText(progress[0]);
        }

        @Override
        protected void onPostExecute(String message){
            bt1.setClickable(true);
            bt2.setClickable(true);
            label.setText(message);
        }

        @Override
        protected String doInBackground(Object... params) {
            String address = null;
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            for (BluetoothDevice device : adapter.getBondedDevices()){
                if (device.getName().equals("RemoteCoffee")) address = device.getAddress();
            }
            try{
                BluetoothDevice device = adapter.getRemoteDevice(address);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                socket.connect();
                OutputStream output = socket.getOutputStream();
                //InputStream input = socket.getInputStream();
                output.write((byte) message);
                output.flush();
                publishProgress("Success");
                Thread.sleep(2000);
                //Thread.sleep(100);
                //Log.v("INPUT", "" + input.read());
                //input.close();
                output.close();
                socket.close();
            } catch (Exception e){
                Log.e("Bluetooth", "Unable to connect", e);
                return "Sending failed";
            }
            return "";
        }

    }

    /*@Override
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
    }*/
}
