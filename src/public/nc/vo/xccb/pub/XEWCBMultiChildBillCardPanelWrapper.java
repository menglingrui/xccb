package nc.vo.xccb.pub;


import java.util.ArrayList;
import java.util.HashMap;

import nc.ui.pub.ClientEnvironment;
import nc.ui.pub.beans.UIMenuItem;
import nc.ui.pub.bill.BillData;
import nc.ui.pub.bill.BillScrollPane;
import nc.ui.trade.bill.ICardController;
import nc.ui.zmpub.pub.bill.BillRowNo;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.trade.pub.IExAggVO;
import nc.vo.zmpub.pub.bill.ZMMultiChildBillCardPanelWrapper;

public class XEWCBMultiChildBillCardPanelWrapper extends ZMMultiChildBillCardPanelWrapper{
	private HashMap m_BodyVOClassMap;
	private String[] m_TableCodes;
	public XEWCBMultiChildBillCardPanelWrapper(ClientEnvironment ce,
			ICardController ctl, String pk_busiType, String nodeKey)
			throws Exception {
		super(ce, ctl, pk_busiType, nodeKey);
		// TODO Auto-generated constructor stub
	}


		public XEWCBMultiChildBillCardPanelWrapper(
			ClientEnvironment ce,
			ICardController ctl,
			String pk_busiType,
			String nodeKey,
			ArrayList defAry)
			throws Exception {
			super(ce, ctl, pk_busiType, nodeKey, defAry);

		}

		public XEWCBMultiChildBillCardPanelWrapper(
			ClientEnvironment ce,
			ICardController ctl,
			String pk_busiType,
			String nodeKey,
			BillData billData)
			throws Exception {
			super(ce, ctl, pk_busiType,nodeKey,billData);


		}

		public XEWCBMultiChildBillCardPanelWrapper(
			ClientEnvironment ce,
			ICardController ctl,
			String pk_busiType,
			String nodeKey,
			BillData billData,
			ArrayList defAry)
			throws Exception {
			super(ce, ctl, pk_busiType, nodeKey, billData,defAry);

		}
		
		/**
		* 删除当前所选择的行
		*/
		public void deleteSelectedLines()
		{
			if(getCurrentBodyTableCode()!=null&&getCurrentBodyTableCode().equals("xew_proaccept_b2")){
				return;
			}
			getBillCardPanel().stopEditing();
			if (getBillCardPanel().getBillTable().getSelectedRow() > -1) {
				int[] aryRows = getBillCardPanel().getBillTable(getCurrentBodyTableCode()).getSelectedRows();
				getBillCardPanel().delLine(getCurrentBodyTableCode());
			}
		//	getBillCardPanel().getUI();
		}
		/**
		 * 此处插入方法说明。 创建日期：(2001-3-27 11:09:34)
		 * 
		 * @param e
		 *            java.awt.event.ActionEvent
		 */
		public void onMenuItemClick(java.awt.event.ActionEvent e) {
			try {
				BillScrollPane bsp = getBillCardPanel().getBodyPanel();

				UIMenuItem item = (UIMenuItem) e.getSource();
				//如果已经传成本核算 不允许使用 菜单
				UFBoolean issendcost=PuPubVO.getUFBoolean_NullAs(getBillCardPanel().getHeadItem("ureserve1").getValueObject(), UFBoolean.FALSE);
				if(issendcost.booleanValue()==true){
					return;
				}
				if(getCurrentBodyTableCode()!=null&&getCurrentBodyTableCode().equals("xew_proaccept_b2")){
					return;
				}
				if (item == bsp.getMiInsertLine()) {
					insertLine();
				} else if (item == bsp.getMiAddLine()) {
					addLine();
				} else if (item == bsp.getMiDelLine()) {
					//deleteSelectedLines();
				} else if (item == bsp.getMiCopyLine()) {
					copySelectedLines();
				} else if (item == bsp.getMiPasteLine()) {
					//pasteLines();
				} else if (item == bsp.getMiPasteLineToTail()) {
					pasteLinesToTail();
				}
			} catch (Exception ex) {
				System.out.println("line error!!!!");
			}
		}
		/**
		 * 增行
		 */
		public void addLine()  {
			if(getCurrentBodyTableCode()!=null&&getCurrentBodyTableCode().equals("xew_proaccept_b2")){
				return;
			}
			getBillCardPanel().addLine();
			try {
				BillRowNo.addLineRowNo(getBillCardPanel(), getBillCardPanel().getBillType(), "crowno");
			} catch (Exception ex) {
				System.out.println("行号设置错误");
			}
		}
		
		/**
		* 增行
		*/
		public void insertLine() throws Exception {
			if(getCurrentBodyTableCode()!=null&&getCurrentBodyTableCode().equals("xew_proaccept_b2")){
				return;
			}
			getBillCardPanel().stopEditing();
			getBillCardPanel().insertLine(getCurrentBodyTableCode());
			BillRowNo.addLineRowNo(getBillCardPanel(), getBillCardPanel().getBillType(), "crowno");
			int row=getBillCardPanel().getBillTable().getSelectedRow();
			if(row>=0){
			  BillRowNo.insertLineRowNos(getBillCardPanel(), getBillCardPanel().getBillType(), "crowno", row+1, 1);
			}
		}
		
		public void pasteLinesToTail()
		{
			if(getCurrentBodyTableCode()!=null&&getCurrentBodyTableCode().equals("xew_proaccept_b2")){
				return;
			}
			if(getCopyedBodyVOs()==null||getCopyedBodyVOs().length==0)
				return;
			if(getCopyedBodyVOs()[0].getClass() != getCurrentBodyVOClass())
				return;
			for (int i = 0; i < getCopyedBodyVOs().length; i++){
				getBillCardPanel().stopEditing();
				addLine();
				int lastrow = getBillCardPanel().getBillTable(getCurrentBodyTableCode()).getRowCount()-1;
				getBillCardPanel().getBillModel(getCurrentBodyTableCode()).setBodyRowVO(getCopyedBodyVOs()[i],lastrow);
				try {
					BillRowNo.addLineRowNo(getBillCardPanel(), getBillCardPanel().getBillType(), "crowno");
					
				} catch (Exception ex) {
					System.out.println("行号设置错误");
				}
			}
			execCurrentLoadFormula();	
		}
		protected void initCardPanel()
		{
			try
			{
				Class BillVOClass = Class.forName(getUIControl().getBillVoName()[0]);
				IExAggVO iExAggVO = null;
				try
				{
					iExAggVO = (IExAggVO) BillVOClass.newInstance();
					if (iExAggVO.getTableCodes().length
						!= getUIControl().getBillVoName().length - 2)
						throw new Exception(nc.ui.ml.NCLangRes.getInstance().getStrByID("uifactory","UPPuifactory-000119")/*@res "MultiChildBillCardPanelWrapper初始化错误。VO中的字表个数与CTL类中制定的子表VO Class个数不同。"*/);
				}
				catch (ClassCastException e)
				{
					throw new Exception(nc.ui.ml.NCLangRes.getInstance().getStrByID("uifactory","UPPuifactory-000120")/*@res "MultiChildBillCardPanelWrapper初始化错误。多子表UI的VO必须实现IExAggVO接口。"*/);
				}
				m_BodyVOClassMap = new HashMap();
				for (int i = 0; i < iExAggVO.getTableCodes().length; i++)
				{
					m_BodyVOClassMap.put(
						iExAggVO.getTableCodes()[i],
						Class.forName(getUIControl().getBillVoName()[2 + i]));
				}

				m_TableCodes = iExAggVO.getTableCodes();

				super.initCardPanel();
				openBodyMenuShow();
				for (int i = 0; i < getTableCodes().length; i++)
				{
					//增加监听
					getBillCardPanel().addBodyMenuListener(getTableCodes()[i], this);
				}
			  //
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				System.out.println(
					"加载单据卡片模板错误::MultiChildBillCardPanelWrapper(initCardPanel)!!");
			}
		}
		/**
		 * 此处插入方法说明。
		 * 创建日期：(2004-2-2 19:50:34)
		 * @return java.lang.String[]
		 */
		private String[] getTableCodes() {
			return m_TableCodes;
		}
		public void openBodyMenuShow() {
			String[] tablecodes = getBillCardPanel().getBillData().getBodyTableCodes();

			if (tablecodes != null) {
				for (int i = 0; i < tablecodes.length; i++) {
					getBillCardPanel().setBodyMenuShow(tablecodes[0], true);
				}
			}
		}
		public void closeBodyMenuShow() {
			String[] tablecodes = getBillCardPanel().getBillData().getBodyTableCodes();

			if (tablecodes != null) {
				for (int i = 0; i < tablecodes.length; i++) {
					getBillCardPanel().setBodyMenuShow(tablecodes[0], false);
				}
			}
		}
	/**
	 * 此处插入方法说明。
	 * 创建日期：(2004-2-2 18:52:13)
	 * @return java.lang.String
	 */
	private String getCurrentBodyTableCode() {
		return getBillCardPanel().getCurrentBodyTableCode();
	}
	private Class getCurrentBodyVOClass() {
		return (Class)m_BodyVOClassMap.get(getCurrentBodyTableCode());
	}
	/**
	* 复制当前所选择的行
	*/
	public void copySelectedLines()
	{
		int selectedRow = getBillCardPanel().getBillTable(getCurrentBodyTableCode()).getSelectedRow();
		if (selectedRow != -1)
		{
			int[] rows = getBillCardPanel().getBillTable(getCurrentBodyTableCode()).getSelectedRows();

			CircularlyAccessibleValueObject[] vos =
				(CircularlyAccessibleValueObject[]) java.lang.reflect.Array.newInstance(
					getCurrentBodyVOClass(),
					rows.length);
			for (int i = 0; i < vos.length; i++)
			{
				vos[i] =
					getBillCardPanel().getBillModel(getCurrentBodyTableCode()).getBodyValueRowVO(
						rows[i],
						getCurrentBodyVOClass().getName());
				if(vos[i]!=null){
					try {
						vos[i].setPrimaryKey(null);
						vos[i].setAttributeValue("crowno", null);
					} catch (BusinessException e) {
						e.printStackTrace();
					}
				}
				
				
			}
			getBillCardPanel().getBillData().getBillModel(getCurrentBodyTableCode()).getBodySelectedVOs(
				getCurrentBodyVOClass().getName());
			setCopyedBodyVOs(vos);
		}
	}

}
