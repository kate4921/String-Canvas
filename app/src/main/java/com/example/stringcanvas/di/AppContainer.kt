package com.example.stringcanvas.di

import android.content.Context
import android.graphics.BitmapFactory
import androidx.room.Room
import com.example.stringcanvas.R
import com.example.stringcanvas.data.database.AppDatabase
import com.example.stringcanvas.data.database.InstructionDao
import com.example.stringcanvas.data.database.InstructionEntity
import com.example.stringcanvas.data.datastore.SpeechSettingsRepositoryImpl
import com.example.stringcanvas.data.datastore.ThemeRepositoryImpl
import com.example.stringcanvas.data.datastore.settingsDataStore
import com.example.stringcanvas.data.repository.ImageRepositoryImpl
import com.example.stringcanvas.data.repository.InstructionRepositoryImpl
import com.example.stringcanvas.data.repository.PdfRepositoryImpl
import com.example.stringcanvas.data.speech.AndroidSpeechSynthesizer
import com.example.stringcanvas.domain.repository.ImageRepository
import com.example.stringcanvas.domain.repository.InstructionRepository
import com.example.stringcanvas.domain.repository.PdfRepository
import com.example.stringcanvas.domain.repository.SpeechSettingsRepository
import com.example.stringcanvas.domain.repository.ThemeRepository
import com.example.stringcanvas.domain.service.InstructionGenerator
import com.example.stringcanvas.domain.service.PdfGenerator
import com.example.stringcanvas.domain.service.PdfNotificationHelper
import com.example.stringcanvas.domain.speech.SpeechSynthesizer
import com.example.stringcanvas.domain.usecase.ClearInstructionsUseCase
import com.example.stringcanvas.domain.usecase.DecodeCroppedImageUseCase
import com.example.stringcanvas.domain.usecase.DeleteInstructionByIdUseCase
import com.example.stringcanvas.domain.usecase.GenerateAndNotifyInstructionPdfUseCase
import com.example.stringcanvas.domain.usecase.GenerateInstructionsUseCase
import com.example.stringcanvas.domain.usecase.GetInstructionByIdUseCase
import com.example.stringcanvas.domain.usecase.GetSavedInstructionsUseCase
import com.example.stringcanvas.domain.usecase.GetSpeechSettingsFlow
import com.example.stringcanvas.domain.usecase.GetThemeFlow
import com.example.stringcanvas.domain.usecase.ImageUseCase
import com.example.stringcanvas.domain.usecase.PlayInstructionUseCase
import com.example.stringcanvas.domain.usecase.RenameInstructionUseCase
import com.example.stringcanvas.domain.usecase.SaveBitmapToUriUseCase
import com.example.stringcanvas.domain.usecase.SaveInstructionUseCase
import com.example.stringcanvas.domain.usecase.SetPauseBetweenSteps
import com.example.stringcanvas.domain.usecase.SetSpeechRate
import com.example.stringcanvas.domain.usecase.SetTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


class AppContainer(appCtx: Context) {

    /* -------------------- CoroutineScope для подписок -------------------- */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /* -------------------- DataStore -------------------- */
    private val themeRepo: ThemeRepository =
        ThemeRepositoryImpl(appCtx.settingsDataStore)

    /**  ← добавляем публичный геттер  */
    val themeRepository: ThemeRepository
        get() = themeRepo

    private val speechRepo: SpeechSettingsRepository =
        SpeechSettingsRepositoryImpl(appCtx.settingsDataStore)

    /* -------------------- TTS и PlayInstruction -------------------- */
    private val speechSynth: SpeechSynthesizer by lazy {
        AndroidSpeechSynthesizer(appCtx).apply {
            onError = { msg -> ttsEvents.tryEmit(msg) }
        }
    }

    private val playInstructionUseCase = PlayInstructionUseCase(speechSynth).also { player ->
        // подписка на изменения настроек речи
        scope.launch {
            speechRepo.settingsFlow.collect { (rate, pause) ->
                player.setRate(rate)
                player.setPause(pause)
            }
        }
    }

    /* поток ошибок TTS */
    private val ttsEvents = MutableSharedFlow<String>(replay = 1)

    /* -------------------- Room -------------------- */
    private val db = Room.databaseBuilder(
        appCtx, AppDatabase::class.java, "string-canvas-db"
    ).build()
    val instructionDao: InstructionDao = db.instructionDao()

    /* -------------------- прочие репозитории -------------------- */
    private val imageRepo: ImageRepository   = ImageRepositoryImpl(appCtx)
    private val instrRepo: InstructionRepository =
        InstructionRepositoryImpl(instructionDao, imageRepo)

    private val pdfRepo : PdfRepository   = PdfRepositoryImpl(PdfGenerator(appCtx))
    private val pdfNotifier               = PdfNotificationHelper(appCtx)


    // AppContainer.kt  (после val instructionDao = db.instructionDao())
    init {
        scope.launch {
            if (instructionDao.getSavedInstructions().isEmpty()) {

                /* 1) Берём Bitmap из ресурсов */
                val bmp = BitmapFactory.decodeResource(
                    appCtx.resources,
                    R.drawable.demo_preview      // имя созданного файла
                )

                /* 2) Сохраняем через репозиторий картинок */
                //  ─ метод suspend, поэтому мы уже в корутине
                val demoUri = imageRepo.saveBitmapToUri(bmp, "demo_preview")

                /* 3) Создаём запись в базе */
                val demo = InstructionEntity(
                    name        = "Демо-инструкция",
                    textList    = listOf(
                        "1",
                        "50",
                        "51",
                        "1",
                        "100",
                        "...",
                    ),
                    nailsCount  = 300,
                    linesCount  = 1000,
                    imageUriString = demoUri.toString()   // ← то, что спрашивали
                )

                instructionDao.insertInstruction(demo)
            }
        }
    }


    /* -------------------- Use-cases, сгруппованные для экранов -------------------- */

    /** Инструкция */
    val instructionScreenDeps = InstructionScreenDeps(
        useCases = InstructionUseCases(
            get           = GetInstructionByIdUseCase(instrRepo),
            save          = SaveInstructionUseCase(instrRepo),
            delete        = DeleteInstructionByIdUseCase(instrRepo),
            playInstruction = playInstructionUseCase,
            workWImage    = ImageUseCase(imageRepo),
            renameInstruction = RenameInstructionUseCase(instrRepo),
            downloadPdf   = GenerateAndNotifyInstructionPdfUseCase(pdfRepo, pdfNotifier),
            getSpeechSettings = GetSpeechSettingsFlow(speechRepo)   // ← добавили
        ),
        ttsEvents = ttsEvents.asSharedFlow()
    )


    /** Сохранённые инструкции */
    val savedInstructionsUseCases = SavedInstructionsUseCases(
        get        = GetSavedInstructionsUseCase(instrRepo),
        delete     = DeleteInstructionByIdUseCase(instrRepo),
        getPdf     = GenerateAndNotifyInstructionPdfUseCase(pdfRepo, pdfNotifier),
        updateName = RenameInstructionUseCase(instrRepo)
    )

    /** Домашний экран */
    // ───── Домашний экран ─────
    val homeScreenDeps = HomeScreenDeps(
        decode = DecodeCroppedImageUseCase()
    )

    // ───── для Foreground-Service ─────
    val generationDeps = GenerationDeps(
        generate = GenerateInstructionsUseCase(InstructionGenerator())
    )

    /** Настройки  */
    val settingsScreenUseCases = SettingsScreenUseCases(
        getTheme  = GetThemeFlow(themeRepo),
        setTheme  = SetTheme(themeRepo),
        getSpeech = GetSpeechSettingsFlow(speechRepo),
        setRate   = SetSpeechRate(speechRepo),
        setPause  = SetPauseBetweenSteps(speechRepo)
    )

    val instructionVariantUseCases = InstructionVariantUseCases(
        clear = ClearInstructionsUseCase(),
        save = SaveInstructionUseCase(instrRepo),
        toUri = SaveBitmapToUriUseCase(imageRepo)
    )

    /* -------------------- Очистка ресурсов -------------------- */
    fun shutdown() {
        scope.cancel()
        speechSynth.shutdown()
    }
}


// Пакет use-case'ов
data class InstructionVariantUseCases(
    val clear: ClearInstructionsUseCase,
    val save: SaveInstructionUseCase,
    val toUri: SaveBitmapToUriUseCase
)

data class InstructionUseCases(
    val get: GetInstructionByIdUseCase,
    val save: SaveInstructionUseCase,
    val delete: DeleteInstructionByIdUseCase,
    val playInstruction: PlayInstructionUseCase,
    val workWImage: ImageUseCase,
    val renameInstruction: RenameInstructionUseCase,
    val downloadPdf: GenerateAndNotifyInstructionPdfUseCase,
    /* новое: только читать настройки речи */
    val getSpeechSettings: GetSpeechSettingsFlow
)


data class InstructionScreenDeps(
    val useCases: InstructionUseCases,
    val ttsEvents: SharedFlow<String>
)

data class SavedInstructionsUseCases(
    val get: GetSavedInstructionsUseCase,
    val delete: DeleteInstructionByIdUseCase,
    val getPdf: GenerateAndNotifyInstructionPdfUseCase,
    val updateName: RenameInstructionUseCase
)

data class HomeScreenDeps(
    val decode: DecodeCroppedImageUseCase
)

data class GenerationDeps(
    val generate: GenerateInstructionsUseCase
)

data class SettingsScreenUseCases(
    val getTheme : GetThemeFlow,
    val setTheme : SetTheme,
    val getSpeech: GetSpeechSettingsFlow,
    val setRate  : SetSpeechRate,
    val setPause : SetPauseBetweenSteps
)