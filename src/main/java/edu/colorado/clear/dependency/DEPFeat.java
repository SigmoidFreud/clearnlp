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
package edu.colorado.clear.dependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.colorado.clear.reader.DEPReader;

/**
 * Dependency feature map.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code choijd@colorado.edu})
 */
@SuppressWarnings("serial")
public class DEPFeat extends HashMap<String,String>
{
	/** The delimiter between feature values ({@code ","}). */
	static public final String DELIM_VALUES    = ",";
	/** The delimiter between features ({@code "|"}). */
	static public final String DELIM_FEATS     = "|";
	/** The delimiter between keys and values ({@code "="}). */
	static public final String DELIM_KEY_VALUE = "=";

	/** Constructs an empty feature map. */
	public DEPFeat() {}
	
	/**
	 * Constructs a feature map by decoding the specific features.
	 * @param feats the features to be added.
	 * See the {@code feats} parameter in {@link DEPFeat#add(String)}.
	 */
	public DEPFeat(String feats)
	{
		add(feats);
	}
		
	/**
	 * Adds the specific features to this map.
	 * @param feats {@code "_"} or {@code feat(|feat)*}.<br>
	 * {@code "_"}: indicates no feature.<br>
	 * {@code feat ::= key=value} (e.g., {@code pos=VBD}).
	 */
	public void add(String feats)
	{
		if (feats.equals(DEPReader.BLANK_COLUMN))
			return;
		
		String key, value;
		int    idx;
		
		for (String feat : feats.split("\\"+DELIM_FEATS))
		{
			idx = feat.indexOf(DELIM_KEY_VALUE);
			
			if (idx > 0)
			{
				key   = feat.substring(0, idx);
				value = feat.substring(idx+1);
				put(key, value);				
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractMap#toString()
	 */
	public String toString()
	{
		if (isEmpty())	return DEPReader.BLANK_COLUMN;
		
		StringBuilder build = new StringBuilder();
		List<String>  keys  = new ArrayList<String>(keySet());
		
		Collections.sort(keys);
		for (String key : keys)
		{
			build.append(DELIM_FEATS);
			build.append(key);
			build.append(DELIM_KEY_VALUE);
			build.append(get(key));
		}
		
		return build.toString().substring(DELIM_FEATS.length());
	}
}
