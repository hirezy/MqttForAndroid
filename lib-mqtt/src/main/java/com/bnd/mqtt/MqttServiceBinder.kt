package com.bnd.mqtt

import android.os.Binder

/**
 * What the Service passes to the Activity on binding:-
 *
 *  * a reference to the Service
 *  * the activityToken provided when the Service was started
 *
 *
 */
internal class MqttServiceBinder(
    /**
     * @return a reference to the Service
     */
    val service: MqttService
) : Binder() {

    /**
     * @return the activityToken provided when the Service was started
     */
    var activityToken: String? = null

}