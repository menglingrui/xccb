package nc.ui.xccb.costelement;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import nc.ui.scm.util.ObjectUtils;
import nc.ui.trade.check.BeforeActionCHK;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.VOStatus;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.xccb.costelement.CostelementB1VO;
import nc.vo.xccb.costelement.ExAggCostelemetTVO;
import nc.vo.xew.pub.XewPubTool;


public class ClientCheckCHK extends BeforeActionCHK {
	XewPubTool tool=null;
	public XewPubTool getTool(){
		if(tool==null){
			tool=new XewPubTool();
		}
		return tool;
	}
	public void runBatchClass(Container parent, String billType,
			String actionName, AggregatedValueObject[] vos, Object[] obj)
			throws Exception {
		
		
	}

	public void runClass(Container parent, String billType, String actionName,
			AggregatedValueObject vo, Object obj) throws Exception {
		
		if(actionName.equalsIgnoreCase("WRITE")){
			if(vo==null){
				return;
			}
			if(vo.getChildrenVO()==null || vo.getChildrenVO().length==0){
				return;
			}
			ExAggCostelemetTVO billvo=(ExAggCostelemetTVO) vo;
			CostelementB1VO[] bvos2=(CostelementB1VO[]) billvo.getTableVO(billvo.getTableCodes()[1]);
			checkDriver(bvos2, bvos2, true);
		}	
	}
	/**
	 * 过滤掉删除的
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
	 * 	校验 动因
	 * @param bvos1
	 * @throws Exception 
	 */
	private void checkDriver(CostelementB1VO[] bvos1,CostelementB1VO[] destvos,boolean isEqual) throws BusinessException {
	   if(bvos1==null || bvos1.length==0)
		   return;
	   CostelementB1VO[] nvos=spilt1(bvos1);
	   CostelementB1VO[] nvos1=destvos;
	   if(isEqual==true){
		   nvos1=spilt1(destvos);
	   }
	   for(int i=0;i<nvos.length;i++){
		   CostelementB1VO bvo=nvos[i];
		   
		   //工程类别
		   String pk_invcl1=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_projectcl());		 
		   if(pk_invcl1!=null){
			   for(int j=0;j<nvos1.length;j++){
				   if(isEqual==true){
					   if(i==j){
						   continue;
					   }
				   }
				   CostelementB1VO vo=nvos1[j];
				   //作业类别
				   String pk_invcl11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_projectcl());
				   //作业
				   String number=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
				   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
				   String pk_costelement=vo.getPk_costelement();
				   String costcode=getTool().getCostCodeByPkClient(pk_costelement);
				   String costname=getTool().getCostCodeByPkClient(pk_costelement);
				   
				   
				   if(pk_invcl11!=null){
					   if(pk_invcl1.equals(pk_invcl11)){
							   throw new BusinessException("成本动因页签  成本要素为:["+costcode+"],["+costname+"]" +
							   		" 行号为:["+number+"] 的表体行和行号为：["+nnumber+"]的表体行" +
						   		" 工程类别重复");
					   }
				   }
				   if(pk_invcl11!=null){
					   if(getTool().isProClCross(pk_invcl1, pk_invcl11)){
						   throw new BusinessException("成本动因页签  成本要素为:["+costcode+"],["+costname+"]" +
						   		" 行号为 :["+number+"] 的表体行定义的作业 ，在行号为：["+nnumber+"]的表体行" +
							   		" 中定义的工程类别中已经包含");
					   }
				   }
				   
			   }
		   }
	   }
	}

	}
