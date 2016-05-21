package controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by santos on 4/16/16.
 */
public class Tree<T>  {
    private List<Tree<T>> children;
    private Tree<T> parent = null;
    private T data = null;

    public Tree(T data) {
        this.data = data;
        children = new ArrayList<>();
    }

    public Tree(T data, Tree<T> parent) {
        this.data = data;
        this.parent = parent;
        children = new ArrayList<>();
    }

    public List<Tree<T>> getChildren() {
        return children;
    }

    public Tree<T> getParent() {
        return this.parent;
    }

    public void setParent(Tree<T> parent) {
//        parent.addChild(this);
        this.parent = parent;
    }

    public Tree<T> addChild(T data) {
        Tree<T> child = new Tree<>(data);
        child.setParent(this);
        this.children.add(child);
        return child;
    }

    public void addChild(Tree<T> child) {
        child.setParent(this);
        this.children.add(child);
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }

    public void removeParent() {
        this.parent = null;
    }
}
