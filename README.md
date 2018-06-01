
### 
- real_audio_client 为demo源码
- SDK目录为sdk包

官方文档地址：https://doc.valley.ren/

### DEMO运行指引
    
**1.环境准备：** 
- 1.从官网下载页面获取Android DEMO 源文件包。
- 2.Android Studio 2.1 或以上版本。
- 3.已经下载好 Android SDK 25、Android SDK Build-Tools 25.0.3、Android SDK Platform-Tools 25.\*.\*。
- Android 版本不低于 4.0.3 ，如果用真机调试请开启“允许调试”选项。
- Android 设备已经连接到 Internet。

**2.运行项目：** 

------------
###### 第一次编译 DEMO 项目时，需要下载特定的 gradle套件 及 DEMO 所需要的依赖库，所以请确保开发电脑已经连接到 internet 且能正常访问 https://services.gradle.org

------------
- 1.打开Android Studio，以gradle方式导入项目。
![](https://doc.valley.ren//images/2017-11-03/59fbeaac0673b.png)
- 2.修改项目根目录下面local.properties，指定Android SDK目录到正确的本机路径。
![](https://doc.valley.ren//images/2017-11-03/59fbea34e1bf9.png)
- 3.新建一个configurations。
![](https://doc.valley.ren//images/2017-11-03/59fbeb5a5ff58.png)
参数保持默认即可：
![](https://doc.valley.ren//images/2017-11-03/59fbeb9e72c80.png)
- 4.编译运行，Run-Run app。

**3.部分说明：**

- 1.DEMO主要由LoginActivity及MainActivity组成，语音组件初始化，关闭等部分函数在ApplicationEx实现。
- 2.DEMO运行在测试鉴权ID下面，频道号只能输入1-50数字，正式注册申请ID后则无此限制。
- 3.谷人云实时语音部分大体分为组件初始化，用户麦克风扬声器等设置，用户登陆，Response回调，Notify回调等几个步骤，方便简单，利于接入。
- 4.测试鉴权ID长期开放，欢迎修改测试demo实现一些有趣的小功能并投稿。

### SDK获取及集成    
**1.SDK概述：** 
- 谷人云Android SDK由两个文件组成，其中两个so库文件（libValleyRtcSDK.so），一个jar libs（ValleyRtcSDK.jar）。

**2.下载SDK：** 
- 请在官网下载部分获取Android相关SDK文件。
  
**3.拷贝SDK：**
- 1.将ValleyRtcSDK.jar 拷贝住程序目录下libs目录中，如DEMO源码中则为app目录下libs目录。
- 2.将armeabi-v7a目录（包含libValleyRtcSDK.so）拷贝至程序源码目录jniLibs下，如DEMO源码中则为app/src/main/jniLibs-armeabi-v7a目录。
![](/images/2017-11-03/59fc01d42f9d2.png)


**4.引入SDK：**
- 编辑app目录下build.gradle 文件，在dependencies块中添加如下：
```java
compile files('libs/ValleyRtcSDK.jar')
```
![](/images/2017-11-03/59fc02af8fd7e.png)

**5.添加权限申明：**
- 打开 app/AndroidManifest.xml 文件，添加如下内容：
```java
 <uses-permission android:name="android.permission.INTERNET" />
 <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
 <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 <uses-permission android:name="android.permission.WRITE_SETTINGS" />
 <uses-permission android:name="android.permission.RECORD_AUDIO" />
 <uses-permission android:name="android.permission.SET_DEBUG_APP" />
 <uses-permission android:name="android.permission.READ_PHONE_STATE" />
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
 <uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
 <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
 <uses-permission android:name="android.permission.BLUETOOTH" />
 <uses-permission android:name="android.permission.WAKE_LOCK" />
```
**6.开始使用：**
- 参见功能实现流程以及接口列表。

### 功能实现文档    
**多人语音实现流程简述：**
1. SDK初始化
2. 初始化频道类型（语音、音乐）
3. 设置用户登陆参数及登陆
4. 打开&关闭播放，打开&关闭麦克风
5. 监听Response回调
6. 监听Notify回调
7. 退出频道
8. 销毁对象
9. 刷新用户列表代码示例

**1.SDK初始化：** 
1. InitSDK：初始化SDK环境，设置SDK工作目录，谷人云SDK将会把临时日志，配置文件存放在配置的工作目录中。
2. 具体代码参考DEMO ApplicationEx类中GetAudioClient函数，部分关键代码如下：
```java
protected IRtcChannel  m_pClient  = null;
protected Watcher m_watcher = null;
boolean m_bInit = false;
public IRtcChannel GetAudioClient()
	{		 
		if(!m_bInit)
		{
			m_bInit = true;
			String mDataPath = "";
			try
			{
				File file = Environment.getExternalStorageDirectory();
				mDataPath = file.getAbsolutePath() + "/real_audio_client";
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
```
> 谷人云AndroidSDK对开发者共提供六类接口，分别由六个对应的对象来控制：
ValleyRtcAPI类中包含SDK初始化，退出等接口；
IRtcChannel类中包含登录频道，登出频道的操作，并包含有初始化用户想用的接口功能函数EnableInterface；
IRtcUsers类中包含和用户相关的接口函数，包含取用户列表，踢人，设置用户标识等函数；
IRtcAudio类中包含实时语音相关所有接口，包含打开麦克风，打开扬声器等函数；
IRtcAudioSystem类中包含操作系统声音控制的系列接口；
IRtcMusicControler类中包含音乐模式下主播，观众，耳返等系列接口；
  
  
**2.初始化频道类型（语音、音乐）：**
1. 通过SetAuthoKey函数指定appkey
2. 进行频道初始化，谷人云实时语音SDK提供两种频道类型供用户使用，在初始化频道的时候需要进行设置，谷人云采用全接口化的方式，使用特定的频道类型只需要激活相应的接口列表即可，具体可以参考demo LoginActivity代码实现如下：
```java
ApplicationEx theApp = (ApplicationEx)this.getApplication();
IRtcChannel demo_api  = theApp.GetAudioClient();
ValleyRtcAPI.SetAuthoKey("");//SetAuthoKey为注册函数，填入从谷人申请的id，留空为测试
//音乐频道类型按照如下方式激活接口
demo_api.EnableInterface(IRtcUsers.IID|IRtcAudio.IID|IRtcAudioSystem.IID|IRtcMusicControler.IID);
//语音频道类型按照如下方式激活接口
demo_api.EnableInterface(IRtcUsers.IID|IRtcAudio.IID|IRtcAudioSystem.IID);
```
3. 音乐模式以及语音模式在接口获得上仅多获得了IRtcMusicControler类接口。
4. ValleyRtcAPI.SetAuthoKey可以在SDK初始化时调用，也可以在进行登录频道时调用，强烈建议KEY值应该由服务端传给客户端，不应该写死于客户端中。

**3.设置用户登陆参数及登陆：**

- 1.EnablePlayout：获取IRtcAudio实例，并调用IRtcChannel的GetInterface后，可以调用此函数设置用户默认进入频道以后是否开启播放声音。
- 2.EnableSpeak：获取IRtcAudio实例，并调用IRtcChannel的GetInterface后，可以调用此函数设置用户默认进入频道以后是否开启麦克风声音采集。
- 3.Login：传入频道号，用户ID，用户扩展信息进行登陆。
- 4.ValleyRtcAPI.SetAuthoKey：设置开发者ID，用于接入谷人云正式环境，留空为测试环境。
- 4.具体代码参考DEMO LoginActivity中UserLogin函数实现，部分关键代码如下：

```java
public static String m_strLoginUserID = "test";//用户名
public static String m_strRoomID = "25";//频道号

ApplicationEx theApp = (ApplicationEx)this.getApplication();
IRtcChannel demo_api  = theApp.GetAudioClient();
IRtcAudio demo_real_audio_api = null;
//IRtcAudio 包含实时语音相关，开启通话，禁言，设置音量等接口
demo_real_audio_api = (IRtcAudio)demo_api.GetInterface(IRtcAudio.IID);
demo_real_audio_api.EnablePlayout(true);//打开声音播放
demo_real_audio_api.EnableSpeak(false);//关闭麦克风采集
demo_api.Login(m_strRoomID, m_strLoginUserID, "");//登陆频道
```
> IRtcChannel的GetInterface应该在EnableInterface调用过后进行调用，表示将初始化哪些接口功能。


**4.打开&关闭播放，打开&关闭麦克风：**
- 1.此两个功能为IRtcAudio中接口，在获取实例后可以在任何时候进行调用。
- 2.示例代码如下：

```java
demo_real_audio_api.EnablePlayout(true);
//打开声音接收，播放频道内的语音。
demo_real_audio_api.EnablePlayout(false);
//关闭声音接收，不播放频道内的语音。
demo_real_audio_api.EnableSpeak(true);
//打开麦克风，用户可以发言。
demo_real_audio_api.EnableSpeak(false);
//关闭麦克风，用户不可以发言。

```

**5.监听Respond回调：**
- 1.Respond回调为SDK操作状态码返回，用于接收关键调用的调用情况。
- 2.Respond类别type：
    1. ValleyRtcAPI.RespondLogin：用户登陆状况
    2. IRtcUsers.RespondKickOff：用户踢人状况
    3. IRtcAudio.RespondDisableUserSpeaking：用户禁音状况
    4. IRtcAudio.RespondBlockUser：拒听某人的讲话情况
- 3.Respond返回值ec详见接口列表。
- 4.代码参考DEMO MainActivity 中Respond函数，部分代码如下：

```java
protected IRtcChannel demo_api  = null;
ApplicationEx theApp = (ApplicationEx)this.getApplication();
demo_api  = theApp.GetAudioClient();
demo_api.RegisterRtcSink(this);//设置监听回调

public void Respond(int type, int ec, Object ob)
    {
    	switch (type)
    	{
	    	case ValleyRtcAPI.RespondLogin:
	    	{
	    		if(ERR_OK == ec)							//login successfull and jump to mainview
				{			
					LoginActivity.m_login.ShowLoginOk();
	    			clearUserList();	    			
	    			showStatus("登录成功!");
	    			showUserListInfo();											//if user login successfully , get the userlist
				}
	    	}
	    	break;
	    	case RtcUsers.RespondKickOff:
	    	{
	    	}
	    	break;
	    	case IRtcAudio.RespondDisableUserSpeaking:
	    	{
	    	}
	    	break;
	    	case IRtcAudio.RespondBlockUser:
	    	{
	    	}
	    	break;
    	}
    }
```

**6.监听Notify回调：**
- Notify回调为SDK主动送回的消息。
- Notify类别type：
    1. ValleyRtcAPI.NotifyConnectionLost：SDK连接断开
    2. ValleyRtcAPI.NotifyReConnected：SDK恢复连接
    3. ValleyRtcAPI.NotifyDuplicateLogined：用户重复登陆
    4. IRtcUsers.NotifyUserEnterChannel：新用户进入频道
    5. IRtcUsers.NotifyUserLeaveChannel：用户离开频道
    6. IRtcAudio.NotifyUserSpeaking：用户讲话中
    7. IRtcUsers.NotifyChannelClose：频道关闭
    8. IRtcUsers.NotifyKickOff：踢人通知
    9. IRtcAudio.NotifyDisableUserSpeaking：禁言通知
- Notify消息体ob，详见接口列表。
- 代码参考DEMO MainActivity 中Notify函数，部分代码如下：

```java
protected IRtcChannel demo_api  = null;
ApplicationEx theApp = (ApplicationEx)this.getApplication();
demo_api  = theApp.GetAudioClient();
demo_api.RegisterRtcSink(this);//设置监听回调

public void Notify(int type, Object ob)
	{
		switch (type)
    	{
			case ValleyRtcAPI.NotifyConnectionLost:	// ob: null
			{
   				clearUserList();
    			showStatus("网络断开...");
			}
			break;
			case ValleyRtcAPI.NotifyReConnected:		// ob: null
			{
				showUserListInfo();		//if user login successfully , get the userlist
				showStatus("恢复连接");
			}
			break;
			case ValleyRtcAPI.NotifyDuplicateLogined:	// ob: null  
			{
				LoginActivity.m_login.ShowLoginFailedInfo("重复登录");
    			Close();
			}
			break;
			case IRtcUsers.NotifyUserEnterChannel:	// ob: object_user
			{
				object_user oUser = (object_user)ob;
				adduser(oUser.getUserID());
			}
			break;
			case IRtcUsers.NotifyUserLeaveChannel:	// ob: object_userid
			{
			}
			break;
			case IRtcAudio.NotifyUserSpeaking:	// ob: object_user_speaking
			{
				object_user_speaking user = (object_user_speaking)ob;
				updateUserSpeakingStatus(user.getUserID(),true);
			}
			break;
			case IRtcUsers.NotifyChannelClose:  		//ob: object_error
			{
				showStatus(String.valueOf(ob));
    			clearUserList();
			}
			break;
			case IRtcUsers.NotifyKickOff:		//ob: object_userid
			{
			}
			break;
			case IRtcAudio.NotifyDisableUserSpeaking:		//ob: object_user_disable_speaking
			{
			}
			break;
    	}
	} 
```

**7.退出频道：**
- 1.调用SDK Logout函数，代码参考如下：
```java
protected IRtcChannel demo_api  = null;
demo_api  =  ((ApplicationEx)this.getApplication()).GetAudioClient();
demo_api.Logout(); 
```

**8.销毁对象：**
- 1.调用SDK的CleanSDK用于SDK环境清理。
- 2.部分关键代码如下：

```java
ApplicationEx theApp = (ApplicationEx)this.getApplication();
IRtcChannel demo_api  = theApp.GetAudioClient();
demo_api.Logout();
theApp.exit();

//ApplicationEx中exit代码
public void exit()
	{  
		m_watcher.Stop();
		m_debugger = null;
		m_pClient = null;
		ValleyRtcAPI.CleanSDK();
		System.exit(0);
	}
```

**9.刷新用户列表代码示例**
- 1.显示用户列表，代码参考MainActivity 中showUserListInfo函数：
```java
public void showUserListInfo()   //if login successfully, get the user list and show them
    {
        ApplicationEx theApp = (ApplicationEx)this.getApplication();
        IRtcChannel demo_api  =  theApp.GetAudioClient();
        object_user_sheet usersheet = new object_user_sheet();
        if(ERR_OK == demo_api.GetUserList(usersheet))
        {
            for(int i=0;i<usersheet.size();i++)
            {
                object_user oUser = usersheet.item(i);
                 Map<String, String> mp = new HashMap<String, String>();   
                 mp.put(NAME, GetUserNameInfo(oUser.getUserID(),false));      
                 mp.put(CONTENT, ""); 
                 mp.put(SID, oUser.getUserID()); 
                 m_listUser.add(mp);
                }
            ((SimpleAdapter)m_vUsers.getAdapter()).notifyDataSetChanged();
        }
    }
```
- 2.实现增加用户函数，参照addUser代码：
```java
public void adduser(String strUserID)
    {       
		ApplicationEx theApp = (ApplicationEx)this.getApplication();
		demo_api  = theApp.GetAudioClient();
		demo_user_api = (IRtcUsers)demo_api.GetInterface(IRtcUsers.IID);
		demo_real_audio_api = (IRtcAudio)demo_api.GetInterface(IRtcAudio.IID);
        object_user oUser = new object_user();   
        if(ERR_OK==demo_user_api.GetUser(strUserID, oUser))
        {       
            Map<String, String> mp = new HashMap<String, String>();   
            mp.put(NAME, GetUserNameInfo(oUser.getUserID(),false)); 
            mp.put(CONTENT, ""); 
            mp.put(SID, strUserID); 
            m_listUser.add(mp);   
            ((SimpleAdapter)m_vUsers.getAdapter()).notifyDataSetChanged();       
        } 
    }
```
- 3.监听Notify中NotifyUserEnterRoom 事件，参考Notify函数代码实现：
```java
public void Notify(int type, Object ob)
    {
        switch (type)
        {   
            case IRtcUsers.NotifyUserEnterChannel:               // ob: object_user
            {
                object_user oUser = (object_user)ob;
                adduser(oUser.getUserID());
            }
            break;
        }
    }   
```

### 接口目录
    
**概述：**
谷人云Android 实时语音SDK根据接口类别和作用主要分为六类，分别是：
- 用于SDK相关初始化，清理等操作的ValleyRtcAPI类；
- 用于频道相关各类操作的IRtcChannel类；
- 用于用户相关信息操作的IRtcUsers类；
- 用于实时语音相关操作的IRtcAudio类；
- 用于系统设备相关操作的IRtcAudioSystem类；
- 用于声音处理参数细节设置以及音效等管理设置的IRtcDeviceControler类。

### **ValleyRtcAPI类详述：**

**1.接口函数-InitSDK(Application app, String wkfolder)：**
- 函数说明：InitSDK用于SDK初始化，在使用谷人云SDK过程中第一步需要初始化SDK环境。
- 传入参数：
	`app，Application 类型。`
	`wkfolder，String 类型，用于设定SDK的工作目录，包含临时文件及日志，用户可以自己设定。`
- 返回值：`无`

**2.接口函数-CleanSDK()：**
- 函数说明：程序退出时销毁SDK对象，清理空间，并退出SDK。
- 传入参数：`无`
- 返回值：`无`

**3.接口函数-SetAuthoKey(String authokey)：**
- 函数说明：SDK注册，正式业务上线前应该从谷人云处获得合法key，并传入SetAuthoKey函数进行注册，确保业务正常使用，传入空值为测试环境，（（强烈建议KEY值应该有服务端传输至客户端进行初始化，而不是直接写在客户端））。
- 传入参数：
	`authokey，String类型，从谷人云注册获得，或传入空值则进入测试环境。`
- 返回值：`无`
		
**4.接口函数-GetErrDesc(int ec)：**
- 函数说明：本函数用于将SDK各处返回的错误码转称为错误描述，便于开发。
- 传入参数：`ec，int类型，调用函数或从Respond回调处获得的任何错误码。`
- 返回值：`string类型，错误描述。`

**5.接口函数-GetSDKVersion()：**
- 函数说明：获得SDK版本。
- 传入参数：`无`
- 返回值：`string类型，SDK版本号。`
		
**6.接口函数-CreateChannel()**
- 函数说明：谷人云频道相关的操作接口全部在IRtcChannel里面，SDK允许同时初始化多个频道实例，用户也可以同时加入多个频道，在需要多次初始化IRtcChannel的时候，可以直接new也可以调用ValleyRtcAPI中的CreateChannel()进行获取。
- 返回值：`IRtcChannel对象实例。`
		
### **IRtcChannel类详述：**

**1.接口函数-RegisterRtcSink(IRtcSink sink)：**
- 函数说明：谷人云SDK中大量错误码和通知由回调函数Respond以及Notify完成，RegisterRtcSink函数用于注册回调，使回调信息能正确的被程序获得。
- 传入参数：`sink，IRtcSink类型，可参照demo MainActivity用法。`
- 返回值：`无`
		
**2.接口函数-EnableInterface(int iid)&&GetInterface(int iid)&&DisableInterface(int iid)：**
- 函数说明：`用于初始化接口函数,获得接口实例以及关闭相应接口。`
    `在初始化频道类型的时候应该调用EnableInterface激活相应需要的接口列表。`
	`在需要调用具体接口函数时应该调用GetInterface获得相应的接口功能。`
	`在退出频道或者模式转变的时候应该调用DisableInterface关闭相应的接口。`
- 用法举例（以初始化频道并登陆举例）：
```java
    ApplicationEx theApp = (ApplicationEx)this.getApplication();
    IRtcChannel demo_api  = theApp.GetAudioClient();
    ValleyRtcAPI.SetAuthoKey("");//SetAuthoKey为注册函数，填入从谷人申请的id，留空为测试
    //音乐频道类型按照如下方式激活接口
    demo_api.EnableInterface(IRtcUsers.IID|IRtcAudio.IID|IRtcAudioSystem.IID|IRtcDeviceControler.IID);
    IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
    musiccontroler.Enable(IRtcDeviceControler.typeMusicMode,true);
    //语音频道类型按照如下方式激活接口
    demo_api.EnableInterface(IRtcUsers.IID|IRtcAudio.IID|IRtcAudioSystem.IID|IRtcDeviceControler.IID);
    IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
    musiccontroler.Enable(IRtcDeviceControler.typeMusicMode,false);//若这里不单独设置则默认为语音频道
    //需要设定用户登陆状态的时候调用GetInterface方法如下
    IRtcAudio real_audio_module = (IRtcAudio)demo_api.GetInterface(IRtcAudio.IID);
    //打开播放
    real_audio_module.EnablePlayout(true);
    //关闭采集
    real_audio_module.EnableSpeak(false);
    //登陆
    demo_api.Login(m_strRoomID, m_strLoginUserID, "");	//test room , roomkey is ""
```

**3.Login：**（函数int类型返回值没有填写）
- 函数说明：函数用于用户登陆具体频道。
- 传入参数：
		channelid，String类型，频道号。
		userid，String类型，用户名。
		userinfo，String类型，用户扩展信息。
- 返回值：
		参考本文档最底部错误码返回值对照表。
- 回调：
		监听Respond回调里面ValleyRtcAPI.RespondLogin类型，回调返回值为ec错误码，object_login类型登陆信息。
		
**4.接口函数-Logout()：**
- 函数说明：用户登出当前频道。
- 传入参数：`无`
- 返回值：`无`

**5.接口函数-GetLoginStatus()：**
- 函数说明：获取当前用户登陆状态。
- 传入参数：`无`
- 返回值：`参考本文档最底部错误码返回值对照表。`
		
**6.接口函数-SetChannelAttr(String name, String value)：**
- 函数说明：用于设置频道属性，便于开发者自定义频道标识。
- 传入参数：
		name，string类型，频道名字。
		value，string类型，标识。
- 返回值：`参考本文档最底部错误码返回值对照表。`
- 回调：
		监听Respond回调里面ValleyRtcAPI.RespondChannelAttr类型，回调返回值为ec错误码，object_channel_attr类型频道信息。
- 通知：
		房间信息的更新将会发送广播通知到房间内的所有客户，应用只需要监听Notify中的NotifyChannelAttr即可获取到详细的信息。
		
**7.接口函数-GetChannelAttr(String name, String value)**
- 函数说明：用于获取&&验证频道标识。
- 传入参数：
		name，string类型，频道名字。
		value，string类型，标识。
- 返回值：`参考本文档最底部错误码返回值对照表。`
		
**8.Respond回调-常量RespondLogin**
- 回调说明：登陆状态回调
- 回调信息：
		ec，int类型，错误码。
		ob，object_login类型，用户登陆信息。
		
**9.Respond回调-常量RespondChannelAttr**
- 回调说明：当执行set操作后，服务器执行完毕会给客户端频道标识回调
- 回调信息：
		ec，int类型，错误码。
		ob，object_channel_attr类型，频道标识信息。
	
**10.Notify回调-常量NotifyConnectionLost**
- 回调说明：用户网络连接断开
- 回调信息：`ob，null。`
		
**11.Notify回调-常量NotifyReConnected**
- 回调说明：用户网络恢复连接
- 回调信息：`ob，null`
		
**12.Notify回调-常量NotifyDuplicateLogined**
- 回调说明：用户重复登陆
- 回调信息：`ob，null`
		
**13.Notify回调-常量NotifyChannelClose**
- 回调说明：频道关闭信息
- 回调信息：`ob，object_error类型，可通过object_error获得错误码。`
		
**14.Notify回调-常量NotifyChannelAttr**
- 回调说明：频道标识信息
- 回调信息：`ob，object_channel_attr类型。`
		
### **IRtcUsers类详述：**
**1.接口函数-GetUserCount()：**
- 函数说明：获取当前频道用户数。
- 传入参数：`无`
- 返回值：`返回当前频道用户数。`

**2.接口函数-GetUserList(object_user_sheet userlist)：**
- 函数说明：获取当前频道用户列表。
- 传入参数：
		userlist,object_user_sheet类型，函数会将返回值天道userlist参数供用户调用。
- 返回值：
		参考本文档最底部错误码返回值对照表。

**3.接口函数-GetUser(String uid, object_user user)：**
- 函数说明：获取具体用户信息。
- 传入参数：
		uid，String类型，用户id。
		user，object_user类型，函数会将返回值天道user参数供用户调用。
- 返回值：
		参考本文档最底部错误码返回值对照表。

**4.接口函数-KickOff(String uid)：**
- 函数说明：踢人。
- 传入参数：
		uid，String类型，用户id。
- 返回值：
		参考本文档最底部错误码返回值对照表。
- 回调：
		监听Respond回调里面IRtcUsers.RespondKickOff类型，回调返回值为ec错误码，object_userid用户id。

**5.接口函数-SetUserAttr(String uid, String name, String value)**
- 函数说明：用户设置用户信息，方便开发者标识用户
- 传入参数：
		uid，string类型，用户id
		name，string类型，用户名
		value，string类型，用户标识
- 返回值：
		参考本文档最底部错误码返回值对照表。
- 回调：
		监听Respond回调里面IRtcUsers.RespondUserAttr类型，回调返回值为ec错误码，object_user_attr用户标识信息。
- 通知：
	`用户进行设定的时候请务必确保uid的输入为当前用户的id，设置成功以后房间内的所有用户将会收到notify广播，`
    ` 监听NotifyUserAttr类型的notify可以获得用户的设定详情。`

**6.接口函数-GettUserAttr(String uid, String name, String value)**
- 函数说明：获取&&校验用户信息
-传入参数：
		uid，string类型，用户id
		name，string类型，用户名
		value，string类型，用户标识
- 返回值：
		参考本文档最底部错误码返回值对照表。

		
**7.Respond回调-常量RespondKickOff**
- 回调说明：踢人情况
- 回调信息：
		ec，int类型，错误码。
		ob，object_userid类型，用户id。
		
**8.Respond回调-常量RespondUserAttr**
- 回调说明：用户信息
- 回调信息：
		ec，int类型，错误码。
		ob，object_user_attr类型，用户标识信息。
		
**9.Notify回调-常量NotifyUserEnterChannel**
- 回调说明：用户进入频道
- 回调信息：
		ob，object_user类型，用户信息。

**10.Notify回调-常量NotifyUserLeaveChannel**
- 回调说明：用户离开频道
- 回调信息：
		ob，object_userid类型，用户id。

**11.Notify回调-常量NotifyKickOff**
- 回调说明：用户被踢
- 回调信息：
		ob，object_userid类型，用户id。
		
**Notify回调-常量NotifyUserAttr**
- 回调说明：用户标识更新
- 回调信息：
		ob，object_user_attr，用户标识信息。
		
### **IRtcAudio类详述：**

**1.接口函数-BlockUser(String uid, boolean block)：**
- 函数说明：拒听某个用户语音。
- 传入参数：
		uid，String类型，用户id。
		block，boolean类型，执行操作，true为执行。
- 返回值：
		参考本文档最底部错误码返回值对照表。
- 回调：
		监听Respond回调里面IRtcAudio.RespondBlockUser类型，回调返回值为ec错误码，object_block_speak用户拒听信息。

**2.接口函数-DisableUserSpeak(String uid, boolean disspeak)：**
- 函数说明：用户禁言。
- 传入参数：
		uid，String类型，用户id。
		disspeak，boolean类型，执行操作，true为执行。
- 返回值：
		参考本文档最底部错误码返回值对照表。
- 回调：
		监听Respond回调里面IRtcAudio.RespondDisableUserSpeak类型，回调返回值为ec错误码，object_disable_speak为用户禁言信息。

**3.接口函数-EnableSpeak(boolean enable)：**
- 函数说明：打开麦克风。
- 传入参数：
		enable，boolean，执行操作，true为执行。
- 返回值：
		参考本文档最底部错误码返回值对照表。

**4.接口函数GetSpeakEnabled()：**
- 函数说明：返回当前用户的麦克风状态。
- 返回值：
		true：用户当前打开麦克风。
		false：用户当前关闭麦克风。

**5.接口函数EnablePlayout(boolean enable)：**
- 函数说明：打开频道声音播放。
- 传入参数：
		enable，boolean，执行操作，true为执行。
- 返回值：
		参考本文档最底部错误码返回值对照表。

**6.接口函数GetPlayoutEnabled()：**
- 函数说明：返回当前用户的播放状态。
- 返回值：
		true：用户当前打开播放。
		false：用户当前关闭播放。
		
**7.Respond回调-常量RespondDisableUserSpeak**
- 回调说明：用户禁言操作返回
- 回调信息：
		ec，int类型，错误码。
		ob，object_disable_speak类型，用户禁言情况。
		
**8.Respond回调-常量RespondBlockUser**
- 回调说明：用户拒听操作返回
- 回调信息：
		ec，int类型，错误码。
		ob，object_block_speak类型，用户拒听情况。
		
**9.Notify回调-常量NotifyDisableUserSpeak**
- 回调说明：远程用户禁言操作回调
- 回调信息：
		ob,object_disable_speak类型，用户禁言情况。
		
**10.Notify回调-常量NotifyUserSpeaking**
- 回调说明：用户正在讲话
- 回调信息：
		ob,object_user_speaking类型，用户讲话情况。

### **IRtcAudioSystem类详述: **
**1.接口函数SetPlayoutVolume(int volume)：**
- 函数说明：设置播放音量，在系统音量基础上在做增益，一般设置80%，demo设置为100%。
- 传入参数：
		volume，int类型，音量百分比。

**2.接口函数SetSpeakerphoneOn(boolean on)**
- 函数说明：打开扬声器播放，这个函数无论是否插耳机或者蓝牙耳机，都会强制打开扬声器播放。
- 传入参数：
		on，boolean类型，true为打开。
		
**3.接口函数GetPlayoutVolume()**
- 函数说明：获取当前设置的播放音量。
- 返回值：
		int类型，当前音量，最高100。100代表为当前系统音量的四倍增益，50为正常音量，无增益。
		
**4.接口函数GetSpeakphoneOn()**
- 函数说明：获取当前扬声器打开状态。
- 返回值：
		boolean类型，true代表当前扬声器打开，false代表当前为听筒播放。

### **IRtcDeviceControler类详述：**

**1.接口函数-SetBackgroudMusic(int trackIndex, String filepath, boolean loopflag, float volume, boolean bSendToNet, boolean bPlayout)：**
- 函数说明：设置背景音乐，音效。
- 传入参数：
	`trackIndex，int类型，表示第几音轨，支持0-4共五路音轨的设置。`
	`filepath，string类型，音乐文件完整路径。`
	`loopflag，boolean类型，是否重复播放。`
	`volume，float类型，当前音轨音量控制， 0.0 ~ 1.0f。`
	`bSendToNet，boolean类型，是否发送至网络，设置为false代表只本地播放。`
	`bPlayout，boolean类型，是否在本地通过扬声器输出。`
- 返回值：
		参考本文档最底部错误码返回值对照表。

**2.接口函数-SetBackgroudMusicVolume(int trackIndex, float volume)：**
- 函数说明：单独调节音效或者背景音乐音轨音量。
- 传入参数：
	`trackIndex，int类型，表示第几音轨，支持0-4共五路音轨的设置，传入-1代表对全部音轨进行设置。`
	`volume，float类型，当前音轨音量控制， 0.0 ~ 1.0f。`
- 返回值：
		参考本文档最底部错误码返回值对照表。
		
**3.接口函数-IsSpeakWithMusic()：**
- 函数说明：返回当前是否按照音乐模式采集。
- 返回值：
	`true：当前按照音乐模式采集。`
	`false：当前按照普通模式采集。`
		
**4.功能模式控制：**
- 说明：该接口对象中包含了整体声音处理函数的细分设定，具体分为AEC,AGC,VAD,NS,高音质输入输出，背景音乐支持等等功能设定。
- 示例：
```java
IRtcDeviceControler musiccontroler = (IRtcDeviceControler)demo_api.GetInterface(IRtcDeviceControler.IID);
//登陆前进行设定
musiccontroler.Enable(IRtcDeviceControler.typeMusicMode,true);
//typeMusicMode为true代表该房间为音乐模式，true为默认值，音乐模式下所有声音将以高音质的方式进行传输，适用于直播，语音直播等场景；false代表该房间为通话模式，通话模式下声音将以16K采样频率进行传输。
musiccontroler.Enable(IRtcDeviceControler.typeBackgroundMusic,true);
//typeBackgroundMusic为true代表该房间支持背景音乐以及音效叠加，默认值为false。
//以下设定在登陆前或者登陆后设置都有效，只有在音乐模式开启的情况下才会生效
musiccontroler.Enable(IRtcDeviceControler.typeCtrlByHeadset,false);
//typeCtrlByHeadset为true表示音乐房间内参数交由程序自动控制，false表明用户自己控制，用户调用下面几个来进行细节的控制，默认为true。
musiccontroler.Enable(IRtcDeviceControler.typeAec,true);
//typeAec为true表明打开回声消除。
musiccontroler.Enable(IRtcDeviceControler.typeNs,true);
//typeNs为true表明打开噪声抑制。
musiccontroler.Enable(IRtcDeviceControler.typeVad,true);
//typeVad为true表明打开静音检测。
musiccontroler.Enable(IRtcDeviceControler.typeAgc,true);
//typeAgc为true表明打开声音增益
```
- 模式举例：

1.语音通话房间：
```java
musiccontroler.Enable(IRtcDeviceControler.typeMusicMode,false);
```
2.音乐房间并且当前用户为主播，需要进行高音质的歌唱：
```java
musiccontroler.Enable(IRtcDeviceControler.typeMusicMode,true);
musiccontroler.Enable(IRtcDeviceControler.typeBackgroundMusic,true);
musiccontroler.Enable(IRtcDeviceControler.typeCtrlByHeadset,false);
musiccontroler.Enable(IRtcDeviceControler.typeAec,false);
musiccontroler.Enable(IRtcDeviceControler.typeNs,false);
musiccontroler.Enable(IRtcDeviceControler.typeVad,false);
musiccontroler.Enable(IRtcDeviceControler.typeAgc,true);
```
3.音乐房间并且当前用户为观众，需要进行普通连麦与主播互动：
```java
musiccontroler.Enable(IRtcDeviceControler.typeMusicMode,true);
musiccontroler.Enable(IRtcDeviceControler.typeBackgroundMusic,true);
musiccontroler.Enable(IRtcDeviceControler.typeCtrlByHeadset,false);
musiccontroler.Enable(IRtcDeviceControler.typeAec,true);
musiccontroler.Enable(IRtcDeviceControler.typeNs,true);
musiccontroler.Enable(IRtcDeviceControler.typeVad,true);
musiccontroler.Enable(IRtcDeviceControler.typeAgc,true);
```

**5.接口函数StartRtmp(String url, int sreamtypes, boolean bUseServer)**
- 函数说明：旁路第三方推流。
- 传入参数：
    `url，string类型，推流地址。`
	`sreamtypes，int类型，推流类型，1为音频，2为音视频。`
	`bUseServer，boolean类型，是否服务端推流，false为本地直接推流。`
- 返回值：`参考本文档最底部错误码返回值对照表。`
		

**6.接口函数StopRtmp()**
- 函数说明：停止推流，房主推出房间默认自动停止推流。
- 返回值：`参考本文档最底部错误码返回值对照表。`
		

**7.接口函数StartRecordEx(int sreamtypes)**
- 函数说明：服务端录音。
- 传入参数：`sreamtypes，int类型，推流类型，1为音频，2为音视频。`
- 返回值：`参考本文档最底部错误码返回值对照表。`
		
**8.接口函数StopRecordEx()**
- 函数说明：停止服务端录音。
- 返回值：`参考本文档最底部错误码返回值对照表。`
		
**8.接口函数PauseRecordEx()**
- 函数说明：暂停服务端录音。
- 返回值：`参考本文档最底部错误码返回值对照表。`

**9.Notify回调-常量NotifyPlayAudioEnd**
- 回调说明：播放背景音乐结束，循环播放表示一次播放结束 object_number.getInt()获取播放结束的音轨号
- 回调信息：
    `ec，int类型，错误码。`
    `ob，object_number类型，音轨号。`
		
		

### **ec错误码：**
`ec 类型为int，含义如下：`
`ERR_SUCCEED 0         // 操作成功`
`ERR_NOT_LOGINED  -1   // 未登录成功  `     
`ERR_ALREADY_RUN  -2   // 已经在运行了  ` 
`ERR_USER_NOTFOUND -3  // 为找到用户   `
`ERR_EXCUTING -4       // 已经执行中了`   
`ERR_NOT_INITIALIZE -5 // 未初始化`
`ERR_UNSUPPORT -6      // 功能不支持`
`ERR_ARGUMENT  -7	  // 参数错误`

`ERR_CHANNEL_EXPIRED 1        // 频道已经失效`
`ERR_CONNECT_SERVER_FAILED 2  // 连接服务器失败`
`ERR_REQUEST_TIMEOUT 3        // 请求超时`
`ERR_CONFIG 4                 // 配置信息错误 `
`ERR_NET_POOL 5               // 网络不好`
`ERR_VERSION_UNSUPPORTED 6    // 版本不支持`
`ERR_AUTHO_FAILED 7           // 授权失败 `
`ERR_NOT_ENOUGH_CHANNEL 8     // 频道资源不足`
`ERR_SERVER_ERROR 9           // 服务器错误`
`ERR_OPEN_RECORD_DEVICE 10    // 打开麦克风采集失败`
`ERR_OPEN_PLAYOUT_DEVICE 11   // 打开播放失败`
`ERR_RECORD_PERMISSION 12     // 没有录音权限`
