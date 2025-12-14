package com.example.android_project

import android.Manifest
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MediaPlayerActivity : AppCompatActivity() {

    lateinit var mediaPlayer: MediaPlayer
    lateinit var tracksListView: ListView
    lateinit var seekBar: SeekBar
    lateinit var volumeSeekBar: SeekBar
    lateinit var albumArtImageView: ImageView

    val audioFiles = ArrayList<File>()
    var currentIndex = 0
    val handler = Handler(Looper.getMainLooper())

    val permLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok ->
        if (ok) loadMusic() else Toast.makeText(this, "Нет разрешения", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        mediaPlayer = MediaPlayer()

        val playButton: Button = findViewById(R.id.playButton)
        val pauseButton: Button = findViewById(R.id.pauseButton)
        val nextButton: Button = findViewById(R.id.nextButton)
        val prevButton: Button = findViewById(R.id.prevButton)

        seekBar = findViewById(R.id.seekBar)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        tracksListView = findViewById(R.id.tracksListView)
        albumArtImageView = findViewById(R.id.albumArtImageView)

        playButton.setOnClickListener { playMusic() }
        pauseButton.setOnClickListener { pauseMusic() }
        nextButton.setOnClickListener { nextTrack() }
        prevButton.setOnClickListener { prevTrack() }

        tracksListView.setOnItemClickListener { _, _, position, _ ->
            playTrack(position)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer.seekTo(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                mediaPlayer.setVolume(progress / 100f, progress / 100f)
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        permLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
    }

    fun loadMusic() {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/Music"
        val folder = File(path)

        val list = folder.listFiles()
        if (list != null) {
            for (f in list) {
                if (f.isFile && f.name.endsWith(".mp3")) {
                    audioFiles.add(f)
                }
            }
        }

        bubbleSort()

        val names = ArrayList<String>()
        for (f in audioFiles) {
            names.add(f.nameWithoutExtension)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        tracksListView.adapter = adapter
    }

    fun bubbleSort() {
        for (i in 0 until audioFiles.size - 1) {
            for (j in 0 until audioFiles.size - i - 1) {
                if (audioFiles[j].name > audioFiles[j + 1].name) {
                    val tmp = audioFiles[j]
                    audioFiles[j] = audioFiles[j + 1]
                    audioFiles[j + 1] = tmp
                }
            }
        }
    }

    fun playTrack(position: Int) {
        try {
            currentIndex = position
            mediaPlayer.reset()
            mediaPlayer.setDataSource(audioFiles[position].absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()

            seekBar.max = mediaPlayer.duration
            updateSeek()

            setAlbumArt(audioFiles[position])

        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при воспроизведении", Toast.LENGTH_SHORT).show()
        }
    }

    fun setAlbumArt(file: File) {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media.ALBUM_ID)
        val selection = "${MediaStore.Audio.Media.DATA}=?"
        val selectionArgs = arrayOf(file.absolutePath)

        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                if (index >= 0) {
                    val albumId = it.getLong(index)

                    if (albumId > 0) {
                        val albumArtUri =
                            Uri.parse("content://media/external/audio/albumart/$albumId")
                        albumArtImageView.setImageURI(albumArtUri)
                        return
                    }
                }
            }
        }
    }

    fun playMusic() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            updateSeek()
        }
    }

    fun pauseMusic() {
        if (mediaPlayer.isPlaying) mediaPlayer.pause()
    }

    fun nextTrack() {
        currentIndex++
        if (currentIndex >= audioFiles.size) currentIndex = 0
        playTrack(currentIndex)
    }

    fun prevTrack() {
        currentIndex--
        if (currentIndex < 0) currentIndex = audioFiles.size - 1
        playTrack(currentIndex)
    }

    fun updateSeek() {
        handler.postDelayed({
            if (mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
                updateSeek()
            }
        }, 500)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacksAndMessages(null)
    }
}