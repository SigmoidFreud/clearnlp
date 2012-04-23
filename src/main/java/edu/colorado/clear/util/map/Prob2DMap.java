/**
* Copyright (c) 2011, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package edu.colorado.clear.util.map;

import java.util.HashMap;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.carrotsearch.hppc.cursors.ObjectCursor;

import edu.colorado.clear.util.pair.Pair;
import edu.colorado.clear.util.pair.StringDoublePair;


@SuppressWarnings("serial")
public class Prob2DMap extends HashMap<String,ObjectIntOpenHashMap<String>>
{
	static private final String TOTAL = "_T_";
	private int i_total;
	
	public void add(String key, String value)
	{
		ObjectIntOpenHashMap<String> map;
		
		if (containsKey(key))
		{
			map = get(key);
			
			map.put(value, map.get(value)+1);
			map.put(TOTAL, map.get(TOTAL)+1);
		}
		else
		{
			map = new ObjectIntOpenHashMap<String>();
			put(key, map);

			map.put(value, 1);
			map.put(TOTAL, 1);
		}
		
		i_total++;
	}
	
	public StringDoublePair[] getProb1D(String key)
	{
		Pair<Double,StringDoublePair[]> p = getProb1DAux(key);
		return (p == null) ? null : p.o2;
	}
	
	public StringDoublePair[] getProb2D(String key)
	{
		Pair<Double,StringDoublePair[]> p = getProb1DAux(key);
		if (p == null)	return null;
		
		double prior = p.o1;
		StringDoublePair[] probs = p.o2;
		
		for (StringDoublePair prob : probs)
			prob.d *= prior;
		
		return probs;
	}
	
	private Pair<Double,StringDoublePair[]> getProb1DAux(String key)
	{
		ObjectIntOpenHashMap<String> map = get(key);
		if (map == null)	return null;
		
		StringDoublePair[] probs = new StringDoublePair[map.size()-1];
		int i = 0, total = map.get(TOTAL);
		String value;
		
		for (ObjectCursor<String> cur : map.keys())
		{
			value = cur.value;
			
			if (!value.equals(TOTAL))
				probs[i++] = new StringDoublePair(value, (double)map.get(value)/total);
		}
		
		double prior = (double)total / i_total; 
		return new Pair<Double,StringDoublePair[]>(prior, probs);
	}
}