package nc.bs.pf.changedir;

import nc.vo.pf.change.UserDefineFunction;
import nc.vo.xew.proaccept.ProAcceptBVO;
import nc.vo.xew.pub.Xewpubconst;
/**
 * 成本汇总单 表体vo（XEW8）->公用成本核算单 表体vo(XEWA)
 * XEW3->HN
 * 创建日期：(2004-11-18)
 * @author：平台脚本生成
 */
public class CHGCB04TOCB05bodyVO extends nc.bs.pf.change.VOConversion {
/**
 * CHG20TO21 构造子注解。
 */
public CHGCB04TOCB05bodyVO() {
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
			 "H_pk_costelement->H_pk_costelement",//成本要素
			 "H_worknum->H_"+ProAcceptBVO.costdrivervale,//动因值
			 "H_nmy->H_"+ProAcceptBVO.costallonmy,//分配金额
			 "H_pk_cubasdoc->H_pk_cubasdoc",
			 "H_pk_cumandoc->H_pk_cumandoc",
			 "H_pk_project->H_pk_project",
			 "H_pk_jobbasfil->H_pk_jobbasfil",
			 "H_pk_invbasdoc->H_pk_invbasdoc",
			 "H_pk_invmandoc->H_pk_invmandoc",
			 "H_pk_defdoc2->H_sermonth",//服务月限
			 "H_vlastbillid->H_pk_costaccount",
			 "H_vlastbillrowid->H_pk_costaccount_b",
			 "H_vlastbillcode->H_vbillno",
			 "H_csourcebillcode->H_vbillno",
		
			 "H_vsourcebillid->H_pk_costaccount",
			 "H_vsourcebillrowid->H_pk_costaccount_b",
			 "H_VRESERVE5->H_pk_proaccept_b",
//			 "H_->H_",
//			 "H_->H_",
//			 "H_->H_",
//			 "H_->H_",
//			 "H_->H_",	 	 
		};
}
/**
* 获得公式。
* @return java.lang.String[]
*/
public String[] getFormulas() {
   return new String[]{
		   "H_vlastbilltype->\""+Xewpubconst.bill_code_costsum+"\""  ,
		   "H_vsourcebilltype->\""+Xewpubconst.bill_code_costsum+"\"",		   
   };
}
/**
* 返回用户自定义函数。
*/
public UserDefineFunction[] getUserDefineFunction() {
	return null;
}
}
