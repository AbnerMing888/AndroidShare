package com.abner.share.net

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 *AUTHOR:AbnerMing
 *DATE:2021/11/25
 *INTRODUCE:暂时使用HttpURLConnection
 */
class NetUtils private constructor() {

    private var mOnHttpCallBackListener: OnHttpCallBackListener? = null

    companion object {
        private var instance: NetUtils? = null
            get() {
                if (field == null) {
                    field = NetUtils()
                }
                return field
            }

        fun get(): NetUtils {
            return instance!!
        }
    }

    fun get(url: String): NetUtils {
        thread {
            val urlPath = URL(url)
            val connection = urlPath.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            val responseCode = connection.responseCode
            val obtain = Message.obtain()
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //成功
                val inputStream = connection.inputStream
                val json = streamToString(inputStream)
                obtain.obj = json
                obtain.what = HttpURLConnection.HTTP_OK
            } else {
                obtain.what = HttpURLConnection.HTTP_NOT_FOUND
            }
            mHandler.sendMessage(obtain)
        }
        return this
    }


    private val mHandler = Handler(Looper.getMainLooper()) { p0 ->
        if (p0.what == HttpURLConnection.HTTP_OK) {
            //成功
            val obj = p0.obj as String
            if (mOnHttpCallBackListener != null) {
                mOnHttpCallBackListener!!.onSuccess(obj)
            }
        } else {
            //失败
            if (mOnHttpCallBackListener != null) {
                mOnHttpCallBackListener!!.onError()
            }
        }
        false
    }

    fun setOnHttpCallBackListener(
        success: (json: String) -> Unit,
        error: () -> Unit
    ) {
        mOnHttpCallBackListener = object : OnHttpCallBackListener {
            override fun onSuccess(json: String) {
                success(json)
            }

            override fun onError() {
                error()
            }


        }
    }

    interface OnHttpCallBackListener {

        fun onSuccess(json: String)

        fun onError()
    }

    @Throws(Exception::class)
    private fun streamToString(inputStream: InputStream): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bytes = ByteArray(1024)
        var len = -1
        while (inputStream.read(bytes).also { len = it } != -1) {
            byteArrayOutputStream.write(bytes, 0, len)
        }
        return String(byteArrayOutputStream.toByteArray())
    }
}