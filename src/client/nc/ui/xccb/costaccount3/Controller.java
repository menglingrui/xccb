package nc.ui.xccb.costaccount3;
import nc.ui.trade.bill.AbstractManageController;
import nc.ui.trade.businessaction.IBusinessActionType;
import nc.ui.trade.button.IBillButton;
import nc.vo.xccb.costaccount.AggCostAccountVO;
import nc.vo.xccb.costaccount.CostAccountVO;
import nc.vo.xccb.costaccount.CostAccoutBVO;
import nc.vo.xccb.pub.XewcbPuBtnConst;
import nc.vo.xccb.pub.Xewcbpubconst;
import nc.vo.zmpub.pub.consts.ZmpubBtnConst;
public class Controller extends AbstractManageController{

	public String[] getCardBodyHideCol() {
		return null;
	}

	public int[] getCardButtonAry() {
		return  new int[] {
				IBillButton.Add,
				IBillButton.Edit,
				IBillButton.Query,
				//ICaBillButton.cal,
				IBillButton.Line,
				IBillButton.Save,
				XewcbPuBtnConst.cbfp,
				XewcbPuBtnConst.qxfp,
				IBillButton.Cancel,	
				IBillButton.Commit,
				IBillButton.Action,	
				IBillButton.Delete,			
//				IBillButton.Line,
				IBillButton.Brow,
				IBillButton.Refresh,			 
				IBillButton.Return,
				ZmpubBtnConst.ASSPRINT,
				ZmpubBtnConst.ASSQUERY,
		  };
	}

	public boolean isShowCardRowNo() {
		return true;
	}

	public boolean isShowCardTotal() {
		return true;
	}

	public String getBillType() {
		return Xewcbpubconst.bill_code_costsum;
	}

	public String[] getBillVoName() {
		return new String[]{ AggCostAccountVO.class.getName(),
				CostAccountVO.class.getName(),
				CostAccoutBVO.class.getName()};
	}

	public String getBodyCondition() {
		return null;
	}

	public String getBodyZYXKey() {
		return null;
	}

	public int getBusinessActionType() {
		return IBusinessActionType.PLATFORM;
	}

	public String getChildPkField() {
		return "pk_costaccount_b";
	}

	public String getHeadZYXKey() {
		return null;
	}

	public String getPkField() {
		return "pk_costaccount";
	}

	public Boolean isEditInGoing() throws Exception {
		return null;
	}

	public boolean isExistBillStatus() {
		return true;
	}

	public boolean isLoadCardFormula() {
		return true;
	}

	public String[] getListBodyHideCol() {
		return null;
	}

	public int[] getListButtonAry() {
		return new int[] {
				IBillButton.Add,
				IBillButton.Edit,
				IBillButton.Query,
		//		ICaBillButton.cal,
				IBillButton.Save,
				IBillButton.Cancel,	
				IBillButton.Commit,
				IBillButton.Action,	
				IBillButton.Delete,			
//				IBillButton.Line,
				IBillButton.Brow,
				IBillButton.Refresh,			 
				IBillButton.Card,
				ZmpubBtnConst.ASSPRINT,
				ZmpubBtnConst.ASSQUERY,
		  };
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

}
