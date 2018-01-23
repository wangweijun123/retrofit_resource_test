package com.example.wangweijun1.retrofit_xxx.mylinkedlist;

import com.example.wangweijun1.retrofit_xxx.mylinkedlist.LinkedList.Node;

import org.junit.Test;

/**
 * Created by wangweijun on 2018/1/23.
 */

public class MyLinkedListTest {

    @Test
    public void testMyLinkedList() {
        LinkedList linkedList = new LinkedList();
        Node node0 = new Node(0, null, null);
        Node node1 = new Node(1, null, null);
        Node node2 = new Node(2, null, null);
        Node node3 = new Node(3, null, null);
        Node node4 = new Node(4, null, null);

        linkedList.addLast(node0);
        linkedList.addLast(node1);
        linkedList.addLast(node2);
        linkedList.addLast(node3);
        linkedList.addLast(node4);
        linkedList.print();

//        linkedList.addFirst(node0);
//        linkedList.addFirst(node1);
//        linkedList.addFirst(node2);
//        linkedList.addFirst(node3);
//        linkedList.addFirst(node4);
//        linkedList.print();

//        Node newNode = new Node(9999, null, null);
//        linkedList.add(newNode, 0);
//        linkedList.print();


//        linkedList.removeLast();
//        linkedList.print();
//        linkedList.removeLast();
//        linkedList.print();
//        linkedList.removeLast();
//        linkedList.print();
//        linkedList.removeLast();
//        linkedList.print();
//        linkedList.removeLast();
//        linkedList.print();

        // 没有元素了,抛异常了
//        linkedList.removeLast();
//        linkedList.print();

//        linkedList.removeFirst();
//        linkedList.print();
//        linkedList.removeFirst();
//        linkedList.print();
//        linkedList.removeFirst();
//        linkedList.print();
//        linkedList.removeFirst();
//        linkedList.print();
//        linkedList.removeFirst();
//        linkedList.print();
        // 没有元素了,抛异常了
//        linkedList.removeFirst();
//        linkedList.print();

        linkedList.remove(4);
        linkedList.print();

    }
}
