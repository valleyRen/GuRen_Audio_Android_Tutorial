package com.example.valley_ren_audio;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.*;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.rtc.client.*;

public class LoginActivity extends Activity 
{
	public static LoginActivity m_login = null;
	public static String m_strLoginUserID = "";
	public static String m_strRoomID = "25";
	private long exitTime = 0;
	
	public static boolean m_bFirstLogin = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{ 
		Log.d("ssssssssssssssss", "current version : " + Build.VERSION.SDK_INT + " acv: " +  Build.VERSION_CODES.JELLY_BEAN);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		ApplicationEx theApp = (ApplicationEx)this.getApplication();
        
        theApp.registerActive(this, false);
        
        Button btn = (Button)findViewById(R.id.btn_login); 
        
        btn.setOnClickListener(listener); 
        
        btn = (Button)findViewById(R.id.btn_cancel);
         
        btn.setOnClickListener(listener);
        
        m_login = this;
    	
    	if(m_strLoginUserID.isEmpty())
    	{
    		int nTestUserID = (int)(Math.random() * 10000000);
        	
        	if(!m_bFirstLogin)
        	{
        		m_strLoginUserID = String.valueOf(nTestUserID);    		
        	}    		
    	}

    	TextView title = (TextView)findViewById(R.id.title);
    	title.setText("valley.ren - DEMO " + ValleyRtcAPI.GetSDKVersion());
    	
        EditText e  = (EditText)findViewById(R.id.edituserid);
       	e.setText(m_strLoginUserID);
       	
       	e = (EditText)findViewById(R.id.editroomid); 
       	e.setText(m_strRoomID);
    	
    	    
    	//如果是第一次登录，则直接自动登录，
    	if(!m_bFirstLogin)
    	{
    		m_bFirstLogin = true;
    		//UserLogin();			//如果在批量测试时，需要打开应用就自动登录，则运行这行代码
    	}
    	
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	   	//捕获android 返回键盘，如果在
	    if(keyCode==KeyEvent.KEYCODE_BACK /*&& event.getRepeatCount() == 0*/)
	    {
	    	exit();
	   		return false;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	 
	public void exit() 
	{
		 if ((System.currentTimeMillis() - exitTime) > 2000) 
	     {
			 Toast.makeText(getApplicationContext(), "再按一次退出程序",Toast.LENGTH_SHORT).show();
	         exitTime = System.currentTimeMillis();
	     } 
	     else 
	     {
	    	 Close();		    		         
	     }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public void ShowLoginOk()
	{
		Button btnLogin = (Button)findViewById(R.id.btn_login);
		btnLogin.setEnabled(true);
		
		LoginActivity.this.finish();
	}
	public void ShowLoginFailedInfo(String strErrMsg)
	{
		TextView txt = (TextView)findViewById(R.id.log_view); 
		txt.setTextColor(Color.RED);
		txt.setText(strErrMsg);
		
		Button btnLogin = (Button)findViewById(R.id.btn_login);
		btnLogin.setEnabled(true);
	}

	public static final int READ_CONTACTS_REQUEST_CODE = 101;

	public void test(){
		while (true){
			Integer ccc = 0;
			long time=System.currentTimeMillis();
			long now = System.currentTimeMillis();
			while (now - time < 3000){
				ccc += 1;
				try{
					Thread.sleep(10);
					now = System.currentTimeMillis();
				}catch (InterruptedException f)
				{
					f.printStackTrace();
				}
			}
		}
	}

	public void UserLogin()
	{
		TextView txt = (TextView)findViewById(R.id.log_view);
		txt.setTextColor(Color.RED);
		txt.setText("登录中...");

    	EditText e  = (EditText)findViewById(R.id.edituserid);
        String text = e.getText().toString();
        m_strLoginUserID = text;
        if(text.isEmpty())
        {
	       	e.setFocusable(true);
	       	e.setFocusableInTouchMode(true);
	       	e.requestFocus();
	       	e.requestFocusFromTouch();
			Toast.makeText(getApplicationContext(), "请输入用户ID",
					Toast.LENGTH_SHORT).show();
	       	return;
        }
        
        m_strLoginUserID = text.trim();

        e = (EditText)findViewById(R.id.editroomid); 
        text = e.getText().toString();
        m_strRoomID = text;
         
        if(text.isEmpty())
        {
	        m_strRoomID =  "1";
	        e.setFocusable(true);
	        e.setFocusableInTouchMode(true);
	        e.requestFocus();
	        e.requestFocusFromTouch();
			Toast.makeText(getApplicationContext(), "请输入房间ID",
					Toast.LENGTH_SHORT).show();
	        return;
        }else if(Integer.valueOf(text) >= 50){
			e.setFocusable(true);
			e.setFocusableInTouchMode(true);
			e.requestFocus();
			e.requestFocusFromTouch();
			Toast.makeText(getApplicationContext(), "测试房间ID应小于50",
					Toast.LENGTH_SHORT).show();
			return;
		}
         
   
       	Button btnLogin = (Button)findViewById(R.id.btn_login);
    	btnLogin.setEnabled(false);

		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_SETTINGS}, 1);



		ApplicationEx theApp = (ApplicationEx)this.getApplication();
		IRtcChannel demo_api  = theApp.GetAudioClient();

		ApplicationEx.user_id = m_strLoginUserID;

		//ValleyRtcAPI.SetAuthoKey("5a00e500d503f7f6ollnTdu");//SetAuthoKey为注册函数，填入从谷人申请的id，留空为测试
		ValleyRtcAPI.SetAuthoKey("");//SetAuthoKey为注册函数，填入从谷人申请的id，留空为测试
		if(m_strRoomID.equals("7") || m_strRoomID.equals("8") || m_strRoomID.equals("9"))
		{
			demo_api.EnableInterface(IRtcUsers.IID|IRtcAudio.IID|IRtcAudioSystem.IID|IRtcDeviceControler.IID);
			IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
			musiccontroler.Enable(IRtcDeviceControler.typeBackgroundMusic,true);
			musiccontroler.Enable(IRtcDeviceControler.typeMusicMode,true);
			ApplicationEx.room_music = true;
			ApplicationEx.room_music_echo = false;

			//推流测试代码
			//网宿
			//musiccontroler.StartRtmp("rtmp://push.wangsu.valley.ren/live/valley",1,false);
			//金山
			//musiccontroler.StartRtmp("rtmp://push.ksyun.valley.ren/live/valley",1,false);
			//白山
			//musiccontroler.StartRtmp("rtmp://push.paoba1.qingcdn.com/live/valley",1,false);
		}
		else if(m_strRoomID.equals("1")){
			demo_api.EnableInterface(IRtcUsers.IID|IRtcAudio.IID|IRtcAudioSystem.IID|IRtcDeviceControler.IID);
			IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
			musiccontroler.Enable(IRtcDeviceControler.typeBackgroundMusic,true);
			musiccontroler.Enable(IRtcDeviceControler.typeMusicMode,true);

			ApplicationEx.room_music = true;
			ApplicationEx.room_music_echo = true;
		}
		else
		{
			demo_api.EnableInterface(IRtcUsers.IID|IRtcAudio.IID|IRtcAudioSystem.IID|IRtcDeviceControler.IID);
			IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
			musiccontroler.Enable(IRtcDeviceControler.typeMusicMode,false);
			musiccontroler.Enable(IRtcDeviceControler.typeBackgroundMusic,true);
			//demo_api.DisableInterface(IRtcDeviceControler.IID);
			ApplicationEx.room_music = false;
			ApplicationEx.room_music_echo = false;

		}

		IRtcAudio real_audio_module = (IRtcAudio)demo_api.GetInterface(IRtcAudio.IID);

		real_audio_module.EnablePlayout(true);
		real_audio_module.EnableSpeak(false);
    	demo_api.Login(m_strRoomID, m_strLoginUserID, "");	//test room , roomkey is ""



		//用户同时进入多个房间测试代码，需要测试时放开
		//IRtcChannel demo_api2  = theApp.GetAudioClient2();////
		//demo_api2.EnableInterface(IRtcUsers.IID|IRtcAudio.IID|IRtcAudioSystem.IID|IRtcDeviceControler.IID);
		//IRtcDeviceControler musiccontroler2 = (IRtcDeviceControler)demo_api2.GetInterface(IRtcDeviceControler.IID);
		//musiccontroler2.Enable(IRtcDeviceControler.typeMusicMode,false);
		//musiccontroler2.Enable(IRtcDeviceControler.typeBackgroundMusic,true);
		//IRtcAudio real_audio_module2 = (IRtcAudio)demo_api2.GetInterface(IRtcAudio.IID);
		//real_audio_module2.EnablePlayout(true);
		//real_audio_module2.EnableSpeak(true);
		//demo_api2.Login("44", m_strLoginUserID, "");	//test room , roomkey is ""



	    theApp.setAudioEngChkState(1, false);
	    theApp.setAudioEngChkState(2, false);
	        
	    theApp.setAudioEngChkState(11, false);
	    theApp.setAudioEngChkState(12, false);
	    theApp.setAudioEngChkState(13, false);
	    theApp.setAudioEngChkState(14, false);
	    theApp.setAudioEngChkState(15, false);
	    theApp.setAudioEngChkState(16, false);
	    theApp.setAudioEngChkState(17, false);
	    theApp.setAudioEngChkState(18, false);
	    theApp.setAudioEngChkState(19, false);
	    theApp.setAudioEngChkState(20, false);
        		       
	}
	
	public void BtnClickHanlder(View v)
	{						
	    Button btn = (Button)v; 
	    switch(btn.getId()) 
	    { 
		    case R.id.btn_login: 
		    {
		    	UserLogin();
		    }
		    break;
		    case R.id.btn_cancel: 
		    {			    
		    	Close();
		    } 
		    break;  
	    }
	}
	
	public void Close()
	{
		ApplicationEx theApp = (ApplicationEx)this.getApplication();
		IRtcChannel demo  = theApp.GetAudioClient();
		
		demo.Logout();
    	theApp.exit(); 
	}
	
	OnClickListener listener = new OnClickListener() 
	{ 
	    public void onClick(View v) 
	    {	    	
	    	BtnClickHanlder(v);
	    }
	};	
}

