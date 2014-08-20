package nc.ui.xccb.costelement;
import java.util.Hashtable;

import nc.vo.pub.SuperVO;
import nc.vo.xccb.costelement.CostelementB1VO;
import nc.vo.xccb.costelement.CostelementBVO;
/**
  *
  *����ҵ��������ȱʡʵ��
  *@author mlr
  *@version tempProject version
  */
public class Delegator extends nc.ui.trade.bsdelegate.BDBusinessDelegator{


	@Override
	public Hashtable loadChildDataAry(String[] tableCodes, String key)
			throws Exception {
		// ��������������ȡ���ӱ������
		CostelementBVO[] b1vos = (CostelementBVO[]) this.queryByCondition(CostelementBVO.class, "pk_costelement='" + key + "' and isnull(dr,0)=0");	
		CostelementB1VO[] b2vos = (CostelementB1VO[]) this.queryByCondition(CostelementB1VO.class, "pk_costelement='" + key + "' and isnull(dr,0)=0");
		// ��ѯ���ݷ�Hashtable������
		Hashtable<String, SuperVO[]> dataHT = new Hashtable<String, SuperVO[]>();
		if (b1vos != null && b1vos.length > 0) {
			dataHT.put(tableCodes[0], b1vos);
		}
		if (b2vos != null && b2vos.length > 0) {
			dataHT.put(tableCodes[1], b2vos);
		}
	    return dataHT;
	}	
}