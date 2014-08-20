package nc.ui.xccb.costelement;
import nc.ui.pub.ButtonObject;
import nc.ui.pub.ClientEnvironment;
import nc.ui.trade.base.IBillOperate;
import nc.ui.trade.bill.BillTemplateWrapper;
import nc.ui.trade.business.HYPubBO_Client;
import nc.ui.trade.controller.IControllerBase;
import nc.ui.trade.manage.BillManageUI;
import nc.ui.trade.pub.VOTreeNode;
import nc.ui.trade.treemanage.TreeManageEventHandler;
import nc.ui.zmpub.pub.bill.BillRowNo;
import nc.uif.pub.exception.UifException;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.xccb.costelement.CostelementVO;
import nc.vo.xccb.pub.Xewcbpubconst;

public class EventHandler extends TreeManageEventHandler{
	public EventHandler(BillManageUI billUI, IControllerBase control) {
		super(billUI, control);
	}
	@Override
	protected void onBoRefresh() throws Exception {
		ClientUI ui=(ClientUI) getBillUI();
		String pk_accoutbook =ui.getPk_accoutbook();
		if(pk_accoutbook==null || pk_accoutbook.length()==0){
			throw new UifException("请选择账簿");
		}	
		ui.loadTreeData(pk_accoutbook);
		getBillTreeManageUI().afterInit();
		getBillTreeManageUI().modifyRootNodeShowName("节点");
		getBillTreeManageUI().setBillOperate(nc.ui.trade.base.IBillOperate.OP_INIT);
	}
	/**
	 * 按钮m_boEdit点击时执行的动作,如有必要，请覆盖.
	 */
	protected void onBoEdit() throws Exception {
	
		if (getBillTreeManageUI().isListPanelSelected()) {
			getBillTreeManageUI().setCurrentPanel(BillTemplateWrapper.CARDPANEL);
			getBufferData().updateView();
		}
		ClientUI ui=(ClientUI) getBillUI();
		ui.isdatasource();
		super.onBoEdit();
	}
	
	/**
	 * 
	 */
	public void onTreeSelected(VOTreeNode selectnode){
		try{
			onQueryHeadData(selectnode);
		} catch (BusinessException ex) {
			getBillUI().showErrorMessage(ex.getMessage());
			ex.printStackTrace();
		} catch (Exception e) {
			getBillUI().showErrorMessage(e.getMessage());
			e.printStackTrace();
		}
	}
	/**
	 * 按账簿复制操作
	 */
	public void onBoAccountCopy(){
		ClientUI ui =(ClientUI) getBillUI();
	    String pk_accountbook=ui.getPk_accoutbook();
	    if(pk_accountbook==null || pk_accountbook.length()==0)
	    	getBillUI().showErrorMessage("请选择要复制的账簿");		
	}
	/**
	 */
	private void onQueryHeadData(VOTreeNode selectnode) throws Exception{

		Class voClass = Class.forName(getUIController().getBillVoName()[1]);

		SuperVO vo = (SuperVO)voClass.newInstance();
			
		String strWhere = "(isnull(dr,0)=0)";

		if(vo.getParentPKFieldName() != null)
			strWhere = "(" + strWhere + ") and " + vo.getParentPKFieldName() + "='" + selectnode.getData().getPrimaryKey() + "'";

		SuperVO[] queryVos =
		    getBusiDelegator().queryHeadAllData(
				voClass,
				getUIController().getBillType(),
				strWhere);

		//清空缓冲数据
		getBufferData().clear();
		if (queryVos != null && queryVos.length != 0)
		{
			for (int i = 0; i < queryVos.length; i++)
			{
				AggregatedValueObject aVo =
					(AggregatedValueObject) Class
						.forName(getUIController().getBillVoName()[0])
						.newInstance();
				aVo.setParentVO(queryVos[i]);
				getBufferData().addVOToBuffer(aVo);
			}
			getBillUI().setListHeadData(queryVos);
			getBufferData().setCurrentRow(0);
			getBillUI().setBillOperate(IBillOperate.OP_NOTEDIT);
		}
		else
		{
			getBillUI().setListHeadData(queryVos);
			getBufferData().setCurrentRow(-1);

			getBillUI().setBillOperate(IBillOperate.OP_INIT);
		}
	}
	/**
	* 单据增加的处理
	* 创建日期：(2002-12-23 12:43:15)
	*/
	public void onBoAdd(ButtonObject bo) throws Exception {
		VOTreeNode node=getBillTreeManageUI().getBillTreeSelectNode();
		if(node==null){
			if(isExistRoot()){
				getBillUI().showErrorMessage("请选择中要新增节点");
				return;
			}
		}	
	    super.onBoAdd(bo);
	}
	/**
	 * 是否已经存在根结点
	 * @return
	 * @throws UifException 
	 */
	private boolean isExistRoot() throws UifException {
		ClientUI ui=(ClientUI) getBillUI();
		String pk_accoutbook =ui.getPk_accoutbook();
		if(pk_accoutbook==null || pk_accoutbook.length()==0){
			throw new UifException("请选择账簿");
		}		
		SuperVO[] vos=HYPubBO_Client.queryByCondition(CostelementVO.class, " isnull(dr,0)=0 " +
				" and pk_corp='"+ClientEnvironment.getInstance().getCorporation().getPrimaryKey()+"' and reserve1='root'" +
				" and pk_accoutbook='"+pk_accoutbook+"'");
		if(vos==null || vos.length==0){
			return false;
		}						
		return true;
	}
	//重载这个方法使得在BillManagerUI中能够调用得到
	@Override
	protected void onBoSave() throws Exception {
		super.onBoSave();
		onBoRefresh();
	}
	/**
	 * 按钮m_boDel点击时执行的动作,如有必要，请覆盖. 档案的删除处理
	 */
	protected void onBoDelete() throws Exception {
		// 界面没有数据或者有数据但是没有选中任何行
		if (getBufferData().getCurrentVO() == null || getBufferData().getCurrentVO().getParentVO()==null)
			return;
		CostelementVO vo= (CostelementVO) getBufferData().getCurrentVO().getParentVO();
		String  id=vo.getPrimaryKey();
		if(isExistNextRoot(id)){
			getBillUI().showErrorMessage("存在下级节点");
			return;
		}					
		super.onBoDelete();
		onBoRefresh();
	}
	/**
	 * 是否已经存下级节点
	 * @return
	 * @throws UifException 
	 */
	private boolean isExistNextRoot(String id) throws UifException {
		ClientUI ui=(ClientUI) getBillUI();
		String pk_accoutbook =ui.getPk_accoutbook();
		if(pk_accoutbook==null || pk_accoutbook.length()==0){
			throw new UifException("请选择账簿");
		}	
		SuperVO[] vos=HYPubBO_Client.queryByCondition(CostelementVO.class, " isnull(dr,0)=0 " +
				" and pk_corp='"+ClientEnvironment.getInstance().getCorporation().getPrimaryKey()+"' and reserve1='"+id+"'" +
				" and pk_accoutbook='"+pk_accoutbook+"'");
		if(vos==null || vos.length==0){
			return false;
		}						
		return true;
	}
	/**
	 * 按钮m_boLineAdd点击时执行的动作,如有必要，请覆盖.
	 */
	protected void onBoLineAdd() throws Exception {
	   super.onBoLineAdd();
	   BillRowNo.addLineRowNo(getBillCardPanelWrapper().getBillCardPanel(), Xewcbpubconst.bill_code_costelement, "vdef10");
	}

}
