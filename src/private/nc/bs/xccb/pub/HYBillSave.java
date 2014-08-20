package nc.bs.xccb.pub;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import nc.bs.logging.Logger;
import nc.bs.trade.business.HYSuperDMO;
import nc.bs.trade.comsave.BillSave;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.SuperVO;
import nc.vo.pub.VOStatus;
import nc.vo.trade.pub.IExAggVO;


public class HYBillSave extends BillSave {

	public static String DATA_ERROR_ORACAL = "ORA-01438";

	public static String DATA_ERROR_MSSQL = "算术溢出错误";

	public static String DATA_ERROR_DB2 = "";
	
	public static String DATA_OVERFLOW_MSSQL = "将 float 转换为数据类型 numeric 时出现算术溢出错误。";

	public static String DATA_OVERFLOW_ORACLE = "ORA-01438: 值大于此列指定的允许精度";

	public static String DATA_OVERFLOW_DB2 = "";
	

	@Override
	public ArrayList saveBill(AggregatedValueObject billVo)
			throws BusinessException {
		ArrayList al = null;
		try {
			//单据保存前处理
			beforeSave(billVo);
			//保存
			al = super.saveBill(billVo);
			if (al != null && al.size()>1)
				billVo = (AggregatedValueObject)al.get(1);
			else
				billVo = null;
			//单据保存后处理
			afterSave(billVo);
		} catch (Exception e) {
//			if (e instanceof BusinessException) {
////				Logger.error("调试到这里：" + e);
////				e.printStackTrace();
//				if (e.getCause() == null)
//					transError(e.getMessage());
//				else {
//					String errMsg = e.getCause().getMessage() == null ? "" : e
//							.getCause().getMessage();
////					Logger.debug("调试到这里：errMsg==" + errMsg);
//					transError(e.getMessage());
//					
//				}
//			} else {
//				Logger.error("单据保存异常", e);
//				throw new BusinessException("单据保存异常", e);
//			}
			if (e instanceof BusinessException) {
				Logger.error("调试到这里：" + e);
				e.printStackTrace();
				if (e.getCause() == null)
					throw (BusinessException) e;
				else if (e.getCause() != null && e.getCause().getMessage() != null
						&& e.getCause().getMessage().indexOf("输入的值大于数据库允许的精确度,请重新输入。") > 0) {
					throw new BusinessException("输入的值大于数据库允许的精确度,请重新输入。");
				} else {
					String errMsg = e.getCause().getMessage() == null ? "" : e.getCause().getMessage();
					Logger.debug("调试到这里：errMsg==" + errMsg);
					if (isDataErrorMsg(errMsg)) {
						errMsg = "输入的值大于数据库允许的精确度,请重新输入。";
					}
					BusinessException ex1 = new BusinessException(errMsg);
					throw ex1;
				}
			} else {
				Logger.error("单据保存异常", e);
				throw new BusinessException("单据保存异常", e);
			}
		}
		return al;
	}

	/**
	 * 是否数据精度错误
	 * @param msg
	 * @return
	 * twh (2006-11-30 上午10:46:18)<br>
	 */
	private boolean isDataErrorMsg(String msg) {
		if ((DATA_ERROR_DB2.length() > 0 && msg.indexOf(DATA_ERROR_DB2) >= 0)
				|| (DATA_ERROR_MSSQL.length() > 0 && msg
						.indexOf(DATA_ERROR_MSSQL) >= 0)
				|| (DATA_ERROR_ORACAL.length() > 0 && msg
						.indexOf(DATA_ERROR_ORACAL) >= 0))
			return true;
		else
			return false;
	}

	/**
	 * 单据保存前处理
	 * @param billVo
	 * @throws BusinessException
	 * twh (2006-11-30 上午10:39:22)<br>
	 */
	protected void beforeSave(AggregatedValueObject billVo)
			throws BusinessException {
		return;
	}

	/**
	 * 单据保存后处理
	 * @param billVo
	 * @throws BusinessException
	 * twh (2006-11-30 上午10:39:34)<br>
	 */
	protected void afterSave(AggregatedValueObject billVo)
			throws BusinessException {
		return;
	}
	
	/**
	 * 转换错误信息为用户可接受的信息
	 * @author zhucx1
	 * 20072007-10-27下午03:25:34
	 * @param errMsg
	 * @throws BusinessException
	 */
	private void transError(String errMsg) throws BusinessException{
		if (isDataErrorMsg(errMsg)) {
			errMsg = "输入的值大于数据库允许的精确度,请重新输入。";
		}
		BusinessException ex1 = new BusinessException(errMsg);
		throw ex1;

	}

	/**
	 * 单据VO保存。 创建日期：(2004-2-27 11:15:29)
	 * 
	 * @return nc.vo.pub.AggregatedValueObject
	 * @param vo
	 *            nc.vo.pub.AggregatedValueObject
	 * @exception java.rmi.RemoteException
	 *                异常说明。
	 */
	public AggregatedValueObject saveBillCom(nc.vo.pub.AggregatedValueObject billVo) throws BusinessException {
		if (billVo == null)
			throw new BusinessException(nc.bs.ml.NCLangResOnserver.getInstance().getStrByID("uffactory_hyeaa",
					"UPPuffactory_hyeaa-000041")/*
												 * @res
												 * "警告：saveBill得到的参数为null，未能保存任何数据"
												 */);

		if (billVo.getParentVO() == null) {
			System.out.println("警告：saveBill得到的参数的表头VO未null，未能保存任何数据");
			return null;
		}
		HYSuperDMO dmo = null;
		AggregatedValueObject retVO = null;
		try {

			dmo = new HYSuperDMO();
			if (isNew(billVo)) {
				retVO = saveBillWhenAdd(billVo, dmo);
			} else {
				retVO = saveBillWhenEdit(billVo, dmo);
			}
			return retVO;
		}

		catch (Exception e) {
			if (e instanceof BusinessException) {
				throw (BusinessException) e;
			}
			Logger.error("调试到这里：" + e);
			e.printStackTrace();
			if (e.getCause() == null)
				throw (BusinessException) e;
			else {
				String errMsg = e.getCause().getMessage() == null ? "" : e.getCause().getMessage();
				Logger.debug("调试到这里：errMsg==" + errMsg);
				if (isDataErrorMsg(errMsg)) {
					errMsg = "输入的值大于数据库允许的精确度,请重新输入。";
				}
				BusinessException ex1 = new BusinessException(errMsg);
				throw ex1;
			}
		}
	}

	private AggregatedValueObject saveBillWhenAdd(AggregatedValueObject vo, HYSuperDMO dmo) throws RemoteException {

		try {
			// 插入表头：
			SuperVO headVO = (SuperVO) vo.getParentVO();
			headVO.setAttributeValue("dr", new Integer(0));
			String key = dmo.insert(headVO);

			// 插入子表
			SuperVO[] items = (SuperVO[]) vo.getChildrenVO();
			if (vo instanceof IExAggVO) {
				items = (SuperVO[]) ((IExAggVO) vo).getAllChildrenVO();
			}
			if (items != null) {
				// 插入表体各项：
				for (int i = 0; i < items.length; i++) {
					items[i].setAttributeValue("dr", new Integer(0));
					items[i].setStatus(nc.vo.pub.VOStatus.NEW);
				}
				saveItems(dmo, items, headVO.getPKFieldName(), key);
			}

			vo.setParentVO(dmo.queryByPrimaryKey(headVO.getClass(), key));
			return vo;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException("HYPubBO::saveBillWhenAdd(AggregateValueObject) Exception!", e);
		}

	}
	
	/**
	* 向数据库中插入一个VO对象。
	*
	* 创建日期：(2003-9-18)
	* @param sendmny nc.vo.testforei.AggregatedValueObject
	* @return java.lang.String  所插入VO对象的主键字符串。
	*
	*/
	private AggregatedValueObject saveBillWhenEdit(
		AggregatedValueObject billVO,
		HYSuperDMO dmo)
		throws RemoteException {

		try
		{
			SuperVO headvo = (SuperVO) billVO.getParentVO();
			String billPK = headvo.getPrimaryKey();
			String mainPKFiledName = headvo.getPKFieldName();
			headvo.setAttributeValue("dr",new Integer(0));
			dmo.update(headvo);
			boolean isMultiChild = false;
			//插入子表
			SuperVO[] items = (SuperVO[]) billVO.getChildrenVO();
			if (billVO instanceof IExAggVO)
			{
				isMultiChild = true;
				items = (SuperVO[]) ((IExAggVO) billVO).getAllChildrenVO();
			}
			if (items != null)
				saveItems(dmo, items, mainPKFiledName, billPK);

			billVO.setParentVO(dmo.queryByPrimaryKey(headvo.getClass(), billPK));
			if (items != null && items.length > 0)
				setChildData(billVO,isMultiChild,dmo);
			return billVO;
		}
		catch (Exception e)
		{
			Logger.error(e);
			throw new RemoteException(
				"HYPubBO::saveBillWhenEdit(AggregatedValueObject) Exception!",
				e);
		}

	}
	
	/**
	 * 设置子表数据。
	 * 创建日期：(2004-2-27 20:59:29)
	 * @param vo nc.vo.pub.AggregatedValueObject
	 * @exception java.lang.Exception 异常说明。
	 */
	private void setChildData(
		AggregatedValueObject vo,
		boolean isMultiChild,
		HYSuperDMO dmo)
		throws java.lang.Exception {
		//查询子表
		if (isMultiChild)
		{
			IExAggVO exAggVO = (IExAggVO) vo;
			for (int i = 0; i < exAggVO.getTableCodes().length; i++)
			{
				String tableCode = exAggVO.getTableCodes()[i];
				SuperVO[] items = (SuperVO[]) exAggVO.getTableVO(tableCode);
				exAggVO.setTableVO(tableCode, dealChildVO(items));
			}
		}
		else
		{
			SuperVO[] itemVos = (SuperVO[]) vo.getChildrenVO();
			SuperVO headVo = (SuperVO) vo.getParentVO();
			vo.setChildrenVO(
				dmo.queryByWhereClause(
					itemVos[0].getClass(),
					headVo.getPKFieldName()
						+ "='"
						+ headVo.getPrimaryKey()
						+ "' and isnull(dr,0)=0"));
		}
	}

	/**
	 * 处理子表数据（除去删除的)。
	 * 创建日期：(2004-2-27 21:27:21)
	 * @param items nc.vo.pub.SuperVO[]
	 * @exception java.lang.Exception 异常说明。
	 */
	private SuperVO[] dealChildVO(SuperVO[] items) throws java.lang.Exception {
		if (items == null)
			return null;
		Vector v = new Vector();
		for (int i = 0; i < items.length; i++)
		{
			if (items[i].getStatus() != VOStatus.DELETED)
				v.addElement(items[i]);
		}
		if (v.size() > 0)
		{
			SuperVO[] vos =
				(SuperVO[]) java.lang.reflect.Array.newInstance(
					items.getClass().getComponentType(),
					v.size());
			v.copyInto(vos);
			return vos;
		}
		return null;
	}
	 
	
	protected boolean isNew(AggregatedValueObject billVO) throws BusinessException {
		return billVO.getParentVO().getPrimaryKey() == null || billVO.getParentVO().getPrimaryKey().length() == 0;

	}
}
