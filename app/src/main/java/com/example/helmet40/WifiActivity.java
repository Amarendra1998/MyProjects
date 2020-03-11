package com.example.helmet40;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WifiActivity<connectionInfoListener> extends AppCompatActivity {
    Button btnoff,btnDiscover,btnSend;
ListView listView;
TextView read_msg_box;
    public TextView ConnectionStats;
EditText writeMsg;
WifiManager wifiManager;
WifiP2pManager mManager;
WifiP2pManager.Channel mChannel;
BroadcastReceiver mReceiver;
IntentFilter intentFilter;
List<WifiP2pDevice>peers = new ArrayList<WifiP2pDevice>();
String[]deviceNameArray;
WifiP2pDevice[] deviceArray;
static final int MESSAGE_READ = 1;
ServerClass serverClass;
ClientClass clientClass;
SendReceive sendReceive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        initialWork();
        exqListener();
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readbuff = (byte[])msg.obj;
                    String tmpmsg = new String(readbuff,0,msg.arg1);
                    read_msg_box.setText(tmpmsg);
                    break;
            }
            return true;
        }
    });
    private void exqListener() {
        btnoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(false);
                    btnoff.setText("ON");
                }else {
                    wifiManager.setWifiEnabled(true);
                    btnoff.setText("OFF");
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        ConnectionStats.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int reason) {
                      ConnectionStats.setText("Discovery Starting failed");
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "Connected to"+device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(), "Not Connected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = writeMsg.getText().toString();
                sendReceive.write(msg.getBytes());
            }
        });
    }

    private void initialWork() {
        btnoff = (Button)findViewById(R.id.button2);
        btnDiscover = (Button)findViewById(R.id.button3);
        btnSend = (Button)findViewById(R.id.button4);
        listView = (ListView)findViewById(R.id.list);
        read_msg_box =  (TextView)findViewById(R.id.textView7);
        ConnectionStats = (TextView)findViewById(R.id.textView6);
        writeMsg = (EditText)findViewById(R.id.editText);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;

                for (WifiP2pDevice device: peerList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                ArrayAdapter<String>adapter  = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_expandable_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
            }
            if (peers.size()==0){
                Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
            if (info.groupFormed && info.isGroupOwner){
                ConnectionStats.setText("Host");
                serverClass = new ServerClass();
                serverClass.start();
            }else if (info.groupFormed){
                   ConnectionStats.setText("Client");
                   clientClass = new ClientClass(groupOwnerAddress);
                   clientClass.start();
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            //super.run();
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt){
            socket = skt;

            try {
                inputStream = socket.getInputStream();
                outputStream  = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
           // super.run();
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket!=null)
            {
                try {
                    bytes = inputStream.read(buffer);
                if (bytes>0)
                {
                    handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class ClientClass extends Thread{
        Socket socket;
        String hostAdd;

        public ClientClass(InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            //super.run();
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
