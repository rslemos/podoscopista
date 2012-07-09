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

@SuppressWarnings("rawtypes")
public class ListFootprint {
	@Footprint("Object array")
	public Object measureObjectArray() {
		return new Object[0];
	}
	
	@Footprint("ArrayList initialized with exact capacity")
	public Object measureArrayListInit() {
		return new ArrayList(0);
	}
	
	@Footprint("ArrayList after #trimToSize()")
	public Object measureArrayListTrimToSize() {
		ArrayList list = new ArrayList();
		list.trimToSize();
		return list;
	}
	
	@Footprint("LinkedList")
	public Object measureLinkedList() {
		return new LinkedList();
	}

}
