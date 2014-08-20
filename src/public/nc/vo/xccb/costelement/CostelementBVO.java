
package nc.vo.xccb.costelement;
	
import nc.vo.zmpub.pub.bill.BaseVO;
	
/**
   成本要素定义-数据来源过滤表
 * 创建日期:2012-12-11 09:18:57
 * @author Administrator
 * @version NCPrj 1.0
 */
@SuppressWarnings("serial")
public class CostelementBVO extends BaseVO {
	
	private String pk_costelement;//主表主键
	private String pk_costelement_b;//主键
	private String pk_accountsub;//会计科目
	private String pk_invcl;//材料分类
	private String pk_invmandoc;//材料
	private String pk_invcl1;//作业分类
	private String pk_invmandoc1;//作业
	private String vdef1;//作业类别基本ID
	private String reserve3;//作业基本ID
	private String reserve4;//材料基本ID
	private String reserve5;//材料分类基本ID
	private String vdef10;//行号
	public String getVdef10() {
		return vdef10;
	}

	public void setVdef10(String vdef10) {
		this.vdef10 = vdef10;
	}

	private String reserve1;//成本归集处理 用来临时存放  核算账簿
	private String reserve2;//成本归集处理 用来临时存放  公司
	//private String reserve3;//成本归集处理 用来临时存放  

	
	
	
	public String getPk_accountsub() {
		return pk_accountsub;
	}

	public String getReserve1() {
		return reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	public String getReserve2() {
		return reserve2;
	}

	public void setReserve2(String reserve2) {
		this.reserve2 = reserve2;
	}

	public void setPk_accountsub(String pk_accountsub) {
		this.pk_accountsub = pk_accountsub;
	}

	public String getPk_costelement() {
		return pk_costelement;
	}

	public void setPk_costelement(String pk_costelement) {
		this.pk_costelement = pk_costelement;
	}

	public String getPk_invcl1() {
		return pk_invcl1;
	}

	public void setPk_invcl1(String pk_invcl1) {
		this.pk_invcl1 = pk_invcl1;
	}

	public String getPk_costelement_b() {
		return pk_costelement_b;
	}

	public void setPk_costelement_b(String pk_costelement_b) {
		this.pk_costelement_b = pk_costelement_b;
	}

	public String getPk_invcl() {
		return pk_invcl;
	}

	public void setPk_invcl(String pk_invcl) {
		this.pk_invcl = pk_invcl;
	}

	public String getPk_invmandoc() {
		return pk_invmandoc;
	}

	public void setPk_invmandoc(String pk_invmandoc) {
		this.pk_invmandoc = pk_invmandoc;
	}

	public String getPk_invmandoc1() {
		return pk_invmandoc1;
	}

	public void setPk_invmandoc1(String pk_invmandoc1) {
		this.pk_invmandoc1 = pk_invmandoc1;
	}

	public java.lang.String getParentPKFieldName() {
		return "pk_costelement";
	}   
    
	public java.lang.String getPKFieldName() {
	  return "pk_costelement_b";
	}
    
	public java.lang.String getTableName() {
		return "xccb_costelement_b";
	}    
    
     public CostelementBVO() {
		super();	
	}

	public String getVdef1() {
		return vdef1;
	}

	public void setVdef1(String vdef1) {
		this.vdef1 = vdef1;
	}

	public String getReserve3() {
		return reserve3;
	}

	public void setReserve3(String reserve3) {
		this.reserve3 = reserve3;
	}

	public String getReserve4() {
		return reserve4;
	}

	public void setReserve4(String reserve4) {
		this.reserve4 = reserve4;
	}

	public String getReserve5() {
		return reserve5;
	}

	public void setReserve5(String reserve5) {
		this.reserve5 = reserve5;
	}    
} 
