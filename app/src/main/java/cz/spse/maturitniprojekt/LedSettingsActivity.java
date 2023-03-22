package cz.spse.maturitniprojekt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class LedSettingsActivity extends Activity {
    private int ledId;
    private boolean fromRoom;
    private AllLedInfo allLedInfo;

    private Ledinroom ledinroom1;
    private Ledinroom ledinroom2;
    private Ledinroom ledinroom3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledsettings);

        ImageView backElement = findViewById(R.id.back);
        TextView nameElement = findViewById(R.id.nameled);
        ImageView renameElement = findViewById(R.id.rename);


        Bundle extras = getIntent().getExtras();
        this.ledId = extras.getInt("id");
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

        ArrayList<LedModeObject> modes;

        ArrayList<LedObject> ledObjects = this.allLedInfo.getLedObjects();
        ArrayList<RoomObject> roomObjects = this.allLedInfo.getRoomObjects();

        if(this.fromRoom){
            modes = roomObjects.get(this.ledId).getLedModes();
            nameElement.setText(roomObjects.get(this.ledId).getName());
        }
        else{
            modes = ledObjects.get(this.ledId).getLedModes();
            nameElement.setText(ledObjects.get(this.ledId).getName());
        }

        LinearLayout list = findViewById(R.id.list);

        ledinroom1 = findViewById(R.id.led1);
        ledinroom2 = findViewById(R.id.led2);
        ledinroom3 = findViewById(R.id.led3);

        if(fromRoom){

            ArrayList<LedObject> ledsInRoom = roomObjects.get(this.ledId).getLedObjects();
            ArrayList<LedObject> allLeds = allLedInfo.getLedObjects();

            ledinroom1.setName(allLeds.get(0).getName());
            ledinroom2.setName(allLeds.get(1).getName());
            ledinroom3.setName(allLeds.get(2).getName());

            for (int i = 0; i < ledsInRoom.size(); i++) {
                if(ledsInRoom.get(i).getId() == 0){
                    ledinroom1.setState(true);
                }
                else if(ledsInRoom.get(i).getId() == 1){
                    ledinroom2.setState(true);
                }
                else if(ledsInRoom.get(i).getId() == 2){
                    ledinroom3.setState(true);
                }
            }
        }
        if(!fromRoom){
            TextView text = findViewById(R.id.text);

            text.setVisibility(View.GONE);
            ledinroom1.setVisibility(View.GONE);
            ledinroom2.setVisibility(View.GONE);
            ledinroom3.setVisibility(View.GONE);
        }

        for (int i = 0; i < modes.size(); i++) {
            Ledmode ledmode = new Ledmode(this, modes.get(i).getName(), i, ledId, modes.get(i).getMode(), this.fromRoom, this.allLedInfo, this, modes.get(i).isState());
            ledmode.setPadding(25, 25, 25,25);
            ledmode.setBackground(getDrawable(R.drawable.border));
            list.addView(ledmode);
        }
            ArrayList info = new ArrayList();
            info.add(this.ledId);
            info.add(this.fromRoom);
            info.add(this.allLedInfo);
            Add add = new Add(this, 2, info, this);
            add.setPadding(25, 25, 25,25);
            add.setBackground(getDrawable(R.drawable.border));
            list.addView(add);

        renameElement.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(LedSettingsActivity.this);
            builder.setTitle("Zadej název");
            final EditText input = new EditText(LedSettingsActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            input.setText(nameElement.getText());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.out.println(input.getText().toString());
                    nameElement.setText(input.getText().toString());
                    if(fromRoom)
                        roomObjects.get(ledId).setName(input.getText().toString());

                    else
                        ledObjects.get(ledId).setName(input.getText().toString());

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



        backElement.setOnClickListener(view -> {
            if(fromRoom){
                int pocetLedek = 0;
                if(ledinroom1.getState()){
                    pocetLedek++;
                }
                if(ledinroom2.getState()){
                    pocetLedek++;
                }
                if(ledinroom3.getState()){
                    pocetLedek++;
                }
                if(pocetLedek == 0){
                    Toast.makeText(this, "Musí být vybrána alespoň jedna LED", Toast.LENGTH_SHORT).show();
                }
                else{
                    allLedInfo.getRoomObjects().get(ledId).getLedObjects().clear();

                    if(ledinroom1.getState()){
                        allLedInfo.getRoomObjects().get(ledId).getLedObjects().add(allLedInfo.getLedObjects().get(0));
                    }
                    if(ledinroom2.getState()){
                        allLedInfo.getRoomObjects().get(ledId).getLedObjects().add(allLedInfo.getLedObjects().get(1));
                    }
                    if(ledinroom3.getState()){
                        allLedInfo.getRoomObjects().get(ledId).getLedObjects().add(allLedInfo.getLedObjects().get(2));
                    }

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

                Intent myIntent = new Intent(LedSettingsActivity.this, RoomActivity.class);
                LedSettingsActivity.this.startActivity(myIntent);
            }
            else{
                Intent myIntent = new Intent(LedSettingsActivity.this, MainActivity.class);
                LedSettingsActivity.this.startActivity(myIntent);
            }
        });
    }
    @Override
    public void onBackPressed() {
        System.out.println("Back button pressed");
        if(fromRoom){
            int pocetLedek = 0;
            if(ledinroom1.getState()){
                pocetLedek++;
            }
            if(ledinroom2.getState()){
                pocetLedek++;
            }
            if(ledinroom3.getState()){
                pocetLedek++;
            }
            if(pocetLedek == 0){
                Toast.makeText(this, "Musí být vybrána alespoň jedna LED", Toast.LENGTH_SHORT).show();
            }
            else{
                allLedInfo.getRoomObjects().get(ledId).getLedObjects().clear();

                if(ledinroom1.getState()){
                    allLedInfo.getRoomObjects().get(ledId).getLedObjects().add(allLedInfo.getLedObjects().get(0));
                }
                if(ledinroom2.getState()){
                    allLedInfo.getRoomObjects().get(ledId).getLedObjects().add(allLedInfo.getLedObjects().get(1));
                }
                if(ledinroom3.getState()){
                    allLedInfo.getRoomObjects().get(ledId).getLedObjects().add(allLedInfo.getLedObjects().get(2));
                }

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

            Intent myIntent = new Intent(LedSettingsActivity.this, RoomActivity.class);
            LedSettingsActivity.this.startActivity(myIntent);
        }
        else{
            Intent myIntent = new Intent(LedSettingsActivity.this, MainActivity.class);
            LedSettingsActivity.this.startActivity(myIntent);
        }
    }
}
