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

import java.lang.reflect.Method;
import java.util.ArrayList;

import objectexplorer.MemoryMeasurer;
import objectexplorer.ObjectGraphMeasurer;

public class FootprintRunner {

	private Class<?> clazz;
	private ArrayList<Method> methods;
	private Chart chart;

	public Chart run(Class<?> clazz) {
		try {
			this.clazz = clazz;
			
			collectAnnotatedMethods();
	
			chart = new Chart(clazz.getName(), methods.size());
	
			for (Method method : methods) {
				processMethod(method);
			}
			
			return chart;
		} finally {
			this.clazz = null;
			methods = null;
			chart = null;
		}
	}

	private void collectAnnotatedMethods() {
		methods = new ArrayList<Method>(clazz.getMethods().length);
		
		for (Method method : clazz.getMethods()) {
			if (method.getAnnotation(Footprint.class) != null) {
				if (method.getParameterTypes().length > 0)
					throw new IllegalArgumentException();
				
				methods.add(method);
			}
		}
		
		methods.trimToSize();
	}

	private void processMethod(Method method) {
		Chart.Method chartMethod = chart.new Method(method.getName(), method.getAnnotation(Footprint.class).value());

		try {
			Object thiz = clazz.newInstance();
			Object result = method.invoke(thiz, new Object[0]);
			
			chartMethod.footprint = ObjectGraphMeasurer.measure(result);
			chartMethod.size = MemoryMeasurer.measureBytes(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
