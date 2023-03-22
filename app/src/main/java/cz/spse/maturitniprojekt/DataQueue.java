package cz.spse.maturitniprojekt;

import java.util.ArrayList;

public class DataQueue {
    private ArrayList<Packet> packets;

    public void addPacket(Packet packet) {
        packets.add(packet);
    }

    public void removePacket() {
        packets.remove(0);
    }

    public Packet getPacket() {
        return packets.get(0);
    }

    public DataQueue() {
        this.packets = new ArrayList<>();
    }

    public boolean isEmpty() {
        return packets.isEmpty();
    }
}
