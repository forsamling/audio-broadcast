package no.forsamling.audiobroadcast.controller;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import no.forsamling.audiobroadcast.Global;

public class Playback {
//    private final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Global.RECORDER_SAMPLERATE,    AudioFormat.CHANNEL_OUT_MONO, Global.RECORDER_AUDIO_ENCODING, AudioTrack.getMinBufferSize(Global.RECORDER_SAMPLERATE, Global.RECORDER_CHANNELS, Global.RECORDER_AUDIO_ENCODING),  AudioTrack.MODE_STREAM);
private final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, Global.RECORDER_SAMPLERATE,    AudioFormat.CHANNEL_OUT_MONO, Global.RECORDER_AUDIO_ENCODING, AudioTrack.getMinBufferSize(Global.RECORDER_SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO, Global.RECORDER_AUDIO_ENCODING),  AudioTrack.MODE_STREAM);
    public Playback() {
        float maxVolume = AudioTrack.getMaxVolume();
        this.audioTrack.setStereoVolume(maxVolume, maxVolume);
    }

    public void play(short[] audioData) {
        this.audioTrack.write(audioData, 0, audioData.length);
        if (this.audioTrack.getPlayState() != 3) {
            this.audioTrack.play();
        }
    }

    public void stop() {
        this.audioTrack.stop();
    }
}


