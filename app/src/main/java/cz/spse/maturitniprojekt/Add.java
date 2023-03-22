package cz.spse.maturitniprojekt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Add extends LinearLayout {
    private ArrayList info;
    private int mod;
    private Activity activity;
    private ImageView image;

    public Add(Context context) {
        super(context);
        initializeViews(context);
    }
    public Add(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public Add(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    public Add(Context context, int mod, ArrayList info, Activity activity) {
        super(context);
        this.mod = mod;
        this.info = info;
        this.activity = activity;
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.add_view, this);
        this.image = findViewById(R.id.addImage);

        image.setOnClickListener(view -> {
            AllLedInfo allLedInfo = null;
            if(mod==1){
                allLedInfo = (AllLedInfo) info.get(0);
                RoomObject roomObject = new RoomObject(0, "Název místnosti", false);
                LedObject led1 = allLedInfo.getLedObjects().get(0);
                roomObject.addLedObject(led1);
                allLedInfo.addRoomObject(roomObject);
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
            if(mod==2){
                int ledid = (int) info.get(0);
                boolean fromRoom = (boolean) info.get(1);
                allLedInfo = (AllLedInfo) info.get(2);

                int[] checkedItem = {-1};
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle("Vyber mód");

                final String[] listItems = new String[]{"Stálá barva", "Odpočet", "Sekvence", "Rozvrh"};
                alertDialog.setSingleChoiceItems(listItems, checkedItem[0], (dialog, which) -> {
                    checkedItem[0] = which;
                });
                AllLedInfo finalAllLedInfo = allLedInfo;
                alertDialog.setPositiveButton("ok", (dialog, which) -> {
                    if(checkedItem[0] == 0){
                        Packet packet = new Packet(ledid+1, new int[]{0, 0, 0, 0}, 0, 0, false);
                        LedModeObject ledModeObject = new LedModeObject("Název modu", 0, false);
                        ledModeObject.addPacket(packet);
                        if(fromRoom){
                            finalAllLedInfo.getRoomObjects().get(ledid).addLedMode(ledModeObject);
                        }
                        else{
                            finalAllLedInfo.getLedObjects().get(ledid).addLedMode(ledModeObject);
                        }
                    }
                    if(checkedItem[0] == 1){
                        Packet packet = new Packet(ledid+1, new int[]{0, 0, 0, 0}, 0, 0, false);
                        Packet packet1 = new Packet(ledid+1, new int[]{0, 0, 0, 0}, 0, 10, false);
                        LedModeObject ledModeObject = new LedModeObject("Název modu", 1, false);
                        ledModeObject.addPacket(packet);
                        ledModeObject.addPacket(packet1);
                        if(fromRoom){
                            finalAllLedInfo.getRoomObjects().get(ledid).addLedMode(ledModeObject);
                        }
                        else{
                            finalAllLedInfo.getLedObjects().get(ledid).addLedMode(ledModeObject);
                        }
                    }
                    if(checkedItem[0] == 2){
                        Packet packet = new Packet(ledid+1, new int[]{0, 0, 0, 0}, 0, 5, true);
                        Packet packet1 = new Packet(ledid+1, new int[]{0, 0, 0, 0}, 0, 5, true);
                        LedModeObject ledModeObject = new LedModeObject("Název modu", 2, false);
                        ledModeObject.addPacket(packet);
                        ledModeObject.addPacket(packet1);
                        if(fromRoom){
                            finalAllLedInfo.getRoomObjects().get(ledid).addLedMode(ledModeObject);
                        }
                        else{
                            finalAllLedInfo.getLedObjects().get(ledid).addLedMode(ledModeObject);
                        }
                    }
                    if(checkedItem[0] == 3){
                        Packet packet1 = new Packet(ledid+1, new int[]{0, 0, 0, 0}, 0, 3600, true);
                        Packet packet2 = new Packet(ledid+1, new int[]{0, 0, 0, 0}, 0, 86400, true);
                        LedModeObject ledModeObject = new LedModeObject("Název modu", 3, false);
                        ledModeObject.addPacket(packet1);
                        ledModeObject.addPacket(packet2);
                        if(fromRoom){
                            finalAllLedInfo.getRoomObjects().get(ledid).addLedMode(ledModeObject);
                        }
                        else{
                            finalAllLedInfo.getLedObjects().get(ledid).addLedMode(ledModeObject);
                        }
                    }
                    try {
                        FileOutputStream out = context.openFileOutput("data.txt", Context.MODE_PRIVATE);
                        ObjectOutputStream objOut = new ObjectOutputStream(out);
                        objOut.writeObject(finalAllLedInfo);
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
                alertDialog.setNegativeButton("Cancel", (dialog, which) -> {
                    System.out.println("konec");
                    dialog.dismiss();
                });

                AlertDialog customAlertDialog = alertDialog.create();
                customAlertDialog.show();
            }

            if(mod==3){
                int ledId = (int) info.get(0);
                int id = (int) info.get(1);
                boolean fromRoom = (boolean) info.get(2);
                allLedInfo = (AllLedInfo) info.get(3);

                Packet packet = new Packet(ledId+1, new int[]{0, 0, 0, 0}, 0, 10, true);

                if(fromRoom){
                    allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().add(packet);
                }
                else{
                    allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().add(packet);
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

            if(mod==4){
                int ledId = (int) info.get(0);
                int id = (int) info.get(1);
                allLedInfo = (AllLedInfo) info.get(2);
                boolean fromRoom = (boolean) info.get(3);

                Packet packet = new Packet(ledId+1, new int[]{0, 0, 0, 0}, 0, 3600, true);
                if(fromRoom){
                    allLedInfo.getRoomObjects().get(ledId).getLedModes().get(id).getPacket().add(packet);
                }
                else{
                    allLedInfo.getLedObjects().get(ledId).getLedModes().get(id).getPacket().add(packet);
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
    }


}
