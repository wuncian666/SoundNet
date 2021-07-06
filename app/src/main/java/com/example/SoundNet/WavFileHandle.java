package com.example.SoundNet;

import android.content.Context;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WavFileHandle {
    public final static String TAG = "WavFileHandle";
    int RECORD_CHANNELS = 1;
    int RECORD_BPP = 16;

    public void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in;
        FileOutputStream out;

        long audioTotalLen, dataTotalLen;
        long byteRate =  RECORD_BPP * Common.DEFAULT_SAMPLE_RATE * RECORD_CHANNELS / 8;

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            audioTotalLen = in.getChannel().size();
            dataTotalLen = audioTotalLen + 36;

            // 產生標頭檔
            writeWaveFileHeader(out, audioTotalLen, dataTotalLen, byteRate);

            byte[] bytes = new byte[600];
            //ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);
            while (in.read(bytes) != -1) {
                out.write(bytes);
            }

            in.close();
            out.close();

            Log.i(TAG, "copyWaveFile: " + "\nwav: " + outFilename + "\nraw: " + inFilename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFolderName(Context context) {
        String root = context.getExternalFilesDir(null).getAbsolutePath();
        // 創建資料夾
        File folder = new File(root, Common.AUDIO_RECORDER_FOLDER);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return (folder.getAbsolutePath() + "/");
    }

    public String getRawFilename(Context context, String audioTempFile) {
        String root = context.getExternalFilesDir(null).getAbsolutePath();

        File folder = new File(root, Common.AUDIO_RECORDER_FOLDER);
        if (!folder.exists()) folder.mkdirs();

        File tempFile = new File(root, audioTempFile);
        if (tempFile.exists()) tempFile.delete();

        return (folder.getAbsolutePath() + "/" + audioTempFile);
    }

    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long byteRate)
            throws IOException {
        Log.i(TAG, "writeWaveFileHeader: add wav header");
        byte[] header = new byte[4088];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) Common.RECORDER_CHANNELS_INT;
        header[23] = 0;
        header[24] = (byte) ((long) Common.DEFAULT_SAMPLE_RATE & 0xff);
        header[25] = (byte) (((long) Common.DEFAULT_SAMPLE_RATE >> 8) & 0xff);
        header[26] = (byte) (((long) Common.DEFAULT_SAMPLE_RATE >> 16) & 0xff);
        header[27] = (byte) (((long) Common.DEFAULT_SAMPLE_RATE >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (Common.RECORDER_CHANNELS_INT * Common.RECORDER_BPP / 8); // block align
        header[33] = 0;
        header[34] = Common.RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 4088);
    }

}
