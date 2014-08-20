package nc.bs.xccb.pub;

import nc.bs.logging.Logger;
import nc.bs.trade.business.HYSuperDMO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.pf.IPfBackCheck2;

public class HYBillUnApprove  {
/**
 * BillApprove 构造子注解。
 */
public HYBillUnApprove() {
	super();
}
/**
* 各业务组件可进行重载
* @param vo nc.vo.pub.AggregatedValueObject
* @exception java.lang.Exception 异常说明。
*/
protected void specialUnApprove(AggregatedValueObject vo, HYSuperDMO dmo)
	throws java.lang.Exception {
}
/**
 * 进行反审批单据处理，返回AggregateValueObject;
 * 创建日期：(2004-2-27 18:58:22)
 * @param billVo nc.vo.pub.AggregatedValueObject
 * @exception java.rmi.RemoteException 异常说明。
 */
public AggregatedValueObject unApproveHYBill(AggregatedValueObject billVo) throws BusinessException {
	HYSuperDMO dmo = null;
	try
	{
		SuperVO headVO = (SuperVO) billVo.getParentVO();
		dmo = new HYSuperDMO();

		specialUnApprove(billVo, dmo);
		//更新单据状态为未审批
		if(!(billVo instanceof IPfBackCheck2)){
			updateBillStatusForUnapp(billVo, dmo);
			}		
		//查询TS
		billVo.setParentVO(
			dmo.queryByPrimaryKey(headVO.getClass(), headVO.getPrimaryKey()));
		return billVo;
	}
	catch (BusinessException be)
	{
		throw be;
	}
	catch (Exception ex)
	{
		Logger.error(ex.getMessage(), ex);
		throw new BusinessException("业务审批组件异常",ex);
	}
}
/**
 * 将单据设置为未审批状态
 * 创建日期：(2004-9-8 13:14:45)
 * @param vo nc.vo.pub.AggregatedValueObject
 */
private void updateBillStatusForUnapp(AggregatedValueObject vo, HYSuperDMO dmo) throws Exception{
	try {
		nc.vo.pub.SuperVO header = (nc.vo.pub.SuperVO)vo.getParentVO();
		
		String pk_bill = (String)header.getPrimaryKey();

		String pkFieldName = header.getPKFieldName();

		dmo.backNoState(
			header.getTableName(),
			pkFieldName, 
			pk_bill,
			null,
			null,
			null
			);
	}
	catch (BusinessException be)
	{
		throw be;
	}
	catch (Exception ex)
	{
		Logger.error(ex.getMessage(), ex);
		throw new BusinessException("业务审批组件异常",ex);
	}
	}
}
