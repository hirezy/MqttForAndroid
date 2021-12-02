package com.bnd.mqtt
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttException

/**
 *
 *
 * Implementation of the IMqttDeliveryToken interface for use from within the
 * MqttAndroidClient implementation
 */
internal class MqttDeliveryTokenAndroid(
    client: MqttAndroidClient?,
    userContext: Any?,
    listener: IMqttActionListener?, // The message which is being tracked by this token
    private var message: MqttMessage
) : MqttTokenAndroid(client, userContext, listener), IMqttDeliveryToken {
    /**
     * @see IMqttDeliveryToken.getMessage
     */
    @Throws(MqttException::class)
    override fun getMessage(): MqttMessage {
        return message
    }

    fun setMessage(message: MqttMessage) {
        this.message = message
    }

    fun notifyDelivery(delivered: MqttMessage) {
        message = delivered
        super.notifyComplete()
    }
}