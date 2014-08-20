package nc.bs.xccb.pubsumdeal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import nc.bd.accperiod.AccountCalendar;
import nc.bd.accperiod.InvalidAccperiodExcetion;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.pub.SystemException;
import nc.bs.trade.business.HYPubBO;
import nc.bs.zmpub.pub.tool.SingleVOChangeDataBsTool;
import nc.itf.uap.pf.IPFBusiAction;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.pub.vosplit.SplitBillVOs;
import nc.vo.trade.pub.IBillStatus;
import nc.vo.xccb.costaccount.AggCostAccountVO;
import nc.vo.xccb.costaccount.CostAccountVO;
import nc.vo.xccb.costaccount.CostAccoutBVO;
import nc.vo.xccb.costelement.CostelementBVO;
import nc.vo.xccb.pub.XewcbPubTool;
import nc.vo.xccb.pub.Xewcbpubconst;
import nc.vo.xccb.sumdel.SumDealVO;
import nc.vo.zmpub.pub.report.ReportBaseVO;
/**
 * 公用成本汇总归集类  采用报表模式 进行成本的归集 
 * @author mlr
 */
public class SumDealBO {
	public InvocationInfoProxy cl=nc.bs.framework.common.InvocationInfoProxy.getInstance();

	private BaseDAO dao=null;
		public BaseDAO getDao() {
		if (dao == null) {
			dao = new BaseDAO();
		}
		return dao;
	}
	private XewcbPubTool  tool;
	public XewcbPubTool getTool(){
		if(tool==null){
			tool=new XewcbPubTool();
		}
		return tool;
	}
	/**
	 * 按公司+核算账簿+会计期间维度归集公用成本	
	 * @param pk_corp
	 * @param pk_accoutbook
	 * @param pk_accperiod
	 * @return
	 * @throws Exception
	 */
	public SumDealVO[] doCollectionCost(String pk_corp,String pk_accoutbook,String pk_accperiod ) throws Exception{
		//归集来自总账的成本
		SumDealVO[] jxvos=doCollectctionCostFromJX(pk_corp,pk_accoutbook,pk_accperiod);
		//归集来自井巷管理工程的成本
		SumDealVO[] zzvos=doCollectctionCostFromZZ(pk_corp,pk_accoutbook,pk_accperiod);
		//成本合并
		List list=new ArrayList();
	    if(jxvos!=null&& jxvos.length>0){
		 for(int i=0;i<jxvos.length;i++){
			list.add(jxvos[i]);
		 }
	    }
	    if(zzvos!=null&&zzvos.length>0){
		 for(int i=0;i<zzvos.length;i++){
			list.add(zzvos[i]);
		 }
	    }
	    SumDealVO[] vos=spiltAlloPubCost(list,pk_accperiod);
		return vos;
	}
	/**
	 * 过滤掉已经分配的成本
	 * @param list
	 * @param pk_accperiod 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws InvalidAccperiodExcetion 
	 * @throws SystemException 
	 */
	public SumDealVO[] spiltAlloPubCost(List list, String pk_accperiod) throws SystemException, InvalidAccperiodExcetion, SQLException, NamingException {
		if(list==null || list.size()==0){
			return null;
		}
		SumDealVO[] dvos=(SumDealVO[]) list.toArray(new SumDealVO[0]);
		SumDealVO[][] dvoss=(SumDealVO[][]) SplitBillVOs.getSplitVOs(dvos, SumDealVO.workcenters);
		String[] dealworkcenters=SumDealVO.workcenters;
		String[] costworkcenters=CostAccountVO.workcenters;
		//存放 没有分配的成本
		List<SumDealVO> nlist=new ArrayList<SumDealVO>();
		for(int i=0;i<dvoss.length;i++){
			SumDealVO[] vos=dvoss[i];
			if(vos==null || vos.length==0)
				continue;
			String wsql=new String();
			//设置查询的工作中心
			for(int j=0;j<dealworkcenters.length;j++){
				String value=PuPubVO.getString_TrimZeroLenAsNull(vos[0].getAttributeValue(dealworkcenters[j]));
				if(value!=null){
					wsql=wsql+" and "+costworkcenters[j]+" = '"+value+"' " ;
				}else{
					wsql=wsql+" and "+costworkcenters[j]+" is null ";
				}
			}	
			
				wsql=wsql+" and h.pk_corp='"+vos[0].getPk_corp()+"'";//公司
				wsql=wsql+" and b.ureserve1='Y'";//已分配成本
				ReportBaseVO[] bvos=getTool().getDMO().queryVOBySql(getQuerySqlAccountCost(pk_accperiod,wsql));
				for(int j=0;j<vos.length;j++){
					SumDealVO dvo=vos[j];
					String pk_costelement=dvo.getPk_costelement();
					if(!isExistCost(pk_costelement,bvos)){
						nlist.add(dvo);
					}		
				}			
			}
		return nlist.toArray(new SumDealVO[0]);
	}	
	
	private boolean isExistCost(String pk_costelement, ReportBaseVO[] bvos) {
		if(pk_costelement==null || pk_costelement.length()==0)
			return false;
		if(bvos==null || bvos.length==0)
			return false;
		boolean isEqual=false;
		for(int i=0;i<bvos.length;i++){
			ReportBaseVO vo=bvos[i];
			if(vo==null)
				continue;
			String pk_costelement1=PuPubVO.getString_TrimZeroLenAsNull(vo.getAttributeValue("pk_costelement"));
			if(pk_costelement.equals(pk_costelement1)){
			    return true;	
			}
		}	
		return false;
	}
	
	
	public String getQuerySqlAccountCost(String pk_accperiod,String wsql) throws InvalidAccperiodExcetion{
		StringBuffer sql=new StringBuffer();
		sql.append(" select ");
		sql.append(" b.* ");
		sql.append(" from xccb_costaccount h");
		sql.append(" join xccb_costaccount_b b");
		sql.append(" on h.pk_costaccount = b.pk_costaccount ");
		sql.append(" where isnull(h.dr,0)=0");
		sql.append(" and isnull(b.dr,0)=0 ");
		sql.append(wsql);
		AccountCalendar ac=AccountCalendar.getInstanceByAccperiodMonth(pk_accperiod);
		AccperiodmonthVO mothvo=ac.getMonthVO();
		String sdate=mothvo.getBegindate().toString();
		String edate=mothvo.getEnddate().toString();
		sql.append("  and h.dbilldate >='"+sdate+"'");
		sql.append("  and h.dbilldate <='"+edate+"'");		
		return sql.toString();
	}
	public SumDealVO[] doCollectctionCostFromJX(String pk_corp,
			String pk_accoutbook, String pk_accperiod) throws Exception {
//		//按公司+核算账簿+数据来源+是否末级+是否分摊成本+是否封存   查询成本要素
//		CostelementBVO[] costvos=getTool().doQueryCostElement(pk_corp,pk_accoutbook,Xewpubconst.data_source_jx,
//				new UFBoolean(true),new UFBoolean(true),new UFBoolean(false));
//		SumDealVO[] dealvos=getTool().doCollectionCostByCostElement(costvos,pk_accperiod,Xewpubconst.data_source_jx);		
//		return dealvos;
		return null;
	}
	/**
	 * 按公司+核算账簿+会计期间维度归集来自总账的成本
	 * @param pk_corp
	 * @param pk_accoutbook
	 * @param pk_accperiod
	 * @return
	 * @throws Exception 
	 */
	public  SumDealVO[] doCollectctionCostFromZZ(String pk_corp,
			String pk_accoutbook, String pk_accperiod) throws Exception {
		//按公司+核算账簿+数据来源+是否末级+是否分摊成本+是否封存   查询成本要素
		CostelementBVO[] costvos=getTool().doQueryCostElement(pk_corp,pk_accoutbook,Xewcbpubconst.data_source_zz,
				new UFBoolean(true),new UFBoolean(true),new UFBoolean(false));
		SumDealVO[] dealvos=getTool().doCollectionCostByCostElement(costvos,pk_accperiod,Xewcbpubconst.data_source_zz);		
		return dealvos;
	}	
	/**
	 * 成本处理 按工作中心分单 ---->生成公用成本汇总单
	 * @param dealvos
	 * @param infor  infor(0)=当前日期  infor(1)=当前操作员  infor(3)=公司
	 * @return
	 * @throws Exception 
	 */
	public void doDealPubCost(SumDealVO[] dealvos,String[] infor,String period,String pk_accoutbook) throws Exception{
		if(dealvos==null || dealvos.length==0)
			return;
		SumDealVO[][] voss=(SumDealVO[][]) SplitBillVOs.getSplitVOs(dealvos, SumDealVO.workcenters);
		AggCostAccountVO[] accbillvos=getAccountBillVO(voss,infor);
		//设置操作员
		for(int i=0;i<accbillvos.length;i++){
			accbillvos[i].getParentVO().setAttributeValue("voperatorid", infor[1]);
		}
		AccountCalendar ac=AccountCalendar.getInstance();
		
		dealSumConstVO(accbillvos,period,pk_accoutbook);
		//保存成本汇总单
		saveCostsum(accbillvos);
	}
	/**
	 * 处理生成的成本汇总单vo   
	 * 
	 * 查看 当前会计期间 同一工作中心 是否已经存在成本汇总 
	 * @param accbillvos
	 * @throws Exception 
	 */
	public void dealSumConstVO(AggCostAccountVO[] accbillvos,String period,String pk_accoutbook) throws Exception {
		if(accbillvos==null || accbillvos.length==0)
			return;
		for(int i=0;i<accbillvos.length;i++){
			AggCostAccountVO billvo=accbillvos[i];
			CostAccountVO hvo=(CostAccountVO) billvo.getParentVO();
			CostAccoutBVO[] bvos=(CostAccoutBVO[]) billvo.getChildrenVO();
		    String[] costworkcenters=CostAccountVO.workcenters1;
			String wsql=new String();
			wsql=" 1=1 ";
				//设置查询的工作中心
				for(int j=0;j<costworkcenters.length;j++){
					String value=PuPubVO.getString_TrimZeroLenAsNull(hvo.getAttributeValue(costworkcenters[j]));
					if(value!=null){
						wsql=wsql+" and "+costworkcenters[j]+" = '"+value+"' " ;
					}else{
						wsql=wsql+" and "+costworkcenters[j]+" is null ";
					}				
				}	
				wsql=wsql+" and pk_corp='"+hvo.getPk_corp()+"'";
				wsql=wsql+" and isnull(dr,0)=0";
				AccountCalendar ac=AccountCalendar.getInstanceByAccperiodMonth(period);
				AccperiodmonthVO mothvo=ac.getMonthVO();
				String sdate=mothvo.getBegindate().toString();
				String edate=mothvo.getEnddate().toString();
				wsql =wsql+"  and dbilldate >='"+sdate+"'";
				wsql =wsql+"  and dbilldate <='"+edate+"'";
				wsql=wsql+" and pk_billtype='"+Xewcbpubconst.bill_code_costsum+"'";
				wsql=wsql+" and pk_accoutbook='"+pk_accoutbook+"'";
		    List	list=(List) getDao().retrieveByClause(CostAccountVO.class, wsql);
		    if(list==null || list.size()==0)
		    	continue;
		    CostAccountVO  ohvo=(CostAccountVO) list.get(0);
		    if(ohvo.getVbillstatus()!=IBillStatus.FREE){
		    	String vbillno=ohvo.getVbillno();
		    	throw new Exception("无法处理,下游成本汇总单 单据为["+vbillno+"] 已经提交 或 审批 ");
		    }
		    accbillvos[i].setParentVO(ohvo);
		    String pk_h=ohvo.getPrimaryKey();
		    List list1=(List) getDao().retrieveByClause(CostAccoutBVO.class, " pk_costaccount ='"+pk_h+"' and isnull(dr,0)=0 ");
		    List<CostAccoutBVO>  zlist=new ArrayList<CostAccoutBVO>();
		    for(int j=0;j<bvos.length;j++){
		    	CostAccoutBVO bvo=bvos[j];
		    	String pk_costelement=bvo.getPk_costelement();
		    	for(int k=0;k<list1.size();k++){
		    		CostAccoutBVO ovo=(CostAccoutBVO) list1.get(k);
		    		if(pk_costelement.equals(ovo.getPk_costelement())){
		    			ovo.setNmy(PuPubVO.getUFDouble_NullAsZero(bvo.getNmy()));
		    			ovo.setStatus(VOStatus.UPDATED);
		    			zlist.add(ovo);
		    			break;
		    		}
		    		if(k==list1.size()-1){
		    			//bvo.setPk_costaccount(ovo.getPk_costaccount());
		    			bvo.setStatus(VOStatus.NEW);
		    			zlist.add(bvo);
		    		}
		    	}
		    }
		    accbillvos[i].setChildrenVO((CircularlyAccessibleValueObject[]) zlist.toArray(new CostAccoutBVO[0]));				
		}
		
	}
	/**
	 * 调用成本汇总单保存脚本
	 * @param accbillvos
	 * @throws BusinessException 
	 */
    public void saveCostsum(AggCostAccountVO[] accbillvos ) throws BusinessException {    	
    	if(accbillvos==null || accbillvos.length==0)
    		return;
    	UFDate dbilldate=PuPubVO.getUFDate(accbillvos[0].getParentVO().getAttributeValue("dmakedate"));
		IPFBusiAction bsBusiAction = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		for(int i=0;i<accbillvos.length;i++){
			bsBusiAction.processAction("WRITE",Xewcbpubconst.bill_code_costsum,dbilldate.toString(),null,accbillvos[i], null,null);	
		}	
	}

	/**
     * 生成 成本汇总单 单据vo
     * @param voss
	 * @param infor 
     * @return
     * @throws Exception 
     */
	private AggCostAccountVO[] getAccountBillVO(SumDealVO[][] voss, String[] infor) throws Exception {
		
		if(voss==null || voss.length==0)
			return null;
		AggCostAccountVO[] abillvos=new AggCostAccountVO[voss.length];
		for(int i=0;i<voss.length;i++){
			if(voss[i]==null || voss[i].length==0)
				continue;
			SumDealVO headvo=voss[i][0];
			SumDealVO[] bodyvos=voss[i];
			CostAccountVO[] headvos=(CostAccountVO[]) SingleVOChangeDataBsTool.runChangeVOAry(new SumDealVO[]{headvo}, CostAccountVO.class, "nc.bs.pf.changedir.CHGCB03TOCB04headVO");
			CostAccoutBVO[] bodyvoss=(CostAccoutBVO[]) SingleVOChangeDataBsTool.runChangeVOAry(bodyvos, CostAccoutBVO.class, "nc.bs.pf.changedir.CHGCB03TOCB04bodyVO");		
			abillvos[i]=new AggCostAccountVO();
			HYPubBO bo=new HYPubBO();
			String billno=bo.getBillNo(Xewcbpubconst.bill_code_costsum, (String) headvos[0].getAttributeValue("pk_corp"), null, null);
			headvos[0].setAttributeValue("vbillno", billno);
			headvos[0].setAttributeValue("voperatorid", infor[1]);
			headvos[0].setAttributeValue("dbilldate",  new UFDate(infor[0]));
			headvos[0].setAttributeValue("dmakedate",  new UFDate(infor[0]) );
			abillvos[i].setParentVO(headvos[0]);
			abillvos[i].setChildrenVO(bodyvoss);
		}
		return abillvos;
	}
}
