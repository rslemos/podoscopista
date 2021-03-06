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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import objectexplorer.MemoryMeasurer;
import objectexplorer.ObjectGraphMeasurer;

import br.eti.rslemos.podoscopista.data.Sample;

import com.google.common.collect.ArrayListMultimap;

public class FootprintRunner {

	private Class<?> clazz;
	private ArrayListMultimap<Class<?>, Field> datapoints;
	private ArrayList<Method> methods;
	
	private ArrayList<Sample> samples;
	private Sample sampleTemplate;

	public List<Sample> run(Class<?> clazz) {
		try {
			this.clazz = clazz;
			
			collectDataPoints();
			collectAnnotatedMethods();
	
			samples = new ArrayList<Sample>();
			sampleTemplate = new Sample();
			sampleTemplate.className = clazz.getName();
			
			for (Method method : methods) {
				processMethod(method);
			}
			
			samples.trimToSize();
			return samples;
		} finally {
			this.clazz = null;
			datapoints = null;
			methods = null;
			samples = null;
			sampleTemplate = null;
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
		
		sampleTemplate.methodName = method.getName();
		sampleTemplate.methodDescription = method.getAnnotation(Footprint.class).value();

		for (Object[] invocation : invocations) {
			Sample sample = sampleTemplate.clone();
			samples.add(sample);

			sample.parameters = invocation;
			try {
				try {
					Object thiz = clazz.newInstance();
					Object result = method.invoke(thiz, invocation);
					
					sample.footprint = ObjectGraphMeasurer.measure(result);
					sample.size = MemoryMeasurer.measureBytes(result);
				} catch (InvocationTargetException e) {
					throw e.getCause();
				} finally {
					System.gc();
				}
			} catch (Throwable e) {
				sample.size = -1;
				sample.exception = new Sample.Exception();
				sample.exception.className = e.getClass().getName();
				sample.exception.message = e.getMessage();
				sample.exception.stackTrace = e.getStackTrace();
			}
			
		}
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
