package com.example.bluetoothchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Output;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private final int LOCATION_PERMISSION_REQUEST = 101;
    private final int SELECT_DEVICE = 102;


    private ListView listMainChat;
    private EditText edCreateMessage;
    private Button btnSendMessage;
    private ArrayAdapter<String> adapterMainChat;
    private ArrayAdapter<String> chatAdapter;
    private Context context;
    private TextView status;
    private SendReceive sendReceive;


    /**
     * Message Handler...
     */
    public static final int STATE_LISTENING = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTION_FAILED = 4;
    public static final int STATE_MESSAGE_RECEIVED = 5;
    int REQUEST_ENABLE_BLUETOOTH = 1;
    public static final String APP_NAME = "deviceName";
    private static final UUID UID = UUID.fromString("802941e6-b39c-11eb-8529-0242ac130003");
    public static final String TOAST = "toast";
    private String connectedDevice;


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch(msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[])msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    chatAdapter.add("Other:  "+tempMsg);
                    break;
            }

            return true;
        }
    });

    private class BluetoothServer extends Thread{
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket socket;

        BluetoothServer()
        {
            socket = null;
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            try{
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, UID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            Log.e("Wohoo", "runned yr");
            while(socket == null)
            {
                try{
                    Message msg = Message.obtain();
                msg.what = STATE_CONNECTING;
                handler.sendMessage(msg);
                    socket = serverSocket.accept();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                msg.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(msg);
                }

                if(socket != null)
                {
                    Message msg = Message.obtain();
                    msg.what = STATE_CONNECTED;
                    handler.sendMessage(msg);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }


        }
    }

    private class BluetoothClient extends Thread{

        private BluetoothDevice bluetoothDevice;
        private BluetoothSocket socket;


        public BluetoothClient(BluetoothDevice device){
            this.bluetoothDevice = device;
            try{
                socket = device.createRfcommSocketToServiceRecord(UID);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        public void run(){
            try {
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                Message msg = Message.obtain();
                msg.what =  STATE_CONNECTED;
                handler.sendMessage(msg);

                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Message msg = Message.obtain();
                msg.what =  STATE_CONNECTION_FAILED;
                handler.sendMessage(msg);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initBluetooth();
        status = (TextView)findViewById(R.id.photoPickerButton);
        context = this;
        listMainChat = findViewById(R.id.list_conversation);
        chatAdapter = new ArrayAdapter<String>(this, R.layout.device_list_item);
        listMainChat.setAdapter(chatAdapter);

    }

    private void init() {
        listMainChat = findViewById(R.id.list_conversation);
        edCreateMessage = findViewById(R.id.ed_enter_message);
        btnSendMessage = findViewById(R.id.btn_send_message);

        adapterMainChat = new ArrayAdapter<String>(this, R.layout.message_layout);
        listMainChat.setAdapter(adapterMainChat);


    }


    private void initBluetooth(){
        // creating an object of this calling by calling the static method getDefaultAdapter().
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null)
        {
            Toast.makeText(this, "Bluetooth not found", Toast.LENGTH_SHORT).show();
        }
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.valueOf(edCreateMessage.getText());
                chatAdapter.add("YOU:  "+message);
                edCreateMessage.clearFocus();
                sendReceive.write(message.getBytes());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.add_person:
                Toast.makeText(this, "Add Person...", Toast.LENGTH_SHORT).show();
                checkPermission();
                return true;

            case R.id.menu_enabled_bluetooth:
                enableBluetooth();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    /**
     * if running android version 6.0 and above version need to access permission dynamically...
     * checking permission is granted or not, if not granting permission.
     */
    private void checkPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        }
        else{
            Intent intent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(intent, SELECT_DEVICE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==SELECT_DEVICE)
        {
            if(resultCode==RESULT_OK){
                String str = data.getStringExtra("call");
                if(str.equals("BluetoothServer")){
                    BluetoothServer server = new BluetoothServer();
                    server.start();
                    chatAdapter.clear();
                    Log.e("This time server  ", "server");
                }
                else if(str.equals("BluetoothClient")){
                    String address = data.getStringExtra("address");
                    BluetoothClient client = new BluetoothClient(BluetoothAdapter.
                            getDefaultAdapter().
                            getRemoteDevice(address));
                    client.start();
                    chatAdapter.clear();
                    Log.e("This time addresss", address);
                }
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_REQUEST){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                /**
                 * todo: start new acitivity here...
                 */
                Intent intent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(intent, SELECT_DEVICE);
            }
            else{
                    new AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setMessage("Please grant location permission")
                            .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    checkPermission();
                                }
                            })
                            .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.this.finish();
                                }
                            }).show();
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void enableBluetooth(){
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
        }
        else if(bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(intent);
        }
    }

    private class SendReceive extends Thread{

        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run(){
            byte[] buffer = new byte[2048];
            int bytes;
            while(true){
                try{
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] buff){
            try{
                outputStream.write(buff);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

        }



    }
}