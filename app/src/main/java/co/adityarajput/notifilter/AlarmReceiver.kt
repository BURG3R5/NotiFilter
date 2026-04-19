package co.adityarajput.notifilter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.adityarajput.notifilter.services.NotificationListener
import co.adityarajput.notifilter.utils.Logger

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Logger.d("AlarmReceiver", "Received intent with action: ${intent.action}")

        if (intent.action == Constants.ACTION_DISMISS_STALE) {
            val key = intent.getStringExtra(Constants.EXTRA_SBN_KEY) ?: return
            val isClearable = intent.getBooleanExtra(Constants.EXTRA_SBN_IS_CLEARABLE, false)

            NotificationListener.instance.dismissNotification(key, isClearable)
        }
    }
}
