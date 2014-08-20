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
 * �ɱ����� ��̨ҵ������
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
	 * �����ƽ̨
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
	 * �����ƽ̨
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
     * ���ƽ̨�ӿ�
     * @return
     * @throws ComponentException
     */
	public IDapSendMessage getIDapSendMessage() throws ComponentException {
		return ((IDapSendMessage) NCLocator.getInstance().lookup(IDapSendMessage.class.getName()));
	}
	/**
	 * ���ݳɱ�����vo ��û��ƽ̨vo
	 * @param head
	 * @param items
	 * @param approvedate
	 * @return
	 */
	private DapMsgVO getAccountVO(CostAccountVO head, CostAccoutBVO[] items,String approvedate) {
        nc.vo.dap.out.DapMsgVO PfStateVO = new nc.vo.dap.out.DapMsgVO();
		PfStateVO.setCorp(head.getPk_corp()); //��˾����2
		PfStateVO.setSys("JX"); //ϵͳ��PK3
		PfStateVO.setProc(head.getPk_billtype()); //�������ͼ�ҵ����PK4
		PfStateVO.setBusiType(head.getPk_busitype());
		//ҵ������PK5
		PfStateVO.setBusiName(null);
		PfStateVO.setProcMsg(head.getPrimaryKey()); //������Ϣ7,���ݴ���Ϣ��ѯƽ̨���õ���Ϣ
		PfStateVO.setBillCode(head.getVbillno()); //���ݱ���8
	//	PfStateVO.set
		PfStateVO.setBusiDate(new UFDate(approvedate)); //ҵ������9
		if (items != null) {
		    PfStateVO.setOperator(InvocationInfoProxy.getInstance().getUserCode()); //ҵ��Ա10PK
			PfStateVO.setCurrency(Xewcbpubconst.pk_currency); //����11PK
		}
		//ȡ���ܽ��
		UFDouble znmy=new UFDouble(0.0);
		for(int i=0;i<items.length;i++){
			znmy=znmy.add(items[i].getNmy());
		}
		PfStateVO.setMoney(znmy); //���12
		PfStateVO.setComment(head.getVmemo()); //˵��13
		PfStateVO.setChecker(head.getVapproveid()); //�����14PK
		return PfStateVO;
    }
}
