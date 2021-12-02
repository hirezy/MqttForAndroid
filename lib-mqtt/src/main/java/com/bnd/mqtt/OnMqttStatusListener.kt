package com.bnd.mqtt

/***
 * 监听mqtt连接状态监听
 */
interface OnMqttStatusListener {

    fun onChange(state: MqttStatus, throwable: Throwable?)

}