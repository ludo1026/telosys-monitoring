package org.telosys.webtools.monitoring;

public class CircularStack {
	
	protected final int size;
	protected final String[] arrays;
	protected int nextIndex = 0;
	
	public CircularStack(int size) {
		this.size = size;
		this.arrays = new String[size];
	}
	
	public void push(String value) {
		synchronized(arrays) {
			
			this.arrays[this.nextIndex] = value;
			
			this.nextIndex++;
			if(this.nextIndex >= this.size) {
				this.nextIndex = 0;
			}
			
		}
	}
	
	public String[] getAll() {
		synchronized(arrays) {
		
			String[] results = new String[this.size];
			for(int i=0; i<this.size; i++) {
				results[i] = this.arrays[i];
			}
			return results;
			
		}
	}
	
}
