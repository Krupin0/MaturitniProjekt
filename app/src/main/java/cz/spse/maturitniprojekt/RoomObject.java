package cz.spse.maturitniprojekt;

import java.io.Serializable;
import java.util.ArrayList;

public class RoomObject implements Serializable {
    private int id;
    private String name;
    private boolean state;
    private ArrayList<LedModeObject> ledModes = new ArrayList<>();
    private ArrayList<LedObject> ledObjects = new ArrayList<>();

    public void addLedmode(LedModeObject packet){
        ledModes.add(packet);
    }

    public RoomObject(int id, String name, boolean state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public void addLedMode(LedModeObject ledModeObject){
        ledModes.add(ledModeObject);
    }
    public void addLedObject(LedObject ledObject){
        ledObjects.add(ledObject);
    }

    public ArrayList<LedModeObject> getLedModes() {
        return ledModes;
    }
    public ArrayList<LedObject> getLedObjects() {
        return ledObjects;
    }

    public String getName() {
        return name;
    }

    public boolean isState() {
        return state;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
