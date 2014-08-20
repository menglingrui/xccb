package nc.ui.xccb.costelement;

import nc.lfw.data.DatasetRelation;
import nc.ui.trade.businessaction.IBusinessActionType;
import nc.ui.trade.button.IBillButton;
import nc.ui.trade.treemanage.AbstractTreeManageController;
import nc.vo.xccb.costelement.CostelementB1VO;
import nc.vo.xccb.costelement.CostelementBVO;
import nc.vo.xccb.costelement.CostelementVO;
import nc.vo.xccb.costelement.ExAggCostelemetTVO;
import nc.vo.xccb.pub.Xewcbpubconst;

public  class Controller extends AbstractTreeManageController{


	
	public String[] getBillVoName() {
		return new String[] { ExAggCostelemetTVO.class.getName(),
				CostelementVO.class.getName(),
				CostelementBVO.class.getName(),
				CostelementB1VO.class.getName()
				};
	}


	public DatasetRelation[] getDatasetRelations() {
		return null;
	}


	public boolean isAutoManageTree() {
		return true;
	}


	public boolean isTableTree() {
		return false;
	}


	public String[] getCardBodyHideCol() {
		return null;
	}


	public int[] getCardButtonAry() {
		return new int[] {
				IBillButton.Add,IBillButton.Edit,
				IBillButton.Line, IBillButton.Save, 
				IBillButton.Delete,IBillButton.Return,
				IBillButton.Cancel,
				IBillButton.Refresh,IBillButton.Print,		
				
		};
	}


	public boolean isShowCardRowNo() {
		return true;
	}


	public boolean isShowCardTotal() {
		return true;
	}


	public String getBillType() {
		return  Xewcbpubconst.bill_code_costelement;
	}


	public String getBodyCondition() {
		return null;
	}


	public String getBodyZYXKey() {
		return null;
	}


	public int getBusinessActionType() {
		return IBusinessActionType.BD;
	}


	public String getChildPkField() {
		return "pk_costelement_b";
	}


	public String getHeadZYXKey() {
		return null;
	}


	public String getPkField() {
		return "pk_costelement";
	}


	public Boolean isEditInGoing() throws Exception {
		return null;
	}


	public boolean isExistBillStatus() {
		return false;
	}


	public boolean isLoadCardFormula() {
		return true;
	}


	public String[] getListBodyHideCol() {
		return null;
	}




	public String[] getListHeadHideCol() {
		return null;
	}


	public boolean isShowListRowNo() {
		return true;
	}


	public boolean isShowListTotal() {
		return true;
	}


	public boolean isChildTree() {
		return false;
	}


	public int[] getListButtonAry() {
	   return new int[] {
				IBillButton.Add,IBillButton.Edit,
				IBillButton.Line, IBillButton.Save, 
				IBillButton.Delete,IBillButton.Card,
				IBillButton.Cancel,
				IBillButton.Refresh,IBillButton.Print,		  		
		};
	}







}
