package com.example.SoundNet.Config;

public class AudioConfig {
    public static final String[] TARGET_STRING_ARRAY = {"HELLO!", "IOTLAB"};

    public final static int DEFAULT_SAMPLE_RATE = 48000;
    public final static double FRAME_DURATION = 0.125;

    public final static int BUFFER_SIZE = 4800;
    public final static int FRAME_SIZE = 240;

    public final static int MIN_FREQUENCY = 18000;
    public final static int SYNC_FREQUENCY = 20048;
    public final static int TARGET_FREQUENCY = 20400;

    public final static int MAX_DEMODULATE_SIZE = 86400;

    public final static double THRESHOLD = 0.8;

    public final static int RECORD_TIMEOUT = 2;

    public final static String ACK = "60";

    /**存錄音檔使用函數*/
    public final static String AUDIO_RECORDER_FOLDER = "FSK_AUDIO";

    public final static String AUDIO_GENERATOR_FILENAME_RAW = "GENERATOR.raw";
    public final static String AUDIO_GENERATOR_FILENAME_WAV = "GENERATOR.wav";

    public final static String DATA_FILE = "goertzleData.txt";
    public final static int RECORDER_CHANNELS_INT = 1;
    public final static int RECORDER_BPP = 16;

    public final static int NUM_SYMBOL = (int) (FRAME_DURATION * DEFAULT_SAMPLE_RATE);
}
