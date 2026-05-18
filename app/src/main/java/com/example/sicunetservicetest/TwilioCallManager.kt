package com.example.sicunetservicetest

import android.content.Context
import android.util.Log
import com.twilio.voice.*

object TwilioCallManager {

    private var activeCall: Call? = null

    // ── 1. Make a call ────────────────────────────────────────
    fun call(
        context: Context,
        accessToken: String,       // from your backend
        to: String,                // E.164 number or client identity
        onRinging: () -> Unit = {},
        onConnected: () -> Unit = {},
        onDisconnected: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val params = mapOf("To" to to)

        val options = ConnectOptions.Builder(accessToken)
            .params(params)
            .build()

        activeCall = Voice.connect(context, options, object : Call.Listener {

            override fun onRinging(call: Call) {
                Log.d("Twilio", "Ringing")
                onRinging()
            }

            override fun onConnected(call: Call) {
                Log.d("Twilio", "Connected")
                onConnected()
            }

            override fun onConnectFailure(call: Call, error: CallException) {
                Log.e("Twilio", "Connect failed: ${error.message}")
                onError(error.message ?: "Connect failed")
                activeCall = null
            }

            override fun onDisconnected(call: Call, error: CallException?) {
                Log.d("Twilio", "Disconnected")
                if (error != null) onError(error.message ?: "Disconnected with error")
                else onDisconnected()
                activeCall = null
            }

            override fun onReconnecting(call: Call, error: CallException) {
                Log.d("Twilio", "Reconnecting…")
            }

            override fun onReconnected(call: Call) {
                Log.d("Twilio", "Reconnected")
            }

            override fun onCallQualityWarningsChanged(
                call: Call,
                currentWarnings: MutableSet<Call.CallQualityWarning>,
                previousWarnings: MutableSet<Call.CallQualityWarning>
            ) {}
        })
    }

    // ── 2. Hang up ────────────────────────────────────────────
    fun hangUp() {
        activeCall?.disconnect()
        activeCall = null
    }

    // ── 3. Mute / unmute ──────────────────────────────────────
    fun mute(muted: Boolean) {
        activeCall?.mute(muted)
    }

    // ── 4. Hold / unhold ──────────────────────────────────────
    fun hold(onHold: Boolean) {
        activeCall?.hold(onHold)
    }

    // ── 5. Send DTMF digits (keypad tones) ───────────────────
    fun sendDigits(digits: String) {
        activeCall?.sendDigits(digits)
    }

    // ── Helpers ───────────────────────────────────────────────
    val isOnCall: Boolean get() = activeCall != null
    val callState: Call.State? get() = activeCall?.state
}