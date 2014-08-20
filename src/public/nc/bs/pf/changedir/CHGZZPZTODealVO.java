package nc.bs.pf.changedir;

import nc.vo.pf.change.UserDefineFunction;
/**
 * 总账凭证->数据归集vo
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
		    "H_pk_corp->H_pk_corp",//公司
		    "H_pk_costelement->H_pk_costelement",//成本要素
		    "H_pk_accoutbook->H_pk_accoutbook",//核算账簿
		    "H_datasourceid1->H_pk_voucher",//主键
		    "H_datasourceid2->H_pk_detail",//子表主键
		    "H_datasourceid3->H_assid",//孙表主键
            "H_pk_defdoc3->H_pk_accsubj",//科目主键			
		    "H_mny->H_localdebitamount",//金额
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
	* 获得公式。
	*/
	public String[] getFormulas() {
		return new String[] {
				"H_datasource->int(1)",
				"H_datainfor->int(1)",
		        "H_dataname->getColValue(bd_accsubj,subjname,pk_accsubj,pk_accsubj )",
				};
	}
	/**
	* 返回用户自定义函数。
	*/
	public UserDefineFunction[] getUserDefineFunction() {
		return null;
	}


}
