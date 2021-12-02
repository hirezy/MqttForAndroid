package com.bnd.mqtt

interface MyMqtt {

    fun connect()

    fun disConnect()

    fun subscribe(topic: String, qos: Int = 1, listener: OnSubTopicListener? = null)

    fun subscribe(topicFilters: Array<String>, qos: IntArray, listener: OnSubTopicListener? = null)

    fun unsubscribe(topic: String, listener: OnSubTopicListener? = null)

    fun pubMessage(topic: String, payload: ByteArray, qos: Int = 1, retain: Boolean = false)


    fun addOnMsgListener(listener: OnMqttMsgListener?)

    fun addOnStatusChangeListener(listener: OnMqttStatusListener?)



}