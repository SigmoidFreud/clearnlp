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
package edu.colorado.clear.propbank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import com.carrotsearch.hppc.IntOpenHashSet;

import edu.colorado.clear.constituent.CTTree;

/**
 * PropBank argument.
 * See <a target="_blank" href="http://code.google.com/p/clearnlp/source/browse/trunk/src/edu/colorado/clear/test/propbank/PBArgTest.java">PBArgTest</a> for the use of this class.
 * @see PBLoc
 * @since v0.1
 * @author Jinho D. Choi ({@code choijd@colorado.edu})
 */
public class PBArg implements Comparable<PBArg>
{
	/** The label of this argument. */
	public    String      label;
	/** The locations of this argument. */
	protected List<PBLoc> l_locs;
	
	/** Constructs a PropBank argument. */
	public PBArg()
	{
		l_locs = new ArrayList<PBLoc>();
	}
	
	/**
	 * Constructs a PropBank argument using the specific string.
	 * @param str {@code <location>(<type><location>)*-label}.
	 */
	public PBArg(String str)
	{
		int    idx;
		String type;

		l_locs = new ArrayList<PBLoc>();
		idx = str.indexOf(PBLib.DELIM_LABEL);
	
		if (idx == -1)
		{
			System.err.println("Error: illegal format - "+str);
			System.exit(1);
		}
		
		label = str.substring(idx+1);
		StringTokenizer tok = new StringTokenizer(str.substring(0, idx), PBLib.LOC_TYPES, true);
		
		if (!tok.hasMoreTokens())
		{
			System.err.println("Error: illegal format - "+str);
			System.exit(1);
		}
		
		addLoc(new PBLoc(tok.nextToken(), ""));
		
		while (tok.hasMoreTokens())
		{
			type = tok.nextToken();
		
			if (!tok.hasMoreTokens())
			{
				System.err.println("Error: illegal format - "+str);
				System.exit(1);
			}
			
			addLoc(new PBLoc(tok.nextToken(), type));
		}
	}
	
	/**
	 * Returns {@code true} if the specific label equals to this argument's label.
	 * @param label the label to be compared.
	 * @return {@code true} if the specific label equals to this argument's label.
	 */
	public boolean isLabel(String label)
	{
		return this.label.equals(label);
	}
	
	/**
	 * Returns the index'th location of this argument.
	 * @param index the index of the location to be returned.
	 * @return the index'th location of this argument.
	 */
	public PBLoc getLoc(int index)
	{
		return l_locs.get(index);
	}
	
	/**
	 * Returns the location matching the specific terminal ID and height.
	 * If there is no such location, returns {@code null}.
	 * @param terminalId the terminal ID to be compared.
	 * @param height the height to be compared.
	 * @return the location matching the specific terminal ID and height.
	 */
	public PBLoc getLoc(int terminalId, int height)
	{
		for (PBLoc loc : l_locs)
		{
			if (loc.equals(terminalId, height))
				return loc;
		}
		
		return null;
	}
	
	/**
	 * Returns all the locations of this argument.
	 * @return all the locations of this argument.
	 */
	public List<PBLoc> getLocs()
	{
		return l_locs;
	}
	
	/**
	 * Adds the specific location to this argument.
	 * @param loc the location to be added.
	 */
	public void addLoc(PBLoc loc)
	{
		l_locs.add(loc);
	}
	
	/**
	 * Adds the specific collection of locations to this argument.
	 * @param locs the collection of locations to be added.
	 */
	public void addLocs(Collection<PBLoc> locs)
	{
		l_locs.addAll(locs);
	}
	
	/**
	 * Removes the first location matching the specific terminal ID and height from this argument.
	 * @param terminalId the terminal ID of the location.
	 * @param height the height of the location.
	 */
	public void removeLoc(int terminalId, int height)
	{
		for (PBLoc loc : l_locs)
		{
			if (loc.equals(terminalId, height))
			{
				l_locs.remove(loc);
				break;
			}
		}
	}
	
	/**
	 * Removes the specific collection of locations from this argument.
	 * @param locs the collection of locations to be removed.
	 */
	public void removeLocs(Collection<PBLoc> locs)
	{
		l_locs.removeAll(locs);
		if (!l_locs.isEmpty())	l_locs.get(0).type = "";
	}
	
	/**
	 * Replaces the locations of this argument.
	 * @param locs the locations to be added.
	 */
	public void replaceLocs(List<PBLoc> locs)
	{
		l_locs = locs;
	}
	
	/**
	 * Sorts the locations of this argument by their terminal IDs.
	 * @see PBLoc#compareTo(PBLoc)
	 */
	public void sortLocs()
	{
		if (l_locs.isEmpty())	return;
		
		Collections.sort(l_locs);
		PBLoc fst = l_locs.get(0), loc;
		
		if (!fst.type.equals(""))
		{
			for (int i=1; i<l_locs.size(); i++)
			{
				loc = l_locs.get(i);
				
				if (loc.type.equals(""))
				{
					loc.type = fst.type;
					break;
				}
			}
			
			fst.type = "";
		}
	}
	
	/**
	 * Returns the number of locations in this argument.
	 * @return the number of locations in this argument.
	 */
	public int getLocSize()
	{
		return l_locs.size();
	}
	
	/**
	 * Returns {@code true} if this argument has no location.
	 * @return {@code true} if this argument has no location.
	 */
	public boolean hasNoLoc()
	{
		return l_locs.isEmpty();
	}
	
	public boolean hasType(String type)
	{
		for (PBLoc loc : l_locs)
		{
			if (loc.isType(type))
				return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		for (PBLoc loc : l_locs)
			build.append(loc.toString());
				
		build.append(PBLib.DELIM_LABEL);
		build.append(label);
		
		return build.toString();
	}
	
	@Override
	public int compareTo(PBArg arg)
	{
		return getLoc(0).compareTo(arg.getLoc(0));
	}
	
	public IntOpenHashSet getTerminalIdSet(CTTree tree)
	{
		IntOpenHashSet set = new IntOpenHashSet();
		
		for (PBLoc loc : l_locs)
			set.addAll(tree.getNode(loc).getSubTerminalIdSet());
		
		return set;
	}
}