package com.example.stringcanvas.domain.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.stringcanvas.MainActivity
import com.example.stringcanvas.R
import com.example.stringcanvas.data.cache.TempInstructionsCache
import com.example.stringcanvas.data.cache.TemporaryInstructionData
import com.example.stringcanvas.di.ServiceLocator
import com.example.stringcanvas.domain.models.Instruction2
import com.example.stringcanvas.utils.ACTION_GEN_DONE
import com.example.stringcanvas.utils.ACTION_GEN_IN_PROGRESS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Foreground-service, который делает тяжёлую генерацию
 * и уведомляет о ходе/результате через нотификацию и broadcast.
 */
@RequiresApi(Build.VERSION_CODES.O)
class InstructionGenerationService : LifecycleService() {

    /* ------------ DI ------------ */

    private val generateUseCase by lazy { ServiceLocator.appContainer.generationDeps.generate }

    /* ------------ notifications ------------ */

    private val channelId = "generation_channel"
    private val NOTIFICATION_ID = 1
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    /* ------------ coroutine scope ------------ */

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /* ------------ LifecycleService ------------ */

    override fun onCreate() {
        super.onCreate()
        createChannel()

        // стартуем “пустой” foreground — без интента
        startForeground(
            NOTIFICATION_ID,
            buildProgressNotification(genId = -1, indeterminate = true)
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        val cacheKey = i?.getLongExtra("cache_key", -1) ?: -1
        val srcBitmap = TempInstructionsCache.get(cacheKey)?.bitmap
        val nails     = i?.getIntExtra("nails", 0) ?: 0
        val threads   = i?.getIntExtra("threads", 0) ?: 0

        if (srcBitmap == null) {
            stopSelf(); return START_NOT_STICKY
        }

        // сразу же обновляем нотификацию, чтобы по тапу открывался LoadingScreen
        notificationManager.notify(
            NOTIFICATION_ID,
            buildProgressNotification(genId = cacheKey, indeterminate = true)
        )

        scope.launch {
            try {
                /* ------------- тяжёлая генерация ------------- */
                val (ins1, ins2) = generateUseCase.execute(srcBitmap, nails, threads)

                /* ------------- складываем результаты ------------- */
                val id1 = System.currentTimeMillis()
                val id2 = id1 + 1          // +1 мс, чтобы ключи не совпали

                TempInstructionsCache.put(id1, createTemp(id1, ins1, nails, threads))
                TempInstructionsCache.put(id2, createTemp(id2, ins2, nails, threads))

                /* ------------- финальная нотификация ------------- */
                showCompleteNotification(id1, id2, ok = true)

                /* ------------- broadcast в UI ------------- */
                sendDoneBroadcast(id1, id2)
            } catch (e: Exception) {
                // ошибка: показываем финальное уведомление с ошибочным текстом
                showCompleteNotification(-1, -1, ok = false)
            } finally {
                // снимаем foreground-флаг, но уведомление не убираем
                stopForeground(/* removeNotification = */ false)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    /* ============ helpers ============ */

    private fun createTemp(id: Long, instr: Instruction2, n: Int, t: Int) =
        TemporaryInstructionData(id, instr.instructionSteps, instr.resultBitmap, n, t)

    /* ----------  notification builders  ---------- */

    private fun buildProgressNotification(
        genId: Long,
        indeterminate: Boolean
    ): Notification =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_info_outline_24)
            .setContentTitle(getString(R.string.generation_in_progress_title))
            .setContentText(getString(R.string.generation_preparing))
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setProgress(100, 0, indeterminate)
            .apply {
                if (genId != -1L) {
                    setContentIntent(progressIntent(genId))
                }
            }
            .build()

    private fun showCompleteNotification(id1: Long, id2: Long, ok: Boolean) {
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baseline_info_outline_24)
            .setContentTitle(getString(R.string.generation_complete_title))
            .setContentText(
                if (ok) getString(R.string.generation_complete_ready)
                else    getString(R.string.generation_complete_error)
            )
            .apply {
                if (ok) setContentIntent(doneIntent(id1, id2))
            }
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notif)
    }

    /* ----------  pending intents ---------- */

    private fun progressIntent(genId: Long): PendingIntent =
        PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                action = ACTION_GEN_IN_PROGRESS
                putExtra(MainActivity.EXTRA_GEN_ID, genId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun doneIntent(id1: Long, id2: Long): PendingIntent =
        PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                action = ACTION_GEN_DONE
                putExtra(MainActivity.EXTRA_IDS, longArrayOf(id1, id2))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    /* ----------  broadcasts ---------- */

    private fun sendDoneBroadcast(id1: Long, id2: Long) {
        sendBroadcast(Intent(ACTION_GEN_DONE).apply {
            setPackage(packageName)   // только своему приложению
            putExtra("id1", id1)
            putExtra("id2", id2)
        })
    }

    /* ----------  channel ---------- */

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            notificationManager.getNotificationChannel(channelId) == null
        ) {
            val ch = NotificationChannel(
                channelId,
                getString(R.string.generation_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.generation_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(ch)
        }
    }
}



//
//import android.annotation.SuppressLint
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.core.app.NotificationCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LifecycleService
//import com.example.stringcanvas.MainActivity
//import com.example.stringcanvas.R
//import com.example.stringcanvas.data.cache.TempInstructionsCache
//import com.example.stringcanvas.data.cache.TemporaryInstructionData
//import com.example.stringcanvas.di.ServiceLocator
//import com.example.stringcanvas.domain.models.Instruction2
//import com.example.stringcanvas.utils.ACTION_GEN_DONE
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch
//
///**
// * Foreground-service, который запускает тяжёлую генерацию
// * и публикует прогресс/результат через уведомление и broadcast.
// *
// * Шаги интеграции:
// * 1) добавьте сервис в AndroidManifest и нужные permission’ы
// * 2) запускайте через ContextCompat.startForegroundService(...)
// * 3) перехватывайте broadcast ACTION_DONE, чтобы открыть экран выбора варианта
// */
//@RequiresApi(Build.VERSION_CODES.O)
//class InstructionGenerationService : LifecycleService() {
//
//    /* ----------  DI ---------- */
//
//    private val appContainer get() = ServiceLocator.appContainer
////    private val generateUseCase by lazy { appContainer.homeScreenUseCases.generate }
//    private val generateUseCase by lazy {
//        ServiceLocator.appContainer.generationDeps.generate
//    }
//
//
//    /* ----------  notifications ---------- */
//
//    private val channelId = "generation_channel"
//    private val NOTIFICATION_ID = 1
//    private val notificationManager by lazy {
//        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//    }
//
//    /* ----------  coroutine scope ---------- */
//
//    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
//
//    /* ----------  LifecycleService overrides ---------- */
//
//    override fun onCreate() {
//        super.onCreate()
//        createChannel()
//        // indeterminate прогресс-бар при старте
//        startForeground(
//            NOTIFICATION_ID,
//            buildNotification(indeterminate = true)
//        )
//    }
//
//    @SuppressLint("MissingSuperCall")
//    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
//        val cacheKey = i?.getLongExtra("cache_key", -1) ?: -1
//        val bitmap   = TempInstructionsCache.get(cacheKey)?.bitmap
//        val nails    = i?.getIntExtra("nails", 0) ?: 0
//        val threads  = i?.getIntExtra("threads", 0) ?: 0
//
//        if (bitmap == null) {          // ничего генерировать
//            stopSelf()
//            return START_NOT_STICKY
//        }
//
//        scope.launch {
//            try {
//                val (ins1, ins2) = generateUseCase.execute(bitmap, nails, threads)
//
//                val t1 = System.currentTimeMillis()
//                val t2 = t1 + 1
//                TempInstructionsCache.put(t1, createTemp(t1, ins1, nails, threads))
//                TempInstructionsCache.put(t2, createTemp(t2, ins2, nails, threads))
////                updateNotification(getString(R.string.generation_complete_ready), complete = true)
//                updateNotificationComplete(t1, t2, ok = true)
//                sendDoneBroadcast(t1, t2)
//
//
//                // уведомляем UI
//                sendBroadcast(Intent(ACTION_DONE).apply {
//                    putExtra("id1", t1)
//                    putExtra("id2", t2)
//                })
//            } catch (e: Exception) {
//                updateNotification(getString(R.string.generation_complete_error), complete = true)
//            } finally {
//                stopSelf()
//            }
//        }
//        return START_NOT_STICKY
//    }
//
//    /* ----------  helpers ---------- */
//
//    private fun createTemp(id: Long, instr: Instruction2, n: Int, t: Int) =
//        TemporaryInstructionData(id, instr.instructionSteps, instr.resultBitmap, n, t)
//
//    /* ---------------- notifications ---------------- */
//
//    /** Канал нужен начиная с Android O */
//    private fun createChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
//            notificationManager.getNotificationChannel(channelId) == null
//        ) {
//            val channel = NotificationChannel(
//                channelId,
//                getString(R.string.generation_channel_name),
//                NotificationManager.IMPORTANCE_LOW          // без звука
//            ).apply {
//                description = getString(R.string.generation_channel_description)
//                setShowBadge(false)
//            }
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    /**
//     * Строим уведомление: если indeterminate=true — отображается
//     * «крутилка», иначе можно задавать процент через progress.
//     */
//    private fun buildNotification(
//        progress: Int = 0,
//        indeterminate: Boolean = false,
//        text: String = getString(R.string.generation_preparing)  // "Подготавливаем данные…"
//    ): Notification {
//        val contentIntent = PendingIntent.getActivity(
//            this,
//            0,
//            Intent(this, MainActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            },
//            PendingIntent.FLAG_IMMUTABLE
//        )
//
//        return NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.baseline_info_outline_24)
//            .setContentTitle(getString(R.string.generation_in_progress_title))   // "Генерация идёт"
//            .setContentText(text)
//            .setOnlyAlertOnce(true)
//            .setOngoing(!indeterminate)
//            .setProgress(100, progress.coerceIn(0, 100), indeterminate)
//            .setContentIntent(contentIntent)
//            .build()
//    }
//
//    /**
//     * Обновляем уведомление: если complete=true — показываем финальный
//     * текст без прогресс-бара и позволяем свайпнуть.
//     */
//    private fun updateNotification(text: String, complete: Boolean = false) {
//        val notification = if (complete) {
//            NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(R.drawable.baseline_info_outline_24)
//                .setContentTitle(getString(R.string.generation_complete_title))      // "Генерация завершена"
//                .setContentText(text)                                                // "Готово" / "Ошибка генерации"
//                .setAutoCancel(true)
//                .build()
//        } else {
//            buildNotification(progress = 0, indeterminate = false, text = text)
//        }
//
//        notificationManager.notify(NOTIFICATION_ID, notification)
//    }
//
//    companion object {
//        const val ACTION_DONE = "generation_done"
//    }
//
//    private fun progressIntent(genId: Long): PendingIntent =
//        PendingIntent.getActivity(
//            this, 0,
//            Intent(this, MainActivity::class.java).apply {
//                action = MainActivity.ACTION_GEN_IN_PROGRESS    // ← константа
//                putExtra(MainActivity.EXTRA_GEN_ID, genId)
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            },
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//    private fun doneIntent(id1: Long, id2: Long): PendingIntent =
//        PendingIntent.getActivity(
//            this, 0,
//            Intent(this, MainActivity::class.java).apply {
//                action = MainActivity.ACTION_GEN_DONE
//                putExtra(MainActivity.EXTRA_IDS, longArrayOf(id1, id2))
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            },
//            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//    private fun sendDoneBroadcast(id1: Long, id2: Long) {
//        sendBroadcast(Intent(ACTION_GEN_DONE).apply {
//            // ограничиваем рассылку только своим пакетом (обходит ограничения имплицитных интентов)
//            setPackage(packageName)
//            putExtra("id1", id1)
//            putExtra("id2", id2)
//        })
//    }
//
//    private fun updateNotificationComplete(id1: Long, id2: Long, ok: Boolean) {
//        val completeIntent = doneIntent(id1, id2)   // → PendingIntent с ACTION_GEN_DONE
//        val notif = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.baseline_info_outline_24)
//            .setContentTitle(getString(R.string.generation_complete_title))
//            .setContentText(
//                if (ok) getString(R.string.generation_complete_ready)
//                else    getString(R.string.generation_complete_error)
//            )
//            .setContentIntent(completeIntent)   // ← откроем нужный экран
//            .setAutoCancel(true)
//            .build()
//
//        notificationManager.notify(NOTIFICATION_ID, notif)
//    }
//
//}
