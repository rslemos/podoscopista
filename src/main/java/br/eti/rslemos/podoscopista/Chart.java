/*******************************************************************************
 * BEGIN COPYRIGHT NOTICE
 * 
 * This file is part of program "Podoscopista"
 * Copyright 2012  Rodrigo Lemos
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * END COPYRIGHT NOTICE
 ******************************************************************************/
package br.eti.rslemos.podoscopista;

import java.util.ArrayList;
import java.util.List;

import objectexplorer.ObjectGraphMeasurer.Footprint;

public class Chart {

	public String className;
	public List<Method> methods;
	
	public Chart(String name, int expectedMethodCount) {
		className = name;
		methods = new ArrayList<Chart.Method>(expectedMethodCount);
	}
	
	class Method {
		public String name;
		public String description;
		public Class<?>[] parameterTypes;
		public List<Invocation> invocations;
		public Regression regression;
		
		public Method(String name, String description, Class<?>[] parameterTypes, int expectedInvocationCount) {
			this.name = name;
			this.description = description;
			this.parameterTypes = parameterTypes;
			this.invocations = new ArrayList<Chart.Invocation>(expectedInvocationCount);
			
			Chart.this.methods.add(this);
		}
	}
	
	static class Invocation {
		public Object[] parameters;
		
		public long size;
		public Footprint footprint;
	}
	
	static class Regression {
		public double[] parameters;
		public double[][] parametersVariance;
		public double[] parametersStandardErrors;
		public double[] residuals;
		public double standardError;
		public double errorVariance;
		public double RSquared;
		public double adjustedRSquared;
		public double totalSumOfSquares;
		public double residualSumOfSquares;
	}
}
