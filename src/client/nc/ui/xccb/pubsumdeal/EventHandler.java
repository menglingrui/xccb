package nc.ui.xccb.pubsumdeal;
import nc.ui.pub.ClientEnvironment;
import nc.ui.pub.beans.UIDialog;
import nc.ui.trade.bill.ICardController;
import nc.ui.trade.card.BillCardUI;
import nc.ui.trade.card.CardEventHandler;
import nc.ui.trade.report.query.QueryDLG;
import nc.ui.zmpub.pub.tool.LongTimeTask;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.query.ConditionVO;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.xccb.pub.XewcbPuBtnConst;
import nc.vo.xccb.pub.XewcbPubTool;
import nc.vo.xccb.pub.Xewcbpubconst;
import nc.vo.xccb.sumdel.SumDealVO;

public class EventHandler extends CardEventHandler {
	public String dealclass="nc.bs.xccb.pubsumdeal.SumDealBO";
	private QueryDLG m_qryDlg = null;
	String pk_accoutbook=null;
	String pk_accperiod=null;
    public XewcbPubTool tool=new XewcbPubTool();
	public EventHandler(BillCardUI billUI, ICardController control) {
		super(billUI, control);
	}
	protected UIDialog createQueryUI()
	{
	      if (m_qryDlg == null) {
	            m_qryDlg = new QueryDLG();           
	            m_qryDlg.setTempletID(ClientEnvironment.getInstance().getCorporation().getPrimaryKey(),
	            		Xewcbpubconst.node_code_pubsumdeal, ClientEnvironment.getInstance().getUser().getPrimaryKey(), null);
	            m_qryDlg.setNormalShow(false);
	        }


		return m_qryDlg;	
	}
	/**
	 * 公用成本汇总处理单查询
	 */
	protected void onBoBodyQuery() throws Exception {
		QueryDLG dig=(QueryDLG) getQueryUI();
		if (dig.showModal() != UIDialog.ID_OK)
			return ;
		String pk_corp=ClientEnvironment.getInstance().getCorporation().getPrimaryKey();	
		ConditionVO[] conds=dig.getConditionVO();
		if(conds==null || conds.length==0){
			getBillUI().showErrorMessage("查询条件不能为空");
			return;
		}
		for(int i=0;i<conds.length;i++){
			if("pk_accperiod".equals(conds[i].getFieldCode())){
				pk_accperiod=conds[i].getValue();
			}
			if("pk_accoutbook".equals(conds[i].getFieldCode())){
				pk_accoutbook=conds[i].getValue();
			}
		} 
		UFDate lodate=ClientEnvironment.getInstance().getDate();
		AccperiodmonthVO ac=tool.getMonthVOByDate(lodate,pk_accoutbook);	
		pk_accperiod=ac.getPrimaryKey();
		SumDealVO[] vos=doQuery(pk_corp, pk_accoutbook, pk_accperiod);	
		getBufferData().clear();

		AggregatedValueObject vo = (AggregatedValueObject) Class.forName(
				getUIController().getBillVoName()[0]).newInstance();
		vo.setChildrenVO(vos);
		getBufferData().addVOToBuffer(vo);

		updateBuffer();
		getBillUI().showHintMessage("查询完成");
	}
    /**
     * 根据  公司 + 核算账簿 维度  
     * @param pk_corp
     * @param pk_accoutbook
     * @param pk_accperiod
     * @return
     * @throws Exception 
     */
	public  SumDealVO[] doQuery(String pk_corp,String pk_accoutbook,String pk_accperiod) throws Exception {
		SumDealVO[] dealvos = null;
	            Class[] ParameterTypes = new Class[]{String.class,String.class,String.class};
	            Object[] ParameterValues = new Object[]{pk_corp,pk_accoutbook,pk_accperiod};
	            Object o = LongTimeTask.calllongTimeService(Xewcbpubconst.module, getBillUI(), 
	                    "正在归集成本...", 1, dealclass, null, 
	                    "doCollectionCost", ParameterTypes, ParameterValues);
	            if(o != null){
	            	dealvos = (SumDealVO[])o;
	            } 	  
	        return dealvos;		
	}
	/**
	 * 汇总
	 */
	public  void doSum() throws Exception {
		SumDealVO[] dvos= (SumDealVO[]) getBillCardPanelWrapper().getBillCardPanel().getBillModel().getBodyValueVOs(SumDealVO.class.getName());		
		if(dvos==null || dvos.length==0)
			return ;
		for(int i=0;i<dvos.length;i++){
			dvos[i].setIssum(new UFBoolean(true));
		}
		SumDealVO[] zdvos=tool.combinDealVO(dvos);
		getBufferData().clear();

		AggregatedValueObject vo = (AggregatedValueObject) Class.forName(
				getUIController().getBillVoName()[0]).newInstance();
		vo.setChildrenVO(zdvos);
		getBufferData().addVOToBuffer(vo);

		updateBuffer();	
	    getBillUI().showHintMessage("汇总完成");
	}
	/**
	 * 处理
	 */
	public  void doDeal() throws Exception {
		SumDealVO[] dvos= (SumDealVO[]) getBillCardPanelWrapper().getBillCardPanel().getBillModel().getBodyValueVOs(SumDealVO.class.getName());	
		if(dvos==null || dvos.length==0)
			return ;
		
		if(PuPubVO.getUFBoolean_NullAs(dvos[0].getIssum(), new UFBoolean(false)).booleanValue()==false){
			getBillUI().showErrorMessage("没有进行汇总处理");
			return ;
		}
		ClientEnvironment.getInstance().getMonthVO().getPrimaryKey();
		String[] infor={ClientEnvironment.getInstance().getDate().toString(),ClientEnvironment.getInstance().getUser().getPrimaryKey(),ClientEnvironment.getInstance().getCorporation().getPrimaryKey()};
        Class[] ParameterTypes = new Class[]{SumDealVO[].class,String[].class,String.class,String.class};
        Object[] ParameterValues = new Object[]{dvos,infor,pk_accperiod,pk_accoutbook};
        Object o = LongTimeTask.calllongTimeService(Xewcbpubconst.module, getBillUI(), 
                "正在成本处理...", 1, dealclass, null, 
                "doDealPubCost", ParameterTypes, ParameterValues);
        
    	AggregatedValueObject vo = (AggregatedValueObject) Class.forName(
				getUIController().getBillVoName()[0]).newInstance();
		vo.setChildrenVO(null);
		getBufferData().addVOToBuffer(vo);
		updateBuffer();	
		getBillCardPanelWrapper().getBillCardPanel().getBillModel().setBodyDataVO(null);
        getBillUI().showHintMessage("处理完成");
  }
	
	protected void onBoElse(int intBtn) throws Exception {		
		super.onBoElse(intBtn);
		switch (intBtn) {
		case XewcbPuBtnConst.hz:
			doSum();
			break;
		case XewcbPuBtnConst.deal://
			doDeal();
			break;	
		}
	}	
}
