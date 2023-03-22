package cz.spse.maturitniprojekt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class RoomActivity extends Activity {
    private AllLedInfo allLedInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        BluetoothService.currentActivity = this;

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

        ArrayList<RoomObject> roomObjects = this.allLedInfo.getRoomObjects();
        LinearLayout list = findViewById(R.id.list);
        ImageView btn_connect = findViewById(R.id.con);

        if(BluetoothService.connected){
            btn_connect.setImageResource(R.drawable.bluetoothonn);
        }

        btn_connect.setOnClickListener(view -> {
            startService(new Intent(this, BluetoothService.class));
        });

        for (int i = 0; i < roomObjects.size(); i++) {
            Room ledmode = new Room(this, roomObjects.get(i).getName(), roomObjects.get(i).isState(), i, allLedInfo, this);
            ledmode.setPadding(25, 25, 25,25);
            ledmode.setBackground(getDrawable(R.drawable.border));
            list.addView(ledmode);
        }
        ArrayList info = new ArrayList();
        info.add(allLedInfo);
        Add add = new Add(this, 1, info, this);
        add.setPadding(25, 25, 25,25);
        add.setBackground(getDrawable(R.drawable.border));
        list.addView(add);

        TextView manLeds = findViewById(R.id.manLeds);

        manLeds.setOnClickListener(view -> {
            this.finish();
            overridePendingTransition(0, 0);
            Intent myIntent = new Intent(RoomActivity.this, MainActivity.class);
            RoomActivity.this.startActivity(myIntent);
        });
    }
    @Override
    public void onBackPressed() {}
}
