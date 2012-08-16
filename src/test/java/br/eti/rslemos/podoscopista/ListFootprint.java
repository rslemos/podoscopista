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
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ListFootprint {
	@Datapoint public static int ZERO = 0;
	@Datapoint public static int ONE = 1;
	@Datapoint public static int TEN = 10;
	@Datapoint public static int HUNDRED = 100;
	@Datapoint public static int THOUSAND = 1000;
	@Datapoint public static int TEN_THOUSAND = 10000;
	@Datapoint public static int HUNDRED_THOUSAND = 100000;
	@Datapoint public static int MILLION = 1000000;
	
	@Footprint("Object array")
	public Object measureObjectArray(int elements) {
		return fill(new Object[elements], elements);
	}
	
	@Footprint("ArrayList initialized with exact capacity")
	public Object measureArrayListInit(int elements) {
		return fill(new ArrayList(elements), elements);
	}
	
	@Footprint("ArrayList initialized with 0 capacity")
	public Object measureArrayListFillOneAtATime(int elements) {
		return fill(new ArrayList(0), elements);
	}
	
	@Footprint("ArrayList after #trimToSize()")
	public Object measureArrayListTrimToSize(int elements) {
		ArrayList list = fill(new ArrayList(0), elements);
		list.trimToSize();
		return list;
	}
	
	@Footprint("LinkedList")
	public Object measureLinkedList(int elements) {
		return fill(new LinkedList(), elements);
	}

	@Footprint("throws Exception")
	public Object throwsException() {
		throw new RuntimeException();
	}
	
	@Footprint("exaust memory")
	public Object exaustMemory() {
		return new int[Integer.MAX_VALUE];
	}
	
	private static <T extends List<?>> T fill(T list, int elements) {
		for (int i = 0; i < elements; i++) {
			list.add(null);
		}
		
		return list;
	}

	private static <T> T[] fill(T[] array, int elements) {
		for (int i = 0; i < elements; i++) {
			array[i] = null;
		}
		
		return array;
	}

}
