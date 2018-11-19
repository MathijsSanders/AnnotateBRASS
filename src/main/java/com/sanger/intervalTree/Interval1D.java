package com.sanger.intervalTree;
public class Interval1D 
{
	public final int low;
	public final int high;
	public Interval1D(int left, int right)  {
		if (left <= right) {
			this.low  = left;
			this.high = right;
		}
		else throw new RuntimeException("Illegal interval");
	}

	public boolean intersects(Interval1D that) {
		if (that.high < this.low) return false;
		if (this.high < that.low) return false;
		return true;
	}

	public boolean contains(int x) {
		return (low <= x) && (x <= high);
	}

	public int compareTo(Interval1D that) {
		if (this.low  < that.low) return -1;
		else if (this.low  > that.low) return +1;
		else if (this.high < that.high) return -1;
		else if (this.high > that.high) return +1;
		else return  0;
	}

	public String toString() {
		return "[" + low + ", " + high + "]";
	}
}