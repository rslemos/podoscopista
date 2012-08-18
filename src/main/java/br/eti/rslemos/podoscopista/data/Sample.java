package br.eti.rslemos.podoscopista.data;

import objectexplorer.ObjectGraphMeasurer.Footprint;

public class Sample implements Cloneable {
	public static class Exception {
		public String className;
		public String message;
		public StackTraceElement[] stackTrace;
	}

	public String className;
	
	public String methodName;
	public String methodDescription;
	public Object[] parameters;
	
	public long size;
	public Footprint footprint;

	public Exception exception;
	
	public Sample clone() {
		try {
			return (Sample) super.clone();
		} catch (CloneNotSupportedException e) {
			throw (UnknownError)new UnknownError().initCause(e);
		}
	}
}