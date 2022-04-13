# AndroidShare
Android官网（微信，QQ，微博）集成，包含登录和分享。<br/>
## Demo效果预览<br/>
![Image 扫描下载](image/share.jpg)<br/>
![Image 效果](image/sharePhone.jpg width='100')<br/>
## 问题须知
<p>
1、Demo里的微信，QQ，微博，都是用的测试的，大家在用的时候，务必换成自己。
</p>
<p>
2、Demo允许是不能实现登录分享的，因为需要签名，毕竟牵扯到保密，签名文件和代码并没有提交，大家如果测试，可以更换成自己的签名文件即可，查看效果，扫描二维码下载即可。
</p>

## 具体调用

### 第一步，根项目下build.gradle
```
repositories {
        ……
        maven { url "https://gitee.com/AbnerAndroid/android-maven/raw/master" }
    }
```
### 第二步，在需要的module下build.gradle
```
    implementation 'com.abner:share:1.0.0'
```

### 第三步，初始化，传自己申请的微信，QQ，微博信息
```
    ShareUtils.get().initShare(
            this,
            BuildConfig.WX_ID,
            BuildConfig.QQ_ID,
            BuildConfig.WB_KEY
        )

```
### 第四步，清单文件注册，WXEntryActivity需要在wxapi包下创建，一定要按照标准，可直接复制Demo中的，tencent1112002456是填自己申请的。

```
 <activity
            android:name=".wxapi.WXEntryActivity"
            android:configChanges="keyboardHidden|orientation|screenSize"
            android:exported="true"
            android:theme="@android:style/Theme.Translucent.NoTitleBar" />
        <activity
            android:name="com.tencent.tauth.AuthActivity"
            android:launchMode="singleTask"
            android:noHistory="true">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data android:scheme="tencent1112002456" />
            </intent-filter>
        </activity>

```

### 第五步，使用，可直接参考Demo中的MainActivity


