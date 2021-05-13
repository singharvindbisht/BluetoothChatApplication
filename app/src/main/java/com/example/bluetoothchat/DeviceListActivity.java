package com.example.bluetoothchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private ListView listPairDevices, listAvailableDevices;
    private ArrayAdapter adapterPairedDevices, adapterAvailableDevices;
    private BluetoothAdapter bluetoothAdapter;
    private ProgressBar progressScanDevices;
    private Context context;
    private Intent intent1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        context = this;
        init();
        intent1 = getIntent();
    }

    public void init()
    {
        listAvailableDevices = findViewById(R.id.available_devices);
        listPairDevices = findViewById(R.id.paired_devices);
        progressScanDevices = findViewById(R.id.progress_scan_devices);

        adapterPairedDevices = new ArrayAdapter<String>(this, R.layout.device_list_item);
        adapterAvailableDevices = new ArrayAdapter<String>(this, R.layout.device_list_item);


        listAvailableDevices.setAdapter(adapterAvailableDevices);
        listPairDevices.setAdapter(adapterPairedDevices);

//        setResult(Activity.RESULT_OK, intent);

        listAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String info = ((TextView) view).getText().toString();

                String address = info.substring(info.length() - 17);

                Intent intent = new Intent();
                intent.putExtra("deviceAddress", address);
                Toast.makeText(context, "Address : "+address, Toast.LENGTH_SHORT).show();
//                BluetoothClient client = new BluetoothClient(BluetoothAdapter.
//                        getDefaultAdapter().
//                        getRemoteDevice(address));
//               client.start();

                intent.putExtra("address", address);
                intent.putExtra("call", "BluetoothClient");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

//        R.id.menu_enabled_bluetooth

        listPairDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();

                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                Log.d("Address", address);
                Intent intent = new Intent();
                intent.putExtra("deviceAddress", address);
                Toast.makeText(context, "Address : "+address, Toast.LENGTH_SHORT).show();
                intent.putExtra("address", address);
                intent.putExtra("call", "BluetoothClient");
                setResult(RESULT_OK, intent);
                finish();
            }
        });


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if(pairedDevice!=null && pairedDevice.size()>0){
            for(BluetoothDevice iterator: pairedDevice)
            {
                adapterPairedDevices.add(iterator.getName()+"\n"+iterator.getAddress());
            }
        }

        IntentFilter intentfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothDeviceListener, intentfilter);
        IntentFilter intentfilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothDeviceListener, intentfilter1);

    }

    private BroadcastReceiver bluetoothDeviceListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressScanDevices.setVisibility(View.GONE);
                if (adapterAvailableDevices.getCount() == 0) {
                    System.out.println("hello inside the 1st part");
                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                } else {
                    System.out.println("hello inside the 2nd part");
                    Toast.makeText(context, "Click on the device to start the chat", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_scan_devices:
                scanDevice();
                //Toast.makeText(this, "Scanning device selected", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_listen_devices:
                Intent intent = new Intent();
                intent.putExtra("call", "BluetoothServer");
                setResult(RESULT_OK, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void scanDevice(){
        progressScanDevices.setVisibility(View.VISIBLE);
        adapterAvailableDevices.clear();
        Toast.makeText(this, "Bluetooth is Searching Devices...", Toast.LENGTH_SHORT).show();

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();

    }

}