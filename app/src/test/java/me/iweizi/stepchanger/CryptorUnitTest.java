package me.iweizi.stepchanger;

import org.junit.Test;

import java.io.RandomAccessFile;

import me.iweizi.stepchanger.qq.Cryptor;
import me.iweizi.stepchanger.utils.Utils;

import static org.junit.Assert.assertEquals;

/**
 * 用于测试Cryptor类
 */
public class CryptorUnitTest {
    private final String stepinfo = "{\"1504800000000_offset\":0,\"1504800000000_init\":426,\"1504800000000_total\":2109,\"isStepCounterEnable\":true,\"last_report_time\":1504856818805,\"1504886400000_init\":2109}";
    private final String key = "4eY#X@~g.+U)2%$<";

    @Test
    public void main() throws Exception {
        testDecrypt();
        testEncrypt();
    }

    @Test
    public void testDecrypt() throws Exception {

        Cryptor cryptor = new Cryptor(key.getBytes());
        RandomAccessFile file = new RandomAccessFile("C:\\Xiaomi\\XiaoMiFlash\\Source\\ThirdParty\\Google\\Android\\step.info", "r");
        byte[] ciphertext = new byte[(int) file.length()];
        file.read(ciphertext);
        byte[] plaintext = cryptor.decrypt(ciphertext);
        System.out.println(new String(plaintext));
        assertEquals(plaintext.length, stepinfo.length());
        assertEquals(stepinfo, new String(plaintext));
    }

    @Test
    public void testEncrypt() throws Exception {
        Cryptor cryptor = new Cryptor(key.getBytes());
        byte[] ciphertext;
        byte[] plaintext;

        ciphertext = cryptor.encrypt(stepinfo.getBytes());
        plaintext = cryptor.decrypt(ciphertext);
        assertEquals(stepinfo, new String(plaintext));

        for (int len = 1; len < 512; len++) {
            RandomString randomString = new RandomString(len);
            for (int i = 0; i < 512; i++) {
                String randStr = randomString.nextString();
                plaintext = cryptor.decrypt(cryptor.encrypt(randStr.getBytes()));
                String decryptStr = new String(plaintext);
                assertEquals(randStr, decryptStr);
            }
        }
    }

    @Test
    public void testBeginOfToday() {
        System.out.println(Utils.beginOfToday());
    }
}