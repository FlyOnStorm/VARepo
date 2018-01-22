package com.ldm.rtsp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ldm.rtsp.R;
import com.ldm.rtsp.encode.EncodeActivity;
import com.ldm.rtsp.utils.Constant;


public class MainActivity extends Activity {
    private EditText rtsp_edt;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
           ActivityCompat.requestPermissions(MainActivity.this,
                   new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                   MY_PERMISSIONS_REQUEST_CALL_PHONE);
       }



        rtsp_edt = (EditText) findViewById(R.id.rtsp_edt);
        findViewById(R.id.rtsp_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //视频流地址，例如：rtsp://192.168.1.168:80/0
                String url = rtsp_edt.getText().toString().trim();
                if (TextUtils.isEmpty(url) || !url.startsWith("rtsp://")) {
                    Toast.makeText(MainActivity.this, "RTSP视频流地址错误！", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, RtspActivity.class);
                intent.putExtra(Constant.RTSP_URL, url);
                startActivity(intent);
            }
        });
        findViewById(R.id.local_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LocalH264Activity.class));
            }
        });
        findViewById(R.id.encode_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EncodeActivity.class));
            }
        });

        startActivity(new Intent(MainActivity.this, EncodeActivity.class));
    }
}