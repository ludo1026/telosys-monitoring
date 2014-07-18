package org.telosys.webtools.monitoring;

import java.util.ArrayList;
import java.util.List;

public class CircularStack {
	
	protected final int size;
	protected final String[] arrays;
	protected int nextIndex = 0;
	protected boolean completed = false;
	
	public CircularStack(int size) {
		this.size = size;
		this.arrays = new String[size];
	}
	
	public void push(String value) {
		int index = getNextIndice();
		this.arrays[index] = value;
	}
	
	public synchronized int getNextIndice() {
		int index = this.nextIndex;
		this.nextIndex++;
		if(this.nextIndex >= this.size) {
			this.nextIndex = 0;
			completed = true;
		}
		return index;
	}
	
	public List<String> getAllAscendant() {
		List<String> results = new ArrayList<String>();
		int index = this.nextIndex;
		boolean completed = this.completed;
		if(completed) {
			for(int i=index; i<this.size; i++) {
				results.add(this.arrays[i]);
			}
		}
		for(int i=0; i<index; i++) {
			results.add(this.arrays[i]);
		}
		return results;
	}
	
	public List<String> getAllDescendant() {
		List<String> results = new ArrayList<String>();
		int index = this.nextIndex;
		boolean completed = this.completed;
		for(int i=index-1; i>=0; i--) {
			results.add(this.arrays[i]);
		}
		if(completed) {
			for(int i=this.size-1; i>=index; i--) {
				results.add(this.arrays[i]);
			}
		}
		return results;
	}
	
}
