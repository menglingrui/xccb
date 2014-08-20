package nc.vo.xccb.pub;

import nc.ui.pub.ServerTimeProxy;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;

public class BillDateGetter {
	
	private BillDateGetter(){}

	/**
	 * 审批日期
	 * 
	 */
	public static UFDate getApproveDate() {
		UFDate date = new UFDate(getServerTime().getMillis());
		return date;
	}

	/**
	 * 单据日期
	 * 
	 */
	public static UFDate getBillDate() {
		UFDate date = new UFDate(getServerTime().getMillis());
		return date;
	}

	/**
	 * 制单日期
	 * 
	 */
	public static UFDate getMakeDate() {
		UFDate date = new UFDate(getServerTime().getMillis());
		return date;
	}

	/**
	 * 获得所登录的服务器日期时间
	 * 
	 */
	public static UFDateTime getServerTime() {
		return ServerTimeProxy.getInstance().getServerTime();
	}

}
