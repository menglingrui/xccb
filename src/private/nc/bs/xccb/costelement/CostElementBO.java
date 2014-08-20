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
//  mlr  保存前校验  编码不能重复   名称不能重复  不能为空    编码规则   ##--##--##  编码规则一致性校验
		if(vo == null || vo.getParentVO() == null)
			return;
		if(intBdAction == IBDACTION.SAVE){
			CostelementVO head = (CostelementVO)vo.getParentVO();
			head.validate();
//			校验编码是否和父类保持一致性  编码位数  必须为  两位  两位
			String code = head.getCostcode();
			String fathercode = getInvclCodeByKey(head.getReserve1(), head.getPk_corp());
			if(fathercode == null){
				fathercode = "root";
			}
			//校验必输项不能为空
			if (PuPubVO.getString_TrimZeroLenAsNull(head.getCostcode()) == null)
				throw new BusinessException("编码为空");

			if (PuPubVO.getString_TrimZeroLenAsNull(head.getCostname()) == null)
				throw new BusinessException("名称为空");
			if (PuPubVO.getString_TrimZeroLenAsNull(head.getDatasource()) == null)
				throw new BusinessException("数据来源为空");
			//校验公司级别编码不允许重复
			BsUniqueCheck.FieldUniqueChecks(head, new String[]{"costcode"}, " and pk_corp='"+head.getPk_corp()+"'" 
					, "编码不允许重复");
			//" and pk_accoutbook='"+head.getPk_accoutbook()+"'"
			//校验编码规则
			checkCode(code, fathercode, head.getPrimaryKey(),head.getPk_corp());
			updateSubFlag(head.getReserve1());
			//数据归集和成本动因相关校验
			checkData(vo);	
		}else if(intBdAction == IBDACTION.DELETE){
//			校验如果存在下级节点 不能删除  或 存在 核算单元 条目已经存在 不允许删除
			CostelementVO head = (CostelementVO)vo.getParentVO();
			String sql = "select count(0) from xccb_costelement where isnull(dr,0) = 0 and pk_corp = '"+head.getPk_corp()+"' and reserve1 = '"+head.getPrimaryKey()+"'";
			if(PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor()), 0)>0){
				throw new BusinessException("存在下级节点");
			}	
			//查询上级节点  是否还存在下级
			String sql1 = "select count(0) from xccb_costelement where isnull(dr,0) = 0 and pk_corp = '"+head.getPk_corp()+
			"' and reserve1 = '"+head.getReserve1()+"'and  pk_costelement <> '"+head.getPrimaryKey()+"'";
			if(PuPubVO.getInteger_NullAs(getDao().executeQuery(sql1, new ColumnProcessor()), 0)<=0){
				String sql2=" update  xccb_costelement set xccb_costelement.reserve14='Y' where pk_costelement = '"+head.getReserve1()+"'";
				getDao().executeUpdate(sql2);
			}
			String pk=head.getPk_costelement();
			String sql2=" select count(0) from xew_costaccount_b h where isnull(dr,0)=0 and h.pk_costelement='"+pk+"'";
			if(PuPubVO.getInteger_NullAs(getDao().executeQuery(sql2, new ColumnProcessor()), 0)>0){
				throw new BusinessException("成本要素已经被引用");
			}
			
			boolean isref=ZMReferenceCheck.isReferenced("xccb_costelement", head.getPk_costelement());
			if(isref){
				throw new BusinessException("成本要素已经被业务单据引用");
			}
		}
	}
	/**
	 * 在同一个账簿内
	 * 校验数据过滤页签---数据作业材料不能交叉 
	 * 校验成本动因页签---工程类别不能交叉
	 * @param vo
	 * @throws Exception 
	 */
	public void checkData(AggregatedValueObject vo) throws Exception {
		//前台编辑性控制
		//数据来源为井巷工程: 数据过滤页签只有 作业类别，作业，材料类别 ，材料可以编辑
		//数据来源为总账:只有会计科目可以编辑
		//成本动因页签：前台不控制		
		//校验ui数据
		checkDataUI(vo);
		//校验数据库数据
		checkDataCK(vo);
	}
	/**
	 * 校验数据库端
	 * @param vo
	 * @throws Exception 
	 */
	public void checkDataCK(AggregatedValueObject vo) throws BusinessException {
		ExAggCostelemetTVO billvo=(ExAggCostelemetTVO) vo;
		//取得表头数据
		CostelementVO   headvo=(CostelementVO) billvo.getParentVO();
		//取得数据过滤
		CostelementBVO[] bvos1=(CostelementBVO[]) billvo.getTableVO(billvo.getTableCodes()[0]);
		if(bvos1==null)
			return;
		//取得成本动因
		CostelementB1VO[] bvos2=(CostelementB1VO[]) billvo.getTableVO(billvo.getTableCodes()[1]);
		
		String pk_corp=headvo.getPk_corp();
		String pk_accountbook=headvo.getPk_accoutbook();
		
		List list=(List) getTool().queryCostElements(pk_accountbook, pk_corp);
		if(list==null || list.size()==0)
			return;
		CostelementBVO[]  nvos=(CostelementBVO[]) list.toArray(new CostelementBVO[0]);
		
		//nvos和bvos1如果是修改保存 那么一定会存在相同的数据 所以必须从nvos中过滤掉相同的数据
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
	 * 校验ui端
	 * @param vo
	 * @throws Exception 
	 */
	public void checkDataUI(AggregatedValueObject vo) throws Exception {
		if(vo==null)
			return;
		ExAggCostelemetTVO billvo=(ExAggCostelemetTVO) vo;
		//取得表头数据
		CostelementVO   headvo=(CostelementVO) billvo.getParentVO();
		//取得数据过滤
		CostelementBVO[] bvos1=(CostelementBVO[]) billvo.getTableVO(billvo.getTableCodes()[0]);
		//取得成本动因
		CostelementB1VO[] bvos2=(CostelementB1VO[]) billvo.getTableVO(billvo.getTableCodes()[1]);
		Integer datasource=PuPubVO.getInteger_NullAs(headvo.getDatasource(), -1);
		if(datasource==Xewcbpubconst.data_source_jx){
			checkFilerJX(bvos1,bvos1,true);
		}else if(datasource==Xewcbpubconst.data_source_zz){
			checkFilerZZ(bvos1,bvos1,true);
		}
	}
	/**
	 * 来自总账的数据过滤校验
	 * @param bvos1
	 * @param isEqual 是否来源和目的校验数据相同
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
			   //会计科目
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
					   
					   //作业类别 
					   String pk_accsubj1=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_accountsub());
					   String number=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
					 
					   if(pk_accsubj1!=null){
						   if(pk_accsubj1.equals(pk_accsubj)){
							       String costcode=getTool().getCostCodeByPk(pk_costelement);
							       String costname=getTool().getCostNameByPk(pk_costelement);						       
								   throw new BusinessException("会计科目 编码为：["+costcode+"],名称为 ["+costname+"] 数据过滤页签 行号为:["+number+"] 的表体行和行号为：["+nnumber+"]的表体行" +
							   		" 会计科目重复");
						   }
						   if(getTool().isAccsubCross(pk_accsubj,pk_accsubj1)){
							   String costcode=getTool().getCostCodeByPk(pk_costelement);
						       String costname=getTool().getCostNameByPk(pk_costelement);
							   throw new BusinessException("会计科目 编码为：["+costcode+"],名称为 ["+costname+"] 数据过滤页签 行号为:["+number+"] 的表体行定义的会计科目 ，和在行号为：["+nnumber+"]的表体行" +
						   		" 中定义的会计科目存在交叉");
		
						   }
					   }				   
				   }			 
			   }
		   }
	}
	

	/**
	 * 	来自井巷工程的数据过滤 校验
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
		   
		   //作业类别
		   String pk_invcl1=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_invcl1());
		   //作业
		   String pk_invmandoc1=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_invmandoc1());
		   //材料类别
		   String pk_invcl=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_invcl());
		   //材料
		   String pk_invmandoc=PuPubVO.getString_TrimZeroLenAsNull(bvo.getPk_invmandoc());
		   if(pk_invmandoc1!=null){
			   for(int j=0;j<nvos1.length;j++){
				   if(isEqual==true){
					   if(i==j){
						   continue;
					   }
				   }
				   CostelementBVO vo=nvos1[j];
				   //作业类别
				   String pk_invcl11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invcl1());
				   //作业
				   String pk_invmandoc11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invmandoc1());
				   String number=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
				   
				   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
				   String pk_costelement=vo.getPk_costelement();
				   String costcode=getTool().getCostCodeByPk(pk_costelement);
				   String costname=getTool().getCostNameByPk(pk_costelement);
				   
				   
				   if(pk_invmandoc11!=null){
					   if(pk_invmandoc11.equals(pk_invmandoc1)){
							   throw new BusinessException("数据过滤页签  成本要素为:["+costcode+"],["+costname+"]" +
							   		" 行号为:["+number+"] 的表体行和行号为：["+nnumber+"]的表体行" +
						   		" 作业重复");
					   }
				   }else if(pk_invcl11!=null){
					   if(getTool().isInvmanContain(pk_invcl11,pk_invmandoc1)){
						   throw new BusinessException("数据过滤页签 成本要素为:["+costcode+"],["+costname+"]" +
						   		" 行号为 :["+number+"] 的表体行定义的作业 ，在行号为：["+nnumber+"]的表体行" +
							   		" 中定义的作业类别中已经包含");
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
				   //作业类别
				   String pk_invcl11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invcl1());
				   //作业
				   String pk_invmandoc11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invmandoc1());
				   String number=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
				   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
				   String pk_costelement=vo.getPk_costelement();
				   String costcode=getTool().getCostCodeByPk(pk_costelement);
				   String costname=getTool().getCostNameByPk(pk_costelement);
				   if(pk_invmandoc11!=null){
					   if(getTool().isInvmanContain(pk_invcl1, pk_invmandoc11)){
						   throw new BusinessException("数据过滤页签   成本要素为:["+costcode+"],["+costname+"] 行号为 :["+nnumber+"] 的表体行定义的作业 ，在行号为：["+number+"]的表体行" +
					   		" 中定义的作业类别中已经包含");
					   }
				   }else if(pk_invcl11!=null){
					   if(getTool().isInvclCross(pk_invcl1,pk_invcl11)){
						   throw new BusinessException("数据过滤页签 行号为:["+number+"] 的表体行定义的作业类别 ，和 成本要素为:["+costcode+"],["+costname+"]" +
						   		"  行号为：["+nnumber+"]的表体行" +
							   		" 中定义的作业类别作业存在交叉");
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
				   //材料类别
				   String pk_invcl11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invcl());
				   //材料
				   String pk_invmandoc11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invmandoc());
				   String number=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
				   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
				   String pk_costelement=vo.getPk_costelement();
				   String costcode=getTool().getCostCodeByPk(pk_costelement);
				   String costname=getTool().getCostNameByPk(pk_costelement);
				   if(pk_invmandoc11!=null){
					   if(pk_invmandoc11.equals(pk_invmandoc)){
							   throw new BusinessException("数据过滤页签 成本要素为:["+costcode+"],["+costname+"] 行号为 :["+number+"] 的表体行和行号为：["+nnumber+"]的表体行" +
						   		" 材料重复");
					   }
				   }else if(pk_invcl11!=null){
					   if(getTool().isInvmanContain(pk_invcl11,pk_invmandoc)){
						   throw new BusinessException("数据过滤页签 成本要素为:["+costcode+"],["+costname+"] 行号为:["+number+"] 的表体行定义的材料 ，在行号为：["+nnumber+"]的表体行" +
							   		" 中定义的材料类别中已经包含");
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
				   //作业类别
				   String pk_invcl11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invcl());
				   //作业
				   String pk_invmandoc11=PuPubVO.getString_TrimZeroLenAsNull(vo.getPk_invmandoc());
				   String number=PuPubVO.getString_TrimZeroLenAsNull(bvo.getVdef10());
				   String nnumber=PuPubVO.getString_TrimZeroLenAsNull(vo.getVdef10());
				   String pk_costelement=vo.getPk_costelement();
				   String costcode=getTool().getCostCodeByPk(pk_costelement);
				   String costname=getTool().getCostNameByPk(pk_costelement);
				   if(pk_invmandoc11!=null){
					   if(getTool().isInvmanContain(pk_invcl, pk_invmandoc11)){
						   throw new BusinessException("数据过滤页签 行号为:["+nnumber+"] 的表体行定义的材料 ，在  成本要素为:["+costcode+"],["+costname+"]行号为：["+number+"]的表体行" +
					   		" 中定义的材料类别中已经包含");
					   }
				   }else if(pk_invcl11!=null){
					   if(getTool().isInvclCross(pk_invcl,pk_invcl11)){
						   throw new BusinessException("数据过滤页签  成本要素为:["+costcode+"],["+costname+"]   行号为:["+number+"] 的表体行定义的材料类别 ，和在行号为：["+nnumber+"]的表体行" +
							   		" 中定义的材料类别,材料存在交叉");
					   }
				   }
				   
			   }		   
		   }
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
	 * 过滤掉删除的
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
	 * 根据上级主键  将上级 是否末级 标志设置为false
	 * @param reserve1
	 * @throws DAOException 
	 */
	public void updateSubFlag(String reserve1) throws DAOException {
		String sql=" update xccb_costelement set xccb_costelement.reserve14='N' where xccb_costelement.pk_costelement='"+reserve1+"'";
		getDao().executeUpdate(sql);
	}

	private void checkCode(String code,String fathercode,String key,String corp) throws Exception{
		//校验第一个子类的编码
		if(fathercode.equalsIgnoreCase("root")){
			if(code.length()!=2){
				throw new BusinessException("分类编码不符合规则，XX--XX--XX");
			}
			return;
		}	
		int  leg=code.length()%2;
		//校验是否是 2的倍数
		if(leg!=0)
			throw new BusinessException("分类编码不符合规则，XX--XX--XX");
		
		if(!code.startsWith(fathercode)){
			throw new BusinessException("和父类编码不一致  子类编码必须开头包含父类编码");
		}
		if(code.length()-fathercode.length()!=2){
			throw new BusinessException("分类编码不符合规则，XX--XX--XX");
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
// 保存后校验
	}
	/**
	 * 按账簿复制成本要素
	 * @throws BusinessException 
	 */
	public void AccountCopy(String soraccountpk,String corp,String desaccountpk) throws BusinessException{
		if(soraccountpk==null || soraccountpk.length()==0)
			throw new BusinessException("来源账簿为空");
		if(corp==null || corp.length()==0)
			throw new BusinessException("公司为空");
		if(desaccountpk==null || desaccountpk.length()==0)
			throw new BusinessException("目的账簿为空");
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
