package com.example.android_project

import android.Manifest
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MediaPlayerActivity : AppCompatActivity() {

    var log_tag: String = "MY_LOG_TAG"
    lateinit var mediaPlayer: MediaPlayer
    lateinit var tracksListView: ListView
    lateinit var playButton: Button
    lateinit var pauseButton: Button
    lateinit var seekBar: SeekBar
    lateinit var volumeSeekBar: SeekBar

    var audioFiles = mutableListOf<File>()
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        tracksListView = findViewById(R.id.tracksListView)
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        seekBar = findViewById(R.id.seekBar)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)

        mediaPlayer = MediaPlayer()

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                playMusic()
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
            }
        }
        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)

        setupClickListeners()
    }

    fun setupClickListeners() {
        playButton.setOnClickListener { playAudio() }
        pauseButton.setOnClickListener { pauseAudio() }

        tracksListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            playTrack(position)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val volume = progress / 100.0f
                    mediaPlayer.setVolume(volume, volume)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun playMusic(){
        var musicPath: String = Environment.getExternalStorageDirectory().path + "/Music"

        Log.d(log_tag, "PATH: " + musicPath)
        var directory: File = File(musicPath)

        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".mp3")) {
                    audioFiles.add(file)
                    Log.d(log_tag, "Found MP3: " + file.name)
                }
            }

            if (audioFiles.isNotEmpty()) {
                showTracksList()
            } else {
                Toast.makeText(this, "No MP3 files found", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Music directory not found", Toast.LENGTH_LONG).show()
        }
    }

    fun showTracksList() {
        val trackNames = audioFiles.map { it.nameWithoutExtension }
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, trackNames)
        tracksListView.adapter = adapter
    }

    fun playTrack(position: Int) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(audioFiles[position].absolutePath)
        mediaPlayer.prepareAsync()

        mediaPlayer.setOnPreparedListener {
            seekBar.max = mediaPlayer.duration
            playAudio()
        }
    }

    fun playAudio() {
        mediaPlayer.start()
        updateSeekBar()
    }

    fun pauseAudio() {
        mediaPlayer.pause()
    }

    fun updateSeekBar() {
        handler.postDelayed({
            if (mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
                updateSeekBar()
            }
        }, 1000)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}