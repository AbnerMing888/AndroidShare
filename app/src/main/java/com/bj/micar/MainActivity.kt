package com.bj.micar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.abner.share.ShareUtils
import com.sina.weibo.sdk.common.UiError
import com.sina.weibo.sdk.share.WbShareCallback


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    private val mSahreArray = arrayOf(
        "QQ登录", "微信登录", "微博登录",
        "QQ好友分享链接", "QQ好友分享图片",
        "QQ空间分享链接", "QQ空间分享图片", "微博分享图片", "微博分享链接",
        "微信好友分享链接", "微信好友分享图片", "微信朋友圈分享链接", "微信朋友圈分享图片"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val linearLayout = findViewById<LinearLayout>(R.id.ll_list)

        mSahreArray.forEachIndexed { index, s ->

            val button = Button(this)
            button.text = s
            button.setOnClickListener {
                shareClick(index)
            }
            linearLayout.addView(button)
        }
        //获取存储权限
        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1000
            );
        }
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:按钮点击
     */
    private fun shareClick(index: Int) {
        when (index) {
            0 -> {//QQ登录
                ShareUtils.get()
                    .login(this, ShareUtils.QQ)
                    .setOnQqCallBackListener({
                        //登录成功
                        Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
                    }, {
                        //登录失败
                    })
            }
            1 -> {//微信登录
                ShareUtils.get().login(this, ShareUtils.WEIXIN)
            }
            2 -> {//微博登录
                ShareUtils.get().login(this, ShareUtils.WEIBO)
                    .setOnWbCallBackListener({
                        //成功
                        Toast.makeText(this, "微博登录成功", Toast.LENGTH_LONG).show()
                    }, {
                        //失败
                    })
            }
            3 -> {//QQ好友分享链接
                //注意，图片地址需要自己生成，这里我使用的是简单的测试
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareUrl(
                    this,
                    ShareUtils.QQ,
                    "我是测试分享标题",
                    "我是测试分享链接",
                    "https://www.vipandroid.cn/ming/page/open.html",
                    imagePath,
                ).setOnQqCallBackListener({
                    Log.e(TAG, "分享成功")
                }, {
                    Log.e(TAG, "分享失败")
                })
            }
            4 -> {//QQ好友分享图片
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareImage(
                    this,
                    ShareUtils.QQ,
                    imagePath,
                ).setOnQqCallBackListener({
                    Log.e(TAG, "分享成功")
                }, {
                    Log.e(TAG, "分享失败")
                })
            }
            5 -> {//QQ空间分享链接
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareUrl(
                    this,
                    ShareUtils.QQ,
                    "我是测试分享标题",
                    "我是测试分享链接",
                    "https://www.vipandroid.cn/ming/page/open.html",
                    imagePath,
                    ShareUtils.SCENE
                )
            }
            6 -> {//QQ空间分享图片
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareImage(
                    this,
                    ShareUtils.QQ,
                    imagePath,
                    ShareUtils.SCENE
                ).setOnQqCallBackListener({
                    Log.e(TAG, "分享成功")
                }, {
                    Log.e(TAG, "分享失败")
                })
            }
            7 -> {
                //微博分享图片
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareImage(this, ShareUtils.WEIBO, imagePath)
            }
            8 -> {
                //微博分享链接
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareUrl(
                    this, ShareUtils.WEIBO,
                    "我是测试标题", "我是测试描述", "https://www.vipandroid.cn", imagePath
                )
            }
            9 -> {
                //微信好友分享链接
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareUrl(
                    this,
                    ShareUtils.WEIXIN,
                    "我是测试标题",
                    "我是测试描述",
                    "https://www.vipandroid.cn",
                    imagePath
                )
            }
            10 -> {//微信好友分享图片
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareImage(this, ShareUtils.WEIXIN, imagePath)
            }
            11 -> {
                //微信朋友圈分享链接
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareUrl(
                    this,
                    ShareUtils.WEIXIN,
                    "我是测试标题",
                    "我是测试描述",
                    "https://www.vipandroid.cn",
                    imagePath, ShareUtils.SCENE
                )
            }
            12 -> {
                //微信朋友圈分享图片
                val absoluteFile = Environment.getExternalStorageDirectory().absoluteFile
                val imagePath = "$absoluteFile/ic_launcher.png"
                ShareUtils.get().qwShareImage(this, ShareUtils.WEIXIN, imagePath, ShareUtils.SCENE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            //QQ回调
            ShareUtils.get().onQqActivityResult(requestCode, resultCode, data)
            //微博回调
            ShareUtils.get().onWbShareActivityResult(data, object : WbShareCallback {
                override fun onComplete() {

                }

                override fun onError(p0: UiError?) {
                }

                override fun onCancel() {

                }

            })
        }

    }
}