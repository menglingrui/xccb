
package nc.vo.xccb.costaccount;
import nc.vo.pub.lang.UFDouble;
import nc.vo.zmpub.pub.bill.HYChildSuperVO;
/**
 * 成本核算表体vo
 * @author mlr
 * @version NCPrj 1.0
 */
@SuppressWarnings("serial")
public class  CostAccoutBVO extends HYChildSuperVO {

	private String pk_costaccount;//主表主键
	private UFDouble worknum;//工作量
	private String pk_project;//项目
	private String pk_jobbasfil;//项目基本id

	private String pk_costaccount_b;//主键
	private String pk_costelement;//成本要素
	private String pk_cumandoc;//供应商
	private String pk_cubasdoc;//供应商基本id
	private String pk_invmandoc;//作业 存货管理档案主键
	private String pk_invbasdoc ;//存货基本档案主键
	public String vreserve2;//直接成本核算单材料主键
	public String vreserve3;//直接成本核算单 材料基本主键
	public String vreserve1;//验收单 存放成本要素 主键的 临时字段
	
	public UFDouble nreserve1;//成本核算调整单 核算金额
	public UFDouble nreserve2;//成本核算调整单 验收金额
	public String vdef10;//
	
	public UFDouble nreserve3;//税率
	public UFDouble nreserve4;//无税金额
	public UFDouble nreserve5;//税额
	private UFDouble nmy;//金额
	
	public UFDouble nreserve6; 
	public UFDouble nreserve7;
	public UFDouble nreserve8;
	public UFDouble nreserve9;
	public UFDouble nreserve10;
	public UFDouble nreserve11;
	
	public String pk_defdoc1;//自定义项档案 服务月限主键
	public String pk_defdoc2; //服务月限
	public String pk_defdoc3;//作业类别
	public String pk_defdoc4;//材料类别
	public String pk_defdoc5;//工程类别
	
	
	public String getPk_defdoc1() {
		return pk_defdoc1;
	}

	public void setPk_defdoc1(String pk_defdoc1) {
		this.pk_defdoc1 = pk_defdoc1;
	}

	public String getPk_defdoc2() {
		return pk_defdoc2;
	}

	public void setPk_defdoc2(String pk_defdoc2) {
		this.pk_defdoc2 = pk_defdoc2;
	}

	public String getPk_defdoc3() {
		return pk_defdoc3;
	}

	public void setPk_defdoc3(String pk_defdoc3) {
		this.pk_defdoc3 = pk_defdoc3;
	}

	public String getPk_defdoc4() {
		return pk_defdoc4;
	}

	public void setPk_defdoc4(String pk_defdoc4) {
		this.pk_defdoc4 = pk_defdoc4;
	}

	public UFDouble getNreserve3() {
		return nreserve3;
	}

	public void setNreserve3(UFDouble nreserve3) {
		this.nreserve3 = nreserve3;
	}

	public UFDouble getNreserve4() {
		return nreserve4;
	}

	public void setNreserve4(UFDouble nreserve4) {
		this.nreserve4 = nreserve4;
	}

	public UFDouble getNreserve5() {
		return nreserve5;
	}

	public void setNreserve5(UFDouble nreserve5) {
		this.nreserve5 = nreserve5;
	}

	public UFDouble getNreserve6() {
		return nreserve6;
	}

	public void setNreserve6(UFDouble nreserve6) {
		this.nreserve6 = nreserve6;
	}

	public UFDouble getNreserve7() {
		return nreserve7;
	}

	public void setNreserve7(UFDouble nreserve7) {
		this.nreserve7 = nreserve7;
	}

	public UFDouble getNreserve8() {
		return nreserve8;
	}

	public void setNreserve8(UFDouble nreserve8) {
		this.nreserve8 = nreserve8;
	}

	public UFDouble getNreserve9() {
		return nreserve9;
	}

	public void setNreserve9(UFDouble nreserve9) {
		this.nreserve9 = nreserve9;
	}

	public UFDouble getNreserve10() {
		return nreserve10;
	}

	public void setNreserve10(UFDouble nreserve10) {
		this.nreserve10 = nreserve10;
	}

	public UFDouble getNreserve11() {
		return nreserve11;
	}

	public void setNreserve11(UFDouble nreserve11) {
		this.nreserve11 = nreserve11;
	}

	public String getVdef10() {
		return vdef10;
	}

	public void setVdef10(String vdef10) {
		this.vdef10 = vdef10;
	}

	public UFDouble getNreserve1() {
		return nreserve1;
	}

	public void setNreserve1(UFDouble nreserve1) {
		this.nreserve1 = nreserve1;
	}

	public UFDouble getNreserve2() {
		return nreserve2;
	}

	public void setNreserve2(UFDouble nreserve2) {
		this.nreserve2 = nreserve2;
	}

	public String getPk_defdoc5() {
		return pk_defdoc5;
	}

	public void setPk_defdoc5(String pk_defdoc5) {
		this.pk_defdoc5 = pk_defdoc5;
	}

	public String getVreserve1() {
		return vreserve1;
	}

	public void setVreserve1(String vreserve1) {
		this.vreserve1 = vreserve1;
	}

	public String getVreserve2() {
		return vreserve2;
	}

	public void setVreserve2(String vreserve2) {
		this.vreserve2 = vreserve2;
	}

	public String getVreserve3() {
		return vreserve3;
	}

	public void setVreserve3(String vreserve3) {
		this.vreserve3 = vreserve3;
	}

	public String getPk_jobbasfil() {
		return pk_jobbasfil;
	}

	public void setPk_jobbasfil(String pk_jobbasfil) {
		this.pk_jobbasfil = pk_jobbasfil;
	}

	public String getPk_cubasdoc() {
		return pk_cubasdoc;
	}

	public void setPk_cubasdoc(String pk_cubasdoc) {
		this.pk_cubasdoc = pk_cubasdoc;
	}

	public String getPk_costaccount() {
		return pk_costaccount;
	}

	public void setPk_costaccount(String pk_costaccount) {
		this.pk_costaccount = pk_costaccount;
	}

	public UFDouble getWorknum() {
		return worknum;
	}

	public void setWorknum(UFDouble worknum) {
		this.worknum = worknum;
	}

	public String getPk_project() {
		return pk_project;
	}

	public void setPk_project(String pk_project) {
		this.pk_project = pk_project;
	}

	public UFDouble getNmy() {
		return nmy;
	}

	public void setNmy(UFDouble nmy) {
		this.nmy = nmy;
	}

	public String getPk_costaccount_b() {
		return pk_costaccount_b;
	}

	public void setPk_costaccount_b(String pk_costaccount_b) {
		this.pk_costaccount_b = pk_costaccount_b;
	}

	public String getPk_costelement() {
		return pk_costelement;
	}

	public void setPk_costelement(String pk_costelement) {
		this.pk_costelement = pk_costelement;
	}

	public String getPk_cumandoc() {
		return pk_cumandoc;
	}

	public void setPk_cumandoc(String pk_cumandoc) {
		this.pk_cumandoc = pk_cumandoc;
	}

	public String getPk_invmandoc() {
		return pk_invmandoc;
	}

	public void setPk_invmandoc(String pk_invmandoc) {
		this.pk_invmandoc = pk_invmandoc;
	}

	public String getPk_invbasdoc() {
		return pk_invbasdoc;
	}

	public void setPk_invbasdoc(String pk_invbasdoc) {
		this.pk_invbasdoc = pk_invbasdoc;
	}

	public java.lang.String getParentPKFieldName() {
		return "pk_costaccount"; 
	}   
    
	public java.lang.String getPKFieldName() {
	  return "pk_costaccount_b";
	}
    
	public java.lang.String getTableName() {
		return "xccb_costaccount_b";
	}    
	
	@Override
	public String getEntityName() {
		
		return "xccb_costaccount_b";
	}

       public CostAccoutBVO() {
		super();	
	}    
} 
