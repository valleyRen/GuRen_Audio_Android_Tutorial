package com.example.valley_ren_audio;
 
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener; 
import android.os.Message;

import java.util.Timer;  
import java.util.TimerTask;

import com.audio.device.DeviceNative;
import com.rtc.client.*;

public class MainActivity extends Activity implements IRtcSink,OnSeekBarChangeListener {
	private SeekBar m_seekbar;
	final static int ERR_OK = 0;  //no error

	protected IRtcChannel demo_api = null;
	protected IRtcChannel demo_api2 = null;//用户同时进入多个房间


	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

		Log.d("seekbar", "Progress is : " + progress);
		IRtcAudioSystem system_api = (IRtcAudioSystem) demo_api.GetInterface(IRtcAudioSystem.IID);
		system_api.SetPlayoutVolume(progress);

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	protected ArrayList<Map<String, String>> m_listUser = null;
	protected ListView m_vUsers = null;

	private Timer m_timer;

	static final String NAME = "name";
	static final String CONTENT = "content";
	static final String SID = "sid";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.d("onCreate", "Create MainActivity" + String.valueOf(this));

		Intent intent = new Intent();
		intent.setClass(MainActivity.this, LoginActivity.class);
		startActivity(intent);

		ApplicationEx theApp = (ApplicationEx) this.getApplication();
		demo_api = theApp.GetAudioClient();
		theApp.registerActive(this, true);
		demo_api.RegisterRtcSink(this,1);

		//用户同时进入多个房间测试代码，测试时放开
		//demo_api2 = theApp.GetAudioClient2();
		//demo_api2.RegisterRtcSink(this,2);


		m_listUser = new ArrayList<Map<String, String>>();

		SimpleAdapter adapter = new SimpleAdapter(this, m_listUser,
				R.layout.list_item,
				new String[]{NAME, CONTENT},
				new int[]{R.id.text3, R.id.text4});

		m_vUsers = (ListView) findViewById(R.id.listuser);

		m_vUsers.setAdapter(adapter);

		Button btn = null;

		btn = (Button) findViewById(R.id.btn_spk1);
		btn.setOnClickListener(listener);

		btn = (Button) findViewById(R.id.btn_mic1);
		btn.setOnClickListener(listener);

		btn = (Button) findViewById(R.id.btn_switch1);
		btn.setOnClickListener(listener);

		btn = (Button) findViewById(R.id.btn_kickoff);
		btn.setOnClickListener(listener);

		btn = (Button) findViewById(R.id.btn_close1);
		btn.setOnClickListener(listener);

		btn = (Button) findViewById(R.id.btn_with_guan);
		btn.setOnClickListener(listener);

		btn = (Button) findViewById(R.id.btn_with_zhu1);
		btn.setOnClickListener(listener);

		btn = (Button) findViewById(R.id.btn_with_zhu2);
		btn.setOnClickListener(listener);

		btn = (Button) findViewById(R.id.btn_with_echo);
		btn.setOnClickListener(listener);

		btn = (Button) findViewById(R.id.btn_with_backmusic);
		btn.setOnClickListener(listener);

		InitButtonStatus();

		m_timer = new Timer();    //create a timer to monitor the user speaking status
		setTimerTask();           //start timer task


		//add the SeekBar
		TextView tSeekbar = (TextView) findViewById(R.id.textplayvolume);
		tSeekbar.setTextColor(Color.GREEN);

		m_seekbar = (SeekBar) findViewById(R.id.seekBar1);
		m_seekbar.setMax(100);
		m_seekbar.setProgress(50);
		m_seekbar.setOnSeekBarChangeListener(this);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Close();
			return true;
		}
		return false;
	}

	protected void InitButtonStatus() {
		Button btn = null;

		btn = (Button) findViewById(R.id.btn_spk1);
		btn.setText("停止接收语音");
		btn.setTextColor(Color.RED);


		btn = (Button) findViewById(R.id.btn_mic1);
		btn.setText("开始讲话");
		btn.setTextColor(Color.BLUE);

		btn = (Button) findViewById(R.id.btn_with_guan);
		btn.setText("当前为观众模式");
		btn.setTextColor(Color.RED);

		btn = (Button) findViewById(R.id.btn_with_zhu1);
		btn.setText("切换为主播模式");
		btn.setTextColor(Color.BLACK);

		btn = (Button) findViewById(R.id.btn_with_zhu2);
		btn.setText("切换为连麦主播模式");
		btn.setTextColor(Color.BLACK);

		btn = (Button) findViewById(R.id.btn_with_backmusic);
		btn.setText("当前背景音乐关闭");
		btn.setTextColor(Color.BLACK);
	}

	protected void onDestroy() {
		m_timer.cancel();

		super.onDestroy();
	}


	protected String GetUserNameInfo(String strUserID, boolean bSpkStatus) {
		String strSpkStatus = bSpkStatus ? "正在发言..." : "";

		return " " + strUserID + " 当前状态: " + strSpkStatus;
	}

	public void clearUserList()                                                    //Clear Userlist
	{
		m_listUser.clear();
		((SimpleAdapter) m_vUsers.getAdapter()).notifyDataSetChanged();
	}

	public void showUserListInfo()                                                //if login successfully, get the user list and show them
	{
		object_user_sheet usersheet = new object_user_sheet();
		IRtcUsers user_api = (IRtcUsers) demo_api.GetInterface(IRtcUsers.IID);
		if (ERR_OK == user_api.GetUserList(usersheet)) {
			for (int i = 0; i < usersheet.size(); i++) {
				object_user oUser = usersheet.item(i);

				Map<String, String> mp = new HashMap<String, String>();
				mp.put(NAME, GetUserNameInfo(oUser.getUserID(), false));
				mp.put(CONTENT, "");
				mp.put(SID, oUser.getUserID());
				m_listUser.add(mp);
			}

			((SimpleAdapter) m_vUsers.getAdapter()).notifyDataSetChanged();        //send a notifier
		}
	}


	public void adduser(String strUserID) {
		object_user oUser = new object_user();
		IRtcUsers user_api = (IRtcUsers) demo_api.GetInterface(IRtcUsers.IID);
		if (ERR_OK == user_api.GetUser(strUserID, oUser)) {
			Map<String, String> mp = new HashMap<String, String>();
			mp.put(NAME, GetUserNameInfo(oUser.getUserID(), false));
			mp.put(CONTENT, "");
			mp.put(SID, strUserID);
			m_listUser.add(mp);
			((SimpleAdapter) m_vUsers.getAdapter()).notifyDataSetChanged();
		}
	}

	public void removeuser(String strUserID) {
		int sz = m_listUser.size();

		for (int i = 0; i < sz; i++) {
			Map<String, String> mp = m_listUser.get(i);
			String strID = mp.get(SID);
			if (strUserID.equals(strID)) {
				m_listUser.remove(i);
				((SimpleAdapter) m_vUsers.getAdapter()).notifyDataSetChanged();
				break;
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void showStatus(String msg) {
		TextView t = (TextView) findViewById(R.id.status);
		t.setText(msg);
	}

	public void updateUserSpeakingStatus(String strUserID, boolean bIsSpeaking) {
		int size = m_listUser.size();
		for (int i = 0; i < size; i++) {
			Map<String, String> mp = m_listUser.get(i);
			if (strUserID.equals(mp.get(SID))) {
				mp.put(NAME, GetUserNameInfo(strUserID, bIsSpeaking));
				((SimpleAdapter) m_vUsers.getAdapter()).notifyDataSetChanged();
				break;
			}
		}
	}

	public void checkUserSpeakingStatus()
	{
		int size = m_listUser.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				Map<String, String> mp = m_listUser.get(i);
				mp.put(NAME, GetUserNameInfo(mp.get(SID), false));
			}
			((SimpleAdapter) m_vUsers.getAdapter()).notifyDataSetChanged();
		}
	}

	private void setTimerTask() {
		m_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				timerHandler.sendMessage(message);
			}
		}, 1000, 1000);
	}

	private Handler timerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int msgId = msg.what;
			switch (msgId) {
				case 1:
					checkUserSpeakingStatus();
					break;
				default:
					break;
			}
		}
	};

	@SuppressWarnings("deprecation")
	public void updateClickButton(View v) {
		Button btn = (Button) v;
		String strText = btn.getText().toString();

		switch (btn.getId()) {
			case R.id.btn_spk1: {
				IRtcAudio  real_audio_api = (IRtcAudio) demo_api.GetInterface(IRtcAudio.IID);
				IRtcUsers user_api = (IRtcUsers) demo_api.GetInterface(IRtcUsers.IID);
				if (strText.equals("开始接收语音")) {
					real_audio_api.EnablePlayout(true);
					user_api.SetUserAttr(ApplicationEx.user_id,"playout","true");
					btn.setText("停止接收语音");
					btn.setTextColor(Color.RED);
				} else {
					real_audio_api.EnablePlayout(false);
					user_api.SetUserAttr(ApplicationEx.user_id,"playout","false");
					btn.setText("开始接收语音");
					btn.setTextColor(Color.BLUE);
				}
			}
			break;
			case R.id.btn_mic1: {
				IRtcAudio  real_audio_api = (IRtcAudio) demo_api.GetInterface(IRtcAudio.IID);
				IRtcUsers user_api = (IRtcUsers) demo_api.GetInterface(IRtcUsers.IID);
				if (strText.equals("开始讲话")) {

					int RECORD_AUDIO_REQUEST_CODE = 101;
					//if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) !=
					//		PackageManager.PERMISSION_GRANTED) {
					//	ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
					if(0 != real_audio_api.EnableSpeak(true)){
						Toast.makeText(getApplicationContext(),real_audio_api.EnableSpeak(true) + "" ,Toast.LENGTH_SHORT).show();
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
					} else {
						//real_audio_api.EnableSpeak(true);
						user_api.SetUserAttr(ApplicationEx.user_id,"speak","true");
						btn.setText("停止讲话");
						btn.setTextColor(Color.RED);
					}
				} else {

					//停止推流测试代码
					IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
					musiccontroler.StopRtmp();

					real_audio_api.EnableSpeak(false);
					user_api.SetUserAttr(ApplicationEx.user_id,"speak","false");
					btn.setText("开始讲话");
					btn.setTextColor(Color.BLUE);
				}
			}
			break;
			case R.id.btn_switch1: {
				IRtcAudioSystem system_api = (IRtcAudioSystem) demo_api.GetInterface(IRtcAudioSystem.IID);
				if (strText.equals("切到听筒播放")) {
					system_api.SetSpeakerphoneOn(false);

					btn.setText("切到扬声器播放");
				} else {
					system_api.SetSpeakerphoneOn(true);

					btn.setText("切到听筒播放");
				}

			}
			break;
			case R.id.btn_with_guan: {
				IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
				musiccontroler.Enable(IRtcDeviceControler.typeCtrlByHeadset,false);
				musiccontroler.Enable(IRtcDeviceControler.typeAec,true);
				musiccontroler.Enable(IRtcDeviceControler.typeNs,true);
				musiccontroler.Enable(IRtcDeviceControler.typeVad,true);
				musiccontroler.Enable(IRtcDeviceControler.typeAgc,true);
				btn.setText("当前为观众模式");
				btn.setTextColor(Color.RED);

				Button btns = null;
				btn = (Button) findViewById(R.id.btn_with_zhu1);
				btn.setText("切换为主播模式");
				btn.setTextColor(Color.BLACK);
				btn = (Button) findViewById(R.id.btn_with_zhu2);
				btn.setText("切换为连麦主播模式");
				btn.setTextColor(Color.BLACK);
			}
			break;
			case R.id.btn_with_zhu1: {
				IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
				musiccontroler.Enable(IRtcDeviceControler.typeCtrlByHeadset,false);
				musiccontroler.Enable(IRtcDeviceControler.typeAec,false);
				musiccontroler.Enable(IRtcDeviceControler.typeNs,false);
				musiccontroler.Enable(IRtcDeviceControler.typeVad,false);
				musiccontroler.Enable(IRtcDeviceControler.typeAgc,false);
				btn.setText("当前为主播模式");
				btn.setTextColor(Color.RED);

				Button btns = null;
				btn = (Button) findViewById(R.id.btn_with_guan);
				btn.setText("切换为观众模式");
				btn.setTextColor(Color.BLACK);
				btn = (Button) findViewById(R.id.btn_with_zhu2);
				btn.setText("切换为连麦主播模式");
				btn.setTextColor(Color.BLACK);
			}
			break;
			case R.id.btn_with_zhu2: {
				IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
				musiccontroler.Enable(IRtcDeviceControler.typeCtrlByHeadset,true);
				btn.setText("当前为连麦主播模式");
				btn.setTextColor(Color.RED);

				Button btns = null;
				btn = (Button) findViewById(R.id.btn_with_guan);
				btn.setText("切换为观众模式");
				btn.setTextColor(Color.BLACK);
				btn = (Button) findViewById(R.id.btn_with_zhu1);
				btn.setText("切换为主播模式");
				btn.setTextColor(Color.BLACK);
			}
			break;
			case R.id.btn_with_echo: {
				IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
				if (strText.equals("打开耳返监听")) {
					musiccontroler.Enable(IRtcDeviceControler.typeEcho,true);

					btn.setText("关闭耳返监听");
					btn.setTextColor(Color.RED);
				} else {
					musiccontroler.Enable(IRtcDeviceControler.typeEcho,false);
					btn.setText("打开耳返监听");
					btn.setTextColor(Color.BLACK);
				}
			}
			break;
			case R.id.btn_with_backmusic: {
				IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
				File file = Environment.getExternalStorageDirectory();
				if(!file.exists())
					file = Environment.getDataDirectory();
				if(!file.exists())
				{
					file =  this.getApplicationContext().getFilesDir();
				}
				if (strText.equals("当前背景音乐关闭")) {
					musiccontroler.SetBackgroudMusic(0,file.getAbsolutePath() + "/ValleyRtcDemo/test.mp3",true,0.1f,false,true);
					btn.setText("当前背景音乐打开但不传输");
					btn.setTextColor(Color.RED);
				} else if(strText.equals("当前背景音乐打开但不传输")){
					musiccontroler.SetBackgroudMusic(0,file.getAbsolutePath() + "/ValleyRtcDemo/test.mp3",true,0.1f,true,true);
					btn.setText("当前背景音乐打开并且传输");
					btn.setTextColor(Color.RED);
				}else {
					btn.setText("当前背景音乐关闭");
					musiccontroler.SetBackgroudMusic(0,"",false,0.5f,false,false);
					btn.setTextColor(Color.BLACK);
				}
			}
			break;
			case R.id.btn_kickoff: {


			}
			break;
			default:
				break;
		}
	}

	public void Close() {
		//logout
		InitButtonStatus();
		demo_api.Logout();

		//return to the login view
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, LoginActivity.class);
		startActivity(intent);
	}

	OnClickListener listener = new OnClickListener() {
		public void onClick(View v) {
			Button btn = (Button) v;

			switch (btn.getId()) {
				case R.id.btn_spk1:
				case R.id.btn_with_guan:
				case R.id.btn_with_zhu1:
				case R.id.btn_with_zhu2:
				case R.id.btn_with_echo:
				case R.id.btn_mic1:
				case R.id.btn_switch1:
				case R.id.btn_kickoff:
				case R.id.btn_with_backmusic:{
					updateClickButton(btn);
				}
				break;

				case R.id.btn_close1: {
					Close();
				}
				break;
			}
		}
	};

	public void Respond(int type, int ec, Object ob,long userdata) {
		if (userdata == 2){
			return;
		}
		switch (type) {
			case IRtcChannel.RespondLogin: {
				if (ERR_OK == ec)                            //login successfull and jump to mainview
				{
					IRtcAudioSystem system_api = (IRtcAudioSystem) demo_api.GetInterface(IRtcAudioSystem.IID);
					system_api.SetPlayoutVolume(50);

					if(ApplicationEx.room_music){

						//默认为观众模式上麦
						IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
						musiccontroler.Enable(IRtcDeviceControler.typeCtrlByHeadset,false);
						musiccontroler.Enable(IRtcDeviceControler.typeAec,true);
						musiccontroler.Enable(IRtcDeviceControler.typeNs,true);
						musiccontroler.Enable(IRtcDeviceControler.typeVad,true);
						musiccontroler.Enable(IRtcDeviceControler.typeAgc,true);


						Button btn = (Button) findViewById(R.id.btn_with_guan);
						btn.setVisibility(View.VISIBLE);
						btn = (Button) findViewById(R.id.btn_with_zhu1);
						btn.setVisibility(View.VISIBLE);
						btn = (Button) findViewById(R.id.btn_with_zhu2);
						btn.setVisibility(View.VISIBLE);
					}else{
						Button btn = (Button) findViewById(R.id.btn_with_guan);
						btn.setVisibility(View.GONE);
						btn = (Button) findViewById(R.id.btn_with_zhu1);
						btn.setVisibility(View.GONE);
						btn = (Button) findViewById(R.id.btn_with_zhu2);
						btn.setVisibility(View.GONE);
					}

					if(ApplicationEx.room_music_echo){
						Button btn = (Button) findViewById(R.id.btn_with_echo);
						btn.setVisibility(View.VISIBLE);
					}else{
						Button btn = (Button) findViewById(R.id.btn_with_echo);
						btn.setVisibility(View.GONE);
					}

					LoginActivity.m_login.ShowLoginOk();

					clearUserList();

					showStatus("登录成功!");

					showUserListInfo();                      //if user login successfully , get the userlist

					//音乐模式蓝牙耳机预设
					AudioManager  audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
					Log.i("Bluetooth",audioManager.getMode() + "");
					if (audioManager.getMode() == audioManager.MODE_NORMAL && audioManager.isBluetoothScoOn()){
						Log.i("Bluetooth2",audioManager.getMode() + "");
						while(audioManager.isBluetoothScoOn()){
							audioManager.stopBluetoothSco();
						}
						audioManager.setBluetoothA2dpOn(true);
					}


				}
			}
			break;
			case IRtcUsers.RespondKickOff: {

			}
			break;
			case IRtcAudio.RespondDisableUserSpeak: {

			}
			break;
			case IRtcAudio.RespondBlockUser: {

			}
			break;
		}
	}

	public void Notify(int type, Object ob,long userdata) {
		if (userdata == 2){
			return;
		}
		switch (type) {
			case IRtcChannel.NotifyConnectionLost:                 // ob: null
			{
				clearUserList();

				showStatus("网络断开...");
			}
			break;
			case IRtcChannel.NotifyReConnected:                    // ob: null
			{
				showUserListInfo();                                //if user login successfully , get the userlist
				showStatus("恢复连接");
			}
			break;
			case IRtcChannel.NotifyDuplicateLogined:               // ob: null
			{
				LoginActivity.m_login.ShowLoginFailedInfo("重复登录");
				Close();
			}
			break;

			case IRtcUsers.NotifyUserEnterChannel:                // ob: object_user
			{
				object_user oUser = (object_user) ob;
				adduser(oUser.getUserID());
			}
			break;

			//update test for userattr
			case IRtcUsers.NotifyUserAttr:
			{
				object_user_attr oUserAttr = (object_user_attr) ob;
				Toast.makeText(getApplicationContext(), oUserAttr.getUserID() + ":" + oUserAttr.getAttrName() + ":" + oUserAttr.getAttrValue(),Toast.LENGTH_SHORT).show();
			}
			break;

			case IRtcUsers.NotifyUserLeaveChannel:                // ob: object_userid
			{
				object_userid OUser = (object_userid) ob;
				removeuser(OUser.getUserID());
			}
			break;
			case IRtcAudio.NotifyUserSpeaking:                // ob: object_user_speaking
			{
				object_user_speaking OUser = (object_user_speaking) ob;
				updateUserSpeakingStatus(OUser.getUserID(), true);
			}
			break;

			case IRtcChannel.NotifyChannelClose:                    //ob: object_error
			{
				object_error OUser = (object_error)ob;
				String errDesc = ValleyRtcAPI.GetErrDesc(OUser.getErrorCode());
				showStatus(errDesc);
				clearUserList();
			}
			break;
			case IRtcUsers.NotifyKickOff:                    //ob: object_userid
			{
				object_userid OUser = (object_userid)ob;
				String userid = OUser.getUserID();
				if (LoginActivity.m_strLoginUserID.equals(userid)) {
					Close();
				} else {
					removeuser(userid);
					String strInfo = userid + " 被踢出";
					showStatus(strInfo);
				}
			}
			break;
			case IRtcAudio.NotifyDisableUserSpeak:            //ob: object_user_disable_speaking
			{

			}
			break;
		}
	}
}