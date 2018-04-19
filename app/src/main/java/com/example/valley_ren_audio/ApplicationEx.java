package com.example.valley_ren_audio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.rtc.client.*;


public class ApplicationEx extends Application
{	
	protected IRtcChannel  m_pClient  = null;
	protected IRtcChannel  m_pClient2  = null;


	protected Activity  	m_activemain  = null;
	protected Activity  	m_activelogin = null;

	protected Watcher   	m_watcher     = null;
	boolean             	m_bInit       = false;

	public static Boolean room_music = false;
	public static Boolean room_music_echo = false;
	public static String  user_id = "";


	//save debug audio save / play flag
	protected boolean [] m_arry_eng_save  = new boolean[]{false,false};
	protected boolean [] m_arry_eng_play  = new boolean[]{false,false,false,false,false,false,false,false,false,false,};
	
	public void setAudioEngChkState(int index,boolean bState)
	{
		if(index>=1 && index <=2)
			m_arry_eng_save[index-1]=bState;
		else if(index>=11)
			m_arry_eng_play[index-11]=bState;
	}
 
	public void chmod777(File file, String root) 
	{
		try 
		{
			if (null == file || !file.exists()) 
			{				
				return;
			}

			Runtime.getRuntime().exec("chmod 777 " + file.getAbsolutePath());
			File tempFile = file.getParentFile();
			String tempName = tempFile.getName();
			if (tempFile.getName() == null || "".equals(tempName)) 
			{
				return;							
			}
			else if (!root.equals("") && root.equals(tempName)) 
			{
				Runtime.getRuntime().exec("chmod 777 " + tempFile.getAbsolutePath());
				return;
			}
			
			chmod777(file.getParentFile(), root);
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public void mkdirs(String path) 
	{
		try 
		{
			File file = new File(path);
			if (!file.isDirectory()) 
			{
				if (!file.mkdirs()) 
				{
					Log.d("ApplicationEx","mkdir failed");
				}				
			    chmod777(new File(path), null);	
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public IRtcChannel GetAudioClient()
	{		 
		if(!m_bInit)
		{
			m_bInit      = true;
			String mDataPath = "";
			try
			{
				File file = Environment.getExternalStorageDirectory();

				if(!file.exists())
					file = Environment.getDataDirectory();

				if(!file.exists())
				{
					file =  this.getApplicationContext().getFilesDir();
				}

				mDataPath = file.getAbsolutePath() + "/ValleyRtcDemo";
				mkdirs(mDataPath);

				CopyAssetsToFiles(getApplicationContext(), mDataPath, "test.mp3" , false);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			ValleyRtcAPI.InitSDK(this, mDataPath);//mDataPath为谷人云相关目录，可以自定义
			m_pClient = new IRtcChannel();
			m_watcher = new Watcher();
			m_watcher.start();
			
		}
		
		return m_pClient;
	}


	//用户同时进入多个房间
	public IRtcChannel GetAudioClient2()
	{
		if(m_pClient2 == null)
			m_pClient2 = new IRtcChannel();
		return m_pClient2;
	}


	public int CopyAssetsToFiles(Context ct, String destPath, String fileName,boolean cover)
	{
		int ret=0;
		InputStream in=null;
		OutputStream out=null;
		File outFile=null;
		byte[] buf=null;
		int len=0;
		String fileSrcPath = "file:///android_asset/" + fileName;
		String fileDestPath = destPath + "/" + fileName;

		try
		{
			in = ct.getAssets().open(fileName);
			outFile = new File(destPath, fileName);
			if(outFile.exists() && !cover)
			{
				in.close();
				return 0;
			}
			out = new FileOutputStream(outFile);
			buf = new byte[1024];

			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
				ret += len;
			}

			in.close();
			out.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return ret;
	}

	public void registerActive(Activity a, boolean bMain)
	{ 
		if(bMain)
			m_activemain = a;
		else
			m_activelogin = a;
	}
	
	public void exit()
	{  
		m_watcher.Stop();
		
		Log.d("Application",  "exit"); 

		if(null != m_activemain)
			m_activemain.finish();
		
		if(null != m_activelogin)
			m_activelogin.finish(); 
		
		m_pClient = null;
		ValleyRtcAPI.CleanSDK();
		System.exit(0);
	}

	private class Watcher extends Thread
	{ 	

		String  command = "top -n 1 -m 3";	
		public  boolean runing  = true;
		public  long    active_tick = SystemClock.uptimeMillis() - 5000;
		
		public void run()
		{  			
			Log.d("Application", "watch thread run");
			Process process = null;  
			InputStream instream = null;  
			BufferedReader bufferReader = null;  
			int time_out = 10*1000;
		   
			try 
			{   
				while(runing)
				{   
					if(SystemClock.uptimeMillis() - active_tick < 5000 && time_out < 500)
					{ 
						String result = "";
						ProcessBuilder builder = new ProcessBuilder(command);  
					    process = Runtime.getRuntime().exec(command);   
					    instream = process.getInputStream();  
					    bufferReader = new BufferedReader(new InputStreamReader(instream, "utf-8"));  
					    Log.d("Application", "read cpu");
					    String readline;  
					    while (runing && null != (readline = bufferReader.readLine())) 
					    {  
					    	if(readline.contains("com.bodtech."))
					    	{
						    	result += readline+"\n";   
					    	}
					    }   
					    
					    process.destroy(); 
					    time_out   =  10*1000;  
					    
					    if(null != m_pClient)
					    { 
					    	;
					    }
						Log.d("Application", result);
					}
 
					if(runing)
					{
						Thread.sleep(500); 
						if(time_out > 0)
							time_out -= 500;
					}
				}
				
			} 
			catch (IOException e) 
			{  
				e.printStackTrace();
			} 
			catch (InterruptedException e) 
			{  
				e.printStackTrace();
			}  	
			
			Log.d("Application", "watch thread end");
		}
		
		
		public void Stop()
		{
			runing = false;
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{ 
				e.printStackTrace();
			} 
		}
	}
}
