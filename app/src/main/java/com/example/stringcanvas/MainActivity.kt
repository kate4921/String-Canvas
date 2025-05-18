package com.example.stringcanvas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.stringcanvas.di.LocalInstructionDao
import com.example.stringcanvas.di.ServiceLocator
import com.example.stringcanvas.presentation.navigation.Screen
import com.example.stringcanvas.presentation.screens.MainScreen
import com.example.stringcanvas.ui.theme.StringCanvasTheme
import com.example.stringcanvas.utils.ACTION_GEN_DONE
import com.example.stringcanvas.utils.ACTION_GEN_IN_PROGRESS
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_GEN_ID = "extra_gen_id"
        const val EXTRA_IDS    = "extra_ids"
    }

    private var keepSplashScreen = true
    private lateinit var navController: NavHostController

    /* --------------------------------------------------------------------- */
    /*                     Broadcast-receiver для GEN_DONE                    */
    /* --------------------------------------------------------------------- */
    private val genDoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {
            if (i?.action != ACTION_GEN_DONE) return

            // ① пробуем взять массив, если пришёл из PendingIntent финального уведомления
            val arr = i.getLongArrayExtra(EXTRA_IDS)

            // ② если массива нет, берём по отдельности (это как раз broadcast от сервиса)
            val id1 = i.getLongExtra("id1", -1)
            val id2 = i.getLongExtra("id2", -1)

            val ids = when {
                arr != null && arr.size >= 2        -> arr
                id1 != -1L && id2 != -1L            -> longArrayOf(id1, id2)
                else                                -> return    // защиты
            }

            if (::navController.isInitialized) {
                val current = navController.currentDestination?.route
                if (current == Screen.Home.route) return          // пусть навигирует Home

                navController.navigate(
                    Screen.InstructionVariant.createRoute(ids[0], ids[1])
                ) {
                    launchSingleTop = true
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
            } else {
                setIntent(i)                                  // обработается позже
            }
        }
    }



    /* --------------------------------------------------------------------- */
    /*                                lifecycle                              */
    /* --------------------------------------------------------------------- */

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        initOpenCV()

        /* ---------- новый код ---------- */
        val themeRepo = ServiceLocator.appContainer.themeRepository
        val prefetchedTheme = runBlocking { themeRepo.themeFlow.first() }
        /* -------------------------------- */

        setContent {
            navController = rememberNavController()

            LaunchedEffect(Unit) {
                handleIntent(intent)
                keepSplashScreen = false
            }

            val themeOption by themeRepo.themeFlow.collectAsState(initial = prefetchedTheme)

            StringCanvasTheme(option = themeOption) {
                CompositionLocalProvider(
                    LocalInstructionDao provides ServiceLocator.appContainer.instructionDao
                ) {
                    MainScreen(navController)
                }
            }
        }
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
    }

    /* Регистрируем / снимаем ресивер вместе с видимостью Activity */
    override fun onStart() {
        super.onStart()
        registerReceiver(
            genDoneReceiver,
            IntentFilter(ACTION_GEN_DONE),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Context.RECEIVER_NOT_EXPORTED
            else
                0
        )
    }

    override fun onStop() {
        unregisterReceiver(genDoneReceiver)
        super.onStop()
    }

    /* --------------------------------------------------------------------- */
    /*                           мульти-интенты                              */
    /* --------------------------------------------------------------------- */

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)                // обновляем Activity.intent
        if (::navController.isInitialized) {
            handleIntent(intent)         // обрабатываем новый интент
        }
    }

    /* --------------------------------------------------------------------- */
    /*                             Intent router                             */
    /* --------------------------------------------------------------------- */

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_GEN_IN_PROGRESS -> {
                navController.navigate(Screen.Loading.route) {
                    launchSingleTop = true
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
            }
            ACTION_GEN_DONE -> {
                val arr = intent.getLongArrayExtra(EXTRA_IDS)
                val ids = when {
                    arr != null && arr.size >= 2 -> arr
                    intent.hasExtra("id1") && intent.hasExtra("id2") ->
                        longArrayOf(intent.getLongExtra("id1", 0),
                            intent.getLongExtra("id2", 0))
                    else -> return
                }
                navController.navigate(
                    Screen.InstructionVariant.createRoute(ids[0], ids[1])
                ) {
                    launchSingleTop = true
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
            }

        }
    }

    /* --------------------------------------------------------------------- */
    /*                                 util                                  */
    /* --------------------------------------------------------------------- */

    private fun initOpenCV() {
        if (OpenCVLoader.initLocal()) {
            Log.d("OpenCV", "OpenCV successfully loaded!")
        } else {
            Log.e("OpenCV", "OpenCV load failed")
        }
    }
}