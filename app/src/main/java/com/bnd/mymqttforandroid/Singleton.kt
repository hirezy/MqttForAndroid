package com.bnd.mymqttforandroid

import android.content.Context
import kotlin.jvm.Volatile

class Singleton private constructor(context: Context) {

    companion object {
        @Volatile
        private var mSingleInstance: Singleton? = null
        fun getInstance(context: Context): Singleton? {
            if (mSingleInstance == null) {
                synchronized(Singleton::class.java) {
                    if (mSingleInstance == null) {
                        mSingleInstance = Singleton(context)
                    }
                }
            }
            return mSingleInstance
        }
    }
}