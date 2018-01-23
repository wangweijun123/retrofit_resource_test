package com.example.wangweijun1.retrofit_xxx.chain;

import org.junit.Test;

/**
 * Created by wangweijun on 2018/1/23.
 */

public class NodeListTest {

    @Test
    public void testNodeList() throws Exception {
        NodeList<String> nodeList = new NodeList<>("first");
        nodeList.add("a");
        nodeList.add("b");
        nodeList.add("c");
        nodeList.print();

        nodeList.add(2, "addNew");
        nodeList.print();

        nodeList.set("c", "C");
        nodeList.print();

        nodeList.addAfter("addNew", "addNew2");
        nodeList.print();

        System.out.println(nodeList.get(2));

        nodeList.remove("addNew2");
        nodeList.print();
        nodeList.remove("first");
        nodeList.print();

        System.out.println(nodeList.contains("a"));
        System.out.println(nodeList.contains("A"));
        nodeList.clear();
        nodeList.print();
    }
}
