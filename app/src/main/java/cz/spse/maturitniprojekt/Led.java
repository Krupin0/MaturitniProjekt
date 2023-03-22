package cz.spse.maturitniprojekt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Led extends LinearLayout {
    private Button settingsElement;
    private TextView nameElement;
    private Switch stateElement;
    private ImageView imageElement;

    private Color color;
    private String name;
    private boolean state;
    private int id;
    private AllLedInfo allLedInfo;
    private Activity activity;
    private DataQueue dataQueue;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGatt bluetoothGatt;

    public Led(Context context) {
        super(context);
        initializeViews(context);
    }

    public Led(Context context, String name, boolean state, int id, AllLedInfo allLedInfo, Activity activity) {
        super(context);
        this.name = name;
        this.state = state;
        this.id = id;
        this.allLedInfo = allLedInfo;
        this.activity = activity;
        initializeViews(context);
    }

    public Led(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public Led(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    @SuppressLint("MissingPermission")
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.led_view, this);

        //System.out.println(this.getId());
        //System.out.println(this.id);

        settingsElement = findViewById(R.id.led_settings);
        nameElement = findViewById(R.id.led_name);
        stateElement = findViewById(R.id.led_turnon);
        imageElement = findViewById(R.id.led_image);

        nameElement.setText(this.name);
        stateElement.setChecked(this.state);


        stateElement.setOnClickListener((buttonView) -> {
            boolean isChecked = stateElement.isChecked();
            System.out.println("LED " + this.id + " is " + isChecked);
            if(BluetoothService.bluetoothGatt == null){

                stateElement.setChecked(!isChecked);
                Toast.makeText(context, "Není připojené Bluetooth", Toast.LENGTH_SHORT).show();
            }
            else if (isChecked) {

                allLedInfo.getLedObjects().get(this.id).setState(true);

                ArrayList<RoomObject> rooms = allLedInfo.getRoomObjects();
                for (int i = 0; i < rooms.size(); i++) {
                    ArrayList<LedObject> ledsInRoom = rooms.get(i).getLedObjects();
                    for (int j = 0; j < ledsInRoom.size(); j++) {
                        if(ledsInRoom.get(j).getId() == this.id){
                            rooms.get(i).setState(false);
                            ArrayList<LedModeObject> modes = rooms.get(i).getLedModes();
                            for (int k = 0; k < modes.size(); k++) {
                                modes.get(k).setState(false);
                            }
                        }
                    }

                }
                try {
                    FileOutputStream out = context.openFileOutput("data.txt", Context.MODE_PRIVATE);
                    ObjectOutputStream objOut = new ObjectOutputStream(out);
                    objOut.writeObject(allLedInfo);
                    out.close();
                    objOut.close();
                } catch (IOException e) {
                    System.out.println("Error");
                    e.printStackTrace();
                }


            }
            else{
                allLedInfo.getLedObjects().get(this.id).setState(false);
                ArrayList<LedModeObject> modes = allLedInfo.getLedObjects().get(this.id).getLedModes();
                for (int i = 0; i < modes.size(); i++) {
                    modes.get(i).setState(false);
                }
                try {
                    FileOutputStream out = context.openFileOutput("data.txt", Context.MODE_PRIVATE);
                    ObjectOutputStream objOut = new ObjectOutputStream(out);
                    objOut.writeObject(allLedInfo);
                    out.close();
                    objOut.close();
                } catch (IOException e) {
                    System.out.println("Error");
                    e.printStackTrace();
                }


                ArrayList<Packet> packetsToPost = new ArrayList<>();
                packetsToPost.add(new Packet(id+1, new int[]{0,0,0,0}, 0, 0, false));

                for (int i = 0; i < packetsToPost.size(); i++) {
                    BluetoothService.dataQueue.addPacket(packetsToPost.get(i));
                }
                BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost.size()});
                BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
            }
        });


        settingsElement.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, LedSettingsActivity.class);
            myIntent.putExtra("id", this.id);
            myIntent.putExtra("fromRoom", false);
            activity.finish();
            context.startActivity(myIntent);
        });
    }
}
