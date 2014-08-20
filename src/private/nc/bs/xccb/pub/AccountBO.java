package nc.bs.xccb.pub;
import nc.bs.dao.BaseDAO;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.xew.pub.XewPubTool;
/**
 * 核算单后台公共处理类
 * @author mlr
 */
public class AccountBO {
	private XewPubTool tool=null;
	public XewPubTool getTool(){
		if(tool==null){
			tool=new XewPubTool();
		}
		return tool;
	}
	private BaseDAO dao=null;
	public BaseDAO getDao() {
	if (dao == null) {
		dao = new BaseDAO();
	}
	return dao;
}
	/**
	 * 成本核算单--->成本核算单(传固定资产)和成本核算单(传会计平台)
	 * @param pvo
	 * @throws Exception
	 */
	public void sendMessage(PfParameterVO pvo)throws Exception{
//		if(pvo==null)
//			return;
//		AggCostAccountVO billvo=(AggCostAccountVO) ObjectUtils.serializableClone(pvo.m_preValueVo);
//		if(billvo==null)
//			return;
//		CostAccountVO headvo=(CostAccountVO) billvo.getParentVO();
//		CostAccoutBVO[] bodyvos=(CostAccoutBVO[]) billvo.getChildrenVO();
//		if(bodyvos==null|| bodyvos.length==0)
//			return;
//		String pk_accoutbook=PuPubVO.getString_TrimZeroLenAsNull(headvo.getPk_accoutbook());
//		String pk_corp=PuPubVO.getString_TrimZeroLenAsNull(headvo.getPk_corp());
//		//获取 接口
//		List<DataFilteringVO> dflist=(List<DataFilteringVO>) getTool().getDao().retrieveByClause(DataFilteringVO.class,
//				" pk_accoutbook='"+pk_accoutbook+"' " +
//				" and  pk_corp='"+pk_corp+"' and isnull(dr,0)=0 ");
//		if(dflist==null || dflist.size()==0)
//			throw new Exception(" [与固定资产和总账的接口（数据过滤）] 节点没有进行定义 ");
//		DataFilteringVO[][] dfvoss=(DataFilteringVO[][]) SplitBillVOs.getSplitVOs(dflist.toArray(new DataFilteringVO[0]), new String[]{"isfixed","isaccounting"});
//		DataFilteringVO[] accountvos=null;//会计平台接口
//		DataFilteringVO[] fixedvos=null;//固定资产接口
////		for(int i=0;i<dfvoss.length;i++){
////			DataFilteringVO[] dfvos=dfvoss[i];
////			if(dfvos==null || dfvos.length==0)
////				continue;			
////			if(PuPubVO.getUFBoolean_NullAs(dfvos[0].getIsaccounting(), new UFBoolean(false)).booleanValue()){
////			  //传会计平台	
////				accountvos=	dfvos;						
////			}else{
////			  //传固定资产				
////				fixedvos=dfvos;								
////			}		
////		}
//		fixedvos=dflist.toArray(new DataFilteringVO[0]);
//		//取出传固定资产和会计平台的数据
//		//存放传固定资产的数据
//		List<CostAccoutBVO> accoutlist=new ArrayList<CostAccoutBVO>();
//		//存放传会计平台的数据
//		List<CostAccoutBVO> fixedlist=new ArrayList<CostAccoutBVO>();
//        for(int i=0;i<bodyvos.length;i++){
//        	CostAccoutBVO bvo=bodyvos[i];
//        	String pk_project=bvo.getPk_project();
//        	String pk_basid=bvo.getPk_jobbasfil();
//        	String pk_costelement=bvo.getPk_costelement();
//        	String pk_ser=bvo.getPk_defdoc1();
//        	if(isSendAccount(pk_basid,pk_costelement,accountvos)){
//        		//accoutlist.add(bvo);
//        	}else if(isSendFixed(pk_basid,pk_costelement,fixedvos,pk_ser)){
//        		fixedlist.add(bvo);
//        	}else{
////        		String procode=getTool().getProCodeByPk(pk_basid);
////        		String proname=getTool().getProNameByPk(pk_basid);
////        		String costcode=getTool().getCostCodeByPk(pk_costelement);
////        		String costname=getTool().getCostNameByPk(pk_costelement);
////        		throw new Exception(" [与固定资产和总账的接口（数据过滤）] 节点 工程编码为:["+procode+"] ,工程名字为：["+proname+"]没有进行定义 "+
////        				" [与固定资产和总账的接口（数据过滤）] 节点 成本要素编码为为:["+costcode+"] ,成本要素名字为：["+costname+"]没有进行定义 ");
//        	}    	
//        }
//        //构造传会计平台的数据
//        if(accoutlist!=null && accoutlist.size()>0){        	
//        	AggCostAccountVO[] billvos=	getTool().getAccountBillVOS(accoutlist);
//        	getTool().setBillInfors(billvos, Xewpubconst.bill_code_costaccount1, pvo,true);
//        	getTool().saveBill(billvos, Xewpubconst.bill_code_costaccount1, pvo.m_currentDate);
//        }
//        //构造固定资产的数据
//        if(fixedlist!=null && fixedlist.size()>0){
//        	AggCostAccountVO[] billvos=	getTool().getAccountBillVOS(fixedlist);
//        	getTool().setBillInfors(billvos, Xewpubconst.bill_code_costaccount2, pvo,true);
//        	getTool().saveBill(billvos, Xewpubconst.bill_code_costaccount2, pvo.m_currentDate);
//        }		
	}
//	/**
//	 * 是否发送固定资产
//	 * @param pk_basid
//	 * @param pk_costelement 
//	 * @param fixedvos
//	 * @return
//	 * @throws BusinessException 
//	 */
//	public boolean isSendFixed(String pk_basid, String pk_costelement, DataFilteringVO[] fixedvos,String pk_ser) throws BusinessException {
//		if(fixedvos==null || fixedvos.length==0)
//			return false;
//		for(int i=0;i<fixedvos.length;i++){
//			DataFilteringVO dvo=fixedvos[i];
//			String pk_procl=dvo.getPk_workscategory();
//			String pk_coste=dvo.getPk_costelement();
//			String pk_ser1=dvo.getReserve5();
//			if(getTool().isProContain(pk_procl, pk_basid)&&getTool().isCostElementContain(pk_coste, pk_costelement)){
//				if(pk_ser1==null || pk_ser1.equals(pk_ser)){
//				  return true;
//				}
//			}
//		}
//		return false;
//	}


//	/**
//	 * 是否发送会计平台
//	 * @param pk_basid
//	 * @param pk_costelement 
//	 * @param accountvos
//	 * @return
//	 * @throws BusinessException 
//	 */
//	public boolean isSendAccount(String pk_basid, String pk_costelement, DataFilteringVO[] accountvos) throws BusinessException {
//		if(accountvos==null || accountvos.length==0)
//			return false; 
//		for(int i=0;i<accountvos.length;i++){
//			DataFilteringVO dvo=accountvos[i];
//			String pk_procl=dvo.getPk_workscategory();
//			String pk_coste=dvo.getPk_costelement();
//			if(getTool().isProContain(pk_procl, pk_basid)&&getTool().isCostElementContain(pk_coste, pk_costelement)){
//				return true;
//			}
//		}
//		return false;
//	}
//	/**
//	 * 成本核算单--->成本核算单(传固定资产)和成本核算单(传会计平台) 删除下游
//	 * @param pvo
//	 * @throws Exception
//	 */
//	public void  sendMessage_del(PfParameterVO pvo)throws Exception{
//		AggCostAccountVO billvo=(AggCostAccountVO) ObjectUtils.serializableClone(pvo.m_preValueVo);
//		if(billvo==null)
//			return;
//		CostAccountVO headvo=(CostAccountVO) billvo.getParentVO();
//		String pk_h=headvo.getPrimaryKey();
////		AggCostAccountVO[] nbillvos1=getTool().getAccountBillVO(Xewpubconst.bill_code_costaccount1,pk_h);
////		getTool().deleteBillVO(Xewpubconst.bill_code_costaccount1, pvo.m_currentDate, nbillvos1);
//		AggCostAccountVO[] nbillvos2=getTool().getAccountBillVO(Xewpubconst.bill_code_costaccount2,pk_h);
//		getTool().deleteBillVO(Xewpubconst.bill_code_costaccount2, pvo.m_currentDate, nbillvos2);
//	}	

}
