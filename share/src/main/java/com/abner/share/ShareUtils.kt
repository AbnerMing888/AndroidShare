package com.abner.share

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.abner.share.listener.BaseUiListener
import com.abner.share.net.NetUtils
import com.sina.weibo.sdk.api.ImageObject
import com.sina.weibo.sdk.api.WebpageObject
import com.sina.weibo.sdk.api.WeiboMultiMessage
import com.sina.weibo.sdk.auth.AuthInfo
import com.sina.weibo.sdk.auth.Oauth2AccessToken
import com.sina.weibo.sdk.auth.WbAuthListener
import com.sina.weibo.sdk.openapi.IWBAPI
import com.sina.weibo.sdk.openapi.SdkListener
import com.sina.weibo.sdk.openapi.WBAPIFactory
import com.sina.weibo.sdk.share.WbShareCallback
import com.tencent.connect.common.Constants
import com.tencent.connect.share.QQShare
import com.tencent.connect.share.QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD
import com.tencent.connect.share.QzoneShare
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

/**
 *AUTHOR:AbnerMing
 *DATE:2021/11/24
 *INTRODUCE:分享登录工具类
 */
class ShareUtils private constructor() {
    private var mWXApi: IWXAPI? = null
    private var mWBAPI: IWBAPI? = null
    private var mTencent: Tencent? = null
    private var mOnQqCallBackListener: OnQqCallBackListener? = null
    private var mOnWbCallBackListener: OnWbCallBackListener? = null
    private var mContext: Context? = null
    private var mShareOrLogin = 0

    companion object {
        const val TAG = "ShareUtils"
        const val WEIXIN = 1000
        const val WEIBO = 1001
        const val QQ = 1002
        const val SCENE = 1//传递此数据，代表着分享空间或朋友圈
        const val THUMB_SIZE = 150 //设置分享到朋友圈的缩略图宽高大小
        const val HTTP_QQ_URL = "https://graph.qq.com/user/get_user_info?"

        private var instance: ShareUtils? = null
            get() {
                if (field == null) {
                    field = ShareUtils()
                }
                return field
            }

        fun get(): ShareUtils {
            return instance!!
        }
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:初始化分享sdk
     */
    fun initShare(
        context: Context,
        wxId: String,
        qqId: String,
        wbKey: String
    ) {
        mContext = context
        //QQ初始化
        mTencent = Tencent.createInstance(qqId, context)

        //微信初始化
        mWXApi = WXAPIFactory.createWXAPI(context, wxId, true)
        mWXApi!!.registerApp(wxId)
        //微博初始化
        val authInfo = AuthInfo(context, wbKey, "https://www.mewcars.com", "")
        mWBAPI = WBAPIFactory.createWBAPI(context)
        mWBAPI!!.registerApp(context, authInfo, object : SdkListener {
            override fun onInitSuccess() {
                Log.e(TAG, "微博初始化成功")
            }

            override fun onInitFailure(p0: Exception?) {
                Log.e(TAG, "微博初始化成功")
            }

        })
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:微信，QQ，微博登录
     */
    fun login(
        activity: Activity,
        type: Int
    ): ShareUtils {
        mShareOrLogin = 0
        when (type) {
            WEIXIN -> {
                //微信
                if (isWxInstalled(activity)) {
                    val req = SendAuth.Req()
                    req.scope = "snsapi_userinfo"
                    req.state = "wechat_login"
                    mWXApi!!.sendReq(req)
                }
            }
            QQ -> {
                //QQ
                if (isQqInstalled(activity)) {
                    mTencent!!.login(activity, "get_simple_userinfo", mBaseUiListener)
                }
            }
            WEIBO -> {
                //WEIBO
                if (isWbInstalled(activity)) {
                    mWBAPI!!.authorize(activity, object : WbAuthListener {
                        override fun onComplete(p0: Oauth2AccessToken?) {
                            if (mOnWbCallBackListener != null) {
                                mOnWbCallBackListener!!.onComplete(p0)
                            }
                        }

                        override fun onError(p0: com.sina.weibo.sdk.common.UiError?) {
                            if (mOnWbCallBackListener != null) {
                                mOnWbCallBackListener!!.onError(p0)
                            }
                        }

                        override fun onCancel() {
                        }

                    })
                }
            }
        }
        return this
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:图文分享：微信，QQ，微博
     */
    fun qwShareUrl(
        activity: Activity,
        type: Int,
        title: String,
        summary: String,
        url: String,
        imageUrl: String = "",
        shareType: Int = 0
    ): ShareUtils {
        mShareOrLogin = 1
        when (type) {
            WEIXIN -> {
                val webpage = WXWebpageObject()
                webpage.webpageUrl = url
                val msg = WXMediaMessage(webpage)
                msg.title = title
                msg.description = summary

                msg.thumbData = bmpToByteArray(getBitmap(imageUrl), true)
                val req = SendMessageToWX.Req()
                req.message = msg
                if (shareType == 0) {
                    req.scene = SendMessageToWX.Req.WXSceneSession//分享到微信
                } else {
                    req.scene = SendMessageToWX.Req.WXSceneTimeline //分享到微信朋友圈
                }
                mWXApi!!.sendReq(req)
            }
            QQ -> {
                val params = Bundle()
                if (shareType == 0) {
                    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT)
                    params.putString(QQShare.SHARE_TO_QQ_TITLE, title)
                    params.putString(QQShare.SHARE_TO_QQ_SUMMARY, summary)
                    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url)
                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imageUrl)
                    mTencent!!.shareToQQ(activity, params, mBaseUiListener)
                } else {
                    params.putInt(
                        QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                        QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT
                    )
                    params.putString(QzoneShare.SHARE_TO_QQ_TITLE, title)
                    params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary)
                    params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url)
                    val imageList = ArrayList<String>()
                    imageList.add(imageUrl)
                    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageList)
                    mTencent!!.shareToQzone(activity, params, mBaseUiListener)
                }

            }
            WEIBO -> {
                val message = WeiboMultiMessage()
                val webObject = WebpageObject()
                webObject.identify = UUID.randomUUID().toString();
                webObject.title = title
                webObject.description = summary
                val bitmap = getBitmap(imageUrl)

                val os = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, os)
                webObject.thumbData = os.toByteArray()

                webObject.actionUrl = url
                message.mediaObject = webObject
                mWBAPI!!.shareMessage(activity, message, false)
            }
        }
        return this
    }

    private fun getBitmap(imageUrl: String): Bitmap {
        if (TextUtils.isEmpty(imageUrl)) {
            return BitmapFactory.decodeResource(mContext!!.resources, R.mipmap.ic_launcher)
        }
        return BitmapFactory.decodeFile(imageUrl)
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:图片分享
     */
    fun qwShareImage(
        activity: Activity,
        type: Int,
        imageUrl: String,
        shareType: Int = 0,
        imageSize: Float = 0f
    ): ShareUtils {
        mShareOrLogin = 1
        when (type) {
            WEIXIN -> {
                //初始化 WXImageObject 和 WXMediaMessage 对象
                val bmp = getBitmap(imageUrl)
                val imgObj = WXImageObject(bmp)//设置要分享的图片
                val msg = WXMediaMessage()
                msg.mediaObject = imgObj
                //设置缩略图
                val thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true)
                bmp.recycle()

                msg.thumbData = bmpToByteArray(thumbBmp, true)

                //构造一个Req
                val req = SendMessageToWX.Req()
                req.message = msg
                if (shareType == 0) {
                    req.scene = SendMessageToWX.Req.WXSceneSession//分享到微信
                } else {
                    req.scene = SendMessageToWX.Req.WXSceneTimeline //分享到微信朋友圈
                }
                //调用api接口，发送数据到微信
                mWXApi!!.sendReq(req)
            }
            QQ -> {
                val params = Bundle()
                if (shareType == 0) {
                    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE)
                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imageUrl)
                    if (imageSize != 0f) {
                        val localPath: String =
                            params.getString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL)!!
                        val file = File(localPath)
                        if (file.length() >= imageSize * 1024 * 1024) {
                            if (mBaseUiListener != null) {
                                mBaseUiListener.onError(
                                    UiError(
                                        Constants.ERROR_IMAGE_TOO_LARGE,
                                        Constants.MSG_SHARE_IMAGE_TOO_LARGE_ERROR,
                                        null
                                    )
                                )
                            }
                            return this
                        }
                    }
                    params.putInt(
                        QQShare.SHARE_TO_QQ_EXT_INT,
                        QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE
                    )
                    mTencent!!.shareToQQ(activity, params, mBaseUiListener)
                } else {
                    params.putInt(
                        QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                        PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD
                    )
                    val imageList = ArrayList<String>()
                    imageList.add(imageUrl)
                    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageList)
                    mTencent!!.publishToQzone(activity, params, mBaseUiListener)
                }

            }
            WEIBO -> {
                val message = WeiboMultiMessage()
                val imageObject = ImageObject()
                val bitmap = getBitmap(imageUrl)
                imageObject.setImageData(bitmap)
                message.imageObject = imageObject
                mWBAPI!!.shareMessage(activity, message, false)
            }
        }
        return this
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:判断QQ是否已经安装
     */
    private fun isQqInstalled(activity: Activity): Boolean {
        return if (mTencent!!.isQQInstalled(activity)) {
            true
        } else {
            Toast.makeText(activity, "您手机暂时未安装QQ", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:判断微信是否安装
     */
    private fun isWxInstalled(activity: Activity): Boolean {
        return if (mWXApi!!.isWXAppInstalled) {
            true
        } else {
            Toast.makeText(activity, "您手机暂时未安装微信", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:判断微博是否安装
     */
    private fun isWbInstalled(activity: Activity): Boolean {
        return if (mWBAPI!!.isWBAppInstalled) {
            true
        } else {
            Toast.makeText(activity, "您手机暂时未安装微博", Toast.LENGTH_LONG).show()
            false
        }
    }


    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:QQ登录注销
     */
    fun qqLoginOut(activity: Activity) {
        mTencent!!.logout(activity)
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:QQ登录回调
     */
    private val mBaseUiListener = object : BaseUiListener() {
        override fun onComplete(p0: Any?) {
            super.onComplete(p0)
            try {
                if (mShareOrLogin == 0) {
                    val jsonObject = JSONObject(p0.toString())
                    val openid = jsonObject.getString("openid")
                    val accessToken = jsonObject.getString("access_token")
                    val oauthConsumerKey = mTencent!!.appId
                    NetUtils.get().get(
                        HTTP_QQ_URL + "access_token="
                                + accessToken + "&openid=" + openid + "&oauth_consumer_key="
                                + oauthConsumerKey
                    ).setOnHttpCallBackListener({
                        if (mOnQqCallBackListener != null) {
                            mOnQqCallBackListener!!.onComplete(it)
                        }
                    }, {

                    })
                } else {
                    //分享
                    if (mOnQqCallBackListener != null) {
                        mOnQqCallBackListener!!.onComplete(p0)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }


        }

        override fun onError(p0: UiError?) {
            super.onError(p0)
            if (mOnQqCallBackListener != null) {
                mOnQqCallBackListener!!.onError(p0)
            }
        }
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:QQ登录后回调
     */
    fun setOnQqCallBackListener(
        complete: (p0: Any?) -> Unit,
        error: (p0: UiError?) -> Unit
    ) {
        mOnQqCallBackListener = object : OnQqCallBackListener {
            override fun onComplete(p0: Any?) {
                complete(p0)
            }

            override fun onError(p0: UiError?) {
                error(p0)
            }

        }
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:QQ登录回调
     */
    interface OnQqCallBackListener {

        fun onComplete(p0: Any?)

        fun onError(p0: UiError?)
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:微博登录后回调
     */
    fun setOnWbCallBackListener(
        complete: (p0: Oauth2AccessToken?) -> Unit,
        error: (p0: com.sina.weibo.sdk.common.UiError?) -> Unit
    ) {
        mOnWbCallBackListener = object : OnWbCallBackListener {
            override fun onComplete(p0: Oauth2AccessToken?) {
                complete(p0)
            }

            override fun onError(p0: com.sina.weibo.sdk.common.UiError?) {
                error(p0)
            }


        }
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:QQ登录回调
     */
    interface OnWbCallBackListener {

        fun onComplete(p0: Oauth2AccessToken?)

        fun onError(p0: com.sina.weibo.sdk.common.UiError?)
    }


    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:QQ登录回调
     */
    fun onQqActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        Tencent.onActivityResultData(requestCode, resultCode, data, mBaseUiListener)
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:微博登录回调
     */
    fun onWbLoginActivityResult(
        activity: Activity,
        requestCode: Int,
        resultCode: Int,
        data: Intent
    ) {
        if (mWBAPI != null) {
            mWBAPI!!.authorizeCallback(activity, requestCode, resultCode, data)
        }
    }

    /**
     * AUTHOR:AbnerMing
     * INTRODUCE:微博分享回调
     */
    fun onWbShareActivityResult(data: Intent, var2: WbShareCallback) {
        if (mWBAPI != null) {
            mWBAPI!!.doResultIntent(data, var2)
        }
    }

    private fun bmpToByteArray(bmp: Bitmap, needRecycle: Boolean): ByteArray? {
        val output = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output)
        if (needRecycle) {
            bmp.recycle()
        }
        val result = output.toByteArray()
        try {
            output.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }
}