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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import objectexplorer.MemoryMeasurer;
import objectexplorer.ObjectGraphMeasurer;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import com.google.common.collect.ArrayListMultimap;

public class FootprintRunner {

	private Class<?> clazz;
	private ArrayListMultimap<Class<?>, Field> datapoints;
	private ArrayList<Method> methods;
	private Chart chart;

	public Chart run(Class<?> clazz) {
		try {
			this.clazz = clazz;
			
			collectDataPoints();
			collectAnnotatedMethods();
	
			chart = new Chart(clazz.getName(), methods.size());
	
			for (Method method : methods) {
				processMethod(method);
			}
			
			for (Chart.Method method : chart.methods) {
				linearRegression(method);
			}
			
			return chart;
		} finally {
			this.clazz = null;
			datapoints = null;
			methods = null;
			chart = null;
		}
	}

	private void collectDataPoints() {
		datapoints = ArrayListMultimap.create();
		
		Field[] fields = clazz.getFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getAnnotation(Datapoint.class) != null && Modifier.isStatic(fields[i].getModifiers())) {
				datapoints.put(fields[i].getType(), fields[i]);
			}
		}
		
		datapoints.trimToSize();
	}

	private void collectAnnotatedMethods() {
		methods = new ArrayList<Method>(clazz.getMethods().length);
		
		for (Method method : clazz.getMethods()) {
			if (method.getAnnotation(Footprint.class) != null) {
				methods.add(method);
			}
		}
		
		methods.trimToSize();
	}

	private void processMethod(Method method) {
		ArrayList<Object[]> invocations = produceInvocations(method);
		
		Chart.Method chartMethod = chart.new Method(method.getName(), method.getAnnotation(Footprint.class).value(), method.getParameterTypes(), invocations.size());
		
		for (Object[] invocation : invocations) {
			Chart.Invocation chartInvocation = new Chart.Invocation();
			chartMethod.invocations.add(chartInvocation);

			try {
				Object thiz = clazz.newInstance();
				Object result = method.invoke(thiz, invocation);
				
				chartInvocation.parameters = invocation;
				chartInvocation.footprint = ObjectGraphMeasurer.measure(result);
				chartInvocation.size = MemoryMeasurer.measureBytes(result);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
		}
	}

	private void linearRegression(Chart.Method method) {
		if (method.parameterTypes.length == 1) {
			Class<?> argType = method.parameterTypes[0];
				
			final boolean isNumber = argType.isPrimitive() ?
					(argType != boolean.class && argType != char.class) :
					Number.class.isAssignableFrom(argType);
			
			if (isNumber)
				linearRegression0(method);
		}
	}

	private void linearRegression0(Chart.Method chartMethod) {
		double[] y = new double[chartMethod.invocations.size()];
		double[][] x = new double[chartMethod.invocations.size()][1];
		int i = 0;
		
		for (Chart.Invocation chartInvocation : chartMethod.invocations) {
			try {
				y[i] = chartInvocation.size;
				x[i][0] = ((Number)chartInvocation.parameters[0]).doubleValue();
				//x[i][1] = Math.sqrt(x[i][0]);
				//x[i][2] = x[i][0] * x[i][0];

				i++;
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
		}
		
		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		regression.newSampleData(y, x);
		
		chartMethod.regression = new Chart.Regression();
		chartMethod.regression.parameters = regression.estimateRegressionParameters();
		chartMethod.regression.parametersStandardErrors = regression.estimateRegressionParametersStandardErrors();
		chartMethod.regression.parametersVariance = regression.estimateRegressionParametersVariance();
		chartMethod.regression.standardError = regression.estimateRegressionStandardError();
		chartMethod.regression.residuals = regression.estimateResiduals();
		chartMethod.regression.errorVariance = regression.estimateErrorVariance();
		chartMethod.regression.RSquared = regression.calculateRSquared();
		chartMethod.regression.adjustedRSquared = regression.calculateAdjustedRSquared();
		chartMethod.regression.residualSumOfSquares = regression.calculateResidualSumOfSquares();
		chartMethod.regression.totalSumOfSquares = regression.calculateTotalSumOfSquares();
	}

	private ArrayList<Object[]> produceInvocations(Method method) {
		// start with empty invocation
		ArrayList<Object[]> invocations = new ArrayList<Object[]>(1);
		invocations.add(new Object[0]);

		// iterate over parameters, multiplying current invocations by number of available datapoints
		for (Class<?> type : method.getParameterTypes()) {
			List<Field> points = datapoints.get(type);
			
			if (points.isEmpty())
				throw new IllegalArgumentException("No @datapoint found for type " + type);
			
			ArrayList<Object[]> newInvocations = new ArrayList<Object[]>(invocations.size() * points.size());
			
			for (Field point : points) {
				try {
					Object value = point.get(null);
					for (Object[] invocation : invocations) {
						Object[] newInvocation = new Object[invocation.length + 1];
						System.arraycopy(invocation, 0, newInvocation, 0, invocation.length);
						newInvocation[invocation.length] = value;
						newInvocations.add(newInvocation);
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
			invocations = newInvocations;
		}
		
		return invocations;
	}
}
