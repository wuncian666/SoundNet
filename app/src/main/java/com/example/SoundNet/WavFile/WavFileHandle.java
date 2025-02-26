package com.example.SoundNet.WavFile;

import android.content.Context;
import android.util.Log;
import com.example.SoundNet.Config.AudioConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavFileHandle {
  private static final String TAG = "WavFileHandle";
  private static final int BUFFER_SIZE = 4096; // 4KB緩衝區
  private static final int WAV_HEADER_SIZE = 44; // 標準WAV頭部大小
  private static final int BYTE_RATE =
      AudioConfig.RECORDER_BPP
          * AudioConfig.DEFAULT_SAMPLE_RATE
          * AudioConfig.RECORDER_CHANNELS_INT
          / 8;
  private static final int BLOCK_ALIGN =
      AudioConfig.RECORDER_CHANNELS_INT * AudioConfig.RECORDER_BPP / 8;

  /**
   * 將原始音頻數據轉換為WAV檔案。
   *
   * @param inFilename 輸入原始檔案路徑
   * @param outFilename 輸出WAV檔案路徑
   * @throws IOException 如果檔案操作失敗
   */
  public void copyWaveFile(String inFilename, String outFilename) throws IOException {
    if (inFilename == null || outFilename == null) {
      throw new IllegalArgumentException("File paths cannot be null");
    }

    File inputFile = new File(inFilename);
    if (!inputFile.exists()) {
      throw new IOException("Input file does not exist: " + inFilename);
    }

    long audioTotalLen = inputFile.length();
    long dataTotalLen = audioTotalLen + WAV_HEADER_SIZE - 8; // 減去RIFF和fmt的8字節

    try (FileInputStream inputStream = new FileInputStream(inFilename);
        FileOutputStream outputStream = new FileOutputStream(outFilename)) {
      // 寫入WAV頭部
      writeWaveFileHeader(outputStream, audioTotalLen, dataTotalLen);

      // 複製音頻數據
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }

      Log.i(TAG, "WAV file created: " + outFilename + " from raw: " + inFilename);
    }
  }

  /**
   * 獲取音頻檔案儲存的資料夾路徑。
   *
   * @param context 應用上下文
   * @return 資料夾路徑（以分隔符結尾）
   */
  public String getFolderName(Context context) {
    if (context == null) {
      throw new IllegalArgumentException("Context cannot be null");
    }

    File folder = new File(context.getExternalFilesDir(null), AudioConfig.AUDIO_RECORDER_FOLDER);
    if (!folder.exists() && !folder.mkdirs()) {
      Log.w(TAG, "Failed to create folder: " + folder.getAbsolutePath());
    }
    return folder.getAbsolutePath() + File.separator;
  }

  /**
   * 獲取原始音頻檔案的完整路徑，若檔案存在則可選擇是否刪除。
   *
   * @param context 應用上下文
   * @param audioTempFile 臨時檔案名稱
   * @param deleteIfExists 是否刪除已存在的檔案
   * @return 檔案路徑
   */
  public String getRawFilename(Context context, String audioTempFile, boolean deleteIfExists) {
    if (context == null || audioTempFile == null) {
      throw new IllegalArgumentException("Context or filename cannot be null");
    }

    File folder = new File(context.getExternalFilesDir(null), AudioConfig.AUDIO_RECORDER_FOLDER);
    if (!folder.exists() && !folder.mkdirs()) {
      Log.w(TAG, "Failed to create folder: " + folder.getAbsolutePath());
    }

    File tempFile = new File(folder, audioTempFile);
    if (deleteIfExists && tempFile.exists() && !tempFile.delete()) {
      Log.w(TAG, "Failed to delete existing file: " + tempFile.getAbsolutePath());
    }

    return tempFile.getAbsolutePath();
  }

  // 重載方法，保持與原程式碼兼容
  public String getRawFilename(Context context, String audioTempFile) {
    return getRawFilename(context, audioTempFile, true);
  }

  /**
   * 寫入WAV檔案頭部。
   *
   * @param outputStream 輸出流
   * @param totalAudioLen 音頻數據長度
   * @param totalDataLen 總數據長度（含頭部）
   * @throws IOException 如果寫入失敗
   */
  private void writeWaveFileHeader(
      FileOutputStream outputStream, long totalAudioLen, long totalDataLen) throws IOException {
    ByteBuffer header = ByteBuffer.allocate(WAV_HEADER_SIZE).order(ByteOrder.LITTLE_ENDIAN);

    // RIFF chunk
    header.put(new byte[] {'R', 'I', 'F', 'F'});
    header.putInt((int) totalDataLen); // 文件總長度 - 8
    header.put(new byte[] {'W', 'A', 'V', 'E'});

    // fmt chunk
    header.put(new byte[] {'f', 'm', 't', ' '});
    header.putInt(16); // fmt chunk大小
    header.putShort((short) 1); // 格式（PCM = 1）
    header.putShort((short) AudioConfig.RECORDER_CHANNELS_INT); // 通道數
    header.putInt(AudioConfig.DEFAULT_SAMPLE_RATE); // 採樣率
    header.putInt(BYTE_RATE); // 位元率
    header.putShort((short) BLOCK_ALIGN); // 塊對齊
    header.putShort((short) AudioConfig.RECORDER_BPP); // 位元深度

    // data chunk
    header.put(new byte[] {'d', 'a', 't', 'a'});
    header.putInt((int) totalAudioLen); // 音頻數據長度

    outputStream.write(header.array());
    Log.d(TAG, "WAV header written, size: " + WAV_HEADER_SIZE);
  }
}
