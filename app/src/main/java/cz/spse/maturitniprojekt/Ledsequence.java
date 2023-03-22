package cz.spse.maturitniprojekt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputType;
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

public class Ledsequence extends LinearLayout {
    private String time;
    private int color;
    private int id; //pozice v arraylistu
    private int ledId; //id ledky
    private int brightness;
    private AllLedInfo allLedInfo;
    private boolean fromRoom;
    private int packetId;
    private Activity activity;

    private TextView timeElement;
    private ImageView imageElement;
    private ImageView deleteElement;

    public Ledsequence(Context context) {
        super(context);
        initializeViews(context);
    }
    public Ledsequence(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public Ledsequence(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    public Ledsequence(Context context, String time, int id, int ledId, int color, AllLedInfo allLedInfo, int brightness, boolean fromRoom, int packetId, Activity activity) {
        super(context);
        this.time = time;
        this.id = id;
        this.ledId = ledId;
        this.color = color;
        this.allLedInfo = allLedInfo;
        this.brightness = brightness;
        this.fromRoom = fromRoom;
        this.packetId = packetId;
        this.activity = activity;
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.ledsequence_view, this);

        timeElement = findViewById(R.id.led_time);
        deleteElement = findViewById(R.id.delete);
        imageElement = findViewById(R.id.led_image);
        imageElement.setBackgroundColor(color);
        time = time.substring(3);
        timeElement.setText(this.time);


        deleteElement.setOnClickListener(view -> {
            if(fromRoom){
                if(allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().size() == 2){
                    Toast.makeText(context, "Musí zde být alespoň dvě změny", Toast.LENGTH_SHORT).show();
                }
                else{
                    allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().remove(packetId);
                }
            }
            else{
                if(allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().size() == 2){
                    Toast.makeText(context, "Musí zde být alespoň dvě změny", Toast.LENGTH_SHORT).show();
                }
                else{
                    allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().remove(packetId);
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
            activity.recreate();
        });

        timeElement.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Uprav sekvenci");

            View dialogView = inflater.inflate(R.layout.dialog_sequence, null);
            builder.setView(dialogView);


            //EditText secs = dialogView.findViewById(R.id.sInput);
            //EditText mins = dialogView.findViewById(R.id.mInput);
            TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
            ColorPickerView picker = dialogView.findViewById(R.id.picker);
            SeekBar brightnessElement = dialogView.findViewById(R.id.brightness);

            timePicker.setIs24HourView(true);
            timePicker.setHour(Integer.parseInt(time.split(":")[0]));
            timePicker.setMinute(Integer.parseInt(time.split(":")[1]));
            //secs.setText(time.split(":")[1]);
            //mins.setText(time.split(":")[0]);

            picker.setInitialColor(color);
            brightnessElement.setProgress((int)((((double)this.brightness+128.0)/255.0) * 100.0));

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //timeElement.setText(mins.getText().toString() + ":" + secs.getText().toString());
                    //imageElement.setBackgroundColor(picker.getColor());

                    color = picker.getColor();
                    brightness = (int)(((double)brightnessElement.getProgress()/100.0)*255.0);
                    //time = mins.getText().toString() + ":" + secs.getText().toString();
                    time = timePicker.getHour() + ":" + timePicker.getMinute();


                    if(fromRoom){
                        allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().get(packetId).setColor(picker.getColor());
                        allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().get(packetId).setBrightness((int)(((double)brightnessElement.getProgress()/100.0)*255.0));
                        if(packetId == allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().size()-1){
                            allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().get(0).setTime(timePicker.getMinute() + (timePicker.getHour()*60));
                        }
                        else{
                            allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().get(packetId+1).setTime(timePicker.getMinute() + (timePicker.getHour()*60));
                        }
                    }
                    else{
                        allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().get(packetId).setColor(picker.getColor());
                        allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().get(packetId).setBrightness((int)(((double)brightnessElement.getProgress()/100.0)*255.0));
                        if(packetId == allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().size()-1){
                            allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().get(0).setTime(timePicker.getMinute() + (timePicker.getHour()*60));
                        }
                        else{
                            allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().get(packetId+1).setTime(timePicker.getMinute() + (timePicker.getHour()*60));
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
                    activity.recreate();
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
