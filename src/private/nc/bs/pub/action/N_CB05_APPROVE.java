package nc.bs.pub.action;

import java.util.Hashtable;

import nc.bs.dao.BaseDAO;
import nc.bs.pub.compiler.AbstractCompiler2;
import nc.bs.pub.compiler.IWorkFlowRet;
import nc.bs.xccb.costaccout.CostAccountBO;
import nc.bs.xccb.pub.AccountBO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.trade.pub.IBillStatus;
import nc.vo.uap.pf.PFBusinessException;
/**
 * 公用成本核算单
 * @author dp
 */
public class N_CB05_APPROVE extends AbstractCompiler2 {
	private java.util.Hashtable m_methodReturnHas = new java.util.Hashtable();
	private Hashtable m_keyHas = null;

	public N_CB05_APPROVE() {
		super();
	}
    private BaseDAO dao=new BaseDAO();
	public BaseDAO getDao() {
		if (dao == null) {
			dao = new BaseDAO();
		}
		return dao;
		}

	/*
	 * 备注：平台编写规则类 接口执行类
	 */
	public Object runComClass(PfParameterVO vo) throws BusinessException {
		try {
			super.m_tmpVo = vo;
			
			Object m_sysflowObj = procActionFlow(vo);
			if (m_sysflowObj != null) {
				nc.bs.pub.compiler.IWorkFlowRet work=(IWorkFlowRet) m_sysflowObj;
				AggregatedValueObject billvo=  (AggregatedValueObject) work.m_inVo;
				SuperVO hvo=(SuperVO) billvo.getParentVO();
				//处理驳回到制单人 单据状态出错
				if(PuPubVO.getInteger_NullAs(hvo.getAttributeValue("vbillstatus"), -2)==-1){
					hvo.setAttributeValue("vbillstatus", IBillStatus.FREE);
					getDao().updateVO(hvo);
					return m_sysflowObj;
				}
				return m_sysflowObj;
			}
			// ####该组件为单动作工作流处理结束...不能进行修改####
			Object retObj = null;
			setParameter("currentVo", vo.m_preValueVo);
//			ProCreateBO bo=new ProCreateBO();
//			bo.sendMessage(vo.m_preValueVo,vo);	
			AccountBO bo=new AccountBO();
			bo.sendMessage(vo);
			//发送会计平台
			CostAccountBO bo1=new CostAccountBO();
			bo1.sendMessage(vo.m_preValueVo,vo);
			
			retObj = runClass("nc.bs.xccb.pub.HYBillApprove", "approveHYBill",
					"nc.vo.pub.AggregatedValueObject:01", vo, m_keyHas,
					m_methodReturnHas);
			
			return retObj;
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
		return "	//####该组件为单动作工作流处理开始...不能进行修改####\nprocActionFlow@@;\n//####该组件为单动作工作流处理结束...不能进行修改####\nObject  retObj  =null;\n setParameter(\"currentVo\",vo.m_preValueVo);           \nretObj =runClassCom@ \"nc.bs.pp.pp0201.ApproveAction\", \"approveHYBill\", \"nc.vo.pub.AggregatedValueObject:01\"@;\n            ArrayList ls = (ArrayList)getUserObj();\n       \n        setParameter(\"userOpt\",ls.get(1));               \n            runClassCom@ \"nc.bs.pp.pp0201.ApproveAction\", \"afterApprove\", \"&userOpt:java.lang.Integer,nc.vo.pub.AggregatedValueObject:01\"@;               \nreturn retObj;\n";
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
