package nc.ui.xccb.pubsumdeal;
import nc.ui.pub.ClientEnvironment;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillItemEvent;
import nc.ui.trade.base.IBillOperate;
import nc.ui.trade.bill.ICardController;
import nc.ui.trade.card.BillCardUI;
import nc.ui.trade.card.CardEventHandler;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.scm.pub.session.ClientLink;
import nc.vo.trade.button.ButtonVO;
import nc.vo.trade.pub.IBillStatus;
import nc.vo.xccb.pub.XewcbPuBtnConst;
/**
 * ���óɱ����ܴ���
 * @author mlr
 */
public class ClientUI extends BillCardUI{

	private static final long serialVersionUID = -5178594056626651498L;
	protected ClientLink cl = new ClientLink(ClientEnvironment.getInstance());;//�ͻ��˻�������
	
	@Override
	public void setBodySpecialData(CircularlyAccessibleValueObject[] vos)
			throws Exception {
		
	}

	/**
	 * ʵ��������༭ǰ���¼�����,
	 * ��������¼�������Ҫ���ظ÷���
	 * �������ڣ�(2004-1-3 18:13:36)
	 */
	protected CardEventHandler createEventHandler() {
		return new EventHandler(this, getUIControl());
	}
	/**
	 * ע���Զ��尴ť
	 */
	protected void initPrivateButton() {
		super.initPrivateButton();
		ButtonVO btnVo = new ButtonVO();
		btnVo.setBtnNo(XewcbPuBtnConst.hz);
		btnVo.setBtnCode("hz");
		btnVo.setBtnName("����");
		btnVo.setBtnChinaName("����");
		btnVo.setOperateStatus(new int[]{IBillOperate.OP_NOTEDIT});
		btnVo.setBusinessStatus(new int[]{IBillStatus.FREE});
		addPrivateButton(btnVo);	
		
		ButtonVO btnVo1= new ButtonVO();
		btnVo1.setBtnNo(XewcbPuBtnConst.deal);
		btnVo1.setBtnCode("deal");
		btnVo1.setBtnName("����");
		btnVo1.setBtnChinaName("����");
		btnVo1.setOperateStatus(new int[]{IBillOperate.OP_NOTEDIT});
		btnVo1.setBusinessStatus(new int[]{IBillStatus.FREE});
		addPrivateButton(btnVo1);	
		super.initPrivateButton();			
	}


	@Override
	public boolean beforeEdit(BillEditEvent e) {
		String key=e.getKey();
		return super.beforeEdit(e);
		
	}
	public boolean beforeEdit(BillItemEvent e) {
		String key=e.getItem().getKey();		

	
	return true;
}

	@Override
	public void setDefaultData() throws Exception {
		
	}

	@Override
	public String getRefBillType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initSelfData() {
		// TODO Auto-generated method stub
		
	}





	@Override
	protected ICardController createController() {
		return new Controller();
	}
}
