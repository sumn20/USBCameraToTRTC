package com.tencent.usbcameratotrtc;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.tencent.usbcameratotrtc.debug.Constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * TRTC 视频互动直播的入口页面
 * <p>
 * - 以主播角色进入视频互动直播房间{@link LiveAnchorActivity}
 * - 以观众角色进入视频互动直播房间{@link LiveAudienceActivity}
 */

/**
 * Entrance View of Interactive Live Video Streaming
 * <p>
 * - Enter a room as an anchor: { LiveAnchorActivity}
 * - Enter a room as audience: {LiveAudienceActivity}
 */
public class LiveEnterActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";

    protected static final int REQ_PERMISSION_CODE = 0x1000;
    // 权限个数计数，获取Android系统权限
    protected int mGrantedCount = 0;
    private EditText mEditInputUserId;
    private EditText mEditInputRoomId;
    private Button mBtnAnchor;
    private Button mBtnAudience;

    private int mRoleSelectFlag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_activity_enter);
        getSupportActionBar().hide();
        initView();
        checkPermission();
    }



    private void initView() {
        mEditInputUserId = findViewById(R.id.et_input_username);
        mEditInputRoomId = findViewById(R.id.et_input_room_id);
        mBtnAnchor = findViewById(R.id.btn_anchor);
        mBtnAudience = findViewById(R.id.btn_audience);
        mBtnAnchor.setOnClickListener(view -> {
            if (mRoleSelectFlag != 1) {
                mRoleSelectFlag = 1;
                mBtnAnchor.setBackgroundColor(getResources().getColor(R.color.live_single_select_button_bg));
                mBtnAudience.setBackgroundColor(getResources().getColor(R.color.live_single_select_button_bg_off));
            }
        });

        mBtnAudience.setOnClickListener(view -> {
            if (mRoleSelectFlag != 2) {
                mRoleSelectFlag = 2;
                mBtnAnchor.setBackgroundColor(getResources().getColor(R.color.live_single_select_button_bg_off));
                mBtnAudience.setBackgroundColor(getResources().getColor(R.color.live_single_select_button_bg));
            }
        });

        findViewById(R.id.bt_enter_room).setOnClickListener(view -> {
            if (checkPermission()) {
                startEnterRoom();
            }

        });
        findViewById(R.id.rtc_entrance_main).setOnClickListener(v -> hideInput());
        findViewById(R.id.entrance_ic_back).setOnClickListener(v -> finish());
        mEditInputRoomId.setText("147369");
        String time = String.valueOf(System.currentTimeMillis());
        String userId = time.substring(time.length() - 8);
        mEditInputUserId.setText(userId);


    }

    private void startEnterRoom() {
        if (TextUtils.isEmpty(mEditInputUserId.getText().toString().trim())
                || TextUtils.isEmpty(mEditInputRoomId.getText().toString().trim())) {
            Toast.makeText(LiveEnterActivity.this, getString(R.string.live_room_input_error_tip), Toast.LENGTH_LONG).show();
            return;
        }
        if (mRoleSelectFlag == 1) {
            Intent intent = new Intent(LiveEnterActivity.this, USBAnchorActivity.class);
            intent.putExtra(Constant.ROOM_ID, mEditInputRoomId.getText().toString().trim());
            intent.putExtra(Constant.USER_ID, mEditInputUserId.getText().toString().trim());
            startActivity(intent);
        } else if (mRoleSelectFlag == 2) {
            Intent intent = new Intent(LiveEnterActivity.this, LiveAudienceActivity.class);
            intent.putExtra(Constant.ROOM_ID, mEditInputRoomId.getText().toString().trim());
            intent.putExtra(Constant.USER_ID, mEditInputUserId.getText().toString().trim());
            startActivity(intent);
        } else {
            Toast.makeText(LiveEnterActivity.this, getString(R.string.live_please_select_role), Toast.LENGTH_SHORT).show();
        }

    }

    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
    //////////////////////////////////    动态权限申请   ////////////////////////////////////////

    protected boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        REQ_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                for (int ret : grantResults) {
                    if (PackageManager.PERMISSION_GRANTED == ret) {
                        mGrantedCount++;
                    }
                }
                if (mGrantedCount == permissions.length) {
                    startEnterRoom();
                } else {
                    Toast.makeText(this, "用户没有允许需要的权限，加入通话失败", Toast.LENGTH_SHORT).show();
                }
                mGrantedCount = 0;
                break;
            default:
                break;
        }
    }
}
