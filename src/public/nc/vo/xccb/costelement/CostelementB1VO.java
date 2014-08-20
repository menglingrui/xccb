
package nc.vo.xccb.costelement;
	
import nc.vo.zmpub.pub.bill.BaseVO;
	
/**
 * 成要素定义-成本分摊表
 * 创建日期:2012-12-11 09:18:57
 * @author mlr
 * @version NCPrj 1.0
 */
@SuppressWarnings("serial")
public class CostelementB1VO extends BaseVO {
	private String costdriver;//成本动因
	private String pk_projectcl;//工程类别
	private String pk_costelement_b1;//主键
	private String pk_costelement;//主表主键
	
	
	
	
	
	public String getCostdriver() {
		return costdriver;
	}


	public void setCostdriver(String costdriver) {
		this.costdriver = costdriver;
	}


	public String getPk_projectcl() {
		return pk_projectcl;
	}


	public void setPk_projectcl(String pk_projectcl) {
		this.pk_projectcl = pk_projectcl;
	}


	public String getPk_costelement_b1() {
		return pk_costelement_b1;
	}


	public void setPk_costelement_b1(String pk_costelement_b1) {
		this.pk_costelement_b1 = pk_costelement_b1;
	}


	public String getPk_costelement() {
		return pk_costelement;
	}


	public void setPk_costelement(String pk_costelement) {
		this.pk_costelement = pk_costelement;
	}


	public java.lang.String getParentPKFieldName() {
		return "pk_costelement";
	}   
    

	public java.lang.String getPKFieldName() {
	  return "pk_costelement_b1";
	}
    

	public java.lang.String getTableName() {
		return "xccb_costelement_b1";
	}    
    

     public CostelementB1VO() {
		super();	
	}    
} 
