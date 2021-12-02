package com.bnd.mqtt

import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 *
 *
 * Mechanism for persisting messages until we know they have been received
 *
 *
 *  * A Service should store messages as they arrive via
 * [.storeArrived].
 *  * When a message has been passed to the consuming entity,
 * [.discardArrived] should be called.
 *  * To recover messages which have not been definitely passed to the
 * consumer, [MessageStore.getAllArrivedMessages] is used.
 *  * When a clean session is started [.clearArrivedMessages] is
 * used.
 *
 */
interface MessageStore {
    /**
     * External representation of a stored message
     */
    interface StoredMessage {
        /**
         * @return the identifier for the message within the store
         */
        val messageId: String?

        /**
         * @return the identifier of the client which stored this message
         */
        val clientHandle: String?

        /**
         * @return the topic on which the message was received
         */
        val topic: String?

        /**
         * @return the identifier of the client which stored this message
         */
        val message: MqttMessage?
    }

    /**
     * Store a message and return an identifier for it
     *
     * @param clientHandle
     * identifier for the client
     * @param message
     * message to be stored
     * @return a unique identifier for it
     */
    fun storeArrived(
        clientHandle: String?, Topic: String?,
        message: MqttMessage?
    ): String?

    /**
     * Discard a message - called when we are certain that an arrived message
     * has reached the application.
     *
     * @param clientHandle
     * identifier for the client
     * @param id
     * id of message to be discarded
     */
    fun discardArrived(clientHandle: String?, id: String?): Boolean

    /**
     * Get all the stored messages, usually for a specific client
     *
     * @param clientHandle
     * identifier for the client - if null, then messages for all
     * clients are returned
     */
    fun getAllArrivedMessages(clientHandle: String?): Iterator<StoredMessage?>?

    /**
     * Discard stored messages, usually for a specific client
     *
     * @param clientHandle
     * identifier for the client - if null, then messages for all
     * clients are discarded
     */
    fun clearArrivedMessages(clientHandle: String?)
    fun close()
}