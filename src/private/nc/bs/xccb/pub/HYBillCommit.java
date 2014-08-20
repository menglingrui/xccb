package nc.bs.xccb.pub;

import java.util.ArrayList;

import nc.bs.logging.Logger;
import nc.bs.trade.business.HYSuperDMO;
import nc.bs.trade.comstatus.BillCommit;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.trade.field.BillField;
import nc.vo.trade.pub.HYBillVO;
/**
 * 提交
 */
public class HYBillCommit extends BillCommit {

	public final ArrayList commitHYBill(AggregatedValueObject billVo)
			throws BusinessException {

		ArrayList retList = null;
		try {
			if(billVo!=null && billVo instanceof HYBillVO){
				HYBillVO hyvo = (HYBillVO)billVo;
				hyvo.setM_billField(BillField.getInstance());
			}
			retList = super.commitBill(billVo);
		} catch (BusinessException be) {
			Logger.error(be.getMessage(), be);
			throw be;
		}catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException("单据提交出现未知异常::", e);
		}
		return retList;	
	}
	/**
	 * 各业务组件可进行重载
	 * @param vo	nc.vo.pub.AggregatedValueObject
	 * @exception java.lang.Exception	异常说明。
	 */
	protected void specialCommit(AggregatedValueObject vo, HYSuperDMO dmo) throws java.lang.Exception {
	}
}
