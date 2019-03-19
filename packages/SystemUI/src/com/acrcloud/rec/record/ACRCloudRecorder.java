package com.acrcloud.rec.record;

import android.media.AudioRecord;
import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.utils.ACRCloudLogger;
import com.acrcloud.rec.sdk.utils.ACRCloudUtils;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ACRCloudRecorder {
    public static int RECORDER_AUDIO_SOURCE = 1;
    private static final int SAMPLE_RATE = 8000;
    private static ACRCloudRecorder mACRCloudRecorder = new ACRCloudRecorder();
    private final String TAG = "ACRCloudRecorder";
    private ACRCloudClient mACRCloudClient = null;
    private ACRCloudRecordThread mACRCloudRecordThread = null;
    private BlockingQueue<byte[]> mAudioQueue = new LinkedBlockingQueue();
    private AudioRecord mAudioRecord = null;
    private int mAudioRecordBufferLen = 16000;
    private int mReserveAudioRecordBufferMS = 3000;
    private int mRetryNum = 5;

    class ACRCloudRecordThread extends Thread {
        private static final int BUFFERLENGTH = 1600;
        private volatile boolean isRecording = false;

        ACRCloudRecordThread() {
        }

        public void stopRecord() {
            this.isRecording = false;
        }

        public void onRecording(byte[] buffer) {
            try {
                if (ACRCloudRecorder.this.mAudioQueue.size() >= (((ACRCloudRecorder.this.mReserveAudioRecordBufferMS * ACRCloudRecorder.SAMPLE_RATE) * 2) / 1000) / BUFFERLENGTH) {
                    ACRCloudRecorder.this.mAudioQueue.poll();
                }
                double volume = ACRCloudUtils.computeDb(buffer, buffer.length);
                if (ACRCloudRecorder.this.mACRCloudClient != null) {
                    ACRCloudRecorder.this.mACRCloudClient.onVolumeChanged(volume);
                }
                ACRCloudRecorder.this.mAudioQueue.put(buffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                this.isRecording = true;
                int retryReadNum = 5;
                while (this.isRecording) {
                    byte[] buffer = new byte[BUFFERLENGTH];
                    int len = 0;
                    if (ACRCloudRecorder.this.mAudioRecord != null) {
                        len = ACRCloudRecorder.this.mAudioRecord.read(buffer, 0, BUFFERLENGTH);
                    }
                    if (len <= 0) {
                        if (retryReadNum <= 0) {
                            this.isRecording = false;
                            break;
                        }
                        retryReadNum--;
                    } else {
                        retryReadNum = 5;
                        onRecording(buffer);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.isRecording = false;
            }
            ACRCloudRecorder.this.mACRCloudClient = null;
        }
    }

    private ACRCloudRecorder() {
    }

    public static ACRCloudRecorder getInstance() {
        return mACRCloudRecorder;
    }

    public byte[] getCurrentAudioBuffer() {
        byte[] buffer = null;
        try {
            return (byte[]) this.mAudioQueue.poll(200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return buffer;
        }
    }

    public boolean hasAudioData() {
        return this.mAudioQueue.size() > 0;
    }

    public void setPreRecordTime(int recordTimeMS) {
        this.mReserveAudioRecordBufferMS = recordTimeMS;
    }

    public boolean startRecording(ACRCloudClient client) {
        int i = 0;
        while (i < this.mRetryNum) {
            try {
                ACRCloudLogger.m26d("ACRCloudRecorder", "Try get AudioRecord : " + i);
                if (this.mAudioRecord != null || init()) {
                    if (this.mAudioRecord.getRecordingState() != 3) {
                        this.mAudioRecord.startRecording();
                    }
                    if (this.mAudioRecord.getRecordingState() == 3) {
                        break;
                    }
                    ACRCloudLogger.m27e("ACRCloudRecorder", "Start record error!");
                    release();
                }
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.mAudioRecord == null) {
            return false;
        }
        if (this.mACRCloudRecordThread == null) {
            this.mACRCloudRecordThread = new ACRCloudRecordThread();
            this.mACRCloudRecordThread.start();
        }
        this.mACRCloudClient = client;
        return true;
    }

    public void stopRecording() {
        try {
            if (this.mACRCloudRecordThread != null) {
                this.mACRCloudRecordThread.stopRecord();
                this.mACRCloudRecordThread = null;
                this.mAudioQueue.clear();
            }
            if (this.mAudioRecord != null && this.mAudioRecord.getRecordingState() == 3) {
                this.mAudioRecord.stop();
            }
            release();
            this.mACRCloudClient = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean init() {
        try {
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, 16, 2);
            if (minBufferSize > 0) {
                this.mAudioRecordBufferLen = minBufferSize * 5;
            }
            ACRCloudLogger.m27e("ACRCloudRecorder", RECORDER_AUDIO_SOURCE + "_" + 1);
            this.mAudioRecord = new AudioRecord(RECORDER_AUDIO_SOURCE, SAMPLE_RATE, 16, 2, this.mAudioRecordBufferLen);
            if (this.mAudioRecord.getState() == 1) {
                return true;
            }
            release();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            this.mAudioRecord = null;
            return false;
        }
    }

    public void release() {
        try {
            if (this.mAudioRecord != null) {
                this.mAudioRecord.release();
                this.mAudioRecord = null;
                ACRCloudLogger.m27e("ACRCloudRecorder", "releaseAutoRecord");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
