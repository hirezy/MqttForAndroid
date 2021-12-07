package com.bnd.mqtt

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import org.eclipse.paho.client.mqttv3.*
import kotlin.math.max

class MqttHelper private constructor(
    private val context: Context,
    private val mqttOptions: MqttOptions
) :
    MyMqtt {
    private var TAG = MqttHelper::class.java.name

    companion object {
        @Volatile
        private var mSingleMqttInstance: MqttHelper? = null
        fun getInstance(context: Context, mqttOptions: MqttOptions): MqttHelper? {
            if (mSingleMqttInstance == null) {
                synchronized(MqttHelper::class.java) {
                    if (mSingleMqttInstance == null) {
                        mSingleMqttInstance = MqttHelper(context, mqttOptions)
                    }
                }
            }
            return mSingleMqttInstance
        }
    }

    private var mState = MqttStatus.FAILURE
    private val mqttConnectOptions: MqttConnectOptions by lazy {
        val options = MqttConnectOptions()
        options.isAutomaticReconnect = true
        options.isCleanSession = mqttOptions.cleanSession // 清除缓存
        options.connectionTimeout = mqttOptions.connectTimeOut    // 超时时间会是设置的 2 倍
        options.keepAliveInterval = mqttOptions.keepAliveInterval //心跳包发送间隔，单位：秒
        options.userName = mqttOptions.username
        options.password = mqttOptions.password.toCharArray()
        options.mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
        options
    }

    private var mqttAndroidClient: MqttAndroidClient? = null

    private var reconnectHandler: Handler? = null
    private var reconnectRunner: ReconnectRunner? = null

    private var mMsgListener: OnMqttMsgListener? = null
    private var mStatusListener: OnMqttStatusListener? = null

    init {
        if (mqttOptions.willTopic.isNotEmpty() && mqttOptions.willTopic.isNotBlank()
            && !mqttOptions.willTopic.contains(MqttTopic.MULTI_LEVEL_WILDCARD)
            && !mqttOptions.willTopic.contains(MqttTopic.SINGLE_LEVEL_WILDCARD)
        ) {
            mqttConnectOptions.setWill(
                mqttOptions.willTopic,
                mqttOptions.willMsg.toByteArray(),
                mqttOptions.willQos,
                false
            )
        }
        mqttAndroidClient = MqttAndroidClient(
            context.applicationContext,
            mqttOptions.serviceUrl,
            mqttOptions.clientId
        )
        mqttAndroidClient!!.setCallback(object : MqttCallbackExtended {
            var lastMessage: MqttMessage? = null
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                if (reconnect) {
                    subscribeToService()
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                if (!topic.isNullOrEmpty() && message != null && message != lastMessage && message.payload != null) {
                    lastMessage = message
                    mMsgListener?.onSubMessage(topic, message.payload)
                }
            }

            override fun connectionLost(cause: Throwable?) {
                changeState(MqttStatus.LOST, cause)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                val payload = token?.message?.payload
                if (payload != null) {
                    mMsgListener?.onPubMessage(payload)
                }
            }
        })
        reconnectHandler = Handler(Looper.getMainLooper())
        connect()
    }

    override fun connect() {
        try {
            if (mqttAndroidClient?.isConnected == true) {
                reconnectHandler?.removeCallbacksAndMessages(null)
                if (mState != MqttStatus.SUCCESS) {
                    changeState(MqttStatus.SUCCESS, null)
                }
            } else {
                mqttAndroidClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
                    override fun onSuccess(token: IMqttToken) {
                        val disconnectedBufferOptions = DisconnectedBufferOptions()
                        disconnectedBufferOptions.isBufferEnabled = true
                        disconnectedBufferOptions.bufferSize = 100
                        disconnectedBufferOptions.isPersistBuffer = false
                        disconnectedBufferOptions.isDeleteOldestMessages = false
                        mqttAndroidClient?.setBufferOpts(disconnectedBufferOptions)
                        subscribeToService()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        changeState(MqttStatus.FAILURE, exception)
                    }
                })
            }
        } catch (e: MqttException) {
            changeState(MqttStatus.FAILURE, e)
        }
    }


    private fun reconnect() {
        connect()
    }

    override fun disConnect() {
        mMsgListener = null
        mStatusListener = null
        reconnectHandler?.removeCallbacksAndMessages(null)
        if (mqttAndroidClient != null) {
            if (mqttAndroidClient?.isConnected == true) {
                mqttAndroidClient?.disconnect()
            }
            mqttAndroidClient?.setCallback(null)
            mqttAndroidClient?.unregisterResources()
            mqttAndroidClient = null
        }
    }


    private fun subscribeToService() {
        try {

            if (mqttOptions!=null){
                val data=mqttOptions.topics
                //更具topics构造qos优先级数组
                data?.let {
                    val intArray = IntArray(data.size)
                    data.forEachIndexed { index, s ->
                        intArray[index] = 1
                    }

                    mqttAndroidClient?.subscribe(
                        data.toTypedArray(),
                        intArray,
                        null,
                        object : IMqttActionListener {
                            override fun onSuccess(token: IMqttToken?) {
                                val topics = token!!.topics
                                Log.i(TAG, "connect onSuccess:${topics.size}")
                                topics?.let { topic->
                                    topic.forEach {
                                        Log.i(TAG, "connect onSuccess topic is:${it}")
                                    }
                                }
                                changeState(MqttStatus.SUCCESS, null)
                            }

                            override fun onFailure(token: IMqttToken?, exception: Throwable?) {
                                changeState(MqttStatus.FAILURE, exception)
                            }
                        })
                }
            }
        } catch (e: Exception) {
            changeState(MqttStatus.FAILURE, e)
        }
    }

    override fun subscribe(topic: String, qos: Int, listener: OnSubTopicListener?) {
        try {
            mqttAndroidClient?.subscribe(topic, qos, null, listener)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    override fun subscribe(
        topics: Array<String>,
        qos: IntArray,
        listener: OnSubTopicListener?
    ) {
        try {
            mqttAndroidClient?.subscribe(topics, qos, null, listener)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


    override fun unsubscribe(topic: String, listener: OnSubTopicListener?) {
        try {
            mqttAndroidClient?.unsubscribe(topic, null, listener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun pubMessage(topics: String, payload: ByteArray, qos: Int, retain: Boolean) {
        try {
            mqttAndroidClient?.publish(topics, payload, qos, retain)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }


    override fun addOnMsgListener(listener: OnMqttMsgListener?) {
        mMsgListener = listener
    }

    override fun addOnStatusListener(listener: OnMqttStatusListener?) {
        mStatusListener = listener
    }

    private fun changeState(state: MqttStatus, throwable: Throwable?) {
        mState = state
        mStatusListener?.onChange(state, throwable)
        if (mState == MqttStatus.LOST) {
            connect()
        }
        reconnectHandler?.removeCallbacksAndMessages(null)
        if (mState != MqttStatus.SUCCESS) {
            if (reconnectRunner == null) {
                reconnectRunner = ReconnectRunner(context.applicationContext)
            }
            reconnectHandler?.postDelayed(
                reconnectRunner!!,
                max(0, mqttOptions.reconnectInterval)
            )
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val info = cm?.activeNetworkInfo
        return info != null && info.isConnected && info.state == NetworkInfo.State.CONNECTED
    }

    private inner class ReconnectRunner(private val context: Context) : Runnable {
        override fun run() {
            if (isNetworkAvailable(context)) {
                reconnect()
            } else {
                reconnectHandler?.postDelayed(this, max(0, mqttOptions.reconnectInterval))
            }
        }
    }

}