package cz.spse.maturitniprojekt;

import java.util.ArrayList;

public class lib {
    public static byte toByte(int i) {
        i -= 128;
        byte b = (byte) i;
        return b;
    }
    public static byte toByte(boolean i) {
        byte b;
        if(i) {
            b = (byte) 1-128;
        }
        else {
            b = (byte) 0 -128;
        }
        return b;
    }
    public static int dayToInt(String day) {
        if(day.equals("Po")) {
            return 0;
        }
        else if(day.equals("Ut")) {
            return 1;
        }
        else if(day.equals("St")) {
            return 2;
        }
        else if(day.equals("Ct")) {
            return 3;
        }
        else if(day.equals("Pa")) {
            return 4;
        }
        else if(day.equals("So")) {
            return 5;
        }
        else if(day.equals("Ne")) {
            return 6;
        }
        else {
            System.out.println("Error: dayToInt() - day not found");
            return 0;
        }
    }

    public static byte[] intToBase255(int value) {
        int[] iresult = new int[4];
        iresult[0] = value / (255 * 255 * 255);
        iresult[1] = (value / (255 * 255)) % 255;
        iresult[2] = (value / 255) % 255;
        iresult[3] = value % 255;

        byte[] result = {toByte(iresult[0]), toByte(iresult[1]), toByte(iresult[2]), toByte(iresult[3])};
        return result;
    }

    public static byte[] sendPackets(ArrayList<Packet> packets) {
        ArrayList<Byte> result = new ArrayList<>();
        for (Packet packet : packets) {
            byte[] packetBytes = packet.packetToPost();
            for (byte packetByte : packetBytes) {
                result.add(packetByte);
            }
        }
        byte[] resultArray = new byte[result.size()+1];
        resultArray[0] = (byte) packets.size();
        for (int i = 0; i < result.size(); i++) {
            resultArray[i+1] = result.get(i);
        }
        for (int i = 0; i < resultArray.length; i++) {
            System.out.println(resultArray[i]);
        }
        return resultArray;
    }
}