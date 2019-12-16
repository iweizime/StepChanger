package me.iweizi.stepchanger.wechat;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import me.iweizi.stepchanger.R;
import me.iweizi.stepchanger.StepData;

/**
 * Created by iweiz on 2017/9/4.
 * 微信的步数数据类
 */

class StepCounterCfg extends StepData {
    private static final int CURRENT_TODAY_STEP = 201;
    private static final int PRE_SENSOR_STEP = 203;
    private static final int LAST_SAVE_STEP_TIME = 204;
    /*
        static final int BEGIN_OF_TODAY = 202;
        static final int REBOOT_TIME = 205;
        static final int SENSOR_TIME_STAMP = 209;
    */
    private static final int LAST_UPLOAD_TIME = 3;
    private static final int LAST_UPLOAD_STEP = 4;

    @SuppressLint("SdCardPath")
    private static final String STEP_COUNTER_CFG = "/data/data/com.tencent.mm/MicroMsg/stepcounter.cfg";
    @SuppressLint("SdCardPath")
    private static final String MM_STEP_COUNTER_CFG = "/data/data/com.tencent.mm/MicroMsg/MM_stepcounter.cfg";
    @SuppressLint("SdCardPath")
    private static final String PUSH_STEP_COUNTER_CFG = "/data/data/com.tencent.mm/MicroMsg/PUSH_stepcounter.cfg";

    public static final File mStepCounterCfgFile;
    public static final File mMMStepCounterCfgFile;
    public static final File mPUSHStepCounterCfgFile;

    private static final String WECHAT = "com.tencent.mm";
    private static final String WECHAT_EX = "com.tencent.mm:exdevice";
    private static StepCounterCfg sStepCounterCfg = null;
    private HashMap<Integer, ?> mMMStepCounterMap = null;
    private StepBean mStepBean = null;



    static {
        mStepCounterCfgFile = new File(STEP_COUNTER_CFG);
        mMMStepCounterCfgFile = new File(MM_STEP_COUNTER_CFG);
        mPUSHStepCounterCfgFile = new File(PUSH_STEP_COUNTER_CFG);

    }

    private StepCounterCfg() {
        super();


        ROOT_CMD = new String[]{
                "chmod 606 " + mStepCounterCfgFile.getAbsolutePath(),
                "chmod 606 " + mMMStepCounterCfgFile.getAbsolutePath(),
                "chmod 606 " + mPUSHStepCounterCfgFile.getAbsolutePath(),
                "chmod 701 " + mStepCounterCfgFile.getParent(),
                "chmod 701 " + mMMStepCounterCfgFile.getParent(),
                "chmod 701 " + mPUSHStepCounterCfgFile.getParent()
        };
        mLoadButtonId = R.id.wechat_load_button;
        mStoreButtonId = R.id.wechat_store_button;
    }

    static StepCounterCfg get() {
        if (sStepCounterCfg == null) {
            sStepCounterCfg = new StepCounterCfg();
        }
        return sStepCounterCfg;
    }

    @Override
    protected int read(Context context) {
        FileInputStream fis;
        ObjectInputStream ois;

        killWechatProcess(context);
        try {

            mStepBean = StepBean.getBean();

            fis = new FileInputStream(mMMStepCounterCfgFile);
            ois = new ObjectInputStream(fis);
            //noinspection unchecked
            mMMStepCounterMap = (HashMap<Integer, ?>) ois.readObject();
            return SUCCESS;
        } catch (Exception e) {
            return FAIL;
        }
    }


    @Override
    protected int write(Context context) {
        if (mStepBean == null) {
            return FAIL;
        }
        try {
            killWechatProcess(context);
            mStepBean.write(getStep());
            return SUCCESS;
        } catch (Exception e) {
            return FAIL;
        }
    }

    @Override
    protected boolean canRead() {
        return (mPUSHStepCounterCfgFile.canRead() || mStepCounterCfgFile.canRead()) && mMMStepCounterCfgFile.canRead();
    }

    @Override
    protected boolean canWrite() {
        return (mPUSHStepCounterCfgFile.canWrite() || mStepCounterCfgFile.canWrite()) && mMMStepCounterCfgFile.canWrite();
    }

    @Override
    public long getLastUploadStep() {
        if (mMMStepCounterMap != null) {
            //noinspection unchecked
            return ((HashMap<Integer, Long>) mMMStepCounterMap).get(LAST_UPLOAD_STEP);
        } else {
            return -1;
        }
    }

    @Override
    public long getLastUploadTime() {
        if (mMMStepCounterMap != null) {
            //noinspection unchecked
            return ((HashMap<Integer, Long>) mMMStepCounterMap).get(LAST_UPLOAD_TIME);
        } else {
            return -1;
        }
    }

    @Override
    public long getLastSaveTime() {
        if (mStepBean != null) {
            return mStepBean.lastSaveStepTime;
        } else {
            return -1;
        }
    }

    @Override
    public long getLastSaveSensorStep() {

        if (mStepBean != null) {
            return mStepBean.preSensorStep;
        } else {
            return -1;
        }
    }

    @Override
    public boolean isLoaded() {
        return mMMStepCounterMap != null && mStepBean != null;
    }

    private void killWechatProcess(Context context) {
        ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(WECHAT);
        am.killBackgroundProcesses(WECHAT_EX);
    }


}
