
### 
- real_audio_client 为demo源码
- SDK目录为sdk包

官方文档地址：https://doc.valley.ren/

### DEMO运行指引

**1.环境准备：** 
- 1.下载Android DEMO 源文件包。
- 2.Android Studio 2.1 或以上版本。
- 3.已经下载好 Android SDK 25、Android SDK Build-Tools 25.0.3、Android SDK Platform-Tools 25.\*.\*。
- Android 版本不低于 4.0.3 ，如果用真机调试请开启“允许调试”选项。
- Android 设备已经连接到 Internet。

**2.运行项目：** 

------------
###### 第一次编译 DEMO 项目时，需要下载特定的 gradle套件 及 DEMO 所需要的依赖库，所以请确保开发电脑已经连接到 internet 且能正常访问 https://services.gradle.org

------------
- 1.打开Android Studio，以gradle方式导入项目。
![](https://doc.valley.ren/images/2017-11-03/59fbeaac0673b.png)
- 2.修改项目根目录下面local.properties，指定Android SDK目录到正确的本机路径。
![](https://doc.valley.ren/images/2017-11-03/59fbea34e1bf9.png)
- 3.新建一个configurations。
![](https://doc.valley.ren/images/2017-11-03/59fbeb5a5ff58.png)
参数保持默认即可：
![](https://doc.valley.ren/images/2017-11-03/59fbeb9e72c80.png)
- 4.编译运行，Run-Run app。

**3.部分说明：**

- 1.DEMO主要由LoginActivity及MainActivity组成，相关DEMO回调事件则注册在DemoEventImp中。
- 2.DEMO运行在测试鉴权ID下面，所有测试ID公用一套环境，会出现和别人测试时候登陆到同一个房间的情况。
- 3.谷人云实时视频部分大体分为组件初始化，用户登陆，回调事件实现，退出等几个步骤，方便简单，利于接入。
- 4.测试鉴权ID长期开放，欢迎修改测试demo实现一些有趣的小功能并投稿。


### SDK获取及集成

**1.SDK概述：** 
- 谷人云Android SDK已经打包成一个文件，xrtcsdk.aar（有需要jar包部分代码的可以联系商务获取）。

**2.下载SDK：** 
- 请在官网下载部分获取Android相关SDK文件。
  
**3.拷贝SDK：**
- 1.将xrtcsdk.aar 拷贝住程序目录下libs目录中，如DEMO源码中则为app目录下libs目录。
- 2.将armeabi-v7a目录（包含libValleyRtcSDK.so）拷贝至程序源码目录jniLibs下，如DEMO源码中则为app/src/main/jniLibs-armeabi-v7a目录。

**4.引入SDK：**
- 编辑app目录下build.gradle 文件，在dependencies块中添加如下：
```java
compile(name:'xrtcsdk', ext:'aar')
```
- 在android块中添加如下：
```java
repositories {
        flatDir {
            dirs 'libs'
        }
    }
```

**5.添加权限申明：**
- 权限部分已经在aar文件写好，上层应用无须单独添加

**6.开始使用：**
- 参见功能实现流程以及接口列表。

### 功能实现文档
**多人视频实现流程简述：**
1. SDK初始化
2. 设置用户登陆参数及登陆
3. 打开&关闭播放，打开&关闭麦克风，打开&关闭视频
4. 实现回调
5. 退出频道&&销毁对象

**1.SDK初始化：** 
1. create：初始化SDK环境，设置APPID，日志文件谷人云SDK默认写到xRTC目录。
2. setVideoProfile：设置本地视频流分辨率属性，属性列表在接口文档xRTCProfile中有详细定义。
3. 具体代码参考DEMO MainActivity，部分关键代码如下：
```java
public void EngineCreate()
    {
        mEventHandler = new DemoEventImp() ;
        mEventHandler.mSActivity = this ;

        mRTCEngine = xRTCEngine.create( this , "game_card_room", mEventHandler ) ;
        int rc = mRTCEngine.setVideoProfile( xRTCProfile.VIDEO_CAPTURE_TYPE_16X9_160 , mSwap ) ;
        if ( rc < 0 )
        {
            return;
        }
        mRTCEngine.enableAudio() ;
        mRTCEngine.enableVideo() ;
    }
```

**2.设置用户登陆参数及登陆：**
1. joinChannel：参数列表为channelname，roomid，token，userid。注释：token123为测试token。
2. xRTCVideoCanvas：参数列表为suefaceview，rendertype，userid。注释：rendertype在接口文档xRTCVideoCanvas类中有详细定义，userid为0代表自己。
3. 具体代码参考demo MainActivity，部分关键代码如下：
```java
	public void onClickStart(View view)
    {
    	//在SDK初始化完成以后调用登陆。
    	//设置surfaceView
        xRTCVideoCanvas myCanvas = new xRTCVideoCanvas( mMyView, 0, 0 ) ;
        mRTCEngine.setupLocalVideo( myCanvas ) ;
        Random random = new Random( System.nanoTime() );
        long userid = random.nextLong() ;
        if ( userid < 0 )
        {
            userid = -1 * userid ;
        }
        mRTCEngine.joinChannel( null, mRoomID, "token123", userid ) ;
    }
```
3. 强烈建议token值应该由服务端传给客户端，不应该写死于客户端中。

**3.打开&关闭播放，打开&关闭麦克风，打开&关闭视频：**
- 1.直接进行函数调用，代码参考如下：

```java
public xRTCEngine mRTCEngine ;
mRTCEngine.muteLocalVideoStream( true ) ;//true为关闭本地视频采集，false为打开。
mRTCEngine.muteLocalAudioStream( true ) ;//true为关闭本地音频采集，false为打开。
mRTCEngine.muteRemoteVideoStream( userid, true) ;//true为关闭视频播放，false为打开，userid为0代表所有用户。
mRTCEngine.muteRemoteAudioStream( userid, true) ;//true为关闭声音播放，false为打开，userid为0代表所有用户。
```
**4.实现回调：**
- 1.用户通过继承xRTCEventHandler类实现所有SDK提供的回调功能，功能包含但不限于如下：
		1.用户加入频道
		2.用户掉线
		3.用户禁音禁画
		4.创建房间
		5.错误回调
- 2.用户在主线程SDK初始化中进行时间注册：
```java
public DemoEventImp mEventHandler ;
public xRTCEngine mRTCEngine ;
mEventHandler = new DemoEventImp() ;
mEventHandler.mActivity = this ;
mRTCEngine = xRTCEngine.create( this , "game_card_room", mEventHandler ) ;
```
- 3.回调函数举例如下,用户加入房间，具体参考DEMO：
```java
//实现于DemoEventImp
public  void onUserJoined(long uid, int elapsed)
    {
        if ( mActivity != null ) {
            mActivity.UserEnter(uid);
        }
        else if ( mSActivity != null ) {
            mSActivity.UserEnter(uid);
        }
    }
```
```java
//实现于MainActivity
public void UserEnter(long uid)
    {
        if ( mViewList.isEmpty() )
        {
            mFailList.add( uid ) ;
            xRTCLogging.e(TAG, "user enter fail...uid:"+uid ) ;
            return ;
        }

        mEnterUserID = uid ;
        SurfaceView user_view = mViewList.remove(0) ;
        if ( user_view != null )
        {
            xRTCVideoCanvas canvas = new xRTCVideoCanvas( user_view, xRTCVideoCanvas.RENDER_TYPE_CROP, uid ) ;
            xRTCLogging.e(TAG, "add surfaceview uid:"+uid +" count:"+ mFailList.size() ) ;
            mViewMap.put( uid, canvas ) ;
            mRTCEngine.setupRemoteVideo(canvas);
        }

    }
```

**5.退出频道&&销毁对象：**
- 参考demo MainActivity实现：
```java
public void onClickStop(View view)
    {
        if ( mRTCEngine == null )
        {
            return;
        }

        mEventHandler.mSActivity = null ;
        xRTCEngine.destroy() ;
        mRTCEngine = null ;

        mFailList.clear();
        mViewMap.clear();
        finish();
    }
@Override
protected void onDestroy() {
        super.onDestroy();
        EngineDestroy();
		}
public void EngineDestroy()
    {
        xRTCEngine.destroy() ;
        mRTCEngine = null ;
    }
```

