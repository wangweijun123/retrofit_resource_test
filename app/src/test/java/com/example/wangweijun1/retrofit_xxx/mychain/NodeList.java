package com.example.wangweijun1.retrofit_xxx.mychain;

/**
 * Created by wangweijun on 2018/1/23.
 * 单向列表(维护着两个指针，head与last指针)
 */

public class NodeList {

    public static class Node {
        int id;
        Node next;//指向下一个节点
        Node(int id) {
            this.id = id;
            next = null;
        }
    }


    private Node head;// 首部节点

    private Node last;// 尾部节点

    private int size;

    public NodeList() {
        head = null;
        last = null;
        size = 0;
    }

    public void add(Node newNode) {
        if (head == null) {
            head = newNode;
            last = head;
        } else {
            last.next = newNode;
            last = newNode;
        }
        size++;
    }


    public void add(Node newNode, int index) {
        checkIndex(index);

        if (index == 0) {// 添加新的头部,对头部做特殊处理
            newNode.next = head;
            head = newNode;
            size++;
            return;
        }

        if (index == size) {// 对尾部做特殊处理
            last.next = newNode;
            last = newNode;
            size++;
            return;
        }
        Node current = head;
        Node pre = null;
        for (int i = 0; i < size; i++) {
            if (i == index) {
                // 已经在链表中找到index对应的current当前的节点
                newNode.next = current;
                pre.next = newNode;

                size++;
                return;
            }
            pre = current;
            current = current.next;
        }

    }


    public boolean remove(int index) {
        // index 索引检测
        if (index < 0 || index > size-1) {
            throw new IndexOutOfBoundsException("无效索引");
        }

        if (index == 0) {// 删除头部节点特殊处理
            head = head.next;
            size--;
            return true;
        }

        Node current = head;
        Node pre = null;
        for (int i = 0; i < size; i++) {
            if (i == index) {
                // 删除current节点(保存当前节点的pre节点,指向当前节点的下一个节点)
                pre.next = current.next;
                current.next = null;
                size--;
                return true;
            }
            pre = current;
            current = current.next;
        }
        return false;
    }


    // size ==4 (0,1,2,3)
    private void checkIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("索引无效");
        }
    }

    public int size() {
        return size;
    }

    public void print() {
        if (head == null) {
            System.out.println("链表为空");
            return;
        }

        Node temp = head;
        while (temp != null) {
            System.out.print(temp.id + " ");
            temp = temp.next;
        }
    }

}
