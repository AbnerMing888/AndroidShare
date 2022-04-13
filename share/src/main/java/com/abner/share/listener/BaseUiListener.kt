package com.abner.share.listener

import com.tencent.tauth.IUiListener
import com.tencent.tauth.UiError

/**
 *AUTHOR:AbnerMing
 *DATE:2021/11/24
 *INTRODUCE:
 */
open class BaseUiListener : IUiListener {
    override fun onComplete(p0: Any?) {
        qqComplete(p0)
    }

    fun qqComplete(p0: Any?) {}

    fun qqError(p0: UiError?) {}

    override fun onError(p0: UiError?) {
        qqError(p0)
    }

    override fun onCancel() {

    }

    override fun onWarning(p0: Int) {

    }
}