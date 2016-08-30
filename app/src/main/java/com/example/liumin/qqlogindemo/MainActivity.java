package com.example.liumin.qqlogindemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;


public class MainActivity extends Activity {

    Tencent mTencent;
    private static boolean isServerSideLogin = false;

    private Button login1;
    private Button login2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTencent = Tencent.createInstance("1105639034", this.getApplicationContext());

        initView();
    }


    private void initView(){
        login1=(Button)findViewById(R.id.login1);
        login2=(Button)findViewById(R.id.login2);
        login1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickLogin();
            }
        });

        login2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickServerSideLogin();
            }
        });
    }

    IUiListener loginListener = new BaseUiListener() {
        @Override
        protected void doComplete(JSONObject jsonObject) {
            try {
                Log.e("SDKQQAgentPref", "AuthorSwitch_SDK:" + SystemClock.elapsedRealtime());
                String token = jsonObject.getString(Constants.PARAM_ACCESS_TOKEN);
                String expires = jsonObject.getString(Constants.PARAM_EXPIRES_IN);
                String openId = jsonObject.getString(Constants.PARAM_OPEN_ID);
                Log.e("Result", token);

            }catch(Exception e) {
            }

        }
    };

    private void onClickLogin() {
        if (!mTencent.isSessionValid()) {
            mTencent.login(this, "all", loginListener);
            isServerSideLogin = false;
            Log.e("SDKQQAgentPref", "FirstLaunch_SDK:" + SystemClock.elapsedRealtime());
        } else {
            if (isServerSideLogin) { // Server-Side
                mTencent.logout(this);
                mTencent.login(this, "all", loginListener);
                isServerSideLogin = false;
                Log.e("SDKQQAgentPref", "FirstLaunch_SDK:" + SystemClock.elapsedRealtime());
                return;
            }
            mTencent.logout(this);
            //updateUserInfo();
            //updateLoginButton();
        }
    }

    private void onClickServerSideLogin() {
        if (!mTencent.isSessionValid()) {
            mTencent.loginServerSide(this, "all", loginListener);
            isServerSideLogin = true;
            Log.d("SDKQQAgentPref", "FirstLaunch_SDK:" + SystemClock.elapsedRealtime());
        } else {
            if (!isServerSideLogin) { // SSO
                mTencent.logout(this);
                mTencent.loginServerSide(this, "all", loginListener);
                isServerSideLogin = true;
                Log.d("SDKQQAgentPref", "FirstLaunch_SDK:" + SystemClock.elapsedRealtime());
                return;
            }
            mTencent.logout(this);
            isServerSideLogin = false;
            //updateUserInfo();
           // updateLoginButton();
        }
    }

    private class BaseUiListener implements IUiListener {

        @Override
        public void onComplete(Object response) {
            if (null == response) {
                Util.showResultDialog(MainActivity.this, "����Ϊ��", "��¼ʧ��");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                Util.showResultDialog(MainActivity.this, "����Ϊ��", "��¼ʧ��");
                return;
            }
            Util.showResultDialog(MainActivity.this, response.toString(), "��¼�ɹ�");
            //
            doComplete((JSONObject)response);
        }

        protected void doComplete(JSONObject values) {

        }

        @Override
        public void onError(UiError e) {
            Util.toastMessage(MainActivity.this, "onError: " + e.errorDetail);
            Util.dismissDialog();
        }

        @Override
        public void onCancel() {
            Util.toastMessage(MainActivity.this, "onCancel: ");
            Util.dismissDialog();
            if (isServerSideLogin) {
                isServerSideLogin = false;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onActivityResult", "-->onActivityResult " + requestCode + " resultCode=" + resultCode);
        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode,resultCode,data,loginListener);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
