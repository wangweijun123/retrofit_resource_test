package com.example.wangweijun1.retrofit_xxx;

import org.junit.Test;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
        String a = "abcA中文";
        // UTF-8 : 97 98 99 -28 -72 -83 -26 -106 -121
        // GBK  ： 97 98 99 -42 -48 -50 -60
        // 怎么编码，怎么解码 byte 范围-127,127
        byte[] data = a.getBytes(Charset.forName("GBK"));
        System.out.println("data size:"+data.length);
        for (int i=0; i<data.length;i++) {
            System.out.print(data[i] + " ");
        }


//        data = a.getBytes(Charset.forName("XXX"));
//        System.out.println("data size:"+data.length);
//        for (int i=0; i<data.length;i++) {
//            System.out.print(data[i] + " ");
//        }
        String result = new String(data, Charset.forName("GBK"));
        System.out.print(result);
        byte[] data2 = {-128, 127};// byte 范围
    }

    @Test
    public void testCleanup() throws Exception {
        DiskLruCacheClenup diskLruCacheClenup = new DiskLruCacheClenup();

        diskLruCacheClenup.run();
    }

    @Test
    public void testHashcode() throws Exception {
        Set<Person> set = new HashSet<Person>();
        Person p1 = new Person(11, 1, "张三");
        Person p2 = new Person(12, 1, "李四");
        Person p3 = new Person(11, 1, "张三");
        Person p4 = new Person(11, 1, "李四");
        //只验证p1、p3
        System.out.println("p1 == p3? :" + (p1 == p3));
        System.out.println("p1.equals(p3)?:" + p1.equals(p3));
        System.out.println("-----------------------分割线--------------------------");
        set.add(p1);
        set.add(p2);
        set.add(p3);// 这里是没添加进去的
        set.add(p4);
        System.out.println("set.size()=" + set.size());
    }
}