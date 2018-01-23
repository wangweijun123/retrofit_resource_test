package com.example.wangweijun1.retrofit_xxx.mylinkedlist;

import java.util.NoSuchElementException;

/**
 * Created by wangweijun on 2018/1/23.
 */

public class LinkedList {

    public static class Node {
        int data;

        Node pre;//前一个节点

        Node next;// 下一个节点

        public Node(int data, Node pre, Node next) {
            this.data = data;
            this.pre = pre;
            this.next = next;
        }
    }

    Node first;

    Node last;

    int size;

    public LinkedList() {
        first = null;
        last = null;
        size = 0;
    }

    public void addLast(Node newNode) {
        if (last == null) { // 链表为空
            first = newNode;
            last = newNode;
        } else {
            last.next = newNode;
            newNode.pre = last;
            last = newNode; // add操作最后一步移动指针first或者last指针
        }
        size++;
    }

    public void addFirst(Node newNode) {
        if (first == null) {
            first = newNode;
            last = newNode;
        } else {
            newNode.next = first;
            first.pre = newNode;
            first = newNode;// add操作最后一步移动指针first或者last指针
        }
        size++;
    }

    public void removeLast() {
        if (last == null) { // 链表为空
            throw new NoSuchElementException("no such element");
        } else {
            last = last.pre;// remove删除操作第一步移动指针first或者last指针
            if (last != null) {
                last.next = null;
            } else {
                first = null;
            }
        }
        size--;
    }

    public void removeFirst() {
        if (first == null) { // 链表为空
            throw new NoSuchElementException("no such element");
        } else {
            first = first.next;// remove删除操作第一步移动指针first或者last指针
            if (first != null) {
                first.pre.next = null;
                first.pre = null;
            } else {
                last = null;
            }
        }
        size--;
    }

    public void add(Node newNode, int index) {
        if (!checkIndex(index)) {
            throw new IndexOutOfBoundsException("index out of bounds exception");
        }

        if (index == 0) {
            addFirst(newNode);
            return;
        }

        if (index == size) {
            addLast(newNode);
            return;
        }

        Node current = first;
        for (int i=0; i<index; i++) {//循坏之后就找到了链表中index索引对应得节点
            current = current.next;
        }

        newNode.next = current;
        newNode.pre = current.pre;
        current.pre.next = newNode;
        current.pre = newNode;

        size++;
    }

    public void remove(int index) {
        if (!(index>=0 && index<size)) {
            throw new IndexOutOfBoundsException("index out of bound");
        }

        if (index == 0) {
            removeFirst();
            return;
        }

        if (index == size-1) {
            removeLast();
            return;
        }

        Node current = first;
        for (int i=0; i<index; i++) {
            current = current.next;
        }

        current.pre.next = current.next;
        current.next.pre = current.pre;
        current = null;

        size--;
    }

    private boolean checkIndex(int index) {
        if (index >=0 && index <= size) {
            return true;
        }
        return false;
    }

    public void print() {
        System.out.print("列表 size : " + size() +"      ");
        Node current = first;
        while (current != null) {
            System.out.print(current.data + " ");
            current = current.next;
        }
        System.out.println();
    }

    public int size() {
        return size;
    }
}
