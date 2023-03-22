package cz.spse.maturitniprojekt;



import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    Context context = this;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothService.currentActivity = this;

        AllLedInfo allLedInfo = new AllLedInfo();

        try {
            FileInputStream in = openFileInput("data.txt");
            ObjectInputStream objIn = new ObjectInputStream(in);
            allLedInfo = (AllLedInfo) objIn.readObject();
            in.close();
            objIn.close();
            System.out.println("OK");
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(allLedInfo.equals(null)){
            allLedInfo = new AllLedInfo();
            allLedInfo.addLedObject(new LedObject(0, "LED1", false));
            allLedInfo.addLedObject(new LedObject(1, "LED2", false));
            allLedInfo.addLedObject(new LedObject(2, "LED3", false));

            try {
                FileOutputStream out = openFileOutput("data.txt", Context.MODE_PRIVATE);
                ObjectOutputStream objOut = new ObjectOutputStream(out);
                objOut.writeObject(allLedInfo);
                out.close();
                objOut.close();
            } catch (IOException e) {
                System.out.println("Error");
                e.printStackTrace();
            }
        }

        ArrayList<LedObject> ledObjects = allLedInfo.getLedObjects();

        LinearLayout list = findViewById(R.id.list);

        for (int i = 0; i < ledObjects.size(); i++) {
            Led led = new Led(this, ledObjects.get(i).getName(), ledObjects.get(i).isState(), ledObjects.get(i).getId(), allLedInfo, this);
            led.setPadding(25, 25, 25,25);
            led.setBackground(getDrawable(R.drawable.border));
            list.addView(led);
        }


        ImageView btn_connect = findViewById(R.id.con);
        TextView manRooms = findViewById(R.id.manRooms);

        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 1);

        if(BluetoothService.connected){
            btn_connect.setImageResource(R.drawable.bluetoothoff);
        }

        btn_connect.setOnClickListener(view -> {
            startService(new Intent(this, BluetoothService.class));
        });

        manRooms.setOnClickListener(view -> {
            this.finish();
            overridePendingTransition(0, 0);
            Intent myIntent = new Intent(MainActivity.this, RoomActivity.class);
            MainActivity.this.startActivity(myIntent);
        });
    }
    @Override
    public void onBackPressed() {}

}