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
			throw new UifException("��ѡ���˲�");
		}	
		ui.loadTreeData(pk_accoutbook);
		getBillTreeManageUI().afterInit();
		getBillTreeManageUI().modifyRootNodeShowName("�ڵ�");
		getBillTreeManageUI().setBillOperate(nc.ui.trade.base.IBillOperate.OP_INIT);
	}
	/**
	 * ��ťm_boEdit���ʱִ�еĶ���,���б�Ҫ���븲��.
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
	 * ���˲����Ʋ���
	 */
	public void onBoAccountCopy(){
		ClientUI ui =(ClientUI) getBillUI();
	    String pk_accountbook=ui.getPk_accoutbook();
	    if(pk_accountbook==null || pk_accountbook.length()==0)
	    	getBillUI().showErrorMessage("��ѡ��Ҫ���Ƶ��˲�");		
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

		//��ջ�������
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
	* �������ӵĴ���
	* �������ڣ�(2002-12-23 12:43:15)
	*/
	public void onBoAdd(ButtonObject bo) throws Exception {
		VOTreeNode node=getBillTreeManageUI().getBillTreeSelectNode();
		if(node==null){
			if(isExistRoot()){
				getBillUI().showErrorMessage("��ѡ����Ҫ�����ڵ�");
				return;
			}
		}	
	    super.onBoAdd(bo);
	}
	/**
	 * �Ƿ��Ѿ����ڸ����
	 * @return
	 * @throws UifException 
	 */
	private boolean isExistRoot() throws UifException {
		ClientUI ui=(ClientUI) getBillUI();
		String pk_accoutbook =ui.getPk_accoutbook();
		if(pk_accoutbook==null || pk_accoutbook.length()==0){
			throw new UifException("��ѡ���˲�");
		}		
		SuperVO[] vos=HYPubBO_Client.queryByCondition(CostelementVO.class, " isnull(dr,0)=0 " +
				" and pk_corp='"+ClientEnvironment.getInstance().getCorporation().getPrimaryKey()+"' and reserve1='root'" +
				" and pk_accoutbook='"+pk_accoutbook+"'");
		if(vos==null || vos.length==0){
			return false;
		}						
		return true;
	}
	//�����������ʹ����BillManagerUI���ܹ����õõ�
	@Override
	protected void onBoSave() throws Exception {
		super.onBoSave();
		onBoRefresh();
	}
	/**
	 * ��ťm_boDel���ʱִ�еĶ���,���б�Ҫ���븲��. ������ɾ������
	 */
	protected void onBoDelete() throws Exception {
		// ����û�����ݻ��������ݵ���û��ѡ���κ���
		if (getBufferData().getCurrentVO() == null || getBufferData().getCurrentVO().getParentVO()==null)
			return;
		CostelementVO vo= (CostelementVO) getBufferData().getCurrentVO().getParentVO();
		String  id=vo.getPrimaryKey();
		if(isExistNextRoot(id)){
			getBillUI().showErrorMessage("�����¼��ڵ�");
			return;
		}					
		super.onBoDelete();
		onBoRefresh();
	}
	/**
	 * �Ƿ��Ѿ����¼��ڵ�
	 * @return
	 * @throws UifException 
	 */
	private boolean isExistNextRoot(String id) throws UifException {
		ClientUI ui=(ClientUI) getBillUI();
		String pk_accoutbook =ui.getPk_accoutbook();
		if(pk_accoutbook==null || pk_accoutbook.length()==0){
			throw new UifException("��ѡ���˲�");
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
	 * ��ťm_boLineAdd���ʱִ�еĶ���,���б�Ҫ���븲��.
	 */
	protected void onBoLineAdd() throws Exception {
	   super.onBoLineAdd();
	   BillRowNo.addLineRowNo(getBillCardPanelWrapper().getBillCardPanel(), Xewcbpubconst.bill_code_costelement, "vdef10");
	}

}
