package com.pierre.tunescout.feature.audiosearch.di

import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.pierre.tunescout.core.audiosearch.AudioSearchAvailability
import com.pierre.tunescout.core.audiosearch.AudioSearchQueryPublisher
import com.pierre.tunescout.core.audiosearch.ObservableAudioSearchQueries
import com.pierre.tunescout.feature.audiosearch.data.datasource.ListeningIntentFactory
import com.pierre.tunescout.feature.audiosearch.data.datasource.SpeechRecognizerFactory
import com.pierre.tunescout.feature.audiosearch.data.query.AudioSearchQueryBus
import com.pierre.tunescout.feature.audiosearch.data.repository.SpeechRecognitionRepositoryImpl
import com.pierre.tunescout.feature.audiosearch.domain.repository.SpeechRecognitionRepository
import com.pierre.tunescout.feature.audiosearch.domain.usecase.ListenToSpeech
import com.pierre.tunescout.feature.audiosearch.domain.usecase.impl.ListenToSpeechUseCase
import com.pierre.tunescout.feature.audiosearch.presentation.viewmodel.AudioSearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

private const val MAX_RESULTS = 1

val audioSearchModule: Module = module {
    factory<AudioSearchAvailability> {
        val context = androidContext()
        AudioSearchAvailability { SpeechRecognizer.isRecognitionAvailable(context) }
    }
    factory<SpeechRecognizerFactory> {
        val context = androidContext()
        SpeechRecognizerFactory { SpeechRecognizer.createSpeechRecognizer(context) }
    }
    factory<ListeningIntentFactory> { ListeningIntentFactory(::createListeningIntent) }
    singleOf(::AudioSearchQueryBus)
    single<ObservableAudioSearchQueries> { get<AudioSearchQueryBus>() }
    single<AudioSearchQueryPublisher> { get<AudioSearchQueryBus>() }
    factoryOf(::SpeechRecognitionRepositoryImpl).bind<SpeechRecognitionRepository>()
    factoryOf(::ListenToSpeechUseCase).bind<ListenToSpeech>()
    viewModelOf(::AudioSearchViewModel)
}

/**
 * Free-form dictation rather than web search, because a song title is not a question; partial
 * results, because the sheet shows the words as they arrive; and no language, so the recognizer
 * listens in the one the device is set to.
 *
 * @return the intent every listening session starts with.
 */
private fun createListeningIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    .putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_RESULTS)
