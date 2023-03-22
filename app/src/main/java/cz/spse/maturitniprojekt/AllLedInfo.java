package cz.spse.maturitniprojekt;

import java.io.Serializable;
import java.util.ArrayList;

public class AllLedInfo implements Serializable {
    private ArrayList<LedObject> ledObjects = new ArrayList<>();
    private ArrayList<RoomObject> roomObjects = new ArrayList<>();

    public void addLedObject(LedObject ledObject) {
        ledObjects.add(ledObject);
    }
    public void addRoomObject(RoomObject roomObject) {
        roomObjects.add(roomObject);
    }

    @Override
    public String toString() {
        return "AllLedInfo{" +
                "ledObjects=" + ledObjects +
                '}';
    }

    public ArrayList<LedObject> getLedObjects() {
        return ledObjects;
    }
    public ArrayList<RoomObject> getRoomObjects() {
        return roomObjects;
    }
}
