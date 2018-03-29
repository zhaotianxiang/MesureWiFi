package com.ustc.ztx.mesurewifi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Vector;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements PermissionInterface{
    private PermissionHelper mPermissionHelper;
    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            //文件读写权限
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.INTERNET,
            //联网和WIFI权限
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
            //位置权限
    };
    private SuperWiFi rss_scan = null;
    Vector<String> RSSList = null;
    private String testlist = null;
    public static int testID = 0;//The ID of the test result

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //init view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //start here firts get the view
        //初始化权限，并且发起权限申请
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
        //
        initViews();//权限申请完毕
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)){
            //权限请求结果，并已经处理了该回调
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public int getPermissionsRequestCode() {
        //设置权限请求requestCode，只有不跟onRequestPermissionsResult方法中的其他请求码冲突即可。
        return 10000;
    }

    @Override
    public String[] getPermissions() {
        //设置该界面所需的全部权限
        return permissions;
    }

    @Override
    public void requestPermissionsSuccess() {
        //权限请求用户已经全部允许
        initViews();
    }

    @Override
    public void requestPermissionsFail() {
        //权限请求不被用户允许。可以提示并退出或者提示权限的用途并重新发起权限申请。
        finish();
    }

    private void initViews(){
        //已经拥有所需权限，可以放心操作任何东西了

        final EditText ipText = (EditText)findViewById(R.id.ipText);//The textlist of the average of the result
        final Button scan_button = (Button)findViewById(R.id.button_scan);//The start button
        final Button clean_button = (Button)findViewById(R.id.button_clean);//Clear the textlist

        rss_scan=new SuperWiFi(this);

        testlist="";
        testID=0;
        //scan listerner
        scan_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                testID=testID+1;
                rss_scan.ScanRss();
                while(rss_scan.isscan()){//Wait for the end
                }
                RSSList=rss_scan.getRSSlist();//Get the test result
                final EditText ipText = (EditText)findViewById(R.id.ipText);
                testlist=testlist+"testID:"+testID+"\n";
                for (String string : RSSList) {
                    testlist += string;
                }
                ipText.setText(testlist);//Display the result in the textlist
            }
        });

        //clearn listerner

        clean_button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                testlist="";
                ipText.setText(testlist);//Clear the textlist
                testID=0;
            }
        });

    }
}
