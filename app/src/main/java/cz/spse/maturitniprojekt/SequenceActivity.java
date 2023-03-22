package cz.spse.maturitniprojekt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class SequenceActivity extends Activity {
    private int id;
    private int ledId;
    private boolean fromRoom;
    private AllLedInfo allLedInfo;
    private LedModeObject mode;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequence);

        ImageView back = findViewById(R.id.back);
        TextView name = findViewById(R.id.name);
        ImageView rename = findViewById(R.id.rename);

        Bundle extras = getIntent().getExtras();
        this.id = extras.getInt("id");
        this.ledId = extras.getInt("ledId");
        this.fromRoom = extras.getBoolean("fromRoom");

        try {
            FileInputStream in = openFileInput("data.txt");
            ObjectInputStream objIn = new ObjectInputStream(in);
            this.allLedInfo = (AllLedInfo) objIn.readObject();
            in.close();
            objIn.close();
            System.out.println("OK");
        } catch (IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        ArrayList<LedObject> ledObjects = this.allLedInfo.getLedObjects();
        ArrayList<RoomObject> roomObjects = this.allLedInfo.getRoomObjects();

        ArrayList<Packet> modes;


        LinearLayout list = findViewById(R.id.list);

        if(this.fromRoom){
            mode = roomObjects.get(this.ledId).getLedModes().get(this.id);
            modes = roomObjects.get(this.ledId).getLedModes().get(this.id).getPacket();
            name.setText(roomObjects.get(this.ledId).getLedModes().get(this.id).getName());
        }
        else{
            mode = ledObjects.get(this.ledId).getLedModes().get(this.id);
            modes = ledObjects.get(this.ledId).getLedModes().get(this.id).getPacket();
            name.setText(ledObjects.get(this.ledId).getLedModes().get(this.id).getName());
        }


        for (int i = 0; i < modes.size(); i++) {
            Ledsequence ledmode;
            if(i == modes.size()-1){
                ledmode = new Ledsequence(this, modes.get(0).timeToString(), id, ledId, modes.get(i).getColor(), allLedInfo, modes.get(i).getBrightness(), this.fromRoom, i, this);
            }
            else{
                ledmode = new Ledsequence(this, modes.get(i+1).timeToString(), id, ledId, modes.get(i).getColor(), allLedInfo, modes.get(i).getBrightness(), this.fromRoom, i, this);
            }
            ledmode.setPadding(25, 25, 25,25);
            ledmode.setBackground(getDrawable(R.drawable.border));
            list.addView(ledmode);
        }
        ArrayList info = new ArrayList<>();
        info.add(ledId);
        info.add(id);
        info.add(fromRoom);
        info.add(allLedInfo);
        Add add = new Add(this, 3, info, this);
        add.setPadding(25, 25, 25,25);
        add.setBackground(getDrawable(R.drawable.border));
        list.addView(add);


        rename.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SequenceActivity.this);
            builder.setTitle("Zadej název");
            final EditText input = new EditText(SequenceActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            input.setText(name.getText());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    name.setText(input.getText().toString());
                    if(fromRoom)
                        mode.setName(input.getText().toString());

                    else
                        mode.setName(input.getText().toString());

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
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }

            });
            builder.show();
        });

        back.setOnClickListener(view -> {
            if(mode.isState()){
                if(BluetoothService.bluetoothGatt == null){

                    Toast.makeText(this, "Neaktualizoval se mód, protože není připojené Bluetooth", Toast.LENGTH_SHORT).show();
                }
                else if(fromRoom){
                    ArrayList<Packet> packetsToPost = new ArrayList<>();
                    ArrayList<Packet> packets = mode.getPacket();
                    ArrayList<LedObject> ledIds = roomObjects.get(ledId).getLedObjects();

                    for (int i = 0; i < ledIds.size(); i++) {
                        for (int j = 0; j < packets.size(); j++) {
                            packetsToPost.add(new Packet(packets.get(j)));
                        }
                    }
                    // System.out.println("Pocet packetu: " + packets.size());
                    for (int i = 0; i < ledIds.size(); i++) {
                        for (int j = 0; j < packets.size(); j++) {
                            packetsToPost.get(i*packets.size()+j).setLed(ledIds.get(i).getId()+1);
                        }
                    }

                    for (int i = 0; i < packetsToPost.size(); i++) {
                        BluetoothService.dataQueue.addPacket(packetsToPost.get(i));
                    }
                    BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost.size()});
                    BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
                }
                else{
                    ArrayList<Packet> packetsToPost = mode.getPacket();

                    for (int i = 0; i < packetsToPost.size(); i++) {
                        BluetoothService.dataQueue.addPacket(packetsToPost.get(i));
                    }
                    BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost.size()});
                    BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
                }
            }


            Intent myIntent = new Intent(SequenceActivity.this, LedSettingsActivity.class);
            myIntent.putExtra("id", this.ledId);
            myIntent.putExtra("fromRoom", this.fromRoom);
            this.finish();
            SequenceActivity.this.startActivity(myIntent);
        });
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onBackPressed() {
        ArrayList<RoomObject> roomObjects = this.allLedInfo.getRoomObjects();
        if(mode.isState()){
            if(BluetoothService.bluetoothGatt == null){

                Toast.makeText(this, "Neaktualizoval se mód, protože není připojené Bluetooth", Toast.LENGTH_SHORT).show();
            }
            else if(fromRoom){
                ArrayList<Packet> packetsToPost = new ArrayList<>();
                ArrayList<Packet> packets = mode.getPacket();
                ArrayList<LedObject> ledIds = roomObjects.get(ledId).getLedObjects();

                for (int i = 0; i < ledIds.size(); i++) {
                    for (int j = 0; j < packets.size(); j++) {
                        packetsToPost.add(new Packet(packets.get(j)));
                    }
                }
                // System.out.println("Pocet packetu: " + packets.size());
                for (int i = 0; i < ledIds.size(); i++) {
                    for (int j = 0; j < packets.size(); j++) {
                        packetsToPost.get(i*packets.size()+j).setLed(ledIds.get(i).getId()+1);
                    }
                }

                for (int i = 0; i < packetsToPost.size(); i++) {
                    BluetoothService.dataQueue.addPacket(packetsToPost.get(i));
                }
                BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost.size()});
                BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
            }
            else{
                ArrayList<Packet> packetsToPost = mode.getPacket();

                for (int i = 0; i < packetsToPost.size(); i++) {
                    BluetoothService.dataQueue.addPacket(packetsToPost.get(i));
                }
                BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost.size()});
                BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
            }
        }


        Intent myIntent = new Intent(SequenceActivity.this, LedSettingsActivity.class);
        myIntent.putExtra("id", this.ledId);
        myIntent.putExtra("fromRoom", this.fromRoom);
        this.finish();
        SequenceActivity.this.startActivity(myIntent);
    }
}

