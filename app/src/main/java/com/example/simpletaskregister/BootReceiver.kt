package lucianowlp.com.simplestodo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val (hour, minute) = ReminderScheduler.getReminderTime(context)
            ReminderScheduler.scheduleDailyReminder(context, hour, minute)
        }
    }
}
