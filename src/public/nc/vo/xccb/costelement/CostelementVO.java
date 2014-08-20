
package nc.vo.xccb.costelement;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.zmpub.pub.bill.BaseVO;
/**
 * 成要素定义-主表
 * @author mlr
 * @version NCPrj 1.0
 */
@SuppressWarnings("serial")
public class CostelementVO extends BaseVO {
	private String pk_accoutbook;//会计账簿
	private Integer datasource;//数据来源
	private String costcode;//成本要素编码
	private String costname;//成本要素名称
	private String pk_costelement;//主键
	private String reserve1;//上级要素主键
	private UFBoolean reserve14;//是否末级	

	public UFBoolean getReserve14() {
		return reserve14;
	}

	public void setReserve14(UFBoolean reserve14) {
		this.reserve14 = reserve14;
	}

	public String getReserve1() {
		return reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	public String getPk_accoutbook() {
		return pk_accoutbook;
	}

	public void setPk_accoutbook(String pk_accoutbook) {
		this.pk_accoutbook = pk_accoutbook;
	}

	public Integer getDatasource() {
		return datasource;
	}

	public void setDatasource(Integer datasource) {
		this.datasource = datasource;
	}

	public String getCostcode() {
		return costcode;
	}

	public void setCostcode(String costcode) {
		this.costcode = costcode;
	}

	public String getPk_costelement() {
		return pk_costelement;
	}

	public void setPk_costelement(String pk_costelement) {
		this.pk_costelement = pk_costelement;
	}

	public String getCostname() {
		return costname;
	}

	public void setCostname(String costname) {
		this.costname = costname;
	}

	public java.lang.String getParentPKFieldName() {
	    return "reserve1";
	}   
    

	public java.lang.String getPKFieldName() {
	  return "pk_costelement";
	}
    
	public java.lang.String getTableName() {
		return "xccb_costelement";
	}    

	@Override
	public String getEntityName() {
		
		return "xccb_costelement";
	}

     public CostelementVO() {
		super();	
	}    
} 
