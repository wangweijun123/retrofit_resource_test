package com.example.wangweijun1.retrofit_xxx;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by quan.wen on 2018/5/15.
 */
public class AuthClientDemo {
    public static class BaseRequest {
        public String appId;
        public String userId; // 一般用于开后门，测试专用

        @SerializedName("sig")
        public String signature;

        @SerializedName("timestamp")
        public String timeStamp;

        public String longitude;
        public String latitude;
        public String gpsType;
        public String fromApp;
        public String deviceId;

        @SerializedName("log_id")
        public String logId;
    }

    public static class NluTerm {
        public String word;
        public String ner;
        public String offset;

        public NluTerm(String word, String ner) {
            this.word = word;
            this.ner = ner;
        }
    }

    public static class ResourceRequest extends BaseRequest {
        @SerializedName("nluTerms")
        public List<NluTerm> nluTerms;
    }

    public static class Hex {
        /**
         * 用于建立十六进制字符的输出的小写字符数组
         */
        private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

        /**
         * 用于建立十六进制字符的输出的大写字符数组
         */
        private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5',
                '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        /**
         * 将字节数组转换为十六进制字符数组
         *
         * @param data
         *            byte[]
         * @return 十六进制char[]
         */
        public static char[] encodeHex(byte[] data) {
            return encodeHex(data, true);
        }

        /**
         * 将字节数组转换为十六进制字符数组
         *
         * @param data
         *            byte[]
         * @param toLowerCase
         *            <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
         * @return 十六进制char[]
         */
        public static char[] encodeHex(byte[] data, boolean toLowerCase) {
            return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
        }

        /**
         * 将字节数组转换为十六进制字符数组
         *
         * @param data
         *            byte[]
         * @param toDigits
         *            用于控制输出的char[]
         * @return 十六进制char[]
         */
        protected static char[] encodeHex(byte[] data, char[] toDigits) {
            int l = data.length;
            char[] out = new char[l << 1];
            // two characters form the hex value.
            for (int i = 0, j = 0; i < l; i++) {
                out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
                out[j++] = toDigits[0x0F & data[i]];
            }
            return out;
        }

        /**
         * 将字节数组转换为十六进制字符串
         *
         * @param data
         *            byte[]
         * @return 十六进制String
         */
        public static String encodeHexStr(byte[] data) {
            return encodeHexStr(data, true);
        }

        /**
         * 将字节数组转换为十六进制字符串
         *
         * @param data
         *            byte[]
         * @param toLowerCase
         *            <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
         * @return 十六进制String
         */
        public static String encodeHexStr(byte[] data, boolean toLowerCase) {
            return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
        }

        /**
         * 将字节数组转换为十六进制字符串
         *
         * @param data
         *            byte[]
         * @param toDigits
         *            用于控制输出的char[]
         * @return 十六进制String
         */
        protected static String encodeHexStr(byte[] data, char[] toDigits) {
            return new String(encodeHex(data, toDigits));
        }

        /**
         * 将十六进制字符数组转换为字节数组
         *
         * @param data
         *            十六进制char[]
         * @return byte[]
         * @throws RuntimeException
         *             如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
         */
        public static byte[] decodeHex(char[] data) {

            int len = data.length;

            if ((len & 0x01) != 0) {
                return new byte[0];
            }

            byte[] out = new byte[len >> 1];

            // two characters form the hex value.
            for (int i = 0, j = 0; j < len; i++) {
                int f = toDigit(data[j], j) << 4;
                j++;
                f = f | toDigit(data[j], j);
                j++;
                out[i] = (byte) (f & 0xFF);
            }

            return out;
        }

        /**
         * 将十六进制字符转换成一个整数
         *
         * @param ch
         *            十六进制char
         * @param index
         *            十六进制字符在字符数组中的位置
         * @return 一个整数
         * @throws RuntimeException
         *             当ch不是一个合法的十六进制字符时，抛出运行时异常
         */
        protected static int toDigit(char ch, int index) {
            int digit = Character.digit(ch, 16);
            if (digit == -1) {
                return -1;
            }
            return digit;
        }
    }

    public static final String appId = "10015"; // your assigned appId
    public static final String appKey = "e77dbb407daf01dffcca831ed1b8bc72c56ea553"; // your assigned appId
    public static final String SERVICE_NLP = "/nlp";
    public static final String SERVICE_SEGMENT = "/nlp/segmentation";
    public static final String SERVICE_RESOURCE = "/nlp/resource";

    private static byte[] HmacSHA1(String data, byte[] key) {
        try {
            String algorithm = "HmacSHA1";
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data.getBytes("UTF8"));
        } catch (Exception e) {
            return new byte[0];
        }
    }

    public static String genSignature(String appKey, String timeStamp, String serviceName) {
        byte[] kDate = Hex.decodeHex(appKey.toCharArray());
        byte[] kRegion = HmacSHA1(timeStamp, kDate);
        byte[] kService = HmacSHA1(serviceName, kRegion);
        byte[] kSigning = HmacSHA1("aws4_request", kService);
        return Hex.encodeHexStr(kSigning, true);
    }

    public static void main(String [] args) throws Exception {
        Gson gson = new Gson();
        AuthClientDemo authClientDemo = new AuthClientDemo();

        Long requestTime = 0L;
        String timeStamp = null;
        String signature = null;
        String strJson = null;

        Map<String, String> requestMap = new HashMap<>();
        // /nlp 接口示例
        requestMap.put("query", "我在五道口看战狼2");
        requestMap.put("appId", AuthClientDemo.appId);
        requestMap.put("deviceId", "put_your_deviceId_here");
        requestTime = System.currentTimeMillis() / 1000;
        timeStamp = String.valueOf(requestTime);
        requestMap.put("timestamp", timeStamp);
        signature = genSignature(AuthClientDemo.appKey, timeStamp, SERVICE_NLP);
        requestMap.put("sig", signature);
        requestMap.put("fromApp", "put_your_appName_here");
        requestMap.put("latitude", "40.607182");
        requestMap.put("longitude", "118.484954");
        requestMap.put("gpsType", "GCJ02");

        strJson = gson.toJson(requestMap);
        System.out.println("--\t /nlp sample json");
        System.out.println(strJson);

        // /nlp/segmentation 接口示例
        requestMap.clear();
        requestMap.put("query", "我在五道口看战狼2");
        requestMap.put("appId", AuthClientDemo.appId);
        requestMap.put("deviceId", "put_your_deviceId_here");
        requestTime = System.currentTimeMillis() / 1000;
        timeStamp = String.valueOf(requestTime);
        requestMap.put("timestamp", timeStamp);
        signature = genSignature(AuthClientDemo.appKey, timeStamp, SERVICE_SEGMENT);
        requestMap.put("sig", signature);
        requestMap.put("fromApp", "put_your_appName_here");
        requestMap.put("latitude", "40.607182");
        requestMap.put("longitude", "118.484954");
        requestMap.put("gpsType", "GCJ02");

        strJson = gson.toJson(requestMap);
        System.out.println("--\t /nlp/segmentation sample json");

        System.out.println(strJson);

        // /nlp/resource 接口示例
        List<NluTerm> nluTerms = new ArrayList<>();
        nluTerms.add(new NluTerm("五道口", "ADDR"));
        nluTerms.add(new NluTerm("战狼2", "FILM"));
        ResourceRequest resourceRequest = new ResourceRequest();
        resourceRequest.nluTerms = nluTerms;
        resourceRequest.appId = AuthClientDemo.appId;
        resourceRequest.deviceId = "put_your_deviceId_here";
        resourceRequest.timeStamp = timeStamp;
        signature = genSignature(AuthClientDemo.appKey, timeStamp, SERVICE_RESOURCE);
        resourceRequest.signature = signature;
        resourceRequest.fromApp = "put_your_appName_here";
        resourceRequest.latitude = "40.607182";
        resourceRequest.longitude = "118.484954";
        resourceRequest.gpsType = "GCJ02";
        strJson = gson.toJson(resourceRequest, ResourceRequest.class);
        System.out.println("--\t /nlp/resource sample json");

        System.out.println(strJson);

    }

}
