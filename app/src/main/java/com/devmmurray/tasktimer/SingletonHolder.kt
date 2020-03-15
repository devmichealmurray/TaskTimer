package com.devmmurray.tasktimer

import android.util.Log

/**
 * Double-Checked Locking Algorithm written by Christophe Beyls
 * https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e
 */

private const val TAG = "SingletonHolder"

open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        Log.d(TAG, "getInstance Starts")
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}