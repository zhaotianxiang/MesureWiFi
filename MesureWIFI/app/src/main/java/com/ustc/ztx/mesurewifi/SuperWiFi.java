package com.ustc.ztx.mesurewifi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import static android.provider.CalendarContract.Instances.BEGIN;
import static com.ustc.ztx.mesurewifi.PermisionUtils.verifyStoragePermissions;

public class SuperWiFi extends MainActivity {
    private static final int MY_PERMISSION_REQUEST_CODE = 10000;
    private Map<String,Integer> WIFIMap = new TreeMap<String, Integer>();
    static final String TAG = "SuperWiFi";//调试的标志
    static SuperWiFi wifi = null;//wifi
    static Object sync = new Object();
    static int TESTTIME = 25;//Number of measurement
    WifiManager wm = null;
    private Vector<String> scanned = null;//被扫描过的容器
    boolean isScanning = false;
    private int[] APRSS=new int[10];
    private FileOutputStream out;//输出文件
    private int p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public SuperWiFi(Context context)
    {
        this.wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        this.scanned = new Vector<String>();
    }

    public void ScanRss() {
        startScan();
    }

    public boolean isscan() {
        return isScanning;
    }

    public Vector<String> getRSSlist() {
        return scanned;
    }

    private void startScan() {
        // start the wifi scan thread
        this.isScanning = true;
        //new thread
        Thread scanThread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                scanned.clear();
                for(int j=1;j<=10;j++){
                    APRSS[j-1]=0;
                }
                p=1;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis());
                //Get the current time
                String str = formatter.format(curDate);
                for(int k=1;k<=10;k++){
                    Log.d(TAG,"testID:"+testID+" TestTime: "+str+"BEGIN ");
                }
                while(p<=TESTTIME)//Scan for a certain times
                {//扫描
                    performScan();
                    p=p+1;
                }
                List<Map.Entry<String, Integer>> entryArrayList = new ArrayList<>(WIFIMap.entrySet());
                for(Map.Entry<String, Integer> entry : entryArrayList){
                    scanned.add("WIFI名称："+entry.getKey()+"  WIFI强度："+entry.getValue()+"\n");
                }
                for(int k=1;k<=10;k++){//Mark the end of the test in the file
                    Log.d(TAG,"testID:"+testID+"END\n");
                }
                isScanning=false;
            }
        });
        scanThread.start();
    }
    //执行performScan()之前一定要动态获取权限
    private void performScan() {
        if(wm == null)
            return;
        try{
            //测试能否执行
            if(!wm.isWifiEnabled()){
                wm.setWifiEnabled(true);
            }
            wm.startScan();
            try {
                Thread.sleep(1000);//Wait for 3000ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.scanned.clear();
            //meiyoudongzi

            List<ScanResult> sr = wm.getScanResults();
            Iterator<ScanResult> it = sr.iterator();
            while(it.hasNext())
            {
                ScanResult ap = it.next();
                String WIFIName = ap.SSID;
                int WIFIStrength = 0;
                //名称和强度
                String Data = null;
                for(int k=1;k<=10;k++){
                    WIFIStrength+=ap.level;
                    APRSS[k-1]=APRSS[k-1]+ap.level;
                    Data+="第"+k+"条记录"+WIFIName+" "+ap.level+"\n";
                }
                Log.d(TAG,Data);
                WIFIStrength = WIFIStrength / 10;
                WIFIMap.put(WIFIName,WIFIStrength);
            }

        }catch(Exception e){
            this.isScanning = false;
            this.scanned.clear();
            Log.d(TAG,e.toString());
        }
    }

    private void write2file(String filename, String a) {
        try {
            //verifyStoragePermissions(this);
            File file = new File("/sdcard/"+filename);
            if (!file.exists()){
                file.createNewFile();}
// Open a random filestream by Read&Write
            RandomAccessFile randomFile = new
                    RandomAccessFile("/sdcard/"+filename, "rw");
// The length of the file(byte)
            long fileLength = randomFile.length();
// Put the writebyte to the end of the file
            randomFile.seek(fileLength);
            randomFile.writeBytes(a);
//Log.e("!","!!");
            randomFile.close();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

