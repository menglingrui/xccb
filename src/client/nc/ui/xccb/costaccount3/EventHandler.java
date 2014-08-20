package nc.ui.xccb.costaccount3;

import java.util.ArrayList;
import java.util.List;

import nc.ui.pub.ClientEnvironment;
import nc.ui.trade.controller.IControllerBase;
import nc.ui.trade.manage.BillManageUI;
import nc.ui.zmpub.pub.bill.FlowManageEventHandler;
import nc.ui.zmpub.pub.tool.LongTimeTask;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.pub.session.ClientLink;
import nc.vo.xccb.costaccount.CostAccountVO;
import nc.vo.xccb.costaccount.CostAccoutBVO;
import nc.vo.xccb.pub.XewcbPuBtnConst;
import nc.vo.xccb.pub.Xewcbpubconst;


public class EventHandler extends FlowManageEventHandler {
	public String dealclass="nc.bs.xccb.costaccount3.PubCostBO";

	public EventHandler(BillManageUI billUI, IControllerBase control) {
		super(billUI, control);
	}
	protected void onBoElse(int intBtn) throws Exception {		
		super.onBoElse(intBtn);
		switch (intBtn) {
		case XewcbPuBtnConst.cbfp:
			doCostAllo();
			break;
		case XewcbPuBtnConst.qxfp://
			doCancelAllo();
			break;	
		case XewcbPuBtnConst.clmx://分配明细
			quotaQuery();
			break;
		}
	}
	/**
	 * 取消分配
	 * @throws Exception 
	 */
	public void doCancelAllo() throws Exception{
		getBillCardPanel().stopEditing();
		if (getBillCardPanel().getBillTable().getSelectedRow() > -1) {

		}else{
		   getBillUI().showErrorMessage("请选择表体要取消分配的数据");
		   return;
		}	   
		CostAccountVO  shvo=(CostAccountVO) getBillCardPanel().getBillData().getHeaderValueVO(CostAccountVO.class.getName());
		CostAccoutBVO[] svos= (CostAccoutBVO[]) getBillCardPanelWrapper().getSelectedBodyVOs();	
		List<CostAccoutBVO> list=new ArrayList<CostAccoutBVO>();//存放待取消分配的成本
		for(int i=0;i<svos.length;i++){
			CostAccoutBVO svo=svos[i];
			if(PuPubVO.getUFBoolean_NullAs(svo.getUreserve1(), new UFBoolean(false)).booleanValue()==true){
				list.add(svo);
			}
		}		
		if(list==null || list.size()==0){
			getBillUI().showErrorMessage("被选择的数据未分配");
			return;
		}
		String[] infor={ClientEnvironment.getInstance().getDate().toString(),ClientEnvironment.getInstance().getUser().getPrimaryKey(),ClientEnvironment.getInstance().getCorporation().getPrimaryKey()};
		CostAccoutBVO[] svos1 = null;
        Class[] ParameterTypes = new Class[]{CostAccountVO.class,CostAccoutBVO[].class,String[].class};
        Object[] ParameterValues = new Object[]{shvo,svos,infor};		
        Object o=	 LongTimeTask.calllongTimeService(Xewcbpubconst.module, getBillUI(), 
			        "正在分摊成本...", 1, dealclass, null, 
			        "doCancelAllo", ParameterTypes, ParameterValues);
        if(o != null){
        	svos1 = (CostAccoutBVO[])o;
        } 	 
        getBillCardPanel().getBillModel().setBodyDataVO(svos1);
        onBoRefresh();
	    getBillUI().showHintMessage("取消分配完成");		
	}
	/**
	 * 成本分配
	 * @throws Exception 
	 */
	public void doCostAllo() throws Exception{
		getBillCardPanel().stopEditing();
		if (getBillCardPanel().getBillTable().getSelectedRow() > -1) {

		}else{
		   getBillUI().showErrorMessage("请选择表体要分配的数据");
		   return;
		}	   
		CostAccountVO  shvo=(CostAccountVO) getBillCardPanel().getBillData().getHeaderValueVO(CostAccountVO.class.getName());
		CostAccoutBVO[] svos= (CostAccoutBVO[]) getBillCardPanelWrapper().getSelectedBodyVOs();	
		List<CostAccoutBVO> list=new ArrayList<CostAccoutBVO>();//存放待分配的成本
		for(int i=0;i<svos.length;i++){
			CostAccoutBVO svo=svos[i];
			if(PuPubVO.getUFBoolean_NullAs(svo.getUreserve1(), new UFBoolean(false)).booleanValue()==false){
				list.add(svo);
			}
		}		
		if(list==null || list.size()==0){
			getBillUI().showErrorMessage("被选择的数据已经分配");
			return;
		}
		String[] infor={ClientEnvironment.getInstance().getDate().toString(),ClientEnvironment.getInstance().getUser().getPrimaryKey(),ClientEnvironment.getInstance().getCorporation().getPrimaryKey()};
		CostAccoutBVO[] svos1 = null;
        Class[] ParameterTypes = new Class[]{CostAccountVO.class,CostAccoutBVO[].class,String[].class};
        Object[] ParameterValues = new Object[]{shvo,svos,infor};		
        Object o=	 LongTimeTask.calllongTimeService(Xewcbpubconst.module, getBillUI(), 
			        "正在分摊成本...", 1, dealclass, null, 
			        "doCostAllo", ParameterTypes, ParameterValues);
        if(o != null){
        	svos1 = (CostAccoutBVO[])o;
        } 	 
        getBillCardPanel().getBillModel().setBodyDataVO(svos1);
        onBoRefresh();
	    getBillUI().showHintMessage("分配完成");		
	}
	
	@Override
	protected void onBoDelete() throws Exception {
		boolean ui=((BillManageUI) getBillUI()).isListPanelSelected();
		CostAccoutBVO[] a;
		if(ui){
			a=(CostAccoutBVO[]) getBillListPanel().getBillListData().getBodyBillModel().getBodyValueVOs(CostAccoutBVO.class.getName());
		}else{
			a= (CostAccoutBVO[]) getBillCardPanel().getBillData().getBillModel().getBodyValueVOs(CostAccoutBVO.class.getName());
		}
		List<CostAccoutBVO> list=new ArrayList<CostAccoutBVO>();
		for(int i=0;i<a.length;i++){
			UFBoolean ub=a[i].getUreserve1();
			if(ub==null){
				super.onBoDelete();
				return;
			}
			boolean bool=ub.booleanValue();
			if(bool){
				list.add(a[i]);
			}
		}
		if(list!=null && list.size()>0){
			getBillUI().showErrorMessage("已经分配,不能删除");
		}else{
			super.onBoDelete();
		}
		
		
	}
	
	/**
	 * 按钮m_boEdit点击时执行的动作,如有必要，请覆盖.
	 */
	protected void onBoEdit() throws Exception {
		boolean ui=((BillManageUI) getBillUI()).isListPanelSelected();
		CostAccoutBVO[] a;
		if(ui){
			a=(CostAccoutBVO[]) getBillListPanel().getBillListData().getBodyBillModel().getBodyValueVOs(CostAccoutBVO.class.getName());
		}else{
			a= (CostAccoutBVO[]) getBillCardPanel().getBillData().getBillModel().getBodyValueVOs(CostAccoutBVO.class.getName());
		}
		List<CostAccoutBVO> list=new ArrayList<CostAccoutBVO>();
		for(int i=0;i<a.length;i++){
			UFBoolean ub=a[i].getUreserve1();
			if(ub==null){
				super.onBoEdit();
				return;
			}
			boolean bool=ub.booleanValue();
			if(bool){
				list.add(a[i]);
			}
		}
		if(list!=null && list.size()>0){
			getBillUI().showErrorMessage("已经分配,不能进行修改");
		}else{
			super.onBoEdit();
		}
		
	}
	
	protected void quotaQuery() throws Exception{
		ClientLink cl = new ClientLink(ClientEnvironment.getInstance());
		String corp = PuPubVO.getString_TrimZeroLenAsNull(getBillCardPanel().getHeadItem("pk_corp"));
		ClientUI ui = (ClientUI) getBillUI();
		QueryUI qui=new QueryUI(Xewcbpubconst.bill_code_costaccount3_query, cl.getUser(), corp, null, ui, false);
		qui.showModal();
	}
}
