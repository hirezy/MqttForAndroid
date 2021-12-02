package com.bnd.mqtt

/**
 * MQTT 收发消息监听
 */
interface OnMqttMsgListener {

    /**
     * 订阅的 MQTT 消息
     */
    fun onSubMessage(topic: String, payload: ByteArray)

    /**
     * 发布的 MQTT 消息
     */
    fun onPubMessage(payload: ByteArray)

}