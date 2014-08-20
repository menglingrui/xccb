
package nc.vo.xccb.costaccount;	
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.zmpub.pub.bill.HYHeadSuperVO;
/**
 * �ɱ������ͷvo
 * @author mlr
 * @version NCPrj 1.0
 */
@SuppressWarnings("serial")
public class CostAccountVO extends HYHeadSuperVO {
	
//	/**
//	 * �ɱ�����vo �������Ķ�Ӧ�ֶ�
//	 */
//	public static String[] workcenters={"h.pk_deptdoc","h.pk_minearea","h.vdef1","h.vdef2","h.vdef3","h.vdef4",
//		 "h.vdef5","h.vdef6","h.vdef7","h.vdef8"};
//	/**
//	 * �ɱ�����vo �������Ķ�Ӧ�ֶ�
//	 */
//	public static String[] workcenters1={"pk_deptdoc","pk_minearea","vdef1","vdef2","vdef3","vdef4",
//		 "vdef5","vdef6","vdef7","vdef8"};
	
	/**
	 * �ɱ�����vo �������Ķ�Ӧ�ֶ�
	 */
	public static String[] workcenters={"h.pk_deptdoc","h.vdef1","h.vdef2","h.vdef3","h.vdef4",
		 "h.vdef5","h.vdef6","h.vdef7","h.vdef8"};
	/**
	 * �ɱ�����vo �������Ķ�Ӧ�ֶ�
	 */
	public static String[] workcenters1={"pk_deptdoc","vdef1","vdef2","vdef3","vdef4",
		 "vdef5","vdef6","vdef7","vdef8"};
	private String pk_corp;//��˾
	private String pk_costaccount;//����
	private String pk_accoutbook;//�˲�
	private String pk_deptdoc;//����
	private String pk_minearea;//����
	public String vreserve5;//�ɱ����㵥���̶��ʲ���  ��������ʲ�����������
	public UFBoolean ureserve3;//�Ƿ��Ѿ����̶��ʲ�
	public String vdef9;//�Ͽ�
	
	
	
	public String getVdef9() {
		return vdef9;
	}

	public void setVdef9(String vdef9) {
		this.vdef9 = vdef9;
	}

	public UFBoolean getUreserve3() {
		return ureserve3;
	}

	public void setUreserve3(UFBoolean ureserve3) {
		this.ureserve3 = ureserve3;
	}

	public String getVreserve5() {
		return vreserve5;
	}

	public void setVreserve5(String vreserve5) {
		this.vreserve5 = vreserve5;
	}

	public UFDateTime ts;	
	
	public UFDateTime getTs() {
		return ts;
	}

	public void setTs(UFDateTime ts) {
		this.ts = ts;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public String getPk_costaccount() {
		return pk_costaccount;
	}

	public void setPk_costaccount(String pk_costaccount) {
		this.pk_costaccount = pk_costaccount;
	}

	public String getPk_accoutbook() {
		return pk_accoutbook;
	}

	public void setPk_accoutbook(String pk_accoutbook) {
		this.pk_accoutbook = pk_accoutbook;
	}

	public String getPk_deptdoc() {
		return pk_deptdoc;
	}

	public void setPk_deptdoc(String pk_deptdoc) {
		this.pk_deptdoc = pk_deptdoc;
	}

	public String getPk_minearea() {
		return pk_minearea;
	}

	public void setPk_minearea(String pk_minearea) {
		this.pk_minearea = pk_minearea;
	}

	public java.lang.String getParentPKFieldName() {
	    return null;
	}   

	public java.lang.String getPKFieldName() {
	  return "pk_costaccount";
	}
    

	public java.lang.String getTableName() {
		return "xccb_costaccount";
	}   
	
	@Override
	public String getEntityName() {
		
		return "xccb_costaccount";
	}

     public CostAccountVO() {
		super();	
	}




 
} 
