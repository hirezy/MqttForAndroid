package com.bnd.mymqttforandroid
import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bnd.mqtt.*
import kotlinx.android.synthetic.main.activity_main.*


@SuppressLint("HardwareIds")
class MainActivity : AppCompatActivity(),OnMqttMsgListener, OnMqttStatusListener {
    private var TAG= MainActivity::class.java.name

    private var TOPIC = "deTy/1/ctrl"
    private companion object {
        private const val TOPIC = "ChatTopic"
    }

    private lateinit var mqttHelper: MyMqtt

    private val mAdapter: MessageAdapter by lazy {
        MessageAdapter()
    }

    private val mAndroidId: String by lazy {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtTitle.text = "通信中..."
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter

        val options = MqttOptions(
            serviceUrl = "tcp://192.168.0.106:61613",
            username = "admin",
            password = "password",
            clientId = "android-${System.currentTimeMillis()}",
            willTopic = "will/android",
            willMsg = "I'm Died - $mAndroidId"
        )
        mqttHelper = MqttHelper.getInstance(MainApp.instance.mContext!!, options)!!
        mqttHelper.addOnMsgListener(this)
        mqttHelper.addOnStatusListener(this)
        mqttHelper.connect()

        btnSend.setOnClickListener {
            val content: String? = editContent.text?.toString()
            if (content.isNullOrEmpty()) {
                Toast.makeText(this, "请输入要发送的内容", Toast.LENGTH_SHORT).show()
            } else {
                mqttHelper.pubMessage("$TOPIC/$mAndroidId", content.toByteArray())
                editContent.setText("")
            }
        }
    }


    override fun onSubMessage(topic: String, payload: ByteArray) {
        if (topic != "$TOPIC/$mAndroidId") {
            mAdapter.addMessage(String(payload))
            recyclerView?.smoothScrollToPosition(mAdapter.itemCount - 1)
        }
    }

    override fun onPubMessage(payload: ByteArray) {
        mAdapter.addMessage(String(payload), true)
        recyclerView?.smoothScrollToPosition(mAdapter.itemCount - 1)
    }

    /**
     * MQTT 连接状态改变:
     *  [MqttStatus.SUCCESS]    // 连接成功
     *  [MqttStatus.FAILURE]    // 连接失败
     *  [MqttStatus.LOST]   // 连接中断
     */
    @SuppressLint("SetTextI18n")
    override fun onChange(state: MqttStatus, throwable: Throwable?) {
        btnSend.isEnabled = state == MqttStatus.SUCCESS
        editContent.isEnabled = state == MqttStatus.SUCCESS
        if (state == MqttStatus.SUCCESS) {
            mqttHelper.subscribe("$TOPIC/+")
            txtTitle?.text = TOPIC
        } else {
            txtTitle?.text = state.name
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttHelper.disConnect()
    }

}