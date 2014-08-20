package nc.ui.xccb.costelement;
import nc.ui.pub.ClientEnvironment;
import nc.ui.trade.bsdelegate.BDBusinessDelegator;
import nc.ui.trade.bsdelegate.BusinessDelegator;
import nc.ui.trade.pub.IVOTreeDataByID;
import nc.vo.pub.SuperVO;
import nc.vo.xccb.costelement.CostelementVO;
/**
 * 构建树形数据-依据id构建树
 * @author mlr
 *
 */
public class TreeManagerData implements IVOTreeDataByID{


	public String getShowFieldName() {
		return "costcode,costname";
	}
	
	public SuperVO[] getTreeVO() {
		SuperVO[] treeVos=null;
		BusinessDelegator busi=new BDBusinessDelegator();
		try {
			treeVos=busi.queryByCondition(CostelementVO.class, " isnull(dr,0)=0 and pk_corp='"+ClientEnvironment.getInstance().getCorporation().getPrimaryKey()+"'");
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return treeVos;
	}
	public SuperVO[] getTreeVOByAccoutbookid(String id) {
		SuperVO[] treeVos=null;
		BusinessDelegator busi=new BDBusinessDelegator();
		try {
			treeVos=busi.queryByCondition(CostelementVO.class, " " +
					" isnull(dr,0)=0 and pk_corp='"+ClientEnvironment.getInstance().getCorporation().getPrimaryKey()+"'" +
					" and pk_accoutbook='"+id+"' order by costcode");
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return treeVos;
	}

	public String getIDFieldName() {
		return "pk_costelement";
	}

	public String getParentIDFieldName() {
		return "reserve1";
	}

}
