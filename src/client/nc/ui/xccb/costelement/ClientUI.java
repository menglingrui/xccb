package nc.ui.xccb.costelement;
import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultTreeModel;

import nc.ui.pub.ButtonObject;
import nc.ui.pub.ClientEnvironment;
import nc.ui.pub.beans.UIRefPane;
import nc.ui.pub.bill.BillCardBeforeEditListener;
import nc.ui.pub.bill.BillCardPanel;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillEditListener;
import nc.ui.pub.bill.BillItem;
import nc.ui.pub.bill.BillItemEvent;
import nc.ui.pub.bill.BillModel;
import nc.ui.pub.bill.IBillItem;
import nc.ui.trade.base.IBillOperate;
import nc.ui.trade.bsdelegate.BusinessDelegator;
import nc.ui.trade.button.IBillButton;
import nc.ui.trade.pub.IVOTreeData;
import nc.ui.trade.pub.IVOTreeDataByID;
import nc.ui.trade.pub.TreeCreateTool;
import nc.ui.trade.pub.VOTreeNode;
import nc.ui.trade.treemanage.MultiChildBillTreeManageUI;
import nc.ui.zmpub.formula.LoadFormula;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.xccb.pub.Xewcbpubconst;
import nc.vo.xccb.costelement.checkClassInterface;
import nc.vo.xccb.pub.XewcbPubTool;
import nc.vo.zmpub.pub.tool.ZmPubTool;
/**
 * 成本要素定义
 * @author mlr
 */
public class ClientUI extends MultiChildBillTreeManageUI{

	private static final long serialVersionUID = 4175119275908541161L;
	private String pk_accoutbook=null;//会计账簿
	private String pk_glorg=null;
	private String pk_glorgbook=null;
	
	private JPanel leftPane=null;
	protected BillCardPanel CardPanel = null;
	private TreeCreateTool m_treedata = null;
	private XewcbPubTool xpt=new XewcbPubTool();
	protected JPanel getLeft() {
		if (leftPane == null) {
			leftPane = new JPanel();
			leftPane.setName("leftPane");
			leftPane.setLayout(new BorderLayout());
			leftPane.add(getTreeSP(), "Center");
			leftPane.add(getbillCardPanel1(), BorderLayout.NORTH);
		}
		return leftPane;
	}
	public TreeCreateTool getBillTreeData() {

		if(m_treedata == null)
			m_treedata = new TreeCreateTool();

		return m_treedata;
	}
	protected BillCardPanel getbillCardPanel1() {
		if (CardPanel == null) {
			CardPanel = new BillCardPanel();
			CardPanel.setName("billCardPanel");
			CardPanel.loadTemplet(Xewcbpubconst.node_code_costelement_1, null, _getOperator(),
						_getCorp().getPrimaryKey());
			CardPanel.setSize(200, 100);
			CardPanel.setEnabled(true);
		}
		initlistener();
		return CardPanel;
	}
	
	public String getPk_glorg() {
		return pk_glorg;
	}
	public void setPk_glorg(String pk_glorg) {
		this.pk_glorg = pk_glorg;
	}
	public String getPk_glorgbook() {
		return pk_glorgbook;
	}
	public void setPk_glorgbook(String pk_glorgbook) {
		this.pk_glorgbook = pk_glorgbook;
	}
	/**
	 * 会计账簿编辑后事件
	 */
	private void initlistener() {
		CardPanel.addBillEditListenerHeadTail(new BillEditListener(){
			public void afterEdit(BillEditEvent e) {
			    pk_accoutbook=(String)getbillCardPanel1().getHeadItem("pk_accoutbook").getValueObject();
				try {
					loadTreeData(pk_accoutbook);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		    }
			
			public void bodyRowChange(BillEditEvent e) {
				
				
			}					
		});	
	    CardPanel.setBillBeforeEditListenerHeadTail(new BillCardBeforeEditListener(){
	    //	select b.pk_glbook from bd_glorg h join bd_glorgbook b on h.pk_glorg=b.pk_glorg where nvl(h.dr,0)=0 and nvl(b.dr,0)=0 and h.pk_entityorg='1021'
			public boolean beforeEdit(BillItemEvent e) {
				String key=e.getItem().getKey();		
					JComponent jf=CardPanel.getHeadItem(key).getComponent();
					if(jf instanceof UIRefPane){
						UIRefPane panel=(UIRefPane) jf;
						panel.setNotLeafSelectedEnabled(false);
						panel.getRefModel().addWherePart(" and bd_glbook.pk_glbook in (select b.pk_glbook from " +
								" bd_glorg h join bd_glorgbook b on h.pk_glorg=b.pk_glorg " +
								" where nvl(h.dr,0)=0 and nvl(b.dr,0)=0 and h.pk_entityorg='"+ClientEnvironment.getInstance().getCorporation().getPrimaryKey()+"')");
					}
				
				return true;
			}			  
		});
	}
	/**
	 * 按会计账簿加载成本要素
	 * @param pk_accoutbook
	 * @throws Exception 
	 */
	public  void loadTreeData(String pk_accoutbook) throws Exception {
		getBillTree().setModel(getBillTreeModel(getCreateTreeData(),pk_accoutbook));
		clearRightData();
		afterInit();
		this.modifyRootNodeShowName("节点");
	}
	/**
	 * 切换会计账簿 清除右侧成本要素数据
	 * @throws Exception 
	 */
	public void clearRightData() throws Exception {
		//清空缓冲数据
		getBufferData().clear();
	    this.setListHeadData(null);
		getBufferData().setCurrentRow(-1);
		this.setBillOperate(IBillOperate.OP_INIT);		
	}
	/**
	 * 成本要素树加载实现
	 */
	public DefaultTreeModel getBillTreeModel(IVOTreeData voTreeData1,String pk_accoutbook) {
		TreeManagerData voTreeData=(TreeManagerData) voTreeData1;
		if(m_treedata == null)
			m_treedata = new TreeCreateTool();

		//初试化缓存
		initBufferData(voTreeData.getTreeVOByAccoutbookid(pk_accoutbook));

		if(getCreateTreeData() instanceof IVOTreeDataByID){
			return m_treedata.createTree(voTreeData.getTreeVOByAccoutbookid(pk_accoutbook),voTreeData.getIDFieldName(),voTreeData.getParentIDFieldName(),voTreeData.getShowFieldName());
		}
		return null;
	}
	/**
	 * 此处插入方法说明。
	 * 创建日期：(2004-02-03 15:15:40)
	 */
	private void initBufferData(SuperVO[] queryVos) {

		try{
			//清空缓冲数据
			getBufferData().clear();
			if (queryVos != null && queryVos.length != 0)
			{
				addBufferData(queryVos);
			}
			else
			{
				getBufferData().setCurrentRow(-1);

			}
		}catch (java.lang.Exception e){
			System.out.println("设置缓冲数据失败！");
			e.printStackTrace();
		}
	}
	@Override
	protected IVOTreeData createTableTreeData() {
		return null;
	}
	public ClientUI(){
		super();	
		initialize();
		init();
	}
	
	public String getPk_accoutbook() {
		return pk_accoutbook;
	}
	public void setPk_accoutbook(String pk_accoutbook) {
		this.pk_accoutbook = pk_accoutbook;
	}
	public JPanel getLeftPane() {
		return leftPane;
	}
	public void setLeftPane(JPanel leftPane) {
		this.leftPane = leftPane;
	}
	public BillCardPanel getCardPanel() {
		return CardPanel;
	}
	public void setCardPanel(BillCardPanel cardPanel) {
		CardPanel = cardPanel;
	}
	public TreeCreateTool getM_treedata() {
		return m_treedata;
	}
	public void setM_treedata(TreeCreateTool m_treedata) {
		this.m_treedata = m_treedata;
	}
	public void initialize()
	{
		try
		{
			this.add(getSplitPane());
			getSplitPane().setLeftComponent(getLeft());
			getSplitPane().setRightComponent(getManagePane());

			getManagePane().setPreferredSize(new java.awt.Dimension(298, 469));

			loadTreeData(null);

			afterInit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			showErrorMessage(nc.ui.ml.NCLangRes.getInstance().getStrByID("uifactory","UPPuifactory-000109")/*@res "发生异常，界面初始化错误"*/);
		}
	}

	private void init(){
	 	getBillTreeData().modifyRootNodeShowName("节点");
	}
	/**
	 * 实例化界面编辑前后事件处理,
	 * 如果进行事件处理需要重载该方法
	 * 创建日期：(2004-1-3 18:13:36)
	 */
	protected nc.ui.trade.manage.ManageEventHandler createEventHandler() {
		return new EventHandler(this, getUIControl());
	}
	
	/**
	* 实例化界面初始控制器
	* 创建日期：(2004-1-3 18:13:36)
	*/
	protected nc.ui.trade.bill.AbstractManageController createController() {
		return new Controller();
	}
	
	@Override
	protected IVOTreeData createTreeData() {
		return new TreeManagerData();
	}


	@Override
	public void setDefaultData() throws Exception {
		VOTreeNode node = getBillTreeSelectNode();
		
		SuperVO vo = (SuperVO)(Class.forName(getUIControl().getBillVoName()[1])).newInstance();
		if(node!=null){
		   getBillCardPanel().setHeadItem(vo.getParentPKFieldName(),node.getNodeID());
		   getBillCardPanel().setHeadItem("costcode", node.getData().getAttributeValue("costcode"));
		}else{
		   getBillCardPanel().setHeadItem(vo.getParentPKFieldName(),"root");
		}
		getBillCardPanel().getHeadItem("pk_corp").setValue(ClientEnvironment.getInstance().getCorporation().getPrimaryKey());
		getBillCardPanel().getHeadItem("pk_accoutbook").setValue(getPk_accoutbook());
		getBillCardPanel().getHeadItem("reserve14").setValue(new UFBoolean(true));
	}
	
	/**
	 * 编辑前处理。 创建日期：(2001-3-23 2:02:27)
	 *
	 * @param e
	 *            ufbill.BillEditEvent
	 */
	public boolean beforeEdit(nc.ui.pub.bill.BillEditEvent e) {//计算公式
		if (e.getPos() == IBillItem.BODY) {
			String key = e.getKey();
			int row = e.getRow();
			LoadFormula la = null;
			if (key.equals("reserve2")) {
				String formuDesc = (String) getBillCardPanel().getBillModel()
						.getValueAt(row, "reserve2");
				String formuCode = (String) getBillCardPanel().getBillModel()
						.getValueAt(row, "costdriver");
				la = new LoadFormula(this, "计算设置公式", formuDesc, formuCode);
				la.showModal();
				getBillCardPanel().getBillModel().setValueAt(la.getFormuDesc(),
						row, "reserve2");
				getBillCardPanel().getBillModel().setValueAt(la.getFormuCode(),
						row, "costdriver");
				String pk_b = (String)getBillCardPanel().getBillModel().getValueAt(row, "pk_costelement_b1");
				if(pk_b == null || "".equals(pk_b)){
					getBillCardPanel().getBillModel().setRowState(row, BillModel.ADD);//新增状态
				}else{
					getBillCardPanel().getBillModel().setRowState(row, BillModel.MODIFICATION);//修改状态
				}
			}
			
			if ("invcode1".equals(key)) {
				JComponent jf = getBillCardPanel().getBodyItem(key).getComponent();
				if (jf instanceof UIRefPane) {
					UIRefPane panel = (UIRefPane) jf;
					panel.getRefModel().addWherePart(
									" and bd_invbasdoc.laborflag='Y'");
				}
			}
			
			if ("invclcode".equals(key)) {
				JComponent jf = getBillCardPanel().getBodyItem(key).getComponent();
				if (jf instanceof UIRefPane) {
					UIRefPane panel = (UIRefPane) jf;
					panel.getRefModel().addWherePart(
									" and coalesce(laborflag,'N')='N'");
				}
			}
			
//			if ("invclcode1".equals(key)) {
//				JComponent jf = getBillCardPanel().getBodyItem(key)
//						.getComponent();
//				if (jf instanceof UIRefPane) {
//					UIRefPane panel = (UIRefPane) jf;
//					panel.getRefModel().addWherePart(
//							" and invclasscode like'" + Xewpubconst.oberate_work_cl + "%'");
//				}
//			}
			
			if ("accountsubcode".equals(key)) {
		    	if(pk_glorg==null || pk_glorg.length()==0){
		    		String pk_corp=ClientEnvironment.getInstance().getCorporation().getPrimaryKey();
		    		try {
						pk_glorg=(String) ZmPubTool.execFomularClient(
								"pk_glorg->getColValue(bd_glorg,pk_glorg,pk_entityorg,pk_entityorg)", 
								new String[]{"pk_entityorg"}, 
								new String[]{pk_corp});
					} catch (BusinessException e1) {
						e1.printStackTrace();
					}		    		
		    	}
		    		try {
		    			pk_glorgbook=(String) ZmPubTool.execFomularClient(
								"pk_glorgbook->getColValue2(bd_glorgbook,pk_glorgbook,pk_glbook,pk_glbook,pk_glorg,pk_glorg)", 
								new String[]{"pk_glbook","pk_glorg"}, 
								new String[]{pk_accoutbook,pk_glorg});
					} catch (BusinessException e1) {
						e1.printStackTrace();
					}		    		
				JComponent jf = getBillCardPanel().getBodyItem(key)
						.getComponent();
				if (jf instanceof UIRefPane) {
					UIRefPane panel = (UIRefPane) jf;
					panel.getRefModel().setWherePart(	
					        " (bd_accsubj.pk_glorgbook = '"+pk_glorgbook+"' and "+
						    " (bd_accsubj.stoped = 'N' or bd_accsubj.stoped is null)) "+
						    "  and (sealflag is null)");
				}
			}
			

		}	
		return true;
	}
	/**
	 * 编辑后事件
	 */
	public void afterEdit(BillEditEvent e) {
		String key = e.getKey();
		int row = e.getRow();
		if (e.getPos() == BillItem.HEAD) {
			if ("datasource".equalsIgnoreCase(key)) {
				isdatasource();
			}
		}else if(e.getPos()==BillItem.BODY){
			if ("invcode1".equals(key)) {
			try {
				String invclcode1 = (String)getBillCardPanel().getBillModel().getValueAt(row, "invclcode1");
				if(invclcode1==null || invclcode1.length()==0){
					String invcode1 = (String)getBillCardPanel().getBillModel().getValueAt(row, "reserve3");
				String pk_invcl = xpt.getPkinvclBypPk(invcode1);
				String invclcode;
			
					invclcode = xpt.getInvclNameByPk(pk_invcl);
				
				getBillCardPanel().getBillModel().setValueAt(invclcode, row, "invclcode1");
				getBillCardPanel().getBillModel().setValueAt(pk_invcl, row, "pk_invcl1");
						getBillCardPanel().execBodyFormula(row, "pk_invcl1");
				}
			
			if ("invclcode".equals(key)) {
				String invcode = (String)getBillCardPanel().getBillModel().getValueAt(row, "invcode");
				if(invcode==null || invcode.length()==0){
					String invcode1 = (String)getBillCardPanel().getBillModel().getValueAt(row, "reserve4");
			String pk_invcl = xpt.getPkinvclBypPk(invcode1);
			String invclcode=xpt.getInvclNameByPk(pk_invcl);
			getBillCardPanel().getBillModel().setValueAt(invclcode, row, "invcode");
			getBillCardPanel().getBillModel().setValueAt(pk_invcl, row, "pk_invcl");
						getBillCardPanel().execBodyFormula(row, "pk_invcl1");
				}
			}
			} catch (BusinessException e1) {
				e1.printStackTrace();
			}
			}
		}
    }
	
	public void isdatasource(){
		Integer datasource =  PuPubVO.getInteger_NullAs(getBillCardPanel().getHeadItem("datasource").getValueObject(), -10) ;
		if(datasource==0){
			getBillCardPanel().getBodyItem("xew_costelement_b","accountsubcode").setEnabled(false);
			getBillCardPanel().getBodyItem("xew_costelement_b","invclcode1").setEnabled(true);
			getBillCardPanel().getBodyItem("xew_costelement_b","invcode1").setEnabled(true);
			getBillCardPanel().getBodyItem("xew_costelement_b","invcode").setEnabled(true);
			getBillCardPanel().getBodyItem("xew_costelement_b","invclcode").setEnabled(true);
		}
		if (datasource == 1){
			getBillCardPanel().getBodyItem("xew_costelement_b","accountsubcode").setEnabled(true);
			getBillCardPanel().getBodyItem("xew_costelement_b","invclcode1").setEnabled(false);
			getBillCardPanel().getBodyItem("xew_costelement_b","invcode1").setEnabled(false);
			getBillCardPanel().getBodyItem("xew_costelement_b","invcode").setEnabled(false);
			getBillCardPanel().getBodyItem("xew_costelement_b","invclcode").setEnabled(false);
		}
	}
	@Override
	protected BusinessDelegator createBusinessDelegator() {
		return new Delegator();
	}
	@Override
	protected void initSelfData() {
		//设置多选
		getBillListPanel().getHeadTable().setRowSelectionAllowed(true);	//true那一行内容能够全部选中,false只能选中一个单元格
//		getBillListPanel().setParentMultiSelect(true);//设置表头多选,带复选框
		getBillListPanel().getHeadTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);//往下拖拽，选中表头所有行
		
		//除去行操作多余按钮
		ButtonObject btnobj = getButtonManager().getButton(IBillButton.Line);
		if (btnobj != null) {
			btnobj.removeChildButton(getButtonManager().getButton(IBillButton.CopyLine));
			btnobj.removeChildButton(getButtonManager().getButton(IBillButton.PasteLine));
			btnobj.removeChildButton(getButtonManager().getButton(IBillButton.InsLine));
			btnobj.removeChildButton(getButtonManager().getButton(IBillButton.PasteLinetoTail));
		}
//		getBillCardPanel().setBillBeforeEditListenerHeadTail((BillCardBeforeEditListener) this);
	}

	@Override
	public String getRefBillType() {
		return null;
	}
	
	public java.lang.Object getUserObject() {
		return new checkClassInterface();
	}

	@Override
	protected void setHeadSpecialData(CircularlyAccessibleValueObject vo,
			int intRow) throws Exception {
		
	}
	@Override
	protected void setTotalHeadSpecialData(CircularlyAccessibleValueObject[] vos)
			throws Exception {
		
	}
	

}
