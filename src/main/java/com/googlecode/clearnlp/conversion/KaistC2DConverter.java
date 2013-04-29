/**
* Copyright 2012-2013 University of Massachusetts Amherst
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
*   
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.googlecode.clearnlp.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.googlecode.clearnlp.constituent.CTLibKaist;
import com.googlecode.clearnlp.constituent.CTNode;
import com.googlecode.clearnlp.constituent.CTTree;
import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPLibKr;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.headrule.HeadRule;
import com.googlecode.clearnlp.headrule.HeadRuleMap;


/**
 * Constituent to dependency converter for KAIST Treebank.
 * @since 1.3.2
 * @author Jinho D. Choi ({@code choijd@colorado.edu})
 */
public class KaistC2DConverter extends AbstractC2DConverter
{
	private final Pattern DELIM_PLUS  = Pattern.compile("\\+");
	private final int SIZE_HEAD_FLAGS = 6;
	
	public KaistC2DConverter(HeadRuleMap headrules)
	{
		super(headrules);
	}
	
	@Override
	public DEPTree toDEPTree(CTTree cTree)
	{
		setHeads(cTree.getRoot());
		return getDEPTree(cTree);
	}
	
	// ============================= Find heads =============================
	
	@Override
	protected void setHeadsAux(HeadRule rule, CTNode curr)
	{
		findConjuncts(rule, curr);

		CTNode head = getHead(rule, curr.getChildren(), SIZE_HEAD_FLAGS);
		curr.c2d = new C2DInfo(head);
	}
	
	/**
	 * If the specific node contains a coordination structure, find the head of each coordination.
	 * @param curr the specific node to be compared. 
	 * @return {@code true} if this node contains a coordination structure.
	 */
	private void findConjuncts(HeadRule rule, CTNode curr)
	{
		List<CTNode> children = curr.getChildren();
		int i, size = children.size();
		String label;
		CTNode child;
		
		for (i=0; i<size; i++)
		{
			child = children.get(i);
			
			if ((label = getSpecialLabel(child)) != null)
				child.addFTag(label);
			else
				break;
		}
		
		if (CTLibKaist.containsCoordination(children.subList(i,size), DELIM_PLUS))
		{
			for (; i<size; i++)
			{
				child = children.get(i);
				
				if ((label = getConjunctLabel(curr, child)) != null)
					child.addFTag(label);
			}
		}
	}
	
	private String getConjunctLabel(CTNode parent, CTNode child)
	{
		String label;
		
		if (CTLibKaist.isConjunct(child, DELIM_PLUS))
			return DEPLibKr.DEP_CONJ;
		
		if ((label = getSpecialLabel(child)) != null)
			return label;
		
		if (child.isPTag(CTLibKaist.PTAG_ADVP) && !parent.isPTag(CTLibKaist.PTAG_ADVP))
			return DEPLibKr.DEP_ADV;
		
		return DEPLibKr.DEP_CONJ;
	}
	
	@Override
	protected int getHeadFlag(CTNode child)
	{
		if (child.c2d.hasHead())
			return -1;
		
		if (child.isPTag(CTLibKaist.PTAG_AUXP))
			return 1;
		
		if (child.isPTag(CTLibKaist.PTAG_IP))
			return 2;
		
		if (child.hasFTag(CTLibKaist.FTAG_PRN))
			return 3;
		
		if (CTLibKaist.isOnlyEJX(child, DELIM_PLUS))
			return 4;
		
		if (CTLibKaist.isPunctuation(child))
			return 5;
		
		return 0;
	}
	
	// ============================= Get Kaist labels ============================= 
	
	@Override
	protected String getDEPLabel(CTNode C, CTNode P, CTNode p)
	{
		String label;
		
		if ((label = getFunctionLabel(C)) != null)
			return label;
		
		if ((label = getSpecialLabel(C)) != null)
			return label;
		
		String[] pTags = CTLibKaist.getLastPOSTags(C, DELIM_PLUS);
		String   pTag  = pTags[pTags.length-1];
		
		if ((label = getRoleLabel(pTag)) != null)
			return label;
		
		if ((label = getSubLabel(pTags)) != null)
			return label;
		
		if ((label = getSimpleLabel(C)) != null)
			return label;
		
		CTNode d = C.c2d.getDependencyHead();

		if ((label = getSimpleLabel(d)) != null)
			return label;
		
		if (P.isPTag(CTLibKaist.PTAG_ADJP))
			return DEPLibKr.DEP_AMOD;
		
		if (P.isPTag(CTLibKaist.PTAG_ADVP))
			return DEPLibKr.DEP_ADV;
		
		if (P.isPTag(CTLibKaist.PTAG_NP))
			return DEPLibKr.DEP_NMOD;

		if (P.isPTag(CTLibKaist.PTAG_VP))
			return DEPLibKr.DEP_VMOD;
		
		return DEPLibKr.DEP_DEP;
	}
	
	// KAIST
	private String getFunctionLabel(CTNode C)
	{
		if (C.hasFTag(CTLibKaist.FTAG_PRN))
			return DEPLibKr.DEP_PRN;
		
		List<String> list = new ArrayList<String>(C.getFTags());
		return (list.size() == 1) ? list.get(0) : null;
	}
	
	// KAIST
	private String getSpecialLabel(CTNode C)
	{
		CTNode d = C.c2d.getDependencyHead();
		
		if (CTLibKaist.isPunctuation(C) || CTLibKaist.isPunctuation(d))
			return DEPLibKr.DEP_PUNCT;
		
		if (CTLibKaist.isOnlyEJX(C, DELIM_PLUS))
			return DEPLibKr.DEP_EJX;
		
		if (C.isPTag(CTLibKaist.PTAG_AUXP))
			return DEPLibKr.DEP_AUX;
		
		if (CTLibKaist.isConjunction(C, DELIM_PLUS))
			return DEPLibKr.DEP_CC;
		
		if (CTLibKaist.isConjunct(C, DELIM_PLUS))
			return DEPLibKr.DEP_CONJ;
		
		return null;
	}
	
	private String getRoleLabel(String pTag)
	{
		if (pTag.equals(CTLibKaist.POS_JCC))
			return DEPLibKr.DEP_COMP;
		
		if (pTag.equals(CTLibKaist.POS_JCO))
			return DEPLibKr.DEP_OBJ;
		
		if (pTag.equals(CTLibKaist.POS_JCS))
			return DEPLibKr.DEP_SBJ;
		
		if (pTag.equals(CTLibKaist.POS_JCT))
			return DEPLibKr.DEP_COMIT;
		
		if (pTag.equals(CTLibKaist.POS_JXT))
			return DEPLibKr.DEP_TPC;
		
		return null;
	}
	
	private String getSubLabel(String[] pTags)
	{
		for (String pTag : pTags)
		{
			if (pTag.equals(CTLibKaist.POS_ECS))
				return DEPLibKr.DEP_SUB;
			else if (pTag.equals(CTLibKaist.POS_JCR))
				return DEPLibKr.DEP_QUOT;
		}
		
		return null;
	}
	
	private String getSimpleLabel(CTNode C)
	{
		if (C.isPTag(CTLibKaist.PTAG_MODP) || CTLibKaist.isAdnoun(C, DELIM_PLUS))
			return DEPLibKr.DEP_ADN;
		
		if (C.isPTag(CTLibKaist.PTAG_ADVP) || CTLibKaist.isAdverb(C, DELIM_PLUS))
			return DEPLibKr.DEP_ADV;
		
		if (C.isPTagAny(CTLibKaist.PTAG_IP) || CTLibKaist.isInterjection(C, DELIM_PLUS))
			return DEPLibKr.DEP_INTJ;
		
		return null;
	}
		
	// ============================= Get a dependency tree =============================
	
	private DEPTree getDEPTree(CTTree cTree)
	{
		DEPTree dTree = initDEPTree(cTree);
		addDEPHeads(dTree, cTree);
		
		if (dTree.containsCycle())
			System.err.println("Error: cyclic dependencies exist");
		
		return dTree;
	}
	
	/** Adds dependency heads. */
	private void addDEPHeads(DEPTree dTree, CTTree cTree)
	{
		int currId, headId, size = dTree.size(), rootCount = 0;
		DEPNode dNode;
		CTNode cNode;
		String label;
		
		for (currId=1; currId<size; currId++)
		{
			dNode  = dTree.get(currId);
			cNode  = cTree.getToken(currId-1);
			headId = cNode.c2d.d_head.getTokenId() + 1;
			
			if (currId == headId)	// root
			{
				dNode.setHead(dTree.get(DEPLib.ROOT_ID), DEPLibKr.DEP_ROOT);
				rootCount++;
			}
			else
			{
				label = cNode.c2d.s_label;
				dNode.setHead(dTree.get(headId), label);
			}
		}
		
		if (rootCount > 1)	System.err.println("Warning: multiple roots exist");
	}
}
