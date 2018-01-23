package com.example.wangweijun1.retrofit_xxx.mychain;

import com.example.wangweijun1.retrofit_xxx.mychain.NodeList.Node;

import org.junit.Test;

/**
 * Created by wangweijun on 2018/1/23.
 */

public class NodeListTest {

    @Test
    public void testNodeList() throws Exception {
        NodeList nodeList = new NodeList();
        nodeList.add(new Node(0));
        nodeList.add(new Node(1));
        nodeList.add(new Node(2));
        nodeList.add(new Node(3));
        System.out.println("链表包含元素个数 : " + nodeList.size());
        nodeList.print();
        System.out.println();

        int index = 0;
        nodeList.remove(index);
        System.out.println(index+" removed 链表包含元素个数 : " + nodeList.size());
        nodeList.print();

//        nodeList.add(new Node(1000), 4);
//        nodeList.print();
    }
}
