package cz.spse.maturitniprojekt;

import static android.bluetooth.BluetoothAdapter.STATE_CONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;
import static android.bluetooth.BluetoothAdapter.STATE_OFF;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.UUID;


public class BluetoothService extends Service {

   BluetoothLeScanner bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    public static BluetoothGatt bluetoothGatt;
    public static BluetoothGattCharacteristic characteristic;
    public static DataQueue dataQueue = new DataQueue();
    public static Activity currentActivity;
    public static boolean connected = false;
    Context context = this;
    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            System.out.println("Connected");
            if (newState == STATE_CONNECTED){
                gatt.discoverServices();
                //bluetoothGatt = gatt;
            }
            else if(newState == STATE_OFF || newState == STATE_DISCONNECTED){
                System.out.println("Disconnected");
                disconected();
                stopSelf();
            }
        }
        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("Services discovered");
            bluetoothGatt = gatt;
            gatt.getServices().forEach(service -> {
                //System.out.println(service.getUuid());
            });
            UUID serviceUuid = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
            UUID charUuid = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
            characteristic = gatt.getService(serviceUuid).getCharacteristic(charUuid);
            gatt.getService(serviceUuid).getCharacteristics().forEach(ccc -> {
                //System.out.println(ccc.getUuid());
            });
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            //gatt.requestMtu(512);
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("Characteristic read");
            byte[] bytes = characteristic.getValue();
            for (int i = 0; i < bytes.length; i++) {
                System.out.print(Character.toString((char) (bytes[i]+127)));
            }
            System.out.println();
        }
        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("Characteristic write");
            System.out.println(status);
            if(!dataQueue.isEmpty()) {
                characteristic.setValue(dataQueue.getPacket().packetToPost());
                dataQueue.removePacket();
                bluetoothGatt.writeCharacteristic(characteristic);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            System.out.println("MTU changed");
            System.out.println(mtu);
            System.out.println(status);
        }
    };
    @SuppressLint("MissingPermission")
    public static void disconected(){
        bluetoothGatt.close();
        bluetoothGatt = null;
        connected = false;
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(currentActivity, "Odpojeno", Toast.LENGTH_SHORT).show();
            }
        });
        currentActivity.runOnUiThread(new Runnable() {
            public void run() {
                currentActivity.recreate();
            }
        });
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if(!BluetoothAdapter.getDefaultAdapter().isEnabled()){
            Toast.makeText(this, "Bluetooth není zapnuté", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        else if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "Poloha není zapnutá", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        else{
            ScanCallback scanCallback = new ScanCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    BluetoothDevice device = result.getDevice();
                    if (device.getAddress().equals("20:91:48:AD:33:A2")) {
                        System.out.println("Found");
                        connected = true;
                        currentActivity.recreate();
                        Toast.makeText(context, "Připojeno", Toast.LENGTH_SHORT).show();
                        bluetoothLeScanner.stopScan(this);
                        device.connectGatt(context, false, gattCallback);
                    }
                }
            };

            //ZDE START
            bluetoothLeScanner.startScan(scanCallback);
        }
        return START_STICKY;
    }
}
