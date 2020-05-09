package me.iweizi.stepchanger.wechat;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;


abstract class StepBean {
    public long beginOfToday;
    public long lastSaveStepTime;
    public long lastWriteTime;
    public long sensorTimeStamp;
    public long preSensorStep;
    public long todayStep;
    public long lastSaveSensorStep;

    protected HashMap<Integer, Object> mStepCounterMap;
    protected File mCfgFile;

    protected StepBean(File cfgFile) {
        mCfgFile = cfgFile;
    }

    public void read() throws IOException, ClassCastException , ClassNotFoundException{
        FileInputStream fis = new FileInputStream(mCfgFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        // noinspection unchecked
        mStepCounterMap = (HashMap<Integer, Object>) ois.readObject();
        ois.close();
        fis.close();

        readConfig();
    }

    public void write(long step) throws IOException {
        // push
        FileOutputStream fos;
        ObjectOutputStream oos;

        fos = new FileOutputStream(mCfgFile);
        oos = new ObjectOutputStream(fos);
        writeStep(step);
        oos.writeObject(mStepCounterMap);
        oos.close();
        fos.close();
    }


    abstract void readConfig();

    abstract void writeStep(long step);
}

class StepBeanLegacy extends StepBean {
    public StepBeanLegacy(File cfgFile) {
        super(cfgFile);
    }

    @Override
    protected void readConfig() {
        this.beginOfToday = (Long) mStepCounterMap.get(202) * 10000L;
        this.todayStep = (Long) mStepCounterMap.get(201);
        this.preSensorStep = (Long) mStepCounterMap.get(203);
        this.lastSaveStepTime = (Long) mStepCounterMap.get(204);
        this.sensorTimeStamp = (Long) mStepCounterMap.get(209);
        this.lastSaveSensorStep = this.todayStep;
        this.lastWriteTime = this.lastSaveStepTime;
    }

    @Override
    void writeStep(long step) {
        mStepCounterMap.put(201, step);
    }
}

class StepBeanV7 extends StepBean {
    public StepBeanV7(File cfgFile) {
        super(cfgFile);
    }

    @Override
    protected void readConfig() {

        String stringVec = (String) mStepCounterMap.get(301);

        long[] lv = stringVec2long(stringVec);
        this.beginOfToday = lv[0];
        this.lastSaveStepTime = lv[1];
        this.lastWriteTime = lv[2];
        this.sensorTimeStamp = lv[3];
        this.preSensorStep = lv[4];
        this.todayStep = lv[5];
        this.lastSaveSensorStep = lv[6];

    }


    private long[] stringVec2long(String data) {
        if (data == null || data.isEmpty()) {
            return new long[7];
        }
        String[] sv = data.split(",");
        long[] lv = new long[sv.length];

        for (int i = 0; i < lv.length; ++i) {
            String s = sv[i];
            long l;
            if (s != null && !s.isEmpty()) {
                l = Long.valueOf(s);
            } else {
                l = 0;
            }
            lv[i] = l;
        }
        return lv;

    }
    protected void writeStep(long step) {
        this.todayStep = step;
        String data = String.format(Locale.ENGLISH, "%d,%d,%d,%d,%d,%d,%d",
                this.beginOfToday,
                this.lastSaveStepTime,
                this.lastWriteTime,
                this.sensorTimeStamp,
                this.preSensorStep,
                this.todayStep,
                this.lastSaveSensorStep);
        mStepCounterMap.put(301, data);
    }
}
