package com.example.bluetoothchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBluetooth();
    }

    private void initBluetooth(){
        // creating an object of this calling by calling the static method getDefaultAdapter().
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null)
        {
            Toast.makeText(this, "Bluetooth not found", Toast.LENGTH_SHORT).show();
        }
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
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 101){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                /**
                 * todo: start new acitivity here...
                 */
                Intent intent = new Intent(this, DeviceListActivity.class);
                startActivity(intent);
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

}