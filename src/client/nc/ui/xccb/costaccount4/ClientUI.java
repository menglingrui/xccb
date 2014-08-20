package nc.ui.xccb.costaccount4;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;

import nc.ui.pub.ButtonObject;
import nc.ui.pub.ClientEnvironment;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.bill.BillCardBeforeEditListener;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillItemEvent;
import nc.ui.pub.bill.IBillItem;
import nc.ui.trade.bill.AbstractManageController;
import nc.ui.trade.business.HYPubBO_Client;
import nc.ui.trade.button.IBillButton;
import nc.ui.trade.manage.ManageEventHandler;
import nc.ui.zmpub.pub.bill.DefBillManageUI;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.pub.session.ClientLink;
import nc.vo.trade.pub.IBillStatus;
import nc.vo.xccb.pub.Xewcbpubconst;
import nc.vo.xew.pub.Xewpubconst;
/**
 * 公用成本核算单
 * @author mlr
 */
public class ClientUI extends DefBillManageUI implements BillCardBeforeEditListener{

	private static final long serialVersionUID = -5178594056626651498L;
	protected ClientLink cl = new ClientLink(ClientEnvironment.getInstance());;//客户端环境变量
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
		//设置多选
		getBillListPanel().getHeadTable().setRowSelectionAllowed(true);	//true那一行内容能够全部选中,false只能选中一个单元格
		getBillListPanel().setParentMultiSelect(true);//设置表头多选,带复选框
		getBillListPanel().getHeadTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);//往下拖拽，选中表头所有行
		
		//除去行操作多余按钮
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
		return HYPubBO_Client.getBillNo(Xewcbpubconst.bill_code_costaccount4 , cl.getCorp(), null,null);
	}
	@Override
	public void setDefaultData() throws Exception {
		getBillCardPanel().getHeadItem("pk_corp").setValue(cl.getCorp());
		getBillCardPanel().getTailItem("voperatorid").setValue(cl.getUser());
		getBillCardPanel().getTailItem("dmakedate").setValue(cl.getLogonDate());
		getBillCardPanel().getHeadItem("dbilldate").setValue(cl.getLogonDate());
		getBillCardPanel().getHeadItem("vbillstatus").setValue(IBillStatus.FREE);
		getBillCardPanel().getHeadItem("pk_billtype").setValue(Xewcbpubconst.bill_code_costaccount4);
		getBillCardPanel().getHeadItem("vbillno").setValue(getBillNo());
	}

	@Override
	public boolean beforeEdit(BillEditEvent e) {
		String key=e.getKey();
		if(e.getPos() == IBillItem.BODY && "costelementcode".equals(key)){
			String pk_accoutbook=PuPubVO.getString_TrimZeroLenAsNull(getBillCardPanel().getHeadItem("pk_accoutbook").getValue());
			if(pk_accoutbook==null){
				this.showErrorMessage("表头会计账簿为空");
				return false;
			}			
			JComponent jf=getBillCardPanel().getBodyItem(key).getComponent();
			if(jf instanceof UIRefPane){
				UIRefPane panel=(UIRefPane) jf;
				panel.setNotLeafSelectedEnabled(false);
				panel.getRefModel().addWherePart(" and pk_accoutbook='"+pk_accoutbook+"'");
			}
		}	
		if (e.getPos() == IBillItem.BODY && "invdoc".equals(key)) {
			JComponent jf = getBillCardPanel().getBodyItem(key).getComponent();
			if (jf instanceof UIRefPane) {
				UIRefPane panel = (UIRefPane) jf;
				panel
						.getRefModel()
						.addWherePart(
								" and bd_invbasdoc.laborflag='Y'");
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
}
