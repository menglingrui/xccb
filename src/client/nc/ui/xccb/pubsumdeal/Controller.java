package nc.ui.xccb.pubsumdeal;
import nc.ui.trade.bill.ICardController;
import nc.ui.trade.bill.ISingleController;
import nc.ui.trade.businessaction.IBusinessActionType;
import nc.ui.trade.button.IBillButton;
import nc.vo.trade.pub.HYBillVO;
import nc.vo.xccb.pub.XewcbPuBtnConst;
import nc.vo.xccb.pub.Xewcbpubconst;
import nc.vo.xccb.sumdel.SumDealVO;
public class Controller implements ICardController,ISingleController{

	public String[] getCardBodyHideCol() {
		return null;
	}

	public int[] getCardButtonAry() {
		return  new int[] {
//				IBillButton.Add,
//				IBillButton.Edit,
				IBillButton.Query,
				XewcbPuBtnConst.hz,
				XewcbPuBtnConst.deal
				//ICaBillButton.cal,
//				IBillButton.Line,
//				IBillButton.Save,
//				IBillButton.Cancel,	
//				IBillButton.Commit,
//				IBillButton.Action,	
//				IBillButton.Delete,			
////				IBillButton.Line,
//				IBillButton.Brow,
//				IBillButton.Refresh,			 
//				IBillButton.Return,
//				HgPuBtnConst.ASSPRINT,
//				HgPuBtnConst.ASSQUERY
		  };
	}

	public boolean isShowCardRowNo() {
		return true;
	}

	public boolean isShowCardTotal() {
		return true;
	}

	public String getBillType() {
		return Xewcbpubconst.bill_code_pubsumdeal;
	}

	public String[] getBillVoName() {
		return new String[]{ HYBillVO.class.getName(),
				SumDealVO.class.getName(),
				SumDealVO.class.getName()};
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
		return null;
	}

	public String getHeadZYXKey() {
		return null;
	}

	public String getPkField() {
		return null;
	}

	public Boolean isEditInGoing() throws Exception {
		return null;
	}

	public boolean isExistBillStatus() {
		return false;
	}

	public boolean isLoadCardFormula() {
		return false;
	}

	public boolean isSingleDetail() {
		return true;
	}
}
