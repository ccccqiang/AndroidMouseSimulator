package com.tencent.blue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tencent.blue.manager.BluetoothConnectionManager;
import com.tencent.blue.manager.BluetoothHidMouse;
import com.tencent.blue.manager.NewBlueConnectManager;
import com.tencent.blue.storage.DeviceStorage;
import com.tencent.blue.storage.HostDevice;
import com.tencent.blue.viewclass.DeviceListAdapter;
import com.tencent.blue.viewclass.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // 创建蓝牙连接管理器
    NewBlueConnectManager connectionManager;

    private UdpServer udpServer;

    // 设备列表适配器
    DeviceListAdapter deviceListAdapter;

    // 添加用于显示视频流的 ImageView
    private ImageView videoStreamView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 初始化蓝牙连接管理器
        connectionManager = new NewBlueConnectManager(this, new DeviceStorage(this));
        // 连接设备
        connectionManager.init();


        // 初始化 ImageView
        videoStreamView = findViewById(R.id.video_stream_view);
        // 初始化RecyclerView和适配器

        // 启动UDP服务器
        // 启动UDP服务器
        udpServer = new UdpServer();
        udpServer.setImageReceiver(imageData -> runOnUiThread(() -> {
            // 显示接收到的图像数据
            if (imageData != null) {
                videoStreamView.setImageBitmap(imageData);
            } else {
                Log.e("MainActivity", "Failed to decode image data.");
            }
        }));
        udpServer.start(connectionManager);


        // 按钮事件处理
        setupButtonListeners();

    }

    private void setupButtonListeners() {
        Button slipLeft = findViewById(R.id.slipLeft);
        slipLeft.setOnClickListener(v -> {
            if (connectionManager.isConnected()) {
                connectionManager.mouse.sendMouse((byte) 100, (byte) 0);
            } else {
                Toast.makeText(this, "设备未连接", Toast.LENGTH_SHORT).show();
            }
        });

        Button simpleChick = findViewById(R.id.simpleChick);
        simpleChick.setOnClickListener(v -> {
            if (connectionManager.isConnected()) {
                connectionManager.mouse.sendMouse((byte) 0, (byte) 100);

            } else {
                Toast.makeText(this, "设备未连接", Toast.LENGTH_SHORT).show();
            }
        });

        Button slipDown = findViewById(R.id.fireController);
        slipDown.setOnClickListener(v -> {
            connectionManager.mouse.canFire = !connectionManager.mouse.canFire;
            if (connectionManager.mouse.canFire) {
                Toast.makeText(this, "开火", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "停火", Toast.LENGTH_SHORT).show();
            }
        });

        Button showPaired = findViewById(R.id.showPaired);
        showPaired.setOnClickListener(v -> {
           connectionManager.showPaired();
        });

        Button activeBlue = findViewById(R.id.activeBlue);
        activeBlue.setOnClickListener(v -> {
            connectionManager.defaultConnect();
        });

        Button passitiveScan = findViewById(R.id.passitiveScan);
        passitiveScan.setOnClickListener(v -> {
            connectionManager.passiveScan();
        });
    }

    private void updateDeviceList() {
        String src = PreferenceManager.getDefaultSharedPreferences(this).getString(DeviceStorage.DEVICES_KEY, "[]");
        List<HostDevice> devices = new Gson().fromJson(src, new TypeToken<ArrayList<HostDevice>>() {
        }.getType());
        deviceListAdapter.updateDevices(devices);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "断开连接", Toast.LENGTH_SHORT).show();
        udpServer.stop(); // 停止UDP服务器
    }

    public void updateBluetoothStatus() {
        runOnUiThread(this::updateDeviceList);
    }
}