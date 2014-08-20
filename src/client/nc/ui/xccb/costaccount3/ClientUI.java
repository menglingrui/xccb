package nc.ui.xccb.costaccount3;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;

import nc.ui.pub.ButtonObject;
import nc.ui.pub.ClientEnvironment;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.bill.BillCardBeforeEditListener;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillItemEvent;
import nc.ui.pub.bill.IBillItem;
import nc.ui.trade.base.IBillOperate;
import nc.ui.trade.bill.AbstractManageController;
import nc.ui.trade.business.HYPubBO_Client;
import nc.ui.trade.button.IBillButton;
import nc.ui.trade.manage.ManageEventHandler;
import nc.ui.zmpub.pub.bill.DefBillManageUI;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.pub.session.ClientLink;
import nc.vo.trade.button.ButtonVO;
import nc.vo.trade.pub.IBillStatus;
import nc.vo.xccb.pub.Xewcbpubconst;
import nc.vo.xew.pub.XewPuBtnConst;
/**
 * ���óɱ����ܵ�
 * @author mlr
 *
 */
public class ClientUI extends DefBillManageUI implements BillCardBeforeEditListener{

	private static final long serialVersionUID = -5178594056626651498L;
	protected ClientLink cl = new ClientLink(ClientEnvironment.getInstance());;//�ͻ��˻�������
	@Override
	public boolean isLinkQueryEnable() {
		return true;
	}
	@Override
	protected void initEventListener() {
		super.initEventListener();
		getBillCardPanel().setBillBeforeEditListenerHeadTail(this);
	}
	
	@Override
	protected void initSelfData() {
		super.initSelfData();
		//���ö�ѡ
		getBillListPanel().getHeadTable().setRowSelectionAllowed(true);	//true��һ�������ܹ�ȫ��ѡ��,falseֻ��ѡ��һ����Ԫ��
		getBillListPanel().setParentMultiSelect(true);//���ñ�ͷ��ѡ,����ѡ��
		getBillListPanel().getHeadTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);//������ק��ѡ�б�ͷ������
		
		//��ȥ�в������ఴť
		ButtonObject btnobj = getButtonManager().getButton(IBillButton.Line);
		if (btnobj != null) {
			btnobj.removeChildButton(getButtonManager().getButton(IBillButton.CopyLine));
			btnobj.removeChildButton(getButtonManager().getButton(IBillButton.PasteLine));
			btnobj.removeChildButton(getButtonManager().getButton(IBillButton.InsLine));
			btnobj.removeChildButton(getButtonManager().getButton(IBillButton.PasteLinetoTail));
		}
		getBillCardPanel().setBillBeforeEditListenerHeadTail(this);
	}

	@Override
	protected AbstractManageController createController() {
		return new Controller();
	}
	
	protected ManageEventHandler createEventHandler() {
		return new EventHandler(this, getUIControl());
	}
	
	@Override
	public void setBodySpecialData(CircularlyAccessibleValueObject[] vos)
			throws Exception {
		
	}

	@Override
	protected void setHeadSpecialData(CircularlyAccessibleValueObject vo,
			int intRow) throws Exception {
		
	}

	@Override
	protected void setTotalHeadSpecialData(CircularlyAccessibleValueObject[] vos)
			throws Exception {
		
	}
	protected String getBillNo() throws java.lang.Exception {
		return HYPubBO_Client.getBillNo(Xewcbpubconst.bill_code_costsum , cl.getCorp(), null,null);
	}
	@Override
	public void setDefaultData() throws Exception {
		getBillCardPanel().getHeadItem("pk_corp").setValue(cl.getCorp());
		getBillCardPanel().getTailItem("voperatorid").setValue(cl.getUser());
		getBillCardPanel().getTailItem("dmakedate").setValue(cl.getLogonDate());
		getBillCardPanel().getHeadItem("dbilldate").setValue(cl.getLogonDate());
		getBillCardPanel().getHeadItem("vbillstatus").setValue(IBillStatus.FREE);
		getBillCardPanel().getHeadItem("pk_billtype").setValue(Xewcbpubconst.bill_code_costsum);
		getBillCardPanel().getHeadItem("vbillno").setValue(getBillNo());
	}

	@Override
	public boolean beforeEdit(BillEditEvent e) {
		String key=e.getKey();
		if(e.getPos() == IBillItem.BODY && "costelementcode".equals(key)){
			String pk_accoutbook=PuPubVO.getString_TrimZeroLenAsNull(getBillCardPanel().getHeadItem("pk_accoutbook").getValue());
			if(pk_accoutbook==null){
				this.showErrorMessage("��ͷ����˲�Ϊ��");
				return false;
			}			
			JComponent jf=getBillCardPanel().getBodyItem(key).getComponent();
			if(jf instanceof UIRefPane){
				UIRefPane panel=(UIRefPane) jf;
				panel.setNotLeafSelectedEnabled(false);
				panel.getRefModel().addWherePart(" and pk_accoutbook='"+pk_accoutbook+"'");
			}
		}	
		return super.beforeEdit(e);
		
	}

	public boolean beforeEdit(BillItemEvent e) {
		String key = e.getItem().getKey();
		if (key.equals("pk_accoutbook")) {
			JComponent jf = getBillCardPanel().getHeadItem(key).getComponent();
			if (jf instanceof UIRefPane) {
				UIRefPane panel = (UIRefPane) jf;
				panel.setNotLeafSelectedEnabled(false);
				panel.getRefModel().addWherePart(
								" and bd_glbook.pk_glbook in (select b.pk_glbook from "
										+ " bd_glorg h join bd_glorgbook b on h.pk_glorg=b.pk_glorg "
										+ " where nvl(h.dr,0)=0 and nvl(b.dr,0)=0 and h.pk_entityorg='"
										+ ClientEnvironment.getInstance().getCorporation().getPrimaryKey() + "')");
			}
		}
		return true;
}
	/**
	 * ע���Զ��尴ť
	 */
	protected void initPrivateButton() {
		super.initPrivateButton();
		ButtonVO btnVo = new ButtonVO();
		btnVo.setBtnNo(XewPuBtnConst.cbfp);
		btnVo.setBtnCode("cbfp");
		btnVo.setBtnName("�ɱ�����");
		btnVo.setBtnChinaName("�ɱ�����");
		btnVo.setOperateStatus(new int[]{IBillOperate.OP_NOTEDIT});
		btnVo.setBusinessStatus(new int[]{IBillStatus.FREE});
		addPrivateButton(btnVo);	
		
		ButtonVO btnVo1= new ButtonVO();
		btnVo1.setBtnNo(XewPuBtnConst.qxfp);
		btnVo1.setBtnCode("qxfp");
		btnVo1.setBtnName("ȡ������");
		btnVo1.setBtnChinaName("ȡ������");
		btnVo1.setOperateStatus(new int[]{IBillOperate.OP_NOTEDIT});
		btnVo1.setBusinessStatus(new int[]{IBillStatus.FREE});
		addPrivateButton(btnVo1);	
		
		ButtonVO btnvo6 = new ButtonVO();
		btnvo6.setBtnNo(XewPuBtnConst.clmx);
		btnvo6.setBtnName("������ϸ");
		btnvo6.setBtnChinaName("������ϸ");
		btnvo6.setBtnCode(null);// code�������Ϊ��
//		btnvo6.setBusinessStatus(new int[]{IBillStatus.FREE});
		btnvo6.setOperateStatus(new int[] { IBillOperate.OP_NOTEDIT,
				IBillOperate.OP_NO_ADDANDEDIT });
		addPrivateButton(btnvo6);
		super.initPrivateButton();			
	}

}
