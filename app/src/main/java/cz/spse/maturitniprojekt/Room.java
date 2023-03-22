package cz.spse.maturitniprojekt;

import static android.app.PendingIntent.getActivity;
import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
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
import java.util.Arrays;

public class Room extends LinearLayout {
    private Button settingsElement;
    private TextView nameElement;
    private Switch stateElement;
    private ImageView imageElement;
    private ImageView deleteElement;

    private String name;
    private boolean state;
    private int id;
    private AllLedInfo allLedInfo;
    private ArrayList<LedObject> ledsObjects;
    private RoomActivity roomActivity;

    public Room(Context context) {
        super(context);
        initializeViews(context);
    }
    public Room(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public Room(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    public Room(Context context, String name, boolean state, int id, AllLedInfo allLedInfo, RoomActivity roomActivity) {
        super(context);
        this.name = name;
        this.state = state;
        this.id = id;
        this.allLedInfo = allLedInfo;
        this.roomActivity = roomActivity;
        initializeViews(context);
    }

    @SuppressLint("MissingPermission")
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.room_view, this);

        settingsElement = findViewById(R.id.led_settings);
        nameElement = findViewById(R.id.led_name);
        stateElement = findViewById(R.id.led_turnon);
        imageElement = findViewById(R.id.led_image);
        deleteElement = findViewById(R.id.delete);

        nameElement.setText(this.name);
        stateElement.setChecked(this.state);



        stateElement.setOnClickListener((buttonView) -> {
            boolean isChecked = stateElement.isChecked();
            if(BluetoothService.bluetoothGatt == null){

                stateElement.setChecked(!isChecked);
                Toast.makeText(context, "Není připojené Bluetooth", Toast.LENGTH_SHORT).show();
            }
            else if (isChecked) {


                ArrayList<LedObject> ledsInRoom = allLedInfo.getRoomObjects().get(id).getLedObjects();
                for (int i = 0; i < ledsInRoom.size(); i++) {
                    ledsInRoom.get(i).setState(false);
                    ArrayList<LedModeObject> modes = ledsInRoom.get(i).getLedModes();
                    for (int j = 0; j < modes.size(); j++) {
                        modes.get(j).setState(false);
                    }
                }
                ArrayList<RoomObject> rooms = allLedInfo.getRoomObjects();
                    for (int j = 0; j < rooms.size(); j++) {
                        rooms.get(j).setState(false);
                        ArrayList<LedModeObject> modes = rooms.get(j).getLedModes();
                        for (int k = 0; k < modes.size(); k++) {
                            modes.get(k).setState(false);
                        }
                    }
                allLedInfo.getRoomObjects().get(id).setState(true);

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
                //roomActivity.recreate();
                roomActivity.finish();
                roomActivity.overridePendingTransition(0, 0);
                roomActivity.startActivity(roomActivity.getIntent());
            }
            else{
                allLedInfo.getRoomObjects().get(this.id).setState(false);
                ArrayList<LedModeObject> modes = allLedInfo.getRoomObjects().get(this.id).getLedModes();
                for (int i = 0; i < modes.size(); i++) {
                    modes.get(i).setState(false);
                }
                for (int i = 0; i < allLedInfo.getRoomObjects().get(id).getLedObjects().size(); i++) {
                    allLedInfo.getRoomObjects().get(id).getLedObjects().get(i).setState(false);
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
                for (int i = 0; i < allLedInfo.getRoomObjects().get(id).getLedObjects().size(); i++) {
                    packetsToPost.add(new Packet(allLedInfo.getRoomObjects().get(id).getLedObjects().get(i).getId()+1, new int[]{0,0,0,0}, 0, 0, false));
                }

                for (int i = 0; i < packetsToPost.size(); i++) {
                    BluetoothService.dataQueue.addPacket(packetsToPost.get(i));
                }
                BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost.size()});
                BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);

            }
        });

        deleteElement.setOnClickListener(view -> {
            allLedInfo.getRoomObjects().remove(this.id);
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
            roomActivity.finish();
            roomActivity.overridePendingTransition(0, 0);
            roomActivity.startActivity(roomActivity.getIntent());
        });

        settingsElement.setOnClickListener(view -> {
            Intent myIntent = new Intent(context, LedSettingsActivity.class);
            myIntent.putExtra("id", this.id);
            myIntent.putExtra("fromRoom", true);
            roomActivity.finish();
            context.startActivity(myIntent);
        });
    }


}
