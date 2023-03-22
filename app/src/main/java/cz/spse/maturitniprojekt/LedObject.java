package cz.spse.maturitniprojekt;

import java.io.Serializable;
import java.util.ArrayList;

public class LedObject implements Serializable {
    private int id;
    private String name;
    private boolean state;
    private ArrayList<LedModeObject> ledModes = new ArrayList<>();

    public void addLedmode(LedModeObject packet){
        ledModes.add(packet);
    }

    public LedObject(int id, String name, boolean state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public void addLedMode(LedModeObject ledModeObject){
        ledModes.add(ledModeObject);
    }

    public ArrayList<LedModeObject> getLedModes() {
        return ledModes;
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
