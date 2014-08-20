package nc.bs.xccb.pub;
import nc.bd.accperiod.AccountCalendar;
import nc.bs.logging.Logger;
import nc.bs.trade.business.HYSuperDMO;
import nc.bs.trade.comstatus.BillApprove;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFDate;
import nc.vo.xew.pub.BillDateGetter;
public class HYBillApprove extends BillApprove {
	
	public AggregatedValueObject approveHYBill(AggregatedValueObject billVo) throws BusinessException {
		
		AggregatedValueObject retVO = null;
		try {
			retVO = super.approveBill(billVo);
	        valuteDate(retVO);			
		//	retVO=modifyApproveDate(retVO);
			
		} catch (BusinessException re) {
			throw re;
		} catch (Exception e) {
			Logger.error(e.getMessage(), e);
			throw new BusinessException("单据审批出现未知异常::", e);
		}
		return retVO;
	}
     /**
      * 审批校验
      * @author zhf
      * @说明：（鹤岗矿业）
      * 2012-2-17下午12:28:01
      * @param retVO
      * @throws Exception
      */
	private void valuteDate(AggregatedValueObject retVO) throws BusinessException{
		//审批日期 不能小于 制单日期
		UFDate dapro=(UFDate) retVO.getParentVO().getAttributeValue("dapprovedate");		
		UFDate dmke=(UFDate) retVO.getParentVO().getAttributeValue("dbilldate");
        if(dapro.compareTo(dmke)<0)
    	  throw new BusinessException("审批日期 不能小于 单据日期 ");	
//        /*
//         * 波料单(以及其他矿级消耗单据)审批时 
//			
//	                推式 生成下游的其他出库单据时  
//			
//	                制单日期  qu当前操作员登录系统的业务日期
//           
//	                审批日期也是 
//			
//	               但是 要校验  当前登录人的登录日期是否   在 波料单的制单日期的会计期间内
//         */
//        UFDate dbilldate=dmke;
//		//获取基准会计期间方案	
//		AccountCalendar canl = AccountCalendar.getInstance();//获取基准会计方案
//		//设置日期
//		canl.setDate(dbilldate);
//	    //根据制单日期     获取对应会计月
//		AccperiodmonthVO ac= canl.getMonthVO();
//		UFDate startdate=ac.getBegindate();
//		UFDate enddate=ac.getEnddate();
//		//获得登录日期
//	    UFDate logdate=dapro;
//        boolean isAudt=true;
//   		if(logdate.compareTo(startdate)>=0 && logdate.compareTo(enddate)<=0){
//   			isAudt=true;
//   		}else{
//   			isAudt=false;
//   		}	
//        if(isAudt==false){
//        	throw new BusinessException(" 制单日期 和 审批日期不在同一 会计期间内 ");
//        }		
	}

	/**
	 * 修改审批日期
	 * 
	 * created by chenliang
	 * at 2007-8-21 上午11:14:32
	 */
	private AggregatedValueObject modifyApproveDate(AggregatedValueObject billVo) throws BusinessException{
		if(billVo==null||billVo.getParentVO()==null)
			return billVo;
		String primaryKey = billVo.getParentVO().getPrimaryKey();
		if(primaryKey==null||primaryKey.equals(""))
			return billVo;
		HYSuperDMO dmo=new HYSuperDMO();
		SuperVO headvo=dmo.queryByPrimaryKey(billVo.getParentVO().getClass(), primaryKey);
		UFDate approveDate=BillDateGetter.getApproveDate();
		headvo.setAttributeValue("dapprovedate", approveDate);
		billVo.setParentVO(headvo);
		dmo.update(headvo,new String[]{"dapprovedate"});
		billVo.setParentVO(dmo.queryByPrimaryKey(billVo.getParentVO().getClass(), primaryKey));
		return billVo;
	}
	@Override
	protected void specialApprove(AggregatedValueObject vo, HYSuperDMO dmo) throws Exception {
		super.specialApprove(vo, dmo);
	}

}
