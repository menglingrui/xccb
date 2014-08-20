package nc.bs.pub.action;

import java.util.Hashtable;
import nc.bs.pub.compiler.AbstractCompiler2;
import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.pf.PfUtilActionVO;
import nc.vo.uap.pf.PFBusinessException;
/**
 * 公用成本核算单
 * @author dp
 */
public class N_CB05_APPROVECOND extends AbstractCompiler2 {
	private java.util.Hashtable m_methodReturnHas = new java.util.Hashtable();
	private Hashtable m_keyHas = null;


	public N_CB05_APPROVECOND() {
		super();
	}

	/*
	 * 备注：平台编写规则类 接口执行类
	 */
	public Object runComClass(PfParameterVO vo) throws BusinessException {
		try {
			super.m_tmpVo = vo;
			// ####本脚本必须含有返回值,返回DLG和PNL的组件不允许有返回值####
			Object retObj = null;
			// ####重要说明：该组件为返回PNL的界面方法不能进行修改####
			String[] infoAry = new String[3];
			infoAry[0] = (String) getVo().getParentVO().getAttributeValue(
					"pk_billtype");
			infoAry[1] = getVo().getParentVO().getPrimaryKey();
			infoAry[2] = (String) getVo().getParentVO().getAttributeValue(
					"pk_busitype");
			setUserObj(infoAry);
			// ########该方法的下面脚本中不允许出现return语句#########
			PfUtilActionVO actionVo = new PfUtilActionVO();
			actionVo.setIsDLG(true);
			actionVo.setClassName("nc.ui.pub.workflownote.FlowStateDlg");
			actionVo.setMethod("");
			actionVo.setParameter("");
			actionVo.setUIObj(m_tmpVo.m_userObj);
			return actionVo;
			// ##################################################
		} catch (Exception ex) {
			if (ex instanceof BusinessException)
				throw (BusinessException) ex;
			else
				throw new PFBusinessException(ex.getMessage(), ex);
		}
	}

	/*
	 * 备注：平台编写原始脚本
	 */
	public String getCodeRemark() {
		return "	//####本脚本必须含有返回值,返回DLG和PNL的组件不允许有返回值####\n Object retObj  =null;\n //####重要说明：该组件为返回PNL的界面方法不能进行修改####\n String[] infoAry=new String[3];\n infoAry[0]=(String)getVo().getParentVO().getAttributeValue(\"pk_billtype\");\n infoAry[1]=getVo().getParentVO().getPrimaryKey();\n infoAry[2]=(String)getVo().getParentVO().getAttributeValue(\"pk_busitype\");\n setUserObj(infoAry);\n //########该方法的下面脚本中不允许出现return语句#########\n PfUtilActionVO actionVo=new PfUtilActionVO();\n actionVo.setIsDLG(true);\n actionVo.setClassName(\"nc.ui.pub.workflownote.FlowStateDlg\");\n actionVo.setMethod( \"\");\n actionVo.setParameter( \"\");\n actionVo.setUIObj(m_tmpVo.m_userObj);\n return actionVo;\n //##################################################\n";
	}

	/*
	 * 备注：设置脚本变量的HAS
	 */
	private void setParameter(String key, Object val) {
		if (m_keyHas == null) {
			m_keyHas = new Hashtable();
		}
		if (val != null) {
			m_keyHas.put(key, val);
		}
	}
}
