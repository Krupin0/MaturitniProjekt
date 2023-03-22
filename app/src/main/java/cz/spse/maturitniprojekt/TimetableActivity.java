package cz.spse.maturitniprojekt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.colorpickerview.ColorPickerView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class TimetableActivity extends Activity {
    private int id;
    private int ledId;
    private boolean fromRoom;
    private AllLedInfo allLedInfo;
    private LedModeObject mode;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable);

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



        if(this.fromRoom){
            mode = roomObjects.get(this.ledId).getLedModes().get(this.id);
        }
        else{
            mode = ledObjects.get(this.ledId).getLedModes().get(this.id);
        }

        HashMap<Integer, ArrayList> timetable = new HashMap<>();
        String[] days = {"Po", "Ut", "St", "Ct", "Pa", "So", "Ne"};
        int dosavadniTime = 0;
        int celkovyCas = 0;
        for (int i = 1; i < mode.getPacket().size(); i++) {
            celkovyCas += mode.getPacket().get(i).getTime();
        }
        //HashMap<Integer, ArrayList> colors = new HashMap<>();
        //mode.getPacket().get(0).setTime(604800-celkovyCas);
        for (int i = 0; i < mode.getPacket().size(); i++) {
            int time = mode.getPacket().get(i).getTime() + dosavadniTime;
            dosavadniTime = time;
            int day = 0;
            while(true){
                if(time-((day+1)*86400) < 0){
                    if(timetable.containsKey(time-(day*86400))){
                        ArrayList daysAl = (ArrayList<String>)timetable.get(time-(day*86400)).get(0);
                        daysAl.add(days[day]);
                    }
                    else{
                        timetable.put(time-(day*86400), new ArrayList<>());
                        timetable.get(time-(day*86400)).add(new ArrayList<String>());
                        ArrayList daysAl = (ArrayList<String>)timetable.get(time-(day*86400)).get(0);
                        daysAl.add(days[day]);
                        timetable.get(time-(day*86400)).add(mode.getPacket().get(i).getColor());
                        timetable.get(time-(day*86400)).add(mode.getPacket().get(i).getBrightness());
                        timetable.get(time-(day*86400)).add(mode.getPacket().get(i).getLed());
                    }
                    break;
                }
                else{
                    day++;
                }
            }
        }

        LinearLayout list = findViewById(R.id.list);
        timetable.keySet().forEach(key ->{
            ArrayList<String> daysList = (ArrayList<String>) timetable.get(key).get(0);


            int hours = key / 3600;
            int minutes = (key % 3600) / 60;

            String time = String.format("%02d:%02d", hours, minutes);

            Ledtimetable ledmode = new Ledtimetable(this, time, daysList, id, ledId, (Integer) timetable.get(key).get(1), this.allLedInfo, (Byte) timetable.get(key).get(2), timetable, fromRoom, key, this);
            ledmode.setPadding(25, 25, 25,25);
            ledmode.setBackground(getDrawable(R.drawable.border));
            list.addView(ledmode);
        });
        ArrayList info = new ArrayList();
        info.add(this.ledId);
        info.add(this.id);
        info.add(this.allLedInfo);
        info.add(this.fromRoom);
        Add add = new Add(this, 4, info, this);
        add.setPadding(25, 25, 25,25);
        add.setBackground(getDrawable(R.drawable.border));
        list.addView(add);


        name.setText(mode.getName());


        rename.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TimetableActivity.this);
            builder.setTitle("Zadej název");
            final EditText input = new EditText(TimetableActivity.this);
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
            if (mode.isState()) {
                if(BluetoothService.bluetoothGatt == null){
                    //TODO: ZOBRAZIT DIALOG CHYBY
                    Toast.makeText(this, "Neaktualizoval se mód, protože není připojené Bluetooth", Toast.LENGTH_SHORT).show();
                }
                else if (fromRoom) {

                    ArrayList<Packet> packets = allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket();

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
                        if (odPondeli < postupnyCas) {
                            if (i == 0) {
                                indexToStart = packets.size() - 1;
                            } else {
                                indexToStart = i - 1;
                            }
                            break;
                        }
                    }
                    if (indexToStart == -1) {
                        indexToStart = packets.size() - 1;
                    }

                    int celkovyCass = 0;
                    for (int i = 1; i < mode.getPacket().size(); i++) {
                        celkovyCass += mode.getPacket().get(i).getTime();
                    }
                    mode.getPacket().get(0).setTime(604800 - celkovyCass);

                    Packet packet1;
                    Packet packet2;
                    int lastPacketIndex = packets.size() - 1;

                    if (indexToStart == lastPacketIndex) { //POKUD JE POSLEDNI PACKET
                        packet1 = new Packet(mode.getPacket().get(indexToStart));
                        packet2 = new Packet(mode.getPacket().get(0));
                        firstPacketIndex = 1;
                    } else if (indexToStart == lastPacketIndex - 1) { //POKUD JE PREDPOSLEDNI PACKET
                        packet1 = new Packet(mode.getPacket().get(indexToStart));
                        packet2 = new Packet(mode.getPacket().get(indexToStart + 1));
                        firstPacketIndex = 0;
                    } else {
                        packet1 = new Packet(mode.getPacket().get(indexToStart));
                        packet2 = new Packet(mode.getPacket().get(indexToStart + 1));
                        firstPacketIndex = indexToStart + 2;
                    }


                    packet1.setTime(0);
                    packet1.setRepeat(false);

                    packet2.setTime(postupnyCas - odPondeli);
                    packet2.setRepeat(false);

                    packetsToPost.add(packet1);
                    packetsToPost.add(packet2);

                    ArrayList<Packet> packetsToAdd = new ArrayList<>();

                    for (int i = firstPacketIndex; i < packets.size(); i++) {
                        packetsToAdd.add(new Packet(packets.get(i)));
                        if (i == lastPacketIndex) {
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

                    for (int i = 0; i < ledIds.size(); i++) {
                        for (int j = 0; j < packetsToPost.size(); j++) {
                            packetsToPost2.get(i * packetsToPost.size() + j).setLed(ledIds.get(i).getId() + 1);
                        }
                    }


                    for (int i = 0; i < packetsToPost2.size(); i++) {
                        BluetoothService.dataQueue.addPacket(packetsToPost2.get(i));
                    }
                    BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost2.size()});
                    BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
                } else {

                    System.out.println("Jsem v rozvrhu");
                    ArrayList<Packet> packets = allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket();

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
                        if (odPondeli < postupnyCas) {
                            if (i == 0) {
                                indexToStart = packets.size() - 1;
                            } else {
                                indexToStart = i - 1;
                            }
                            break;
                        }
                    }
                    if (indexToStart == -1) {
                        indexToStart = packets.size() - 1;
                    }
                    System.out.println("Index to start: " + indexToStart);

                    int celkovyCass = 0;
                    for (int i = 1; i < mode.getPacket().size(); i++) {
                        celkovyCass += mode.getPacket().get(i).getTime();
                    }
                    mode.getPacket().get(0).setTime(604800 - celkovyCass);

                    Packet packet1;
                    Packet packet2;
                    int lastPacketIndex = packets.size() - 1;

                    if (indexToStart == lastPacketIndex) { //POKUD JE POSLEDNI PACKET
                        packet1 = new Packet(mode.getPacket().get(indexToStart));
                        packet2 = new Packet(mode.getPacket().get(0));
                        firstPacketIndex = 1;
                    } else if (indexToStart == lastPacketIndex - 1) { //POKUD JE PREDPOSLEDNI PACKET
                        packet1 = new Packet(mode.getPacket().get(indexToStart));
                        packet2 = new Packet(mode.getPacket().get(indexToStart + 1));
                        firstPacketIndex = 0;
                    } else {
                        packet1 = new Packet(mode.getPacket().get(indexToStart));
                        packet2 = new Packet(mode.getPacket().get(indexToStart + 1));
                        firstPacketIndex = indexToStart + 2;
                    }


                    packet1.setTime(0);
                    packet1.setRepeat(false);

                    packet2.setTime(postupnyCas - odPondeli);
                    packet2.setRepeat(false);

                    packetsToPost.add(packet1);
                    packetsToPost.add(packet2);

                    ArrayList<Packet> packetsToAdd = new ArrayList<>();

                    for (int i = firstPacketIndex; i < packets.size(); i++) {
                        packetsToAdd.add(new Packet(packets.get(i)));
                        if (i == lastPacketIndex) {
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
            }

            Intent myIntent = new Intent(TimetableActivity.this, LedSettingsActivity.class);
            myIntent.putExtra("id", this.ledId);
            myIntent.putExtra("fromRoom", this.fromRoom);
            this.finish();
            TimetableActivity.this.startActivity(myIntent);
        });
    }
    @SuppressLint("MissingPermission")
    @Override
    public void onBackPressed() {
        if (mode.isState()) {
            if(BluetoothService.bluetoothGatt == null){

                Toast.makeText(this, "Neaktualizoval se mód, protože není připojené Bluetooth", Toast.LENGTH_SHORT).show();
            }
            else if (fromRoom) {

                ArrayList<Packet> packets = allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket();

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
                    if (odPondeli < postupnyCas) {
                        if (i == 0) {
                            indexToStart = packets.size() - 1;
                        } else {
                            indexToStart = i - 1;
                        }
                        break;
                    }
                }
                if (indexToStart == -1) {
                    indexToStart = packets.size() - 1;
                }

                int celkovyCass = 0;
                for (int i = 1; i < mode.getPacket().size(); i++) {
                    celkovyCass += mode.getPacket().get(i).getTime();
                }
                mode.getPacket().get(0).setTime(604800 - celkovyCass);

                Packet packet1;
                Packet packet2;
                int lastPacketIndex = packets.size() - 1;

                if (indexToStart == lastPacketIndex) { //POKUD JE POSLEDNI PACKET
                    packet1 = new Packet(mode.getPacket().get(indexToStart));
                    packet2 = new Packet(mode.getPacket().get(0));
                    firstPacketIndex = 1;
                } else if (indexToStart == lastPacketIndex - 1) { //POKUD JE PREDPOSLEDNI PACKET
                    packet1 = new Packet(mode.getPacket().get(indexToStart));
                    packet2 = new Packet(mode.getPacket().get(indexToStart + 1));
                    firstPacketIndex = 0;
                } else {
                    packet1 = new Packet(mode.getPacket().get(indexToStart));
                    packet2 = new Packet(mode.getPacket().get(indexToStart + 1));
                    firstPacketIndex = indexToStart + 2;
                }


                packet1.setTime(0);
                packet1.setRepeat(false);

                packet2.setTime(postupnyCas - odPondeli);
                packet2.setRepeat(false);

                packetsToPost.add(packet1);
                packetsToPost.add(packet2);

                ArrayList<Packet> packetsToAdd = new ArrayList<>();

                for (int i = firstPacketIndex; i < packets.size(); i++) {
                    packetsToAdd.add(new Packet(packets.get(i)));
                    if (i == lastPacketIndex) {
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

                for (int i = 0; i < ledIds.size(); i++) {
                    for (int j = 0; j < packetsToPost.size(); j++) {
                        packetsToPost2.get(i * packetsToPost.size() + j).setLed(ledIds.get(i).getId() + 1);
                    }
                }


                for (int i = 0; i < packetsToPost2.size(); i++) {
                    BluetoothService.dataQueue.addPacket(packetsToPost2.get(i));
                }
                BluetoothService.characteristic.setValue(new byte[]{(byte) packetsToPost2.size()});
                BluetoothService.bluetoothGatt.writeCharacteristic(BluetoothService.characteristic);
            } else {

                System.out.println("Jsem v rozvrhu");
                ArrayList<Packet> packets = allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket();

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
                    if (odPondeli < postupnyCas) {
                        if (i == 0) {
                            indexToStart = packets.size() - 1;
                        } else {
                            indexToStart = i - 1;
                        }
                        break;
                    }
                }
                if (indexToStart == -1) {
                    indexToStart = packets.size() - 1;
                }
                System.out.println("Index to start: " + indexToStart);

                int celkovyCass = 0;
                for (int i = 1; i < mode.getPacket().size(); i++) {
                    celkovyCass += mode.getPacket().get(i).getTime();
                }
                mode.getPacket().get(0).setTime(604800 - celkovyCass);

                Packet packet1;
                Packet packet2;
                int lastPacketIndex = packets.size() - 1;

                if (indexToStart == lastPacketIndex) { //POKUD JE POSLEDNI PACKET
                    packet1 = new Packet(mode.getPacket().get(indexToStart));
                    packet2 = new Packet(mode.getPacket().get(0));
                    firstPacketIndex = 1;
                } else if (indexToStart == lastPacketIndex - 1) { //POKUD JE PREDPOSLEDNI PACKET
                    packet1 = new Packet(mode.getPacket().get(indexToStart));
                    packet2 = new Packet(mode.getPacket().get(indexToStart + 1));
                    firstPacketIndex = 0;
                } else {
                    packet1 = new Packet(mode.getPacket().get(indexToStart));
                    packet2 = new Packet(mode.getPacket().get(indexToStart + 1));
                    firstPacketIndex = indexToStart + 2;
                }


                packet1.setTime(0);
                packet1.setRepeat(false);

                packet2.setTime(postupnyCas - odPondeli);
                packet2.setRepeat(false);

                packetsToPost.add(packet1);
                packetsToPost.add(packet2);

                ArrayList<Packet> packetsToAdd = new ArrayList<>();

                for (int i = firstPacketIndex; i < packets.size(); i++) {
                    packetsToAdd.add(new Packet(packets.get(i)));
                    if (i == lastPacketIndex) {
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
        }

        Intent myIntent = new Intent(TimetableActivity.this, LedSettingsActivity.class);
        myIntent.putExtra("id", this.ledId);
        myIntent.putExtra("fromRoom", this.fromRoom);
        this.finish();
        TimetableActivity.this.startActivity(myIntent);
    }
}
