package nc.ui.pf.changedir;

import nc.ui.pf.change.VOConversionUI;
import nc.vo.pf.change.UserDefineFunction;

/**
 * 定额单 材料->钻探工程验收单,井巷工程验收单
 * 
 * @author dp
 * 
 */
public class CHGQYOTATODRILLVO extends VOConversionUI {
	/**
	 * CHG20TO21 构造子注解。
	 */
	public CHGQYOTATODRILLVO() {
		super();
	}
	/**
	* 获得后续类的全录经名称。
	* @return java.lang.String[]
	*/
	public String getAfterClassName() {
		return null;
	}
	/**
	* 获得另一个后续类的全录径名称。
	* @return java.lang.String[]
	*/
	public String getOtherClassName() {
		return null;
	}
	/**
	 * 获得字段对应。
	 * @return java.lang.String[]
	 */
	public String[] getField() {
		return new String[] {
				// "H_pk_corp->H_pk_corp",//公司
				 "H_pk_invbasdoc->H_pk_invbasdoc",//存货档案基本id
				 "H_pk_invmandoc->H_pk_invmandoc",//管理id
				 "H_num->H_quantity"//数量
				 
			};
	}
	/**
	* 获得公式。
	* @return java.lang.String[]
	*/
	public String[] getFormulas() {
	  return null;
	}
	/**
	* 返回用户自定义函数。
	*/
	public UserDefineFunction[] getUserDefineFunction() {
		return null;
	}
}
