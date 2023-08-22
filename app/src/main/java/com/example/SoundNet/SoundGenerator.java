package com.example.SoundNet;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.example.SoundNet.WavFile.WavFileHandle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;


public class SoundGenerator {
    private final String TAG = this.getClass().getSimpleName();

    private final double duration = 0.125;

    private final int numSamples = (int) (duration * Common.DEFAULT_SAMPLE_RATE);// 訊號個數: 6000

    private final double SAMPLING_PERIOD = (double) 1 / Common.DEFAULT_SAMPLE_RATE;

    private final double[] temp = new double[numSamples];

    private final double SYNC = 20048;

    private final String message;

    WavFileHandle mWavFileHandle;

    Context context;

    int totalSymbol;
    private final byte[] sound;
    private final byte[] generatedSnd = new byte[2 * numSamples + 4088];

    public SoundGenerator(String message, Context context) {
        this.message = message;
        this.context = context;

        mWavFileHandle = new WavFileHandle();

        totalSymbol = message.length() * 2 + 1;
        sound = new byte[2 * totalSymbol * numSamples + 4088];
    }

    public void generatorSound() throws FileNotFoundException {
        String tempFileName = Common.AUDIO_GENERATOR_FILENAME_RAW;
        String rawFile = mWavFileHandle.getRawFilename(context, tempFileName);
        String folderName = mWavFileHandle.getFolderName(context);
        String wavFile = folderName + Common.AUDIO_GENERATOR_FILENAME_WAV;

        FileOutputStream fileOutputStream = new FileOutputStream(rawFile);

        for (int i = 0; i < numSamples; i++) {
            temp[i] = Math.cos(2 * Math.PI * i * SYNC * SAMPLING_PERIOD);
        }

        byte[] tempByte = new byte[numSamples * 2];

        short[] buffer = new short[numSamples];
        // double to short to Hanning
        for (int i = 0; i < numSamples; i++) {
            final short val = (short) (temp[i] * 32767);
            buffer[i] = val;
        }

        buffer = HanningWindow(buffer, 0, numSamples);

        // short to byte
        ByteBuffer.wrap(tempByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
        // tempByte array put into sound array
        System.arraycopy(tempByte, 0, sound, 0, numSamples * 2);

        // 產生FSK頻率
        int[] fsk = new int[16];// fsk頻率對應
        for (int i = 0; i < 16; i++) {
            fsk[i] = Common.MIN_FREQUENCY + 128 * i;
        }

        int[] encode = encode();// 字串轉為10進制數字
        Log.i(TAG, "generatorSound: encode" + Arrays.toString(encode));

        for (int j = 0; j < encode.length; j++) {
            // 製作6000個該字串的訊號
            int num = encode[j];
            for (int i = 0; i < numSamples; i++) {
                temp[i] = Math.cos(2 * Math.PI * i * fsk[num] * SAMPLING_PERIOD);
                final short val = (short) (temp[i] * 32767);
                buffer[i] = val;
            }
            // 6000筆經過HanningWindow的數值
            buffer = HanningWindow(buffer, 0, numSamples);
            // 6000 short to temp byte
            ByteBuffer.wrap(tempByte).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);

            System.arraycopy(tempByte, 0, sound, (j + 1) * 2 * numSamples, numSamples * 2);
        }
        Log.i(TAG, "generatorSound: " + sound.length);

        try {
            assert fileOutputStream != null;
            fileOutputStream.write(sound);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mWavFileHandle.copyWaveFile(rawFile, wavFile);
    }

    public short[] HanningWindow(short[] signal_in, int pos, int size) {
        for (int i = pos; i < pos + size; i++) {
            int j = i - pos; // j = index into Hanning window function
            signal_in[i] = (short) (signal_in[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * j / size)));
        }
        return signal_in;
    }

    public void generator() {
        // 存檔
        String tempFileName = Common.AUDIO_GENERATOR_FILENAME_RAW;
        String rawFile = mWavFileHandle.getRawFilename(context, tempFileName);
        String folderName = mWavFileHandle.getFolderName(context);
        String wavFile = folderName + Common.AUDIO_GENERATOR_FILENAME_WAV;

        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(rawFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numSamples; i++) {
            temp[i] = Math.cos(2 * Math.PI * i * SYNC * SAMPLING_PERIOD);
        }

        short[] buffer = new short[numSamples];// length: 6000

        // double2short
        for (int i = 0; i < numSamples; i++) {
            short val = (short) (temp[i] * 32767.0);
            buffer[i] = val;
        }

        buffer = HanningWindow(buffer, 0, numSamples);

//        int idx = 0;
//        for (int i = 0; i < numSamples; i++) {
//            final short val = buffer[i];
//            generatedSnd[idx++] = (byte) (val & 0x00ff);
//            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
//        }

        ByteBuffer.wrap(generatedSnd).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
        Log.i(TAG, "generator: " + generatedSnd.length);

        try {
            assert fileOutputStream != null;
            fileOutputStream.write(generatedSnd);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mWavFileHandle.copyWaveFile(rawFile, wavFile);
    }

    public int[] encode() {
        char[] chars = message.toCharArray();
        int[] encode = new int[chars.length * 2];

        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];// chars {H, E, L, L, O, !}
            byte temp = (byte) aChar;// 將 aChar 轉 binary
            String byte2int;// chars {01001000, 01000101, 01001100, 01001100, 01001111, 00100001}

            int j = i * 2;
            // 拆成 {0100, 1000, 0100, 0101, 0100, 1100, 0100, 1100, 0100, 1111, 0010, 0001}
            // 高位元
            byte2int = String.valueOf(((temp & 0xf0) >> 4));
            encode[j] = Integer.parseInt(byte2int, 10);
            // 低位元
            byte2int = String.valueOf((temp & 0x0f));
            encode[j + 1] = Integer.parseInt(byte2int, 10);
            //int {4, 8, 4, 5, 4, 12, 4, 12, 4, 15, 2, 1}
        }
        return encode;
    }


    public void playSound() {
        final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                Common.DEFAULT_SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, (int) sound.length, AudioTrack.MODE_STATIC);
        audioTrack.write(sound, 0, sound.length);
        audioTrack.setVolume(AudioTrack.getMaxVolume());
        audioTrack.play();
    }
}
