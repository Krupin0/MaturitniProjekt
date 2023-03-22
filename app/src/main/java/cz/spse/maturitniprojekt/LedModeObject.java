package cz.spse.maturitniprojekt;

import java.io.Serializable;
import java.util.ArrayList;

public class LedModeObject implements Serializable {
    private ArrayList<Packet> packet;
    private String name;
    private int mode;
    private boolean state;

    public LedModeObject(String name, int mode, boolean state) {
        this.packet = new ArrayList<>();
        this.name = name;
        this.mode = mode;
        this.state = state;
    }

    public void setPackets(ArrayList<Packet> packet) {
        this.packet = packet;
    }

    public int getMode() {
        return mode;
    }

    public ArrayList<Packet> getPacket() {
        return packet;
    }

    public String getName() {
        return name;
    }

    public void addPacket(Packet packet){
        this.packet.add(packet);
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
