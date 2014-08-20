package nc.bs.xccb.pub;
import nc.bs.dao.BaseDAO;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.xew.pub.XewPubTool;
/**
 * ���㵥��̨����������
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
	 * �ɱ����㵥--->�ɱ����㵥(���̶��ʲ�)�ͳɱ����㵥(�����ƽ̨)
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
//		//��ȡ �ӿ�
//		List<DataFilteringVO> dflist=(List<DataFilteringVO>) getTool().getDao().retrieveByClause(DataFilteringVO.class,
//				" pk_accoutbook='"+pk_accoutbook+"' " +
//				" and  pk_corp='"+pk_corp+"' and isnull(dr,0)=0 ");
//		if(dflist==null || dflist.size()==0)
//			throw new Exception(" [��̶��ʲ������˵Ľӿڣ����ݹ��ˣ�] �ڵ�û�н��ж��� ");
//		DataFilteringVO[][] dfvoss=(DataFilteringVO[][]) SplitBillVOs.getSplitVOs(dflist.toArray(new DataFilteringVO[0]), new String[]{"isfixed","isaccounting"});
//		DataFilteringVO[] accountvos=null;//���ƽ̨�ӿ�
//		DataFilteringVO[] fixedvos=null;//�̶��ʲ��ӿ�
////		for(int i=0;i<dfvoss.length;i++){
////			DataFilteringVO[] dfvos=dfvoss[i];
////			if(dfvos==null || dfvos.length==0)
////				continue;			
////			if(PuPubVO.getUFBoolean_NullAs(dfvos[0].getIsaccounting(), new UFBoolean(false)).booleanValue()){
////			  //�����ƽ̨	
////				accountvos=	dfvos;						
////			}else{
////			  //���̶��ʲ�				
////				fixedvos=dfvos;								
////			}		
////		}
//		fixedvos=dflist.toArray(new DataFilteringVO[0]);
//		//ȡ�����̶��ʲ��ͻ��ƽ̨������
//		//��Ŵ��̶��ʲ�������
//		List<CostAccoutBVO> accoutlist=new ArrayList<CostAccoutBVO>();
//		//��Ŵ����ƽ̨������
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
////        		throw new Exception(" [��̶��ʲ������˵Ľӿڣ����ݹ��ˣ�] �ڵ� ���̱���Ϊ:["+procode+"] ,��������Ϊ��["+proname+"]û�н��ж��� "+
////        				" [��̶��ʲ������˵Ľӿڣ����ݹ��ˣ�] �ڵ� �ɱ�Ҫ�ر���ΪΪ:["+costcode+"] ,�ɱ�Ҫ������Ϊ��["+costname+"]û�н��ж��� ");
//        	}    	
//        }
//        //���촫���ƽ̨������
//        if(accoutlist!=null && accoutlist.size()>0){        	
//        	AggCostAccountVO[] billvos=	getTool().getAccountBillVOS(accoutlist);
//        	getTool().setBillInfors(billvos, Xewpubconst.bill_code_costaccount1, pvo,true);
//        	getTool().saveBill(billvos, Xewpubconst.bill_code_costaccount1, pvo.m_currentDate);
//        }
//        //����̶��ʲ�������
//        if(fixedlist!=null && fixedlist.size()>0){
//        	AggCostAccountVO[] billvos=	getTool().getAccountBillVOS(fixedlist);
//        	getTool().setBillInfors(billvos, Xewpubconst.bill_code_costaccount2, pvo,true);
//        	getTool().saveBill(billvos, Xewpubconst.bill_code_costaccount2, pvo.m_currentDate);
//        }		
	}
//	/**
//	 * �Ƿ��͹̶��ʲ�
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
//	 * �Ƿ��ͻ��ƽ̨
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
//	 * �ɱ����㵥--->�ɱ����㵥(���̶��ʲ�)�ͳɱ����㵥(�����ƽ̨) ɾ������
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
