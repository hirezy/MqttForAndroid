package com.bnd.mqtt

import org.eclipse.paho.client.mqttv3.MqttMessage
import android.os.Parcelable
import android.os.Parcel
import android.os.Parcelable.Creator

/**
 *
 *
 * A way to flow MqttMessages via Bundles/Intents
 *
 *
 *
 *
 * An application will probably use this only when receiving a message from a
 * Service in a Bundle - the necessary code will be something like this :-
 *
 * <pre>
 * `
 * private void messageArrivedAction(Bundle data) {
 * ParcelableMqttMessage message = (ParcelableMqttMessage) data
 * .getParcelable(MqttServiceConstants.CALLBACK_MESSAGE_PARCEL);
 * *Use the normal [MqttMessage] methods on the the message object.*
 * }
 *
` *
</pre> *
 *
 *
 *
 * It is unlikely that an application will directly use the methods which are
 * specific to this class.
 *
 */
class ParcelableMqttMessage : MqttMessage, Parcelable {
    /**
     * @return the messageId
     */
    @JvmField
    var messageId: String? = null

    internal constructor(original: MqttMessage) : super(original.payload) {
        qos = original.qos
        isRetained = original.isRetained
        isDuplicate = original.isDuplicate
    }

    internal constructor(parcel: Parcel) : super(parcel.createByteArray()) {
        qos = parcel.readInt()
        val flags = parcel.createBooleanArray()
        isRetained = flags!![0]
        isDuplicate = flags[1]
        messageId = parcel.readString()
    }

    /**
     * Describes the contents of this object
     */
    override fun describeContents(): Int {
        return 0
    }

    /**
     * Writes the contents of this object to a parcel
     *
     * @param parcel
     * The parcel to write the data to.
     * @param flags
     * this parameter is ignored
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(payload)
        parcel.writeInt(qos)
        parcel.writeBooleanArray(booleanArrayOf(isRetained, isDuplicate))
        parcel.writeString(messageId)
    }

    companion object {

        object CREATOR : Creator<ParcelableMqttMessage> {
            override fun createFromParcel(parcel: Parcel): ParcelableMqttMessage {
                return ParcelableMqttMessage(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableMqttMessage?> {
                return arrayOfNulls(size)
            }
        }

//        /**
//         * A creator which creates the message object from a parcel
//         */
//        val CREATOR: Creator<ParcelableMqttMessage?> = object : Creator<ParcelableMqttMessage?> {
//            /**
//             * Creates a message from the parcel object
//             */
//            override fun createFromParcel(parcel: Parcel): ParcelableMqttMessage? {
//                return ParcelableMqttMessage(parcel)
//            }
//
//            /**
//             * creates an array of type [ParcelableMqttMessage][]
//             *
//             */
//            override fun newArray(size: Int): Array<ParcelableMqttMessage?> {
//                return arrayOfNulls(size)
//            }
//        }
    }
}