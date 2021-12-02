package com.bnd.mymqttforandroid
import android.app.Application
import android.content.Context
import com.bnd.mqtt.MqttHelper


class MainApp : Application() {
    //全局唯一的单一实例
    var mqttHelper: MqttHelper? = null
    var mContext:Context?=null
    override fun onCreate() {
        super.onCreate()
        mContext=this
        if (mInstance == null) {
            mInstance = this
        }
    }

    companion object {
        private var mInstance: MainApp? = null
        @JvmStatic
        val instance: MainApp
            get() = mInstance!!
    }
}