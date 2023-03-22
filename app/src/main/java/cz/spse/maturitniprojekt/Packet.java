package cz.spse.maturitniprojekt;

import android.graphics.Color;

import java.io.Serializable;
import java.util.Objects;

public class Packet implements Serializable {
    private byte led;
    private byte red;
    private byte green;
    private byte blue;
    private byte brightness;
    private byte time1;
    private byte time2;
    private byte time3;
    private byte time4;
    private byte repeat;

    public Packet(int led, int[] colors, int brightness, int time, boolean repeat) {
        this.led = lib.toByte(led);
        this.red = lib.toByte(colors[1]);
        this.green = lib.toByte(colors[2]);
        this.blue = lib.toByte(colors[3]);
        this.brightness = lib.toByte(brightness);
        byte[] timex = lib.intToBase255(time);
        this.time1 = timex[0];
        this.time2 = timex[1];
        this.time3 = timex[2];
        this.time4 = timex[3];
        this.repeat = lib.toByte(repeat);
    }

    public Packet(int led, int colors, int brightness, int time, boolean repeat) {
        this.led = lib.toByte(led);
        this.red = lib.toByte(Color.red(colors));
        this.green = lib.toByte(Color.green(colors));
        this.blue = lib.toByte(Color.blue(colors));
        this.brightness = lib.toByte(brightness);
        byte[] timex = lib.intToBase255(time);
        this.time1 = timex[0];
        this.time2 = timex[1];
        this.time3 = timex[2];
        this.time4 = timex[3];
        this.repeat = lib.toByte(repeat);
    }

    public boolean isTimeZero(){
        return time1 == 0 && time2 == 0 && time3 == 0 && time4 == 0;
    }

    public boolean isRepeat(){
        return repeat == 1;
    }

    public byte[] packetToPost() {
        byte[] packet = {led, red, green, blue, brightness, time1, time2, time3, time4, repeat};
        System.out.println(this);
        return packet;
    }

    public int getColor(){
        int rgb = android.graphics.Color.rgb(this.red+128, this.green+128, this.blue+128);
        return rgb;
    }

    public int getLed(){
        return this.led+128;
    }

    public String timeToString(){
        //System.out.println("time1: " + time1 + " time2: " + time2 + " time3: " + time3 + " time4: " + time4);
        int totalSecs =  (16581375*(time1+128)) + (65025*(time2+128)) + (255*(time3+128)) + (time4+128);
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public int getTime(){
        return (16581375*(time1+128)) + (65025*(time2+128)) + (255*(time3+128)) + (time4+128);
    }

    public byte getBrightness() {
        System.out.println("brightness: " + brightness);
        return brightness;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "led=" + led +
                ", red=" + red +
                ", green=" + green +
                ", blue=" + blue +
                ", brightness=" + brightness +
                ", time1=" + time1 +
                ", time2=" + time2 +
                ", time3=" + time3 +
                ", time4=" + time4 +
                ", repeat=" + repeat +
                '}';
    }

    public void setBrightness(int i) {
        this.brightness = lib.toByte(i);
    }

    public void setColor(int color){
        this.red = lib.toByte(Color.red(color));
        this.green = lib.toByte(Color.green(color));
        this.blue = lib.toByte(Color.blue(color));
    }

    public void setLed(int i) {
        this.led = lib.toByte(i);
    }

    public void setTime(int parseInt) {
        byte[] timex = lib.intToBase255(parseInt);
        this.time1 = timex[0];
        this.time2 = timex[1];
        this.time3 = timex[2];
        this.time4 = timex[3];
    }

    public Packet(Packet packet) {
        this.led = packet.led;
        this.red = packet.red;
        this.green = packet.green;
        this.blue = packet.blue;
        this.brightness = packet.brightness;
        this.time1 = packet.time1;
        this.time2 = packet.time2;
        this.time3 = packet.time3;
        this.time4 = packet.time4;
        this.repeat = packet.repeat;
    }

    public void setRepeat(boolean b) {
        this.repeat = lib.toByte(b);
    }
}
