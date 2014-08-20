package nc.bs.pf.changedir;

import nc.vo.pf.change.UserDefineFunction;
import nc.vo.pub.lang.UFDate;
import nc.vo.xew.pub.Xewpubconst;
/**
 * 成本汇总处理vo(XEW7)->成本汇总单 表体vo（XEW8）
 * XEW3->HN
 * 创建日期：(2004-11-18)
 * @author：平台脚本生成
 */
public class CHGCB03TOCB04bodyVO extends nc.bs.pf.change.VOConversion {
/**
 * CHG20TO21 构造子注解。
 */
public CHGCB03TOCB04bodyVO() {
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
//			 "H_pk_corp->H_pk_corp",//公司
//			 "H_pk_accoutbook->H_pk_accoutbook",//账簿
			 "H_pk_costelement->H_pk_costelement",//成本要素
//			 "H_->H_datasource",//数据来源
//			 "H_->H_datainfor",//来源信息
//			 "H_->H_datasourceid1",//来源主表id
//			 "H_->H_datasourceid2",//来源子表id
//			 "H_->H_datasourceid3",//来源孙表id
//			 "H_->H_dataname",//来源内容名称
			 "H_nmy->H_mny",//金额
//			 "H_pk_deptdoc->H_pk_defdoc1",//工作中心 项  部门 
//			 "H_pk_minearea->H_pk_defdoc2",//工作中心 项  矿区
//			 "H_vdef1->H_pk_defdoc11",//工作中心 项
//			 "H_vdef2->H_pk_defdoc12",//工作中心 项
//			 "H_vdef3->H_pk_defdoc13",//工作中心 项
//			 "H_vdef4->H_pk_defdoc14",//工作中心 项
//			 "H_vdef5->H_pk_defdoc15",//工作中心 项
//			 "H_vdef6->H_pk_defdoc16",//工作中心 项
//			 "H_vdef7->H_pk_defdoc17",//工作中心 项
//			 "H_vdef8->H_pk_defdoc18",//工作中心 项		 
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
