package nc.bs.pf.changedir;

import nc.vo.pf.change.UserDefineFunction;
import nc.vo.xew.proaccept.ProAcceptBVO;
import nc.vo.xew.pub.Xewpubconst;
/**
 * �ɱ����ܵ� ����vo��XEW8��->���óɱ����㵥 ����vo(XEWA)
 * XEW3->HN
 * �������ڣ�(2004-11-18)
 * @author��ƽ̨�ű�����
 */
public class CHGCB04TOCB05bodyVO extends nc.bs.pf.change.VOConversion {
/**
 * CHG20TO21 ������ע�⡣
 */
public CHGCB04TOCB05bodyVO() {
	super();
}
/**
* ��ú������ȫ¼�����ơ�
* @return java.lang.String[]
*/
public String getAfterClassName() {
	return null;
}
/**
* �����һ���������ȫ¼�����ơ�
* @return java.lang.String[]
*/
public String getOtherClassName() {
	return null;
}
/**
* ����ֶζ�Ӧ��
* @return java.lang.String[]
*/

public String[] getField() {
	return new String[] {
			 "H_pk_costelement->H_pk_costelement",//�ɱ�Ҫ��
			 "H_worknum->H_"+ProAcceptBVO.costdrivervale,//����ֵ
			 "H_nmy->H_"+ProAcceptBVO.costallonmy,//������
			 "H_pk_cubasdoc->H_pk_cubasdoc",
			 "H_pk_cumandoc->H_pk_cumandoc",
			 "H_pk_project->H_pk_project",
			 "H_pk_jobbasfil->H_pk_jobbasfil",
			 "H_pk_invbasdoc->H_pk_invbasdoc",
			 "H_pk_invmandoc->H_pk_invmandoc",
			 "H_pk_defdoc2->H_sermonth",//��������
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
* ��ù�ʽ��
* @return java.lang.String[]
*/
public String[] getFormulas() {
   return new String[]{
		   "H_vlastbilltype->\""+Xewpubconst.bill_code_costsum+"\""  ,
		   "H_vsourcebilltype->\""+Xewpubconst.bill_code_costsum+"\"",		   
   };
}
/**
* �����û��Զ��庯����
*/
public UserDefineFunction[] getUserDefineFunction() {
	return null;
}
}
