package nc.bs.xccb.costaccout;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.exception.ComponentException;
import nc.itf.dap.pub.IDapSendMessage;
import nc.vo.dap.out.DapMsgVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.xccb.costaccount.CostAccountVO;
import nc.vo.xccb.costaccount.CostAccoutBVO;
import nc.vo.xccb.pub.XewcbPubTool;
import nc.vo.xccb.pub.Xewcbpubconst;
/**
 * 成本核算 后台业务处理类
 * @author mlr
 */
public class CostAccountBO {
	private BaseDAO dao=null;
	public BaseDAO getDao() {
	if (dao == null) {
		dao = new BaseDAO();
	}
	return dao;
}
	private XewcbPubTool  tool;
	public XewcbPubTool getTool(){
		if(tool==null){
			tool=new XewcbPubTool();
		}
		return tool;
	}
	/**
	 * 传会计平台
	 * @param billvo
	 */
	public void sendMessage(AggregatedValueObject billvo,PfParameterVO pfvo) throws ComponentException, BusinessException{
		if(billvo==null || billvo.getParentVO()==null || billvo.getChildrenVO()==null || billvo.getChildrenVO().length==0){
					
		}else{
			CostAccountVO hvo=(CostAccountVO) billvo.getParentVO();
			CostAccoutBVO[] bvos=(CostAccoutBVO[]) billvo.getChildrenVO();
			String  approvedate=pfvo.m_currentDate;
			DapMsgVO accountVo=getAccountVO(hvo,bvos,approvedate);
			accountVo.setMsgType(DapMsgVO.ADDMSG);
			accountVo.setRequestNewTranscation(false);	
			getIDapSendMessage().sendMessage(accountVo, billvo);	
		}
	}
	/**
	 * 传会计平台
	 * @param billvo
	 */
	public void sendMessage_del(AggregatedValueObject billvo,PfParameterVO pfvo) throws ComponentException, BusinessException{
		if(billvo==null || billvo.getParentVO()==null || billvo.getChildrenVO()==null || billvo.getChildrenVO().length==0){
					
		}else{
			CostAccountVO hvo=(CostAccountVO) billvo.getParentVO();
			CostAccoutBVO[] bvos=(CostAccoutBVO[]) billvo.getChildrenVO();
			String  approvedate=pfvo.m_currentDate;
			DapMsgVO accountVo=getAccountVO(hvo,bvos,approvedate);
			accountVo.setMsgType(DapMsgVO.DELMSG);
			accountVo.setRequestNewTranscation(false);	
			getIDapSendMessage().sendMessage(accountVo, billvo);	
		}
	}
    /**
     * 获得平台接口
     * @return
     * @throws ComponentException
     */
	public IDapSendMessage getIDapSendMessage() throws ComponentException {
		return ((IDapSendMessage) NCLocator.getInstance().lookup(IDapSendMessage.class.getName()));
	}
	/**
	 * 根据成本核算vo 获得会计平台vo
	 * @param head
	 * @param items
	 * @param approvedate
	 * @return
	 */
	private DapMsgVO getAccountVO(CostAccountVO head, CostAccoutBVO[] items,String approvedate) {
        nc.vo.dap.out.DapMsgVO PfStateVO = new nc.vo.dap.out.DapMsgVO();
		PfStateVO.setCorp(head.getPk_corp()); //公司主键2
		PfStateVO.setSys("JX"); //系统号PK3
		PfStateVO.setProc(head.getPk_billtype()); //单据类型及业务处理PK4
		PfStateVO.setBusiType(head.getPk_busitype());
		//业务类型PK5
		PfStateVO.setBusiName(null);
		PfStateVO.setProcMsg(head.getPrimaryKey()); //处理信息7,根据此消息查询平台所用的信息
		PfStateVO.setBillCode(head.getVbillno()); //单据编码8
	//	PfStateVO.set
		PfStateVO.setBusiDate(new UFDate(approvedate)); //业务日期9
		if (items != null) {
		    PfStateVO.setOperator(InvocationInfoProxy.getInstance().getUserCode()); //业务员10PK
			PfStateVO.setCurrency(Xewcbpubconst.pk_currency); //币种11PK
		}
		//取出总金额
		UFDouble znmy=new UFDouble(0.0);
		for(int i=0;i<items.length;i++){
			znmy=znmy.add(items[i].getNmy());
		}
		PfStateVO.setMoney(znmy); //金额12
		PfStateVO.setComment(head.getVmemo()); //说明13
		PfStateVO.setChecker(head.getVapproveid()); //审核人14PK
		return PfStateVO;
    }
}
