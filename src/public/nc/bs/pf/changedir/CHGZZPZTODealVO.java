package nc.bs.pf.changedir;

import nc.vo.pf.change.UserDefineFunction;
/**
 * ����ƾ֤->���ݹ鼯vo
 * @author mlr
 *
 */
public class CHGZZPZTODealVO extends  nc.bs.pf.change.VOConversion{


	public CHGZZPZTODealVO() {
		super();
	}

	public String getAfterClassName() {
		return null;
	}

	public String getOtherClassName() {
		return null;
	}	
	public String[] getField() {
		return new String[] {
		    "H_pk_corp->H_pk_corp",//��˾
		    "H_pk_costelement->H_pk_costelement",//�ɱ�Ҫ��
		    "H_pk_accoutbook->H_pk_accoutbook",//�����˲�
		    "H_datasourceid1->H_pk_voucher",//����
		    "H_datasourceid2->H_pk_detail",//�ӱ�����
		    "H_datasourceid3->H_assid",//�������
            "H_pk_defdoc3->H_pk_accsubj",//��Ŀ����			
		    "H_mny->H_localdebitamount",//���
		    "H_pk_defdoc1->H_pk_defdoc1",//
		    "H_pk_defdoc2->H_pk_defdoc2",//
		    "H_pk_defdoc11->H_pk_defdoc11",//
		    "H_pk_defdoc12->H_pk_defdoc12",//
		    "H_pk_defdoc13->H_pk_defdoc13",//
		    "H_pk_defdoc14->H_pk_defdoc14",//
		    "H_pk_defdoc15->H_pk_defdoc15",//
		    "H_pk_defdoc16->H_pk_defdoc16",//
		    "H_pk_defdoc17->H_pk_defdoc17",//
		    "H_pk_defdoc18->H_pk_defdoc18",//		    		    				
		};
	}
	/**
	* ��ù�ʽ��
	*/
	public String[] getFormulas() {
		return new String[] {
				"H_datasource->int(1)",
				"H_datainfor->int(1)",
		        "H_dataname->getColValue(bd_accsubj,subjname,pk_accsubj,pk_accsubj )",
				};
	}
	/**
	* �����û��Զ��庯����
	*/
	public UserDefineFunction[] getUserDefineFunction() {
		return null;
	}


}
