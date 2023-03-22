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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.skydoves.colorpickerview.ColorPickerView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class CountdownActivity extends Activity {
    private int id;
    private int ledId;
    private boolean fromRoom;
    private AllLedInfo allLedInfo;
    private LedModeObject mode;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        ImageView back = findViewById(R.id.back);
        ImageView rename = findViewById(R.id.rename);
        SeekBar brightness = findViewById(R.id.brightness);
        TextView name = findViewById(R.id.name);
        ColorPickerView picker = findViewById(R.id.picker);
        TimePicker time = findViewById(R.id.time);

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



        if(this.fromRoom){
            mode = roomObjects.get(this.ledId).getLedModes().get(this.id);
        }
        else{
            mode = ledObjects.get(this.ledId).getLedModes().get(this.id);
        }


        name.setText(mode.getName());
        brightness.setProgress((int)((((double)mode.getPacket().get(0).getBrightness()+128.0)/255.0) * 100.0));
        picker.setInitialColor(mode.getPacket().get(0).getColor());
        int minutes = (mode.getPacket().get(1).getTime() % 3600) / 60;
        int seconds = mode.getPacket().get(1).getTime() % 60;
        //time.setText(String.valueOf(mode.getPacket().get(1).getTime()));
        time.setIs24HourView(true);
        time.setHour(minutes);
        time.setMinute(seconds);

        rename.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(CountdownActivity.this);
            builder.setTitle("Zadej název");
            final EditText input = new EditText(CountdownActivity.this);
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
            mode.getPacket().get(0).setBrightness((int)(((double)brightness.getProgress()/100.0)*255.0));
            mode.getPacket().get(0).setColor(picker.getColor());
            //mode.getPacket().get(1).setTime(Integer.parseInt(time.getText().toString()));
            int secondsToPost = time.getMinute() + (time.getHour() * 60);
            mode.getPacket().get(1).setTime(secondsToPost);

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

            if(mode.isState()){
                if(BluetoothService.bluetoothGatt == null){
                    Toast.makeText(this, "Neaktualizoval se mód, protože není připojené Bluetooth", Toast.LENGTH_SHORT).show();
                }
                else if(fromRoom){
                    ArrayList<Packet> packetsToPost = mode.getPacket();

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


            Intent myIntent = new Intent(CountdownActivity.this, LedSettingsActivity.class);
            myIntent.putExtra("id", this.ledId);
            myIntent.putExtra("fromRoom", this.fromRoom);
            this.finish();
            CountdownActivity.this.startActivity(myIntent);
        });
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onBackPressed() {
        SeekBar brightness = findViewById(R.id.brightness);
        ColorPickerView picker = findViewById(R.id.picker);
        TimePicker time = findViewById(R.id.time);
        mode.getPacket().get(0).setBrightness((int)(((double)brightness.getProgress()/100.0)*255.0));
        mode.getPacket().get(0).setColor(picker.getColor());
        //mode.getPacket().get(1).setTime(Integer.parseInt(time.getText().toString()));
        int secondsToPost = time.getMinute() + (time.getHour() * 60);
        mode.getPacket().get(1).setTime(secondsToPost);

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

        if(mode.isState()){
            if(BluetoothService.bluetoothGatt == null){
                Toast.makeText(this, "Neaktualizoval se mód, protože není připojené Bluetooth", Toast.LENGTH_SHORT).show();
            }
            else if(fromRoom){
                ArrayList<Packet> packetsToPost = mode.getPacket();

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


        Intent myIntent = new Intent(CountdownActivity.this, LedSettingsActivity.class);
        myIntent.putExtra("id", this.ledId);
        myIntent.putExtra("fromRoom", this.fromRoom);
        this.finish();
        CountdownActivity.this.startActivity(myIntent);
    }
}
