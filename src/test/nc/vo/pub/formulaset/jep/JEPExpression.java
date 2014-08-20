/* 
 * Copyright (c) 2002, Cameron Zemek
 * 
 * This file is part of JSpread.
 * 
 * JSpread is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JSpread is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nc.vo.pub.formulaset.jep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import nc.bs.logging.Logger;
import nc.vo.pub.BusinessRuntimeException;
import nc.vo.pub.formulaset.FormulaException;
import nc.vo.pub.formulaset.FormulaThread;
import nc.vo.pub.formulaset.VarryVO;
import nc.vo.pub.formulaset.core.ASTConstant;
import nc.vo.pub.formulaset.core.ASTFunNode;
import nc.vo.pub.formulaset.core.ASTVarNode;
import nc.vo.pub.formulaset.core.EvaluatorVisitor;
import nc.vo.pub.formulaset.core.Node;
import nc.vo.pub.formulaset.core.SimpleNode;
import nc.vo.pub.formulaset.core.SymbolTable;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.pu.PuPubVO;

/**
 * JEP����ʽ�࣬��װ�˱��ʽ�ĸ��ֲ���
 * 
 * @nopublish
 * @author <a href="mailto:grom@capsicumcorp.com">Cameron Zemek</a>
 * @version 1.0
 */
public class JEPExpression implements nc.vo.pub.formulaset.jep.Expression,
		Serializable {
	protected EvaluatorVisitor evaluator = FormulaThread.getCurrentEvVisitor();

	protected String expression;

	protected Node topNode;

	protected SymbolTable symbols = null; // ������

	protected String m_leftName = null; // ��ʽ��ߴ���ֵ�ı�����

	protected VarryVO m_varryvo = null; // ������Ϣ

	// �Ƿ���Ҫ���½���
	protected boolean m_bNeedReParse = false;

	// ��ʽ�Ƿ���ȷ--���������еĴ���
	protected String m_errorMsg = null;
	
	/**
	 * ��չ���ʽ
	 */
	protected List<JEPExpression> extendExpressionList;
	
	public void addExtendExpression(String key)
	{
		JEPExpression exp = new JEPExpression(expression,getTopNode(),null);
		VarryVO vo = new VarryVO();
		vo.setFormulaName(key);
		exp.setLeftName(key);
		exp.setVarryVO(vo);
		if(extendExpressionList==null)
			extendExpressionList = new ArrayList<JEPExpression>();
		extendExpressionList.add(exp);
	}

	public List<JEPExpression> getExtendExpressionList() {
		return extendExpressionList;
	}


	public String getErrorMsg() {
		return m_errorMsg;
	}

	public void setErrorMsg(String errormsg) {
		m_errorMsg = errormsg;
	}

	/**
	 * Creates a new instance of JEPExpression
	 */
	public JEPExpression(String expression, Node topNode, SymbolTable symbols) {
		this.expression = expression;
		this.topNode = topNode;
		this.symbols = symbols;
	}

	/**
	 * Duplicate an expression
	 */
	public JEPExpression(JEPExpression exp) {
		this.expression = exp.expression;
		this.topNode = exp.topNode;
		this.symbols = exp.symbols;
	} // end constructor

	/**
	 * ���ù�ʽ��Ҫ���½��� ���޸Ĺ�ʽ�﷨��ʱ����
	 * 
	 * @param value
	 */
	public void setNeedReParse(boolean value) {
		m_bNeedReParse = value;
	}

	/**
	 * ��JEP��ʽ�Ƿ���Ҫ���½��� һ������¶�����Ҫ���½��������ǣ� ����BDCache���棬�滻�˽ڵ�
	 * 
	 * @return
	 */
	public boolean isNeedReParse() {
		return m_bNeedReParse;
	}

	/**
	 * �ָ���ʽ����ΪBDCache���㱻�滻Ϊ�����Ľڵ�
	 * 
	 */
	public void restoreInsteadFunNode() {
		iterateTreeAndRestore(topNode);
	}

	/**
	 * 
	 * @param topNode
	 * @throws FormulaException
	 */
	public void iterateTreeAndRestore(Node node) {
		if (node == null)
			return;
		if (node instanceof ASTBDCacheResult) {
			ASTFunNode funnode = ((ASTBDCacheResult) node).getInsteadFunNode();
			node.jjtGetParent().jjtReplaceChild(node, funnode);
			if (topNode == node)
				setTopNode(funnode);
		}
		int nodenum = node.jjtGetNumChildren();
		if (nodenum == 0)
			return;
		for (int i = 0; i < nodenum; i++) {
			Node curnode = node.jjtGetChild(i);
			iterateTreeAndRestore(curnode);
		}
	}

	/**
	 * �滻��ʽ��ı������õ��滻��Ĺ�ʽ�ַ���
	 * 
	 * @param varnames -
	 *            map<ԭ������,�滻������>
	 */
	public String replaceVariables(Map varnames) {
		iterateAndReplaceVariable(topNode, varnames);
		// �����滻����ַ���
		StringBuffer resbuf = new StringBuffer();
		iterateAndGetNodeName(topNode, resbuf);
		// �����ʽ����
		setNeedReParse(true);
		return resbuf.toString();
	}

	private void iterateAndReplaceVariable(Node node, Map varnames) {
		if (node == null || varnames == null)
			return;
		if (node instanceof ASTVarNode) {
			ASTVarNode varnode = (ASTVarNode) node;
			String orgname = varnode.getName();
			String newVarName = (String) varnames.get(orgname);
			if (newVarName != null) {
				varnode.setName(newVarName);
				symbols.remove(orgname);
			}
		}
		int nodenum = node.jjtGetNumChildren();
		if (nodenum == 0)
			return;
		for (int i = 0; i < nodenum; i++) {
			Node curnode = node.jjtGetChild(i);
			iterateAndReplaceVariable(curnode, varnames);
		}
	}

	private void iterateAndGetNodeName(Node node, StringBuffer strbuffer) {
		if (node == null)
			return;

		boolean bFunName = false;
		String strOpt = null;
		if (node instanceof ASTConstant) {
			if (!(((ASTConstant) node).getValue() instanceof Number))
				strbuffer.append("\"").append(((ASTConstant) node).getValue())
						.append("\"");
			else
				strbuffer.append(((ASTConstant) node).getValue());
		} else if (node instanceof ASTVarNode) {
			strbuffer.append(((ASTVarNode) node).getName());
		} else if (node instanceof ASTFunNode) {
			ASTFunNode funNode = (ASTFunNode) node;
			if (funNode.getOperator() == null) {
				strbuffer.append(funNode.getName() + "(");
				bFunName = true;
			} else {
				strOpt = funNode.getOperator().getSymbol();
			}
		}

		int nodenum = node.jjtGetNumChildren();
		if (nodenum == 0)
			return;
		else if (nodenum == 2 && strOpt != null) {
			Node curnode = node.jjtGetChild(0);
			iterateAndGetNodeName(curnode, strbuffer);
			strbuffer.append(strOpt);
			curnode = node.jjtGetChild(1);
			iterateAndGetNodeName(curnode, strbuffer);
		} else {
			for (int i = 0; i < nodenum; i++) {
				Node curnode = node.jjtGetChild(i);
				iterateAndGetNodeName(curnode, strbuffer);
				if (i < nodenum - 1)
					strbuffer.append(",");
			}
		}

		if (bFunName) {
			strbuffer.append(")");
		}

	}

	/**
	 * Return the expression string
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Return the result of the expression
	 */
	public Object getResult() {
		try {
	
			
			//for end mlr 
			Object result = evaluator.getValue(topNode, null, symbols);
			Object finalResult = result;
			if(getExtendExpressionList()!=null)
			{
				List<JEPExpression> extendExpressionList = getExtendExpressionList();
				List<Object[]> objarrs = (List<Object[]>)result;
				if(objarrs.size()>0)
				{
					if(extendExpressionList.size()!=objarrs.size()-1)
					{
						throw new BusinessRuntimeException("����ֶ�һ���ѯʱ��ֵ�ֶεĸ�����Ҫ��ѯ���ֶ���Ŀ�������!��ʽ:"+getExpression());
					}
					finalResult = Arrays.asList(objarrs.get(0));
	                int m=1;
	                for (JEPExpression exdexp : extendExpressionList) {
	                	ASTConstant conv = new ASTConstant(0);
	                	conv.setValue(Arrays.asList(objarrs.get(m++)));
	                	exdexp.setTopNode(conv);
					}
				}
			}
			return finalResult;
			
		}
		catch (RuntimeException e1) 
		{
			throw e1;
		}
		catch (Exception e) {
			// ��ӹ�ʽ��ϸ���
			//Logger.debug("��ʽȡֵ����" + e.getMessage());//change to error
			Logger.error("��ʽȡֵ����" + e.getMessage(),e);
			evaluator.addToErrorList("���ڹ�ʽ��" + getExpression());
			return null; //5.02 �޸�Ϊ���쳣תΪ����ʱ�쳣...
			//throw new RuntimeException("Execption while execute the getValue in Formula ",e) ;
		}
	}

	/**
	 * Return the result of the expression
	 */
	public Object getResult1() {
		try {
			//for add mlr 
			if(topNode!=null&&topNode.jjtGetNumChildren()>0){
			   for(int i=0;i<topNode.jjtGetNumChildren();i++){
				   Node node=topNode.jjtGetChild(i);
		           changeDouble(node);
			   }
			}
	       //for end mlr 
			Object result = evaluator.getValue(topNode, null, symbols);
			Object finalResult = result;
			if(getExtendExpressionList()!=null)
			{
				List<JEPExpression> extendExpressionList = getExtendExpressionList();
				List<Object[]> objarrs = (List<Object[]>)result;
				if(objarrs.size()>0)
				{
					if(extendExpressionList.size()!=objarrs.size()-1)
					{
						throw new BusinessRuntimeException("����ֶ�һ���ѯʱ��ֵ�ֶεĸ�����Ҫ��ѯ���ֶ���Ŀ�������!��ʽ:"+getExpression());
					}
					finalResult = Arrays.asList(objarrs.get(0));
	                int m=1;
	                for (JEPExpression exdexp : extendExpressionList) {
	                	ASTConstant conv = new ASTConstant(0);
	                	conv.setValue(Arrays.asList(objarrs.get(m++)));
	                	exdexp.setTopNode(conv);
					}
				}
			}
			return finalResult;
			
		}
		catch (RuntimeException e1) 
		{
			throw e1;
		}
		catch (Exception e) {
			// ��ӹ�ʽ��ϸ���
			//Logger.debug("��ʽȡֵ����" + e.getMessage());//change to error
			Logger.error("��ʽȡֵ����" + e.getMessage(),e);
			evaluator.addToErrorList("���ڹ�ʽ��" + getExpression());
			return null; //5.02 �޸�Ϊ���쳣תΪ����ʱ�쳣...
			//throw new RuntimeException("Execption while execute the getValue in Formula ",e) ;
		}
	}
    //mlr
	public void changeDouble(Node node) {

		if (node instanceof ASTConstant) {
			UFDouble uf = PuPubVO.getUFDouble_NullAsZero(((ASTConstant) node).getValue());
			((ASTConstant) node).setValue(uf);
		} else {
			if (node != null && node.jjtGetNumChildren() > 0) {
				for (int j = 0; j < node.jjtGetNumChildren(); j++) {
                    changeDouble(node.jjtGetChild(j));
				}
			}
		}

	}
	/**
	 * Return the topNode of the expression.
	 * 
	 * @return
	 */
	public Node getTopNode() {
		return topNode;
	}

	public void setTopNode(Node node) {
		topNode = node;
	}

	/**
	 * Convert result into a string
	 */
	public String toString() {
		return getLeftName()+"->"+getExpression();
	}

	/**
	 * Return the map of variable names to values
	 */
	public Map getVariables() {
		return (Map) symbols;
	}

	/**
	 * ���ش�����Ϣ--��������в����Ĵ���
	 * 
	 * @return
	 */
	public String getErrorInfo() {
		return evaluator.getErrorInfo();
	}

	/**
	 * @return Returns the m_leftName.
	 */
	public String getLeftName() {
		return m_leftName;
	}

	/**
	 * @param name
	 *            The m_leftName to set.
	 */
	public void setLeftName(String name) {
		m_leftName = name;
	}

	/**
	 * @return Returns the varryvo.
	 */
	public VarryVO getVarryVO() {
		return m_varryvo;
	}

	/**
	 * @param varryvo
	 *            The varryvo to set.
	 */
	private void setVarryVO(VarryVO varryvo) {
		m_varryvo = varryvo;
	}

	public void retrieveVarList() {
		if (topNode == null || !(topNode instanceof SimpleNode))
			return;
		List<String> varList = ((SimpleNode) topNode).getVarList();
		varList.removeAll(JEPExpressionParser.getInnerVariables());

		VarryVO curvo = new VarryVO();
		curvo.setFormulaName(getLeftName());
		curvo.setVarry((String[]) varList.toArray(new String[0]));
		setVarryVO(curvo);
		//��չ���ʽvarry��ֵ
		if(getExtendExpressionList()!=null)
		{
			List<JEPExpression> extendExpressionList = getExtendExpressionList();
            for (JEPExpression exdexp : extendExpressionList) {
            	exdexp.getVarryVO().setVarry(curvo.getVarry());
			}
		}

	}
}
