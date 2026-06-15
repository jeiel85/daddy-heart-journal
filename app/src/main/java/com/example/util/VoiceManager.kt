package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class VoiceManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    
    var isRecording = false
        private set

    var isPlaying = false
        private set

    fun startRecording(outputFile: File): Boolean {
        if (isRecording) return false

        try {
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            return true
        } catch (e: Exception) {
            Log.e("VoiceManager", "Failed to start recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            return false
        }
    }

    fun stopRecording() {
        if (!isRecording) return
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error stopping recorder", e)
        } finally {
            mediaRecorder = null
            isRecording = false
        }
    }

    fun startPlayback(inputFile: File, onComplete: () -> Unit): Boolean {
        if (isPlaying) {
            stopPlayback()
        }

        if (!inputFile.exists()) {
            Log.e("VoiceManager", "Input audio file does not exist")
            return false
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(inputFile.absolutePath)
                prepare()
                setOnCompletionListener {
                    stopPlayback()
                    onComplete()
                }
                start()
            }
            isPlaying = true
            return true
        } catch (e: IOException) {
            Log.e("VoiceManager", "Failed to start playback", e)
            mediaPlayer?.release()
            mediaPlayer = null
            return false
        }
    }

    fun stopPlayback() {
        if (!isPlaying) return
        try {
            mediaPlayer?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error stopping playback", e)
        } finally {
            mediaPlayer = null
            isPlaying = false
        }
    }

    fun getDuration(inputFile: File): Int {
        if (!inputFile.exists()) return 0
        val tempMediaPlayer = MediaPlayer()
        return try {
            tempMediaPlayer.setDataSource(inputFile.absolutePath)
            tempMediaPlayer.prepare()
            val duration = tempMediaPlayer.duration
            tempMediaPlayer.release()
            duration
        } catch (e: Exception) {
            tempMediaPlayer.release()
            0
        }
    }
}
