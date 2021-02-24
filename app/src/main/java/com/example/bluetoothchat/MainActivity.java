package com.example.bluetoothchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
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
                return true;

            case R.id.menu_enabled_bluetooth:
                enableBluetooth();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    public void enableBluetooth(){
        if(bluetoothAdapter.isEnabled()){
            Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
        }
        else
            bluetoothAdapter.enable();
    }
}