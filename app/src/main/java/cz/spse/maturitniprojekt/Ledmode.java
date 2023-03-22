package cz.spse.maturitniprojekt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

import dalvik.system.PathClassLoader;

public class Ledmode extends LinearLayout {
    private String name;
    private int id; //pozice v arraylistu
    private int ledId; //id ledky
    private int mod;//0 = static, 1 = coundown, 2 = timetable 3= sequence
    private boolean fromRoom;
    private boolean state;
    private AllLedInfo allLedInfo;
    private Activity activity;

    private Button settingsElement;
    private TextView nameElement;
    private Switch stateElement;
    private ImageView deleteElement;
    private ImageView imageElement;

    public Ledmode(Context context) {
        super(context);
        initializeViews(context);
    }
    public Ledmode(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public Ledmode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    public Ledmode(Context context, String name, int id, int ledId, int mod, boolean fromRoom, AllLedInfo allLedInfo, Activity activity, boolean state) {
        super(context);
        this.name = name;
        this.id = id;
        this.ledId = ledId;
        this.mod = mod;
        this.fromRoom = fromRoom;
        this.allLedInfo = allLedInfo;
        this.activity = activity;
        this.state = state;
        initializeViews(context);
    }

    @SuppressLint("MissingPermission")
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.ledmode_view, this);

        nameElement = findViewById(R.id.led_name);
        settingsElement = findViewById(R.id.led_settings);
        stateElement = findViewById(R.id.led_turnon);
        deleteElement = findViewById(R.id.delete);
        imageElement = findViewById(R.id.led_image);

        nameElement.setText(this.name);
        stateElement.setChecked(this.state);

        if(this.mod == 0){
            int color = 0;
            if(fromRoom){
                color = allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().get(0).getColor();
            }
            else{
                color = allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().get(0).getColor();
            }
            imageElement.setBackgroundColor(color);
        }
        else if(this.mod == 1){
            imageElement.setImageResource(R.drawable.timer);
        }
        else if(this.mod == 2){
            imageElement.setImageResource(R.drawable.sequence);
        }
        else if(this.mod == 3){
            imageElement.setImageResource(R.drawable.calendar);
        }


        stateElement.setOnClickListener((buttonView) -> {
            boolean isChecked = stateElement.isChecked();

            if(BluetoothService.bluetoothGatt == null){

                stateElement.setChecked(!isChecked);
                Toast.makeText(context, "Není připojené Bluetooth", Toast.LENGTH_SHORT).show();
            }

            else if (isChecked) {

                if(fromRoom){

                    ArrayList<LedObject> ledsInRoom = allLedInfo.getRoomObjects().get(ledId).getLedObjects();
                    for (int i = 0; i < ledsInRoom.size(); i++) {
                        ledsInRoom.get(i).setState(true);
                        ArrayList<LedModeObject> modes = ledsInRoom.get(i).getLedModes();
                        for (int j = 0; j < modes.size(); j++) {
                            modes.get(j).setState(false);
                        }
                    }
                    ArrayList<RoomObject> rooms = allLedInfo.getRoomObjects();
                    for (int i = 0; i < rooms.size(); i++) {
                        rooms.get(i).setState(false);
                        for (int j = 0; j < rooms.get(i).getLedModes().size(); j++) {
                            rooms.get(i).getLedModes().get(j).setState(false);
                        }
                    }
                    allLedInfo.getRoomObjects().get(ledId).setState(true);
                    allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).setState(true);
                }
                else{
                    ArrayList<LedModeObject> modes = allLedInfo.getLedObjects().get(ledId).getLedModes();
                    allLedInfo.getLedObjects().get(ledId).setState(true);
                    for (int i = 0; i < modes.size(); i++) {
                        modes.get(i).setState(false);
                    }
                    modes.get(id).setState(true);
                    ArrayList<RoomObject> rooms = allLedInfo.getRoomObjects();
                    for (int i = 0; i < rooms.size(); i++) {
                        if(rooms.get(i).getLedObjects().contains(allLedInfo.getLedObjects().get(ledId))){
                            rooms.get(i).setState(false);
                            ArrayList<LedModeObject> roomModes = rooms.get(i).getLedModes();
                            for (int j = 0; j < roomModes.size(); j++) {
                                roomModes.get(j).setState(false);
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
                    System.out.println("Uloženo");
                } catch (IOException e) {
                    System.out.println("Error");
                    e.printStackTrace();
                }



                System.out.println("posílám data do ledky");
                if(fromRoom){
                    if(allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getMode() == 3){
                        System.out.println("Jsem v rozvrhu");
                        ArrayList<Packet> packets = allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket();
                        LedModeObject mode = allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id);

                        ArrayList<Packet> packetsToPost = new ArrayList<>();


                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime lastMonday = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
                        Duration duration = Duration.between(lastMonday, now);
                        long seconds = duration.getSeconds();
                        int odPondeli = (int) seconds;

                        int postupnyCas = 0;
                        int indexToStart = -1;
                        int firstPacketIndex = -1;
                        for (int i = 0; i < packets.size(); i++) {
                            postupnyCas = postupnyCas + packets.get(i).getTime();
                            if(odPondeli<postupnyCas){
                                if(i == 0){
                                    indexToStart = packets.size()-1;
                                }
                                else{
                                    indexToStart = i-1;
                                }
                                break;
                            }
                        }
                        if(indexToStart == -1){
                            indexToStart = packets.size()-1;
                        }

                        int celkovyCas = 0;
                        for (int i = 1; i < mode.getPacket().size(); i++) {
                            celkovyCas += mode.getPacket().get(i).getTime();
                        }
                        mode.getPacket().get(0).setTime(604800-celkovyCas);

                        Packet packet1;
                        Packet packet2;
                        int lastPacketIndex = packets.size()-1;

                        if(indexToStart == lastPacketIndex){ //POKUD JE POSLEDNI PACKET
                            packet1 = new Packet(mode.getPacket().get(indexToStart));
                            packet2 = new Packet(mode.getPacket().get(0));
                            firstPacketIndex = 1;
                        }
                        else if(indexToStart == lastPacketIndex-1){ //POKUD JE PREDPOSLEDNI PACKET
                            packet1 = new Packet(mode.getPacket().get(indexToStart));
                            packet2 = new Packet(mode.getPacket().get(indexToStart+1));
                            firstPacketIndex = 0;
                        }
                        else{
                            packet1 = new Packet(mode.getPacket().get(indexToStart));
                            packet2 = new Packet(mode.getPacket().get(indexToStart+1));
                            firstPacketIndex = indexToStart+2;
                        }


                        packet1.setTime(0);
                        packet1.setRepeat(false);

                        packet2.setTime(postupnyCas-odPondeli);
                        packet2.setRepeat(false);

                        packetsToPost.add(packet1);
                        packetsToPost.add(packet2);

                        ArrayList<Packet> packetsToAdd = new ArrayList<>();

                        for (int i = firstPacketIndex; i < packets.size(); i++) {
                            packetsToAdd.add(new Packet(packets.get(i)));
                            if(i == lastPacketIndex){
                                for (int j = 0; j < firstPacketIndex; j++) {
                                    packetsToAdd.add(new Packet(packets.get(j)));
                                }
                            }
                        }


                        for (int i = 0; i < packetsToAdd.size(); i++) {
                            packetsToPost.add(packetsToAdd.get(i));
                        }
                        ArrayList<LedObject> ledIds = allLedInfo.getRoomObjects().get(ledId).getLedObjects();
                        ArrayList<Packet> packetsToPost2 = new ArrayList<>();

                        for (int i = 0; i < ledIds.size(); i++) {
                            for (int j = 0; j < packetsToPost.size(); j++) {
                                packetsToPost2.add(new Packet(packetsToPost.get(j)));
                            }
                        }
                        // System.out.println("Pocet packetu: " + packets.size());
                        for (int i = 0; i < ledIds.size(); i++) {
                            for (int j = 0; j < packetsToPost.size(); j++) {
                                packetsToPost2.get(i*packetsToPost.size()+j).setLed(ledIds.get(i).getId()+1);
                            }
                        }



                        for (int i = 0; i < packetsToPost2.size(); i++) {
                            BluetoothService.dataQueue.addPacket(packetsToPost2.get(i));
                        }
                        BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost2.size()});
                        BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
                    }
                    else{
                        ArrayList<LedObject> ledIds = allLedInfo.getRoomObjects().get(ledId).getLedObjects();
                        ArrayList<Packet> packets = allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket();

                        ArrayList<Packet> packetsToPost = new ArrayList<>();

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
                }
                else{
                    if(allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getMode() == 3){
                        System.out.println("Jsem v rozvrhu");
                        ArrayList<Packet> packets = allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket();
                        LedModeObject mode = allLedInfo.getLedObjects().get(ledId).getLedModes().get(id);

                        ArrayList<Packet> packetsToPost = new ArrayList<>();


                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime lastMonday = now.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
                        Duration duration = Duration.between(lastMonday, now);
                        long seconds = duration.getSeconds();
                        int odPondeli = (int) seconds;

                        int postupnyCas = 0;
                        int indexToStart = -1;
                        int firstPacketIndex = -1;
                        for (int i = 0; i < packets.size(); i++) {
                            postupnyCas = postupnyCas + packets.get(i).getTime();
                            if(odPondeli<postupnyCas){
                                if(i == 0){
                                    indexToStart = packets.size()-1;
                                }
                                else{
                                    indexToStart = i-1;
                                }
                                break;
                            }
                        }
                        if(indexToStart == -1){
                            indexToStart = packets.size()-1;
                        }
                        System.out.println("Index to start: " + indexToStart);

                        int celkovyCas = 0;
                        for (int i = 1; i < mode.getPacket().size(); i++) {
                            celkovyCas += mode.getPacket().get(i).getTime();
                        }
                        mode.getPacket().get(0).setTime(604800-celkovyCas);

                        Packet packet1;
                        Packet packet2;
                        int lastPacketIndex = packets.size()-1;

                        if(indexToStart == lastPacketIndex){ //POKUD JE POSLEDNI PACKET
                            packet1 = new Packet(mode.getPacket().get(indexToStart));
                            packet2 = new Packet(mode.getPacket().get(0));
                            firstPacketIndex = 1;
                        }
                        else if(indexToStart == lastPacketIndex-1){ //POKUD JE PREDPOSLEDNI PACKET
                            packet1 = new Packet(mode.getPacket().get(indexToStart));
                            packet2 = new Packet(mode.getPacket().get(indexToStart+1));
                            firstPacketIndex = 0;
                        }
                        else{
                            packet1 = new Packet(mode.getPacket().get(indexToStart));
                            packet2 = new Packet(mode.getPacket().get(indexToStart+1));
                            firstPacketIndex = indexToStart+2;
                        }


                        packet1.setTime(0);
                        packet1.setRepeat(false);

                        packet2.setTime(postupnyCas-odPondeli);
                        packet2.setRepeat(false);

                        packetsToPost.add(packet1);
                        packetsToPost.add(packet2);

                        ArrayList<Packet> packetsToAdd = new ArrayList<>();

                        for (int i = firstPacketIndex; i < packets.size(); i++) {
                            packetsToAdd.add(new Packet(packets.get(i)));
                            if(i == lastPacketIndex){
                                for (int j = 0; j < firstPacketIndex; j++) {
                                    packetsToAdd.add(new Packet(packets.get(j)));
                                }
                            }
                        }


                        for (int i = 0; i < packetsToAdd.size(); i++) {
                            packetsToPost.add(packetsToAdd.get(i));
                        }


                        System.out.println("Pocet packetu: " + packets);

                        for (int i = 0; i < packetsToPost.size(); i++) {
                            BluetoothService.dataQueue.addPacket(packetsToPost.get(i));
                        }
                        BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost.size()});
                        BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
                    }
                    else{
                        ArrayList<Packet> packets = allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket();
                        System.out.println("Pocet packetu: " + packets);

                        for (int i = 0; i < packets.size(); i++) {
                            BluetoothService.dataQueue.addPacket(packets.get(i));
                        }
                        BluetoothService.characteristic.setValue(new byte[]{(byte) packets.size()});
                        BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
                    }
                }

                activity.finish();
                activity.overridePendingTransition(0, 0);
                activity.startActivity(activity.getIntent());
            }

            else{ //POKUD LEDKU VYPINAM
                if(fromRoom){
                    allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).setState(false);
                    allLedInfo.getRoomObjects().get(ledId).setState(false);

                    for (int i = 0; i < allLedInfo.getRoomObjects().get(ledId).getLedObjects().size(); i++) {
                        allLedInfo.getRoomObjects().get(ledId).getLedObjects().get(i).setState(false);
                    }

                    ArrayList<Packet> packetsToPost = new ArrayList<>();
                    for (int i = 0; i < allLedInfo.getRoomObjects().get(ledId).getLedObjects().size(); i++) {
                        packetsToPost.add(new Packet(allLedInfo.getRoomObjects().get(ledId).getLedObjects().get(i).getId()+1, new int[]{0,0,0,0}, 0, 0, false));
                    }

                    for (int i = 0; i < packetsToPost.size(); i++) {
                        BluetoothService.dataQueue.addPacket(packetsToPost.get(i));
                    }
                    BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost.size()});
                    BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
                }
                else{
                    allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).setState(false);
                    allLedInfo.getLedObjects().get(ledId).setState(false);
                    ArrayList<Packet> packetsToPost = new ArrayList<>();
                    packetsToPost.add(new Packet(ledId+1, new int[]{0,0,0,0}, 0, 0, false));

                    for (int i = 0; i < packetsToPost.size(); i++) {
                        BluetoothService.dataQueue.addPacket(packetsToPost.get(i));
                    }
                    BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost.size()});
                    BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
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
                activity.finish();
                activity.overridePendingTransition(0, 0);
                activity.startActivity(activity.getIntent());
            }
        });

        deleteElement.setOnClickListener(view -> {

            if(this.fromRoom){
                allLedInfo.getRoomObjects().get(this.ledId).getLedModes().remove(this.id);;
            }
            else{
                allLedInfo.getLedObjects().get(this.ledId).getLedModes().remove(this.id);;
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
            activity.finish();
            activity.overridePendingTransition(0, 0);
            activity.startActivity(activity.getIntent());
        });

        settingsElement.setOnClickListener(view -> {
            if(this.mod == 0){
                Intent myIntent = new Intent(context, ColorActivity.class);
                myIntent.putExtra("id", this.id);
                myIntent.putExtra("ledId", this.ledId);
                myIntent.putExtra("fromRoom", this.fromRoom);
                context.startActivity(myIntent);
            }
            else if(this.mod == 1){
                Intent myIntent = new Intent(context, CountdownActivity.class);
                myIntent.putExtra("id", this.id);
                myIntent.putExtra("ledId", this.ledId);
                myIntent.putExtra("fromRoom", this.fromRoom);
                context.startActivity(myIntent);
            }
            else if(this.mod == 2){
                Intent myIntent = new Intent(context, SequenceActivity.class);
                myIntent.putExtra("id", this.id);
                myIntent.putExtra("ledId", this.ledId);
                myIntent.putExtra("fromRoom", this.fromRoom);
                context.startActivity(myIntent);
            }
            else if(this.mod == 3){
                Intent myIntent = new Intent(context, TimetableActivity.class);
                myIntent.putExtra("id", this.id);
                myIntent.putExtra("ledId", this.ledId);
                myIntent.putExtra("fromRoom", this.fromRoom);
                context.startActivity(myIntent);
            }
            activity.finish();
        });
    }


}
