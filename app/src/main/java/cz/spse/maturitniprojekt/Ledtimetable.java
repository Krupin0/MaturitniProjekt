package cz.spse.maturitniprojekt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.skydoves.colorpickerview.ColorPickerView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Ledtimetable extends LinearLayout {
    private String time;
    private ArrayList<String> days;
    private int color;
    private int id; //pozice v arraylistu
    private int ledId; //id ledky
    private byte brightness;
    private AllLedInfo allLedInfo;
    private HashMap<Integer, ArrayList> timetable;
    private boolean fromRoom;
    private int packetKey;
    private Activity activity;


    private ImageView colorElement;
    private TextView timeElement;
    private TextView daysElement;
    private ImageView deleteElement;


    public Ledtimetable(Context context) {
        super(context);
        initializeViews(context);
    }
    public Ledtimetable(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public Ledtimetable(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    public Ledtimetable(Context context, String time, ArrayList<String> days, int id, int ledId, int color, AllLedInfo allLedInfo, byte brightness, HashMap timetable, boolean fromRoom, int packetKey, Activity activity) {
        super(context);
        this.time = time;
        this.id = id;
        this.ledId = ledId;
        this.days = days;
        this.color = color;
        this.allLedInfo = allLedInfo;
        this.brightness = brightness;
        this.timetable = timetable;
        this.fromRoom = fromRoom;
        this.packetKey = packetKey;
        this.activity = activity;
        //System.out.println(timetable);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.ledtimetable_view, this);

        timeElement = findViewById(R.id.time);
        daysElement = findViewById(R.id.days);
        colorElement = findViewById(R.id.led_image);
        deleteElement = findViewById(R.id.delete);

        timeElement.setText(this.time);


        String sdays = "";
        for (int i = 0; i < this.days.size(); i++) {
            sdays += days.get(i);
            if(i != days.size()-1){
                sdays += ", ";
            }
        }
        daysElement.setText(sdays);


        colorElement.setBackgroundColor(color);


        deleteElement.setOnClickListener(view -> {

            if(timetable.keySet().size() == 1){
                Toast.makeText(context, "Rozvrh musí mít alespoň jeden čas", Toast.LENGTH_SHORT).show();
            }
            else{

                timetable.remove(packetKey);
                ArrayList<Packet> newPackets = new ArrayList<>();

                timetable.keySet().forEach(key -> {
                    ArrayList<String> dayslist = (ArrayList) timetable.get(key).get(0);
                    for (int i = 0; i < dayslist.size(); i++) {
                        Packet packet = new Packet(ledId+1, (int)timetable.get(key).get(1), Integer.parseInt(timetable.get(key).get(2).toString()), (lib.dayToInt(dayslist.get(i))*86400)+key, true);
                        newPackets.add(packet);
                    }
                });

                newPackets.sort(Comparator.comparingInt(Packet::getTime));
                //System.out.println(newPackets);
                int dosavadniCas = 0;
                for (int i = 0; i < newPackets.size(); i++) {
                    newPackets.get(i).setTime(newPackets.get(i).getTime() - dosavadniCas);
                    dosavadniCas += newPackets.get(i).getTime();
                }
                //System.out.println(newPackets);
                //newPackets.get(0).setTime(newPackets.get(0).getTime() + 604800 - dosavadniCas);

                if(fromRoom){
                    allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).setPackets(newPackets);
                }
                else {
                    allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).setPackets(newPackets);
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
                activity.recreate();
            }
        });

        timeElement.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Uprav rozvrh");

            View dialogView = inflater.inflate(R.layout.dialog_timetable, null);
            builder.setView(dialogView);

            //EditText mins = dialogView.findViewById(R.id.mInput);
            //EditText hours = dialogView.findViewById(R.id.hInput);
            TimePicker timePicker = dialogView.findViewById(R.id.time_picker);

            ColorPickerView picker = dialogView.findViewById(R.id.picker);
            Switch monday = dialogView.findViewById(R.id.switch_po);
            Switch tuesday = dialogView.findViewById(R.id.switch_ut);
            Switch wednesday = dialogView.findViewById(R.id.switch_st);
            Switch thursday = dialogView.findViewById(R.id.switch_ct);
            Switch friday = dialogView.findViewById(R.id.switch_pa);
            Switch saturday = dialogView.findViewById(R.id.switch_so);
            Switch sunday = dialogView.findViewById(R.id.switch_ne);
            SeekBar brightnessElement = dialogView.findViewById(R.id.brightness);

            //mins.setText(time.split(":")[1]);
            //hours.setText(time.split(":")[0]);
            timePicker.setIs24HourView(true);
            timePicker.setHour(Integer.parseInt(time.split(":")[0]));
            timePicker.setMinute(Integer.parseInt(time.split(":")[1]));
            picker.setInitialColor(color);
            brightnessElement.setProgress((int)((((double)this.brightness+128.0)/255.0) * 100.0));


            if(days.contains("Po")){
                monday.setChecked(true);
            }
            if(days.contains("Ut")){
                tuesday.setChecked(true);
            }
            if(days.contains("St")){
                wednesday.setChecked(true);
            }
            if(days.contains("Ct")){
                thursday.setChecked(true);
            }
            if(days.contains("Pa")){
                friday.setChecked(true);
            }
            if(days.contains("So")){
                saturday.setChecked(true);
            }
            if(days.contains("Ne")){
                sunday.setChecked(true);
            }

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ArrayList<String> newDays = new ArrayList<>();
                    if(monday.isChecked()){
                        newDays.add("Po");
                    }
                    if(tuesday.isChecked()){
                        newDays.add("Ut");
                    }
                    if(wednesday.isChecked()){
                        newDays.add("St");
                    }
                    if(thursday.isChecked()){
                        newDays.add("Ct");
                    }
                    if(friday.isChecked()){
                        newDays.add("Pa");
                    }
                    if(saturday.isChecked()){
                        newDays.add("So");
                    }
                    if(sunday.isChecked()){
                        newDays.add("Ne");
                    }
                    if(newDays.size() == 0){
                        Toast.makeText(context, "Musíš vybrat alespoň jeden den", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        timetable.remove(packetKey);
                        //int newKey = Integer.parseInt(hours.getText().toString())*3600 + Integer.parseInt(mins.getText().toString())*60;
                        int newKey = timePicker.getHour()*3600 + timePicker.getMinute()*60;
                        timetable.put(newKey, new ArrayList());
                        timetable.get(newKey).add(newDays);
                        timetable.get(newKey).add(picker.getColor());
                        timetable.get(newKey).add((int)((brightnessElement.getProgress()/100.0) * 255.0));


                        ArrayList<Packet> newPackets = new ArrayList<>();

                        timetable.keySet().forEach(key -> {
                            ArrayList<String> dayslist = (ArrayList) timetable.get(key).get(0);
                            for (int i = 0; i < dayslist.size(); i++) {
                                Packet packet = new Packet(ledId+1, (int)timetable.get(key).get(1), Integer.parseInt(timetable.get(key).get(2).toString()), (lib.dayToInt(dayslist.get(i))*86400)+key, true);
                                newPackets.add(packet);
                            }
                        });

                        newPackets.sort(Comparator.comparingInt(Packet::getTime));
                        //System.out.println(newPackets);
                        int dosavadniCas = 0;
                        for (int i = 0; i < newPackets.size(); i++) {
                            newPackets.get(i).setTime(newPackets.get(i).getTime() - dosavadniCas);
                            dosavadniCas += newPackets.get(i).getTime();
                        }
                        //System.out.println(newPackets);
                        //newPackets.get(0).setTime(newPackets.get(0).getTime() + 604800 - dosavadniCas);

                        if(fromRoom){
                            allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).setPackets(newPackets);
                        }
                        else {
                            allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).setPackets(newPackets);
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
                        activity.recreate();
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
    }


}
