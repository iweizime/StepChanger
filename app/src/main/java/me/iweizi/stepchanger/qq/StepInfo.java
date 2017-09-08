package me.iweizi.stepchanger.qq;

import android.app.ActivityManager;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import me.iweizi.stepchanger.StepData;
import me.iweizi.stepchanger.utils.Utils;

/**
 * Created by iweiz on 2017/9/8.
 * 用于读写/data/data/com.tencent.mobileqq/files/step.info
 */

public class StepInfo extends StepData {

    private static final StepInfo sStepInfo = new StepInfo();
    private static final String STEP_INFO = "/data/data/com.tencent.mobileqq/files/step.info";
    private static final String sKey = "4eY#X@~g.+U)2%$<";
    private static final String QQ = "com.tencent.mobileqq";
    private static final String QQ_MSF = "com.tencent.mobileqq:MSF";

    private static final File sStepInfoFile = new File(STEP_INFO);
    private JSONObject mStepInfo;
    private Cryptor mCryptor;

    private StepInfo() {
        ROOT_CMD = new String[]{
                "chmod o+rw " + STEP_INFO
        };
        mCryptor = new Cryptor(sKey.getBytes());
        mStepInfo = null;
    }

    public static StepInfo get() {
        return sStepInfo;
    }

    @Override
    protected boolean canRead() {
        return sStepInfoFile.canRead();
    }

    @Override
    protected boolean canWrite() {
        return sStepInfoFile.canWrite();
    }

    @Override
    protected int read(Context context) {
        killQQProcess(context);
        RandomAccessFile raf;
        byte[] ciphertext;
        byte[] plaintext;
        try {
            raf = new RandomAccessFile(STEP_INFO, "r");
            ciphertext = new byte[(int) raf.length()];
            raf.read(ciphertext);
            plaintext = mCryptor.decrypt(ciphertext);
            mStepInfo = new JSONObject(new String(plaintext));
        } catch (Throwable e) {
            return FAIL;
        }
        setStep(getLastUploadStep());
        return SUCCESS;
    }

    @Override
    protected int write(Context context) {
        killQQProcess(context);
        try {
            FileOutputStream outputStream = new FileOutputStream(STEP_INFO);
            String offsetKey = String.valueOf(Utils.beginOfToday()) + "_offset";
            int offset = mStepInfo.getInt(offsetKey);
            offset = offset + (int) (getStep() - getLastUploadStep());
            mStepInfo.put(offsetKey, offset);
            outputStream.write(mCryptor.encrypt(mStepInfo.toString().getBytes()));
        } catch (Throwable e) {
            return FAIL;
        }
        return SUCCESS;
    }

    @Override
    public long getLastUploadTime() {
        if (mStepInfo != null) {
            try {
                return mStepInfo.getLong("last_report_time");
            } catch (JSONException e) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public long getLastUploadStep() {
        String beginOfToday = String.valueOf(Utils.beginOfToday());
        if (mStepInfo != null) {
            try {
                return (mStepInfo.getInt(beginOfToday + "_total")
                        - mStepInfo.getInt(beginOfToday + "_init")
                        + mStepInfo.getInt(beginOfToday + "_offset"));
            } catch (JSONException e) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public long getLastSaveTime() {
        return -1;
    }

    @Override
    public long getLastSaveSensorStep() {
        String beginOfToday = String.valueOf(Utils.beginOfToday());
        if (mStepInfo != null) {
            try {
                return mStepInfo.getInt(beginOfToday + "_total");
            } catch (JSONException e) {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public boolean isLoaded() {
        return !(mStepInfo == null);
    }

    private void killQQProcess(Context context) {
        ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(QQ);
        am.killBackgroundProcesses(QQ_MSF);
    }
}
