package nc.bs.pf.changedir;

import nc.vo.pf.change.UserDefineFunction;
import nc.vo.pub.lang.UFDate;
import nc.vo.xew.pub.Xewpubconst;
/**
 * �ɱ����ܴ���vo(XEW7)->�ɱ����ܵ� ����vo��XEW8��
 * XEW3->HN
 * �������ڣ�(2004-11-18)
 * @author��ƽ̨�ű�����
 */
public class CHGCB03TOCB04bodyVO extends nc.bs.pf.change.VOConversion {
/**
 * CHG20TO21 ������ע�⡣
 */
public CHGCB03TOCB04bodyVO() {
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
//			 "H_pk_corp->H_pk_corp",//��˾
//			 "H_pk_accoutbook->H_pk_accoutbook",//�˲�
			 "H_pk_costelement->H_pk_costelement",//�ɱ�Ҫ��
//			 "H_->H_datasource",//������Դ
//			 "H_->H_datainfor",//��Դ��Ϣ
//			 "H_->H_datasourceid1",//��Դ����id
//			 "H_->H_datasourceid2",//��Դ�ӱ�id
//			 "H_->H_datasourceid3",//��Դ���id
//			 "H_->H_dataname",//��Դ��������
			 "H_nmy->H_mny",//���
//			 "H_pk_deptdoc->H_pk_defdoc1",//�������� ��  ���� 
//			 "H_pk_minearea->H_pk_defdoc2",//�������� ��  ����
//			 "H_vdef1->H_pk_defdoc11",//�������� ��
//			 "H_vdef2->H_pk_defdoc12",//�������� ��
//			 "H_vdef3->H_pk_defdoc13",//�������� ��
//			 "H_vdef4->H_pk_defdoc14",//�������� ��
//			 "H_vdef5->H_pk_defdoc15",//�������� ��
//			 "H_vdef6->H_pk_defdoc16",//�������� ��
//			 "H_vdef7->H_pk_defdoc17",//�������� ��
//			 "H_vdef8->H_pk_defdoc18",//�������� ��		 
		};
}
/**
* ��ù�ʽ��
* @return java.lang.String[]
*/
public String[] getFormulas() {
   return null;
}
/**
* �����û��Զ��庯����
*/
public UserDefineFunction[] getUserDefineFunction() {
	return null;
}
}
