
package nc.vo.xccb.costelement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.trade.pub.IExAggVO;
import nc.vo.trade.pub.HYBillVO;
/**
 * �ɱ�Ҫ�ض���-�ۺ�vo
 * ��������:2012-12-11 09:18:58
 * @author mlr
 * @version NCPrj 1.0
 */
@SuppressWarnings("serial")
@nc.vo.annotation.AggVoInfo(parentVO = "nc.vo.xccb.costelement.CostelementVO")
public class ExAggCostelemetTVO extends HYBillVO implements IExAggVO{
    	
	//����װ�ض��ӱ����ݵ�HashMap
	private HashMap hmChildVOs = new HashMap();
		
	/**
	 * ���ض���ӱ�ı���
	 * �����뵥��ģ���ҳǩ�����Ӧ
	 * �������ڣ�2012-12-11 09:18:58
	 * @return String[]
	 */
	public String[] getTableCodes(){
		          
		return new String[]{	 		 		   		
		   		"xew_costelement_b",
				"xew_costelement_b1"
		   		    };
		          
	}

	/**
	 * ���ض���ӱ����������
	 * �������ڣ�2012-12-11 09:18:58
	 * @return String[]
	 */
	public String[] getTableNames(){
		
		return new String[]{
                "xew_costelement_b",
                "xew_costelement_b1",
                         };
	}
		
	/**
	 * ȡ�������ӱ������VO����
	 * �������ڣ�2012-12-11 09:18:58
	 * @return CircularlyAccessibleValueObject[]
	 */
	public CircularlyAccessibleValueObject[] getAllChildrenVO(){
		
		ArrayList al = new ArrayList();
		for(int i = 0; i < getTableCodes().length; i++){
			CircularlyAccessibleValueObject[] cvos
			        = getTableVO(getTableCodes()[i]);
			if(cvos != null)
				al.addAll(Arrays.asList(cvos));
		}
		
		return (SuperVO[]) al.toArray(new SuperVO[0]);
	}
		
	/**
	 * ����ÿ���ӱ��VO����
	 * �������ڣ�2012-12-11 09:18:58
	 * @return CircularlyAccessibleValueObject[]
	 */
	public CircularlyAccessibleValueObject[] getTableVO(String tableCode){
		
		return (CircularlyAccessibleValueObject[])
		            hmChildVOs.get(tableCode);
	}
	
	/**
	 * 
	 * �������ڣ�2012-12-11 09:18:58
	 * @param SuperVO item
	 * @param String id
	 */
	public void setParentId(SuperVO item,String id){}
	
	/**
	 * Ϊ�ض��ӱ�����VO����
	 * �������ڣ�2012-12-11 09:18:58
	 * @param String tableCode
	 * @para CircularlyAccessibleValueObject[] vos
	 */
	public void setTableVO(String tableCode,CircularlyAccessibleValueObject[] vos){
		
		hmChildVOs.put(tableCode,vos);
	}
	
	/**
	 * ȱʡ��ҳǩ����
	 * �������ڣ�2012-12-11 09:18:58
	 * @return String 
	 */
	public String getDefaultTableCode(){
		
		return getTableCodes()[0];
	}
	
	/**
	 * 
	 * �������ڣ�2012-12-11 09:18:58
	 * @param String tableCode
	 * @param String parentId
	 * @return SuperVO[]
	 */
	public SuperVO[] getChildVOsByParentId(String tableCode,String parentId){
		
		return null;
	}
		
	/**
	 * 
	 * �������ڣ�2012-12-11 09:18:58
	 * @return HashMap
	 */
	public HashMap getHmEditingVOs() throws Exception{
		
		return null;
	}
	
	/**
	 * 
	 * ��������:2012-12-11 09:18:58
	 * @param SuperVO item
	 * @return String
	 */
	public String getParentId(SuperVO item){
		
		return null;
	}
}
