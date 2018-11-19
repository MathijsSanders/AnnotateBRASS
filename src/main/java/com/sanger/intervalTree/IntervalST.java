package com.sanger.intervalTree;
import java.util.*;

public class IntervalST<Value> {
	private Node root;
	
	private class Node {
		Interval1D interval;
		Value value;
		Node left, right;
		boolean leftClear = false, rightClear = false;
		int N;
		int max;

		Node(Interval1D interval, Value value) {
			this.interval = interval;
			this.value    = value;
			this.N        = 1;
			this.max      = interval.high;
		}
	}
	public Iterable<Value> constructIterable() {
		LinkedList<Value> ll = new LinkedList<Value>();
		ArrayList<Node> poppush = new ArrayList<Node>();
		Node tmp = null;
		if(root == null)
			return null;
		else {
			poppush.add(root);
			ll.add(root.value);
			tmp = root;
			while(poppush.size() > 0) {
				if(tmp.left != null && !tmp.left.leftClear) {
					tmp = tmp.left;
					tmp.leftClear = true;
					poppush.add(tmp);
					ll.add(tmp.value);
				}
				else if(tmp.right != null && !tmp.right.rightClear) {
					tmp = tmp.right;
					tmp.rightClear = true;
					poppush.add(tmp);
					ll.add(tmp.value);
				}
				else {
					if(tmp.left != null)
						tmp.left.leftClear = false;
					if(tmp.right != null)
						tmp.right.rightClear = false;
					poppush.remove(poppush.size()-1);
					if(poppush.size() > 0)
						tmp = poppush.get(poppush.size()-1);
				}
			}
		}
		return ll;
	}
	public boolean contains(Interval1D interval) {
		return (get(interval) != null);
	}

	public Value get(Interval1D interval) {
		return get(root, interval);
	}

	private Value get(Node x, Interval1D interval) {
		if (x == null) return null;
		int cmp = interval.compareTo(x.interval);
		if (cmp < 0) return get(x.left, interval);
		else if (cmp > 0) return get(x.right, interval);
		else return x.value;
	}

	public void put(Interval1D interval, Value value) {
		if (contains(interval)) { StdOut.println("duplicate"); remove(interval);  }
		root = randomizedInsert(root, interval, value);
	}

	private Node randomizedInsert(Node x, Interval1D interval, Value value) {
		if (x == null) return new Node(interval, value);
		if (Math.random() * size(x) < 1.0) return rootInsert(x, interval, value);
		int cmp = interval.compareTo(x.interval);
		if (cmp < 0) x.left = randomizedInsert(x.left, interval, value);
		else x.right = randomizedInsert(x.right, interval, value);
		fix(x);
		return x;
	}

	private Node rootInsert(Node x, Interval1D interval, Value value) {
		if (x == null) return new Node(interval, value);
		int cmp = interval.compareTo(x.interval);
		if (cmp < 0) { x.left  = rootInsert(x.left, interval, value); x = rotR(x); }
		else { x.right = rootInsert(x.right, interval, value); x = rotL(x); }
		return x;
	}

	private Node joinLR(Node a, Node b) { 
		if (a == null) return b;
		if (b == null) return a;
		if (Math.random() * (size(a) + size(b)) < size(a)) {
			a.right = joinLR(a.right, b);
			fix(a);
			return a;
		} else {
			b.left = joinLR(a, b.left);
			fix(b);
			return b;
		}
	}
	
	public Value remove(Interval1D interval) {
		Value value = get(interval);
		root = remove(root, interval);
		return value;
	}

	private Node remove(Node h, Interval1D interval) {
		if (h == null) return null;
		int cmp = interval.compareTo(h.interval);
		if (cmp < 0) h.left = remove(h.left, interval);
		else if (cmp > 0) h.right = remove(h.right, interval);
		else h = joinLR(h.left, h.right);
		fix(h);
		return h;
	}

	public Interval1D search(Interval1D interval) {
		return search(root, interval);
	}

	public Interval1D search(Node x, Interval1D interval) {
		while (x != null) {
			if (interval.intersects(x.interval)) return x.interval;
			else if (x.left == null) x = x.right;
			else if (x.left.max < interval.low)  x = x.right;
			else x = x.left;
		}
		return null;
	}
	
	public Iterable<Interval1D> searchAll(Interval1D interval) {
		LinkedList<Interval1D> list = new LinkedList<Interval1D>();
		searchAll(root, interval, list);
		return list;
	}

	public boolean searchAll(Node x, Interval1D interval, LinkedList<Interval1D> list) {
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		if (x == null)
			return false;
		if (interval.intersects(x.interval)) {
			list.add(x.interval);
			found1 = true;
		}
		if (x.left != null && x.left.max >= interval.low)
			found2 = searchAll(x.left, interval, list);
		if (found2 || x.left == null || x.left.max < interval.low)
			found3 = searchAll(x.right, interval, list);
		return found1 || found2 || found3;
	}
	
	public int size() { 
		return size(root); 
	}
	private int size(Node x) { 
		if (x == null) return 0;
		else return x.N;
	}

	public int height() { 
		return height(root); 
	}
	private int height(Node x) {
		if (x == null) return 0;
		return 1 + Math.max(height(x.left), height(x.right));
	}

	private void fix(Node x) {
		if (x == null) return;
		x.N = 1 + size(x.left) + size(x.right);
		x.max = max3(x.interval.high, max(x.left), max(x.right));
	}

	private int max(Node x) {
		if (x == null) return Integer.MIN_VALUE;
		return x.max;
	}

	private int max3(int a, int b, int c) {
		return Math.max(a, Math.max(b, c));
	}

	private Node rotR(Node h) {
		Node x = h.left;
		h.left = x.right;
		x.right = h;
		fix(h);
		fix(x);
		return x;
	}

	private Node rotL(Node h) {
		Node x = h.right;
		h.right = x.left;
		x.left = h;
		fix(h);
		fix(x);
		return x;
	}

	public boolean check() { 
		return checkCount() && checkMax(); 
	}
	private boolean checkCount() {
		return checkCount(root);
	}
	private boolean checkCount(Node x) {
		if (x == null) return true;
		return checkCount(x.left) && checkCount(x.right) && (x.N == 1 + size(x.left) + size(x.right));
	}
	private boolean checkMax() {
		return checkMax(root);
	}
	private boolean checkMax(Node x) {
		if (x == null) return true;
		return x.max ==  max3(x.interval.high, max(x.left), max(x.right));
	}
}