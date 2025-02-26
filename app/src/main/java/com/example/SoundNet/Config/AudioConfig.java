package com.example.SoundNet.Config;

public class AudioConfig {
  public static final String[] TARGET_STRING_ARRAY = {"HELLO!", "IOTLAB"};

  public static final int DEFAULT_SAMPLE_RATE = 48000;
  public static final int DETECT_FRAME = 20;
  public static final double FRAME_DURATION = 0.125;

  public static final int BUFFER_SIZE = 4800;
  public static final int FRAME_SIZE = 240;

  public static final int MIN_FREQUENCY = 18000;
  public static final int SYNC_FREQUENCY = 20048;
  public static final int TARGET_FREQUENCY = 20400;

  public static final int MAX_DEMODULATE_SIZE = 86400;

  public static final double THRESHOLD = 0.8;

  public static final int RECORD_TIMEOUT = 2;

  public static final String ACK = "60";

  /** 存錄音檔使用函數 */
  public static final String AUDIO_RECORDER_FOLDER = "FSK_AUDIO";

  public static final String AUDIO_GENERATOR_FILENAME_RAW = "GENERATOR.raw";
  public static final String AUDIO_GENERATOR_FILENAME_WAV = "GENERATOR.wav";

  public static final String DATA_FILE = "goertzleData.txt";
  public static final int RECORDER_CHANNELS_INT = 1;
  public static final int RECORDER_BPP = 16;

  public static final int NUM_SYMBOL = (int) (FRAME_DURATION * DEFAULT_SAMPLE_RATE);
}
