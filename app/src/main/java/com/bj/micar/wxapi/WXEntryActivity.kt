package com.bj.micar.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.bj.micar.BuildConfig
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory


/**
 * 微信回调
 * */
class WXEntryActivity : Activity(), IWXAPIEventHandler {

    private var mIWXAPI: IWXAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mIWXAPI = WXAPIFactory.createWXAPI(this, BuildConfig.WX_ID)
        mIWXAPI!!.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        mIWXAPI!!.handleIntent(intent, this)
    }

    override fun onReq(resp: BaseReq?) {

    }

    override fun onResp(resp: BaseResp?) {
        when (resp!!.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                if (resp.type == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
                    //分享

                    finish()
                } else if (resp.type == ConstantsAPI.COMMAND_SENDAUTH) { //登陆
                    val code = (resp as SendAuth.Resp).code
                    Toast.makeText(this, "微信登录成功", Toast.LENGTH_LONG).show()

                    //根据code，和服务端进行绑定，获取信息后执行后续操作

                    finish()
                }
            }
            else -> {
                finish()
            }
        }
    }

}