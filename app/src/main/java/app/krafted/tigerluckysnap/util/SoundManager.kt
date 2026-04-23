package app.krafted.tigerluckysnap.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import app.krafted.tigerluckysnap.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private var correctSoundId: Int = 0
    private var wrongSoundId: Int = 0

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        correctSoundId = soundPool.load(context, R.raw.correct_sound, 1)
        wrongSoundId = soundPool.load(context, R.raw.wrong_sound, 1)
    }

    fun playCorrectSound() {
        soundPool.play(correctSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playWrongSound() {
        soundPool.play(wrongSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}
