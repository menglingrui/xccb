package nc.bs.xccb.costelement;
import java.util.ArrayList;
import java.util.List;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.trade.business.IBDBusiCheck;
import nc.bs.zmpub.pub.check.BsUniqueCheck;
import nc.bs.zmpub.pub.tool.ZMReferenceCheck;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.ui.scm.util.ObjectUtils;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.trade.pub.IBDACTION;
import nc.vo.xccb.costelement.CostelementB1VO;
import nc.vo.xccb.costelement.CostelementBVO;
import nc.vo.xccb.costelement.CostelementVO;
import nc.vo.xccb.costelement.ExAggCostelemetTVO;
import nc.vo.xccb.pub.XewcbPubTool;
import nc.vo.xccb.pub.Xewcbpubconst;
public class CostElementBO implements IBDBusiCheck {
	XewcbPubTool tool=null;
	public XewcbPubTool getTool(){
		if(tool==null){
			tool=new XewcbPubTool();
		}
		return tool;
	}
	public void check(int intBdAction, AggregatedValueObject vo, Object userObj)
			throws Exception {
//  mlr  ����ǰУ��  ���벻���ظ�   ���Ʋ����ظ�  ����Ϊ��    �������   ##--##--##  �������һ����У��
		if(vo == null || vo.getParentVO() == null)
			return;
		if(intBdAction == IBDACTION.SAVE){
			CostelementVO head = (CostelementVO)vo.getParentVO();
			head.validate();
//			У������Ƿ�͸��ౣ��һ����  ����λ��  ����Ϊ  ��λ  ��λ
			String code = head.getCostcode();
			String fathercode = getInvclCodeByKey(head.getReserve1(), head.getPk_corp());
			if(fathercode == null){
				fathercode = "root";
			}
			//У��������Ϊ��
			if (PuPubVO.getString_TrimZeroLenAsNull(head.getCostcode()) == null)
				throw new BusinessException("����Ϊ��");

			if (PuPubVO.getString_TrimZeroLenAsNull(head.getCostname()) == null)
				throw new BusinessException("����Ϊ��");
			if (PuPubVO.getString_TrimZeroLenAsNull(head.getDatasource()) == null)
				throw new BusinessException("������ԴΪ��");
			//У�鹫˾������벻�����ظ�
			BsUniqueCheck.FieldUniqueChecks(head, new String[]{"costcode"}, " and pk_corp='"+head.getPk_corp()+"'" 
					, "���벻�����ظ�");
			//" and pk_accoutbook='"+head.getPk_accoutbook()+"'"
			//У��������
			checkCode(code, fathercode, head.getPrimaryKey(),head.getPk_corp());
			updateSubFlag(head.getReserve1());
			//���ݹ鼯�ͳɱ��������У��
			checkData(vo);	
		}else if(intBdAction == IBDACTION.DELETE){
//			У����������¼��ڵ� ����ɾ��  �� ���� ���㵥Ԫ ��Ŀ�Ѿ����� ������ɾ��
			CostelementVO head = (CostelementVO)vo.getParentVO();
			String sql = "select count(0) from xccb_costelement where isnull(dr,0) = 0 and pk_corp = '"+head.getPk_corp()+"' and reserve1 = '"+head.getPrimaryKey()+"'";
			if(PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor()), 0)>0){
				throw new BusinessException("�����¼��ڵ�");
			}	
			//��ѯ�ϼ��ڵ�  �Ƿ񻹴����¼�
			String sql1 = "select count(0) from xccb_costelement where isnull(dr,0) = 0 and pk_corp = '"+head.getPk_corp()+
			"' and reserve1 = '"+head.getReserve1()+"'and  pk_costelement <> '"+head.getPrimaryKey()+"'";
			if(PuPubVO.getInteger_NullAs(getDao().executeQuery(sql1, new ColumnProcessor()), 0)<=0){
				String sql2=" update  xccb_costelement set xccb_costelement.reserve14='Y' where pk_costelement = '"+head.getReserve1()+"'";
				getDao().executeUpdate(sql2);
			}
			String pk=head.getPk_costelement();
			String sql2=" select count(0) from xew_costaccount_b h where isnull(dr,0)=0 and h.pk_costelement='"+pk+"'";
			if(PuPubVO.getInteger_NullAs(getDao().executeQuery(sql2, new ColumnProcessor()), 0)>0){
				throw new BusinessException("�ɱ�Ҫ���Ѿ�������");
			}
			
			boolean isref=ZMReferenceCheck.isReferenced("xccb_costelement", head.getPk_costelement());
			if(isref){
				throw new BusinessException("�ɱ�Ҫ���Ѿ���ҵ�񵥾�����");
			}
		}
	}
	/**
	 * ��ͬһ���˲���
	 * У�����ݹ���ҳǩ---������ҵ���ϲ��ܽ��� 
	 * У��ɱ�����ҳǩ---��������ܽ���
	 * @param vo
	 * @throws Exception 
	 */
	public void checkData(AggregatedValueObject vo) throws Exception {
		//ǰ̨�༭�Կ���
		//������ԴΪ���﹤��: ���ݹ���ҳǩֻ�� ��ҵ�����ҵ��������� �����Ͽ��Ա༭
		//������ԴΪ����:ֻ�л�ƿ�Ŀ���Ա༭
		//�ɱ�����ҳǩ��ǰ̨������		
		//У��ui����
		checkDataUI(vo);
		//У�����ݿ�����
		checkDataCK(vo);
	}
	/**
	 * У�����ݿ��
	 * @param vo
	 * @throws Exception 
	 */
	public void checkDataCK(AggregatedValueObject vo) throws BusinessException {
		ExAggCostelemetTVO billvo=(ExAggCostelemetTVO) vo;
		//ȡ�ñ�ͷ����
		CostelementVO   headvo=(CostelementVO) billvo.getParentVO();
		//ȡ�����ݹ���
		CostelementBVO[] bvos1=(CostelementBVO[]) billvo.getTableVO(billvo.getTableCodes()[0]);
		if(bvos1==null)
			return;
		//ȡ�óɱ�����
		CostelementB1VO[] bvos2=(CostelementB1VO[]) billvo.getTableVO(billvo.getTableCodes()[1]);
		
		String pk_corp=headvo.getPk_corp();
		String pk_accountbook=headvo.getPk_accoutbook();
		
		List list=(List) getTool().queryCostElements(pk_accountbook, pk_corp);
		if(list==null || list.size()==0)
			return;
		CostelementBVO[]  nvos=(CostelementBVO[]) list.toArray(new CostelementBVO[0]);
		
		//nvos��bvos1������޸ı��� ��ôһ���������ͬ������ ���Ա����nvos�й��˵���ͬ������
		List<CostelementBVO> nlist=new ArrayList<CostelementBVO>();
		for(int i=0;i<nvos.length;i++){
			CostelementBVO xbvo=nvos[i];
			boolean isEqual=false;
			for(int j=0;j<bvos1.length;j++){
				CostelementBVO xbvo1=bvos1[j];
				if(xbvo.getPrimaryKey().equals(xbvo1.getPrimaryKey())){
					isEqual=true;
					continue;
				}
			}
			if(isEqual==false){
				nlist.add(xbvo);
			}
		}	
		checkFilerJX(bvos1, nlist.toArray(new CostelementBVO[0]),false);
		checkFilerZZ(bvos1, nlist.toArray(new CostelementBVO[0]),false);		
	}
	/**
	 * У��ui��
	 * @param vo
	 * @throws Exception 
	 */
	public void checkDataUI(AggregatedValueObject vo) throws Exception {
		if(vo==null)
			return;
		ExAggCostelemetTVO billvo=(ExAggCostelemetTVO) vo;
		//ȡ�ñ�ͷ����
		CostelementVO   headvo=(CostelementVO) billvo.getParentVO();
		//ȡ�����ݹ���
		CostelementBVO[] bvos1=(CostelementBVO[]) billvo.getTableVO(billvo.getTableCodes()[0]);
		//ȡ�óɱ�����
		CostelementB1VO[] bvos2=(CostelementB1VO[]) billvo.getTableVO(billvo.getTableCodes()[1]);
		Integer datasource=PuPubVO.getInteger_NullAs(headvo.getDatasource(), -1);
		if(datasource==Xewcbpubconst.data_source_jx){
			checkFilerJX(bvos1,bvos1,true);
		}else if(datasource==Xewcbpubconst.data_source_zz){
			checkFilerZZ(bvos1,bvos1,true);
		}
	}
	/**
	 * �������˵����ݹ���У��
	 * @param bvos1
	 * @param isEqual �Ƿ���Դ��Ŀ��У��������ͬ
	 * @throws Exception 
	 */
	public void checkFilerZZ(CostelementBVO[] bvos1,CostelementBVO[] destvos,boolean isEqual) throws BusinessException {
		   if(bvos1==null || bvos1.length==0)
			   return;
		   CostelementBVO[] nvos=spilt(bvos1);
		   CostelementBVO[] nvos1=destvos;
		   if(isEqual==true){
			   nvos1=spilt(destvos);
		   }
		   for(int i=0;i<nvos.length;i++){
			   CostelementBVO bvo=nvos[i];
			   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
			   //��ƿ�Ŀ
			   String pk_accsubj=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_accountsub());
			   if(pk_accsubj!=null){
				   for(int j=0;j<nvos1.length;j++){	
					   if(isEqual==true){
						   if(i==j){
							   continue;
						   }
					   }
					   CostelementBVO vo=nvos1[j];
					   String pk_costelement=vo.getPk_costelement();
					   
					   //��ҵ��� 
					   String pk_accsubj1=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_accountsub());
					   String number=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
					 
					   if(pk_accsubj1!=null){
						   if(pk_accsubj1.equals(pk_accsubj)){
							       String costcode=getTool().getCostCodeByPk(pk_costelement);
							       String costname=getTool().getCostNameByPk(pk_costelement);						       
								   throw new BusinessException("��ƿ�Ŀ ����Ϊ��["+costcode+"],����Ϊ ["+costname+"] ���ݹ���ҳǩ �к�Ϊ:["+number+"] �ı����к��к�Ϊ��["+nnumber+"]�ı�����" +
							   		" ��ƿ�Ŀ�ظ�");
						   }
						   if(getTool().isAccsubCross(pk_accsubj,pk_accsubj1)){
							   String costcode=getTool().getCostCodeByPk(pk_costelement);
						       String costname=getTool().getCostNameByPk(pk_costelement);
							   throw new BusinessException("��ƿ�Ŀ ����Ϊ��["+costcode+"],����Ϊ ["+costname+"] ���ݹ���ҳǩ �к�Ϊ:["+number+"] �ı����ж���Ļ�ƿ�Ŀ �������к�Ϊ��["+nnumber+"]�ı�����" +
						   		" �ж���Ļ�ƿ�Ŀ���ڽ���");
		
						   }
					   }				   
				   }			 
			   }
		   }
	}
	

	/**
	 * 	���Ծ��﹤�̵����ݹ��� У��
	 * @param bvos1
	 * @throws Exception 
	 */
	private void checkFilerJX(CostelementBVO[] bvos1,CostelementBVO[] destvos,boolean isEqual) throws BusinessException {
	   if(bvos1==null || bvos1.length==0)
		   return;
	   CostelementBVO[] nvos=spilt(bvos1);
	   CostelementBVO[] nvos1=destvos;
	   if(isEqual==true){
		   nvos1=spilt(destvos);
	   }
	   for(int i=0;i<nvos.length;i++){
		   CostelementBVO bvo=nvos[i];
		   
		   //��ҵ���
		   String pk_invcl1=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_invcl1());
		   //��ҵ
		   String pk_invmandoc1=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_invmandoc1());
		   //�������
		   String pk_invcl=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_invcl());
		   //����
		   String pk_invmandoc=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_invmandoc());
		   if(pk_invmandoc1!=null){
			   for(int j=0;j<nvos1.length;j++){
				   if(isEqual==true){
					   if(i==j){
						   continue;
					   }
				   }
				   CostelementBVO vo=nvos1[j];
				   //��ҵ���
				   String pk_invcl11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invcl1());
				   //��ҵ
				   String pk_invmandoc11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invmandoc1());
				   String number=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
				   
				   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
				   String pk_costelement=vo.getPk_costelement();
				   String costcode=getTool().getCostCodeByPk(pk_costelement);
				   String costname=getTool().getCostNameByPk(pk_costelement);
				   
				   
				   if(pk_invmandoc11!=null){
					   if(pk_invmandoc11.equals(pk_invmandoc1)){
							   throw new BusinessException("���ݹ���ҳǩ  �ɱ�Ҫ��Ϊ:["+costcode+"],["+costname+"]" +
							   		" �к�Ϊ:["+number+"] �ı����к��к�Ϊ��["+nnumber+"]�ı�����" +
						   		" ��ҵ�ظ�");
					   }
				   }else if(pk_invcl11!=null){
					   if(getTool().isInvmanContain(pk_invcl11,pk_invmandoc1)){
						   throw new BusinessException("���ݹ���ҳǩ �ɱ�Ҫ��Ϊ:["+costcode+"],["+costname+"]" +
						   		" �к�Ϊ :["+number+"] �ı����ж������ҵ �����к�Ϊ��["+nnumber+"]�ı�����" +
							   		" �ж������ҵ������Ѿ�����");
					   }
				   }
				   
			   }
		   }else if(pk_invcl1!=null){
			   for(int j=0;j<nvos1.length;j++){
				   if(isEqual==true){
					   if(i==j){
						   continue;
					   }
				   }
				   CostelementBVO vo=nvos1[j];
				   //��ҵ���
				   String pk_invcl11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invcl1());
				   //��ҵ
				   String pk_invmandoc11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invmandoc1());
				   String number=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
				   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
				   String pk_costelement=vo.getPk_costelement();
				   String costcode=getTool().getCostCodeByPk(pk_costelement);
				   String costname=getTool().getCostNameByPk(pk_costelement);
				   if(pk_invmandoc11!=null){
					   if(getTool().isInvmanContain(pk_invcl1, pk_invmandoc11)){
						   throw new BusinessException("���ݹ���ҳǩ   �ɱ�Ҫ��Ϊ:["+costcode+"],["+costname+"] �к�Ϊ :["+nnumber+"] �ı����ж������ҵ �����к�Ϊ��["+number+"]�ı�����" +
					   		" �ж������ҵ������Ѿ�����");
					   }
				   }else if(pk_invcl11!=null){
					   if(getTool().isInvclCross(pk_invcl1,pk_invcl11)){
						   throw new BusinessException("���ݹ���ҳǩ �к�Ϊ:["+number+"] �ı����ж������ҵ��� ���� �ɱ�Ҫ��Ϊ:["+costcode+"],["+costname+"]" +
						   		"  �к�Ϊ��["+nnumber+"]�ı�����" +
							   		" �ж������ҵ�����ҵ���ڽ���");
					   }
				   }
				   
			   }		   
		   }
		   if(pk_invmandoc!=null){
			   for(int j=0;j<nvos1.length;j++){
				   if(isEqual==true){
					   if(i==j){
						   continue;
					   }
				   }
				   CostelementBVO vo=nvos1[j];
				   //�������
				   String pk_invcl11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invcl());
				   //����
				   String pk_invmandoc11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invmandoc());
				   String number=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
				   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
				   String pk_costelement=vo.getPk_costelement();
				   String costcode=getTool().getCostCodeByPk(pk_costelement);
				   String costname=getTool().getCostNameByPk(pk_costelement);
				   if(pk_invmandoc11!=null){
					   if(pk_invmandoc11.equals(pk_invmandoc)){
							   throw new BusinessException("���ݹ���ҳǩ �ɱ�Ҫ��Ϊ:["+costcode+"],["+costname+"] �к�Ϊ :["+number+"] �ı����к��к�Ϊ��["+nnumber+"]�ı�����" +
						   		" �����ظ�");
					   }
				   }else if(pk_invcl11!=null){
					   if(getTool().isInvmanContain(pk_invcl11,pk_invmandoc)){
						   throw new BusinessException("���ݹ���ҳǩ �ɱ�Ҫ��Ϊ:["+costcode+"],["+costname+"] �к�Ϊ:["+number+"] �ı����ж���Ĳ��� �����к�Ϊ��["+nnumber+"]�ı�����" +
							   		" �ж���Ĳ���������Ѿ�����");
					   }
				   }
				   
			   }
		   }else if(pk_invcl!=null){
			   for(int j=0;j<nvos1.length;j++){
				   if(isEqual==true){
					   if(i==j){
						   continue;
					   }
				   }
				   CostelementBVO vo=nvos1[j];
				   //��ҵ���
				   String pk_invcl11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invcl());
				   //��ҵ
				   String pk_invmandoc11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invmandoc());
				   String number=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
				   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
				   String pk_costelement=vo.getPk_costelement();
				   String costcode=getTool().getCostCodeByPk(pk_costelement);
				   String costname=getTool().getCostNameByPk(pk_costelement);
				   if(pk_invmandoc11!=null){
					   if(getTool().isInvmanContain(pk_invcl, pk_invmandoc11)){
						   throw new BusinessException("���ݹ���ҳǩ �к�Ϊ:["+nnumber+"] �ı����ж���Ĳ��� ����  �ɱ�Ҫ��Ϊ:["+costcode+"],["+costname+"]�к�Ϊ��["+number+"]�ı�����" +
					   		" �ж���Ĳ���������Ѿ�����");
					   }
				   }else if(pk_invcl11!=null){
					   if(getTool().isInvclCross(pk_invcl,pk_invcl11)){
						   throw new BusinessException("���ݹ���ҳǩ  �ɱ�Ҫ��Ϊ:["+costcode+"],["+costname+"]   �к�Ϊ:["+number+"] �ı����ж���Ĳ������ �������к�Ϊ��["+nnumber+"]�ı�����" +
							   		" �ж���Ĳ������,���ϴ��ڽ���");
					   }
				   }
				   
			   }		   
		   }
	   }
	}
	/**
	 * ���˵�ɾ����
	 * @param bvos1
	 * @return
	 * @throws Exception 
	 */
	public CostelementB1VO[] spilt1(CostelementB1VO[] bvos1) throws BusinessException {
		List<CostelementB1VO> list=new ArrayList<CostelementB1VO>();
        for(int i=0;i<bvos1.length;i++){
        	if(bvos1[i].getStatus()==VOStatus.DELETED){
        		
        	}else{
        		list.add(bvos1[i]);
        	}
        }
        List<CostelementB1VO> list1=null;
		try {
			list1 = (List<CostelementB1VO>) ObjectUtils.serializableClone(list);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
			
		}
		return list1.toArray(new CostelementB1VO[0]);
	}

	/**
	 * ���˵�ɾ����
	 * @param bvos1
	 * @return
	 * @throws Exception 
	 */
	public CostelementBVO[] spilt(CostelementBVO[] bvos1) throws BusinessException {
		List<CostelementBVO> list=new ArrayList<CostelementBVO>();
        for(int i=0;i<bvos1.length;i++){
        	if(bvos1[i].getStatus()==VOStatus.DELETED){
        		
        	}else{
        		list.add(bvos1[i]);
        	}
        }
        List<CostelementBVO> list1=null;
		try {
			list1 = (List<CostelementBVO>) ObjectUtils.serializableClone(list);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BusinessException(e.getMessage());
			
		}
		return list1.toArray(new CostelementBVO[0]);
	}
	/**
	 * �����ϼ�����  ���ϼ� �Ƿ�ĩ�� ��־����Ϊfalse
	 * @param reserve1
	 * @throws DAOException 
	 */
	public void updateSubFlag(String reserve1) throws DAOException {
		String sql=" update xccb_costelement set xccb_costelement.reserve14='N' where xccb_costelement.pk_costelement='"+reserve1+"'";
		getDao().executeUpdate(sql);
	}

	private void checkCode(String code,String fathercode,String key,String corp) throws Exception{
		//У���һ������ı���
		if(fathercode.equalsIgnoreCase("root")){
			if(code.length()!=2){
				throw new BusinessException("������벻���Ϲ���XX--XX--XX");
			}
			return;
		}	
		int  leg=code.length()%2;
		//У���Ƿ��� 2�ı���
		if(leg!=0)
			throw new BusinessException("������벻���Ϲ���XX--XX--XX");
		
		if(!code.startsWith(fathercode)){
			throw new BusinessException("�͸�����벻һ��  ���������뿪ͷ�����������");
		}
		if(code.length()-fathercode.length()!=2){
			throw new BusinessException("������벻���Ϲ���XX--XX--XX");
		}
	}	
	private BaseDAO dao = null;
	private BaseDAO getDao(){
		if(dao == null)
			dao = new BaseDAO();
		return dao;
	}
	
	private String getInvclCodeByKey(String key,String logcorp) throws BusinessException{
		if(PuPubVO.getString_TrimZeroLenAsNull(key)==null)
			return null;
		String sql = "select costcode from xccb_costelement where isnull(dr,0) = 0 and pk_corp = '"+logcorp+"' and pk_costelement = '"+key+"'";	
		return PuPubVO.getString_TrimZeroLenAsNull(getDao().executeQuery(sql, new ColumnProcessor()));
	}

	public void dealAfter(int intBdAction, AggregatedValueObject billVo,
			Object userObj) throws Exception {
// �����У��
	}
	/**
	 * ���˲����Ƴɱ�Ҫ��
	 * @throws BusinessException 
	 */
	public void AccountCopy(String soraccountpk,String corp,String desaccountpk) throws BusinessException{
		if(soraccountpk==null || soraccountpk.length()==0)
			throw new BusinessException("��Դ�˲�Ϊ��");
		if(corp==null || corp.length()==0)
			throw new BusinessException("��˾Ϊ��");
		if(desaccountpk==null || desaccountpk.length()==0)
			throw new BusinessException("Ŀ���˲�Ϊ��");
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
