package com.example.anouar.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class ClientActivity extends AppCompatActivity {

    public ArrayList<BluetoothDevice> mBTNonPairedDevicesArray = new ArrayList<>();
    public ArrayList<BluetoothDevice> mBTPairedDevicesArray = new ArrayList<>();
    BluetoothAdapter mBTA = BluetoothAdapter.getDefaultAdapter();

    public static BluetoothDevice selectedDeviceForConnection;
    Intent ControlActivityIntent;
    public static Activity mClientActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        ControlActivityIntent = new Intent(this, Control.class);
        mClientActivity = this;


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        IntentFilter mEnDisBTIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiverAdapterStateChanged, mEnDisBTIntentFilter);

        IntentFilter mBondBTIntentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiverBondStateChanged, mBondBTIntentFilter);

        CheckBox mCH = findViewById(R.id.checkBox);
        if (mBTA.isEnabled()) {
            loadPreviouslyBondedDevices();
        }
        mCH.setChecked(mBTA.isEnabled());
        mCH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableDisableBluetooth();
            }
        });
        ListView mLV = findViewById(R.id.nonPairedList);
        final Toast ta = Toast.makeText(this, "Bonding", Toast.LENGTH_LONG);
        mLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ta.show();
                mBTA.cancelDiscovery();
                Log.d("Bonding", "We Clicked On a ListView Item");
                String Address = mBTNonPairedDevicesArray.get(position).getAddress();
                String Name = mBTNonPairedDevicesArray.get(position).getName();
                Log.d("Bonding", "Address " + Address + " Name " + Name);
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    mBTNonPairedDevicesArray.get(position).createBond();

                }

            }
        });

        ListView PairedDevicesListView = findViewById(R.id.pairedList);
        PairedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                selectedDeviceForConnection = mBTPairedDevicesArray.get(position);


                startActivity(ControlActivityIntent);
            }
        });


        Button ScanButton = findViewById(R.id.scan);
        ScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverDevices();
            }
        });


        refreshDisplay();
    }


    public void enableDisableBluetooth() {


        if (mBTA == null) {
            Toast toast = Toast.makeText(this, "Your Device Does NOT have Bluetooth", Toast.LENGTH_LONG);
            toast.show();
            //NO BLUETOOTH HARDWARE
        } else {

            if (!mBTA.isEnabled()) {

                Intent EnBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(EnBTIntent);


            } else {
                mBTNonPairedDevicesArray.clear();
                mBTPairedDevicesArray.clear();
                mBTA.disable();
                refreshDisplay();
            }

        }
    }

    public void discoverDevices() {

        if (mBTA.isDiscovering()) {
            Toast toast = Toast.makeText(this, "is Scanning", Toast.LENGTH_LONG);
            toast.show();
        } else {
            mBTNonPairedDevicesArray.clear();

            refreshDisplay();
            int MY_PERMISSION_REQUEST_COARSELOCATION = 1;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_COARSELOCATION);
            loadPreviouslyBondedDevices();
            if (mBTA.startDiscovery()) {
                Toast toast = Toast.makeText(this, "Scanning For near By Devices Started", Toast.LENGTH_LONG);
                toast.show();
            } else {
                Toast toast = Toast.makeText(this, "Scanning Failed", Toast.LENGTH_LONG);
                toast.show();
                mBTNonPairedDevicesArray.clear();
                refreshDisplay();
            }


        }

    }

    public void loadPreviouslyBondedDevices() {
        Set<BluetoothDevice> pairedDevices = mBTA.getBondedDevices();
        // mBTPairedDevicesArray.addAll(mBTA.getBondedDevices());
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if (!mBTPairedDevicesArray.contains(device)) {
                    mBTPairedDevicesArray.add(device);
                    mBTNonPairedDevicesArray.remove(device);
                }

            }
        }
        refreshDisplay();
    }

    void refreshDisplay() {
        ListView mLV = findViewById(R.id.nonPairedList);
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < mBTNonPairedDevicesArray.size(); i++) {
            names.add(mBTNonPairedDevicesArray.get(i).getName());
        }

        ArrayAdapter<String> mArrayAdapterForNonPairedListView = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, names);
        mLV.setAdapter(mArrayAdapterForNonPairedListView);

        ListView PairedDevicesListView = findViewById(R.id.pairedList);
        ArrayList<String> namesofpaired = new ArrayList<>();
        for (int i = 0; i < mBTPairedDevicesArray.size(); i++) {
            namesofpaired.add(mBTPairedDevicesArray.get(i).getName());
        }
        ArrayAdapter<String> mArrayAdapterForPairedListView = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, namesofpaired);
        PairedDevicesListView.setAdapter(mArrayAdapterForPairedListView);
    }


    BroadcastReceiver mReceiverAdapterStateChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("Bluetooth State", "Turning OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("BluetoothState", "Turning ON");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("BluetoothState", "Bluetooth is ON");
                        CheckBox mCH = findViewById(R.id.checkBox);
                        mCH.setChecked(true);
                        loadPreviouslyBondedDevices();
                        refreshDisplay();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("BluetoothState", "Bluetooth is OFF");
                        CheckBox mCH1 = findViewById(R.id.checkBox);
                        mCH1.setChecked(false);
                        break;
                }
            }
        }

    };


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mBTPairedDevicesArray.contains(device)) {
                    mBTNonPairedDevicesArray.add(device);
                }

                refreshDisplay();

            }

        }
    };
    BroadcastReceiver mReceiverBondStateChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d("Bonding", "BluetoothDevice BONDED");
                    mBTPairedDevicesArray.add(mDevice);
                    mBTNonPairedDevicesArray.remove(mDevice);
                    refreshDisplay();
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d("Bonding", "BluetoothDevice BONDING");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d("Bonding", "BluetoothDevice NON BONDED");
                }
            }
        }
    };
}
