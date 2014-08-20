package nc.vo.xccb.pub;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import nc.bd.accperiod.AccountCalendar;
import nc.bd.accperiod.InvalidAccperiodExcetion;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.pub.SystemException;
import nc.bs.pub.pf.PfUtilBO;
import nc.bs.trade.business.HYPubBO;
import nc.bs.zmpub.pub.report.ReportDMO;
import nc.bs.zmpub.pub.tool.SingleVOChangeDataBsTool;
import nc.itf.uap.busibean.ISysInitQry;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.vo.xccb.costaccount.AggCostAccountVO;
import nc.vo.xccb.costaccount.CostAccountVO;
import nc.vo.xccb.costaccount.CostAccoutBVO;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.VOStatus;
import nc.vo.pub.compiler.PfParameterVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.para.SysInitVO;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.pub.vosplit.SplitBillVOs;
import nc.vo.trade.pub.IBillStatus;
import nc.vo.xccb.costelement.CostelementBVO;
import nc.vo.xccb.costelement.CostelementVO;
import nc.vo.xccb.sumdel.SumDealVO;
import nc.vo.xcgl.factory.FactoryVO;
import nc.vo.xcgl.pub.consts.PubBillTypeConst;
import nc.vo.xcgl.pub.tool.XcPubTool;
import nc.vo.zmpub.pub.report.ReportBaseVO;
import nc.vo.zmpub.pub.report2.CombinVO;
import nc.vo.zmpub.pub.tool.ZmPubTool;
/**
 * xew项目公共工具类
 * @author mlr
 */
public class XewcbPubTool {
	private BaseDAO dao=null;
	public BaseDAO getDao() {
	if (dao == null) {
		dao = new BaseDAO();
	}
	return dao;
}
	private  ReportDMO dmo=null;
	public ReportDMO getDMO() throws SystemException, NamingException{
		if(dmo==null){
			dmo=new ReportDMO();
		}
		return dmo;
	}
	PfUtilBO pf=null;
	public PfUtilBO getPfBO(){
		if(pf==null){
			pf=new PfUtilBO();
		}
		return pf;
	}
	HYPubBO  hybo=null;
	public HYPubBO  getHyBO(){
		if(hybo==null ){
			hybo=new HYPubBO();
		}
		return hybo;
	}
	/**
	 * 
	 * 基本档案删除前 校验是否已经被引用 查看项目基本档案
	 * @throws BusinessException 
	 */
	public boolean isReferenced(String bname,String bpk) throws BusinessException{
		//jobcode->getColValue(bd_jobbasfil,jobcode,pk_jobbasfil,pk_jobbasfil)
		String sql=" select count(0) from bd_jobbasfil where "+bname+"='"+bpk+"'and isnull(dr,0)=0";
		Integer count=PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor()), -1);
		if(count >0){
			throw new BusinessException("数据已经被引用");
		}
		return false;	
	}
	/**
	 * 
	 * 基本档案删除前 校验是否已经被引用 查看项目基本档案
	 * @throws BusinessException 
	 */
	public boolean isReferenced1(String bname,String bpk) throws BusinessException{
		//jobcode->getColValue(bd_jobbasfil,jobcode,pk_jobbasfil,pk_jobbasfil)
		String sql=" select count(0) from XEW_PROCREATE_B where "+bname+"='"+bpk+"'and isnull(dr,0)=0";
		Integer count=PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor()), -1);
		if(count >0){
			throw new BusinessException("数据已经被引用");
		}
		return false;	
	}
	/**
	 * 
	 * 基本档案删除前 校验是否已经被引用 查看项目基本档案
	 * @throws BusinessException 
	 */
	public boolean isReferenced2(String bname,String bpk) throws BusinessException{
		//jobcode->getColValue(bd_jobbasfil,jobcode,pk_jobbasfil,pk_jobbasfil)
		String sql=" select count(0) from xccb_costaccount_b where "+bname+"='"+bpk+"'and isnull(dr,0)=0";
		Integer count=PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor()), -1);
		if(count >0){
			throw new BusinessException("数据已经被引用");
		}
		return false;	
	}
	/**
	 * 
	 * 基本档案删除前 校验是否已经被引用 查看项目基本档案
	 * @throws BusinessException 
	 */
	public boolean isReferenced3(String bname,String bpk) throws BusinessException{
		//jobcode->getColValue(bd_jobbasfil,jobcode,pk_jobbasfil,pk_jobbasfil)
		String sql=" select count(0) from XEW_PROACCEPT_B where "+bname+"='"+bpk+"'and isnull(dr,0)=0";
		Integer count=PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor()), -1);
		if(count >0){
			throw new BusinessException("数据已经被引用");
		}
		return false;	
	}
	/**
	 * cha kan  pk_costelement shi fou shu yu  pk_coste
	 * @param pk_coste
	 * @param pk_costelement
	 * @return
	 * @throws BusinessException 
	 */
	public boolean isCostElementContain(String pk_coste, String pk_costelement) throws BusinessException {
        if(pk_coste==null|| pk_coste.length()==0)
        	return true;		
        String proclcode=getCostCodeByPk(pk_coste);
		if(pk_costelement==null ||pk_costelement.length()==0)
			return false;
		String sql=" select * from xccb_costelement h where h.costcode like '"+proclcode+"%' " +
				"  and isnull(dr,0)=0  and coalesce(isclose,'N') ='N'";
		ArrayList<CostelementVO> list=(ArrayList<CostelementVO> ) getDao().executeQuery(sql, new BeanListProcessor(CostelementVO.class));
	    if(list==null || list.size()==0)
	    	return false;
	    for(int i=0;i<list.size();i++){
	    	String  pk_cl=(String) list.get(i).getPrimaryKey();
	    	if(pk_costelement.equals(pk_cl)){
	    		return true;
	    	}
	    }
		return false;
	}
//	/**
//	 * 工程项目pk_basid 是否属于该工程类别pk_procl
//	 * @param pk_procl
//	 * @param pk_basid
//	 * @return
//	 * @throws BusinessException 
//	 */
//	public boolean isProContain(String pk_procl, String pk_basid) throws BusinessException {
//        if(pk_procl==null|| pk_procl.length()==0)
//        	return true;		
//        String proclcode=getProclCodeBypPk(pk_procl);
//        String pk_procl1=(String) ZmPubTool.execFomular("def20->getColValue(bd_jobbasfil,def20,pk_jobbasfil,pk_jobbasfil)",new String[]{"pk_jobbasfil"}, new String[]{pk_basid});
//		if(pk_procl1==null ||pk_procl1.length()==0)
//			return false;
//		String sql=" select * from xew_workscategory h where h.vcode like '"+proclcode+"%' " +
//				"  and isnull(dr,0)=0  and coalesce(isclose,'N') ='N'";
//		ArrayList<WorksCategoryVO> list=(ArrayList<WorksCategoryVO> ) getDao().executeQuery(sql, new BeanListProcessor(WorksCategoryVO.class));
//	    if(list==null || list.size()==0)
//	    	return false;
//	    for(int i=0;i<list.size();i++){
//	    	String  pk_cl=(String) list.get(i).getPrimaryKey();
//	    	if(pk_procl1.equals(pk_cl)){
//	    		return true;
//	    	}
//	    }
//		return false;
//	}
	/**
	 /* 查看 pk_invmandoc 这个存货主键  是否属于 pk_invcl 这个存货类别
	 * @param pk_invcl11
	 * @param pk_invmandoc1
	 * @return
	 * @throws BusinessException 
	 */
	public boolean isInvmanContain(String pk_invcl, String pk_invmandoc) throws BusinessException {
		if(pk_invcl==null || pk_invcl.length()==0 || pk_invmandoc==null || pk_invmandoc.length()==0)
			return false;
		String invclcode=getInvclCodeByPk(pk_invcl);
		String sql=" select h.pk_invmandoc from bd_invmandoc h " +
		" join bd_invbasdoc b " +
		" on h.pk_invbasdoc = b.pk_invbasdoc " +
		" join bd_invcl c  " +
		" on b.pk_invcl = c.pk_invcl " +
		" where isnull(h.dr, 0) = 0  " +
		" and isnull(b.dr, 0) = 0 " +
		" and isnull(c.dr, 0) = 0 " +
		" and c.invclasscode like '"+invclcode+"%' ";
		List list=(List) getDao().executeQuery(sql, new ArrayListProcessor());
		if(list==null || list.size()==0)
			return false;
		for(int i=0;i<list.size();i++){
			Object[] o=(Object[]) list.get(i);
			if(o==null || o.length==0)
				continue;
			String pk_b=PuPubVO.getString_TrimZeroLenAsNull(o[0]);
			if(pk_invmandoc.equals(pk_b)){
				return true;
			}
		}		
		return false;
	}
	/**
	 *查看两个会计科目 是否存在交叉
	 * @param pk_accsubj1
	 * @param pk_accsubj2
	 * @return
	 * @throws BusinessException
	 */
	public boolean isAccsubCross(String pk_accsubj1, String pk_accsubj2) throws BusinessException {
		if(pk_accsubj1==null || pk_accsubj1.length()==0 || pk_accsubj2==null || pk_accsubj2.length()==0){
			return false;
		}
		String invclcode=getAccountSubCodeByPk(pk_accsubj1);
		String invclcode1=getAccountSubCodeByPk(pk_accsubj2);
		if(invclcode.startsWith(invclcode1)){
			return true;
		}
		if(invclcode1.startsWith(invclcode)){
		    return true;	
		}
		return false;
	}
    /**
     * 查看两个作业类别是否存在交叉
     * @param pk_invcl1
     * @param pk_invcl12
     * @return
     * @throws BusinessException 
     */
	public boolean isInvclCross(String pk_invcl1, String pk_invcl2) throws BusinessException {
		if(pk_invcl1==null || pk_invcl1.length()==0 || pk_invcl2==null || pk_invcl2.length()==0){
			return false;
		}
		
		String invclcode=getInvclCodeByPk(pk_invcl1);
		String invclcode1=getInvclCodeByPk(pk_invcl2);
		if(invclcode.startsWith(invclcode1)){
			return true;
		}
		if(invclcode1.startsWith(invclcode)){
		    return true;	
		}
		return false;
	}
    /**
     * 查看两个工程类别是否存在交叉
     * @param pk_invcl1
     * @param pk_invcl12
     * @return
     * @throws BusinessException 
     */
	public boolean isProClCross(String pk_invcl1, String pk_invcl2) throws BusinessException {
		if(pk_invcl1==null || pk_invcl1.length()==0 || pk_invcl2==null || pk_invcl2.length()==0){
			return false;
		}
		
		String invclcode=getProclCodeBypPk(pk_invcl1);
		String invclcode1=getProclCodeBypPk(pk_invcl2);
		if(invclcode.startsWith(invclcode1)){
			return true;
		}
		if(invclcode1.startsWith(invclcode)){
		    return true;	
		}
		return false;
	}
	/**
	 * 根据来源单据类型  和 来源单据id 查询成本核算单
	 * @param pk_billtype
	 * @param vlastbillid
	 * @return
	 * @throws DAOException 
	 */
	public List queryCostBVO(String pk_billtype,String vlastbillid) throws DAOException{
		String sql=" select b.* from xccb_costaccount h join xccb_costaccount_b b " +
				" on h.pk_costaccount=b.pk_costaccount and isnull(h.dr,0)=0  and isnull(b.dr,0)=0 " +
				" and  pk_billtype='"+pk_billtype+"' and vlastbillid='"+vlastbillid+"'";
		List list=(List) getDao().executeQuery(sql, new BeanListProcessor(CostAccoutBVO.class));
		return list;
	}
	/**
	 * 根据账簿查询成本要素
	 * @param pk_billtype
	 * @param vlastbillid
	 * @return
	 * @throws DAOException 
	 */
	public List queryCostElements(String pk_accountbook,String pk_corp) throws DAOException{
		String sql=" select b.* from xccb_costelement h join xccb_costelement_b b " +
				" on h.pk_costelement=b.pk_costelement and isnull(h.dr,0)=0  and isnull(b.dr,0)=0 " +
				" and  h.pk_accoutbook='"+pk_accountbook+"' and h.pk_corp='"+pk_corp+"'";
		List list=(List) getDao().executeQuery(sql, new BeanListProcessor(CostelementBVO.class));
		return list;
	}
	/**
	 * 根据来源单据类型  和 来源单据id 查询成本核算单
	 * @param pk_billtype
	 * @param vlastbillid
	 * @return
	 * @throws DAOException 
	 */
	public List queryCostBVO(String vlastbillid) throws DAOException{
		String sql=" select b.* from xccb_costaccount h join xccb_costaccount_b " +
				" on h.pk_costaccount=b.pk_costaccount and isnull(h.dr,0)=0  and isnull(b.dr,0)=0 " +
				" and vlastbillid='"+vlastbillid+"'";
		List list=(List) getDao().executeQuery(sql, new BeanListProcessor(CostAccoutBVO.class));
		return list;
	}
	/**
	 * 构造成本核算单vo
	 * @return
	 * @throws DAOException 
	 */
	public AggCostAccountVO[] getAccountBillVO(List<CostAccoutBVO>  list) throws DAOException{
		   CostAccoutBVO[] costbvos=(CostAccoutBVO[]) list.toArray(new CostAccoutBVO[0]);
		   //按主键分担
		   CostAccoutBVO[][] costbvoss=(CostAccoutBVO[][]) SplitBillVOs.getSplitVOs(costbvos, new String[]{"pk_costaccount"});
		   //构造直接成本核算单的聚合vo
		   AggCostAccountVO[] abillvos=new AggCostAccountVO[costbvoss.length];
		   for(int i=0;i<costbvoss.length;i++){
			   CostAccoutBVO[] abvos=costbvoss[i];
			   if(abvos==null || abvos.length==0){
				   continue;
			   }
			   String pk_costaccount=abvos[0].getPk_costaccount();
			   List li=(List) getDao().retrieveByClause(CostAccountVO.class, " isnull(dr,0)=0 and pk_costaccount='"+pk_costaccount+"'");
			   if(li==null || li.size()==0)
				   continue;
			   CostAccountVO ahvo=(CostAccountVO) li.get(0);
			   abillvos[i]=new AggCostAccountVO();
			   abillvos[i].setParentVO(ahvo);
			   abillvos[i].setChildrenVO(abvos);
		   }
		return abillvos;		
	}
	/**
	 * 构造成本核算单vo
	 * @return
	 * @throws BusinessException 
	 */
	public AggCostAccountVO[] getAccountBillVOS(List<CostAccoutBVO>  list) throws BusinessException{
		   if(list==null || list.size()==0)
			   return null;
		   CostAccoutBVO[] costbvos=(CostAccoutBVO[]) list.toArray(new CostAccoutBVO[0]);
		   //按主键分担
		   CostAccoutBVO[][] costbvoss=(CostAccoutBVO[][]) SplitBillVOs.getSplitVOs(costbvos, new String[]{"pk_costaccount"});
		   //构造直接成本核算单的聚合vo
		   AggCostAccountVO[] abillvos=new AggCostAccountVO[costbvoss.length];
		   for(int i=0;i<costbvoss.length;i++){
			   CostAccoutBVO[] abvos=costbvoss[i];
			   setProCl(abvos);
			   if(abvos==null || abvos.length==0){
				   continue;
			   }
			   String pk_costaccount=abvos[0].getPk_costaccount();
			   List li=(List) getDao().retrieveByClause(CostAccountVO.class, " isnull(dr,0)=0 and pk_costaccount='"+pk_costaccount+"'");
			   if(li==null || li.size()==0)
				   continue;
			   CostAccountVO ahvo=(CostAccountVO) li.get(0);
			   abillvos[i]=new AggCostAccountVO();
			   abillvos[i].setParentVO(ahvo);
			   abillvos[i].setChildrenVO(abvos);
		   }
		return abillvos;		
	}
	/**
	 * 设置工程类别
	 * @param abvos
	 * @throws BusinessException 
	 */
	public void setProCl(CostAccoutBVO[] abvos) throws BusinessException {
		if(abvos==null || abvos.length==0){
			return;
		}
		for(int i=0;i<abvos.length;i++){
			CostAccoutBVO vo=abvos[i];
			String pk_bas=vo.getPk_jobbasfil();
			String pk_cl=getProClPkByProBasPk(pk_bas);
			vo.setVdef10(pk_cl);
		}
		
	}
	/**
	 * 构造成本核算单vo 根据来源单据id
	 * @return
	 * @throws DAOException 
	 */
	public AggCostAccountVO[] getAccountBillVO(String pk_billtype,String vlastbillid) throws DAOException{
	      //查询成本核算单表体
		   List list=queryCostBVO(pk_billtype, vlastbillid);
		   if(list==null || list.size()==0)
			   return null;
		   CostAccoutBVO[] costbvos=(CostAccoutBVO[]) list.toArray(new CostAccoutBVO[0]);
		   //按主键分担
		   CostAccoutBVO[][] costbvoss=(CostAccoutBVO[][]) SplitBillVOs.getSplitVOs(costbvos, new String[]{"pk_costaccount"});
		   //构造直接成本核算单的聚合vo
		   AggCostAccountVO[] abillvos=new AggCostAccountVO[costbvoss.length];
		   for(int i=0;i<costbvoss.length;i++){
			   CostAccoutBVO[] abvos=costbvoss[i];
			   if(abvos==null || abvos.length==0){
				   continue;
			   }
			   String pk_costaccount=abvos[0].getPk_costaccount();
			   List li=(List) getDao().retrieveByClause(CostAccountVO.class, " isnull(dr,0)=0 and pk_costaccount='"+pk_costaccount+"'");
			   if(li==null || li.size()==0)
				   continue;
			   CostAccountVO ahvo=(CostAccountVO) li.get(0);
			   abillvos[i]=new AggCostAccountVO();
			   abillvos[i].setParentVO(ahvo);
			   abillvos[i].setChildrenVO(abvos);
		   }
		return abillvos;		
	}
	/**
	 * 构造成本核算单vo 根据来源单据id
	 * @return
	 * @throws DAOException 
	 */
	public AggCostAccountVO[] getAccountBillVO(String vlastbillid) throws DAOException{
	      //查询成本核算单表体
		   List list=queryCostBVO(vlastbillid);
		   if(list==null || list.size()==0)
			   return null;
		   CostAccoutBVO[] costbvos=(CostAccoutBVO[]) list.toArray(new CostAccoutBVO[0]);
		   //按主键分担
		   CostAccoutBVO[][] costbvoss=(CostAccoutBVO[][]) SplitBillVOs.getSplitVOs(costbvos, new String[]{"pk_costaccount"});
		   //构造直接成本核算单的聚合vo
		   AggCostAccountVO[] abillvos=new AggCostAccountVO[costbvoss.length];
		   for(int i=0;i<costbvoss.length;i++){
			   CostAccoutBVO[] abvos=costbvoss[i];
			   if(abvos==null || abvos.length==0){
				   continue;
			   }
			   String pk_costaccount=abvos[0].getPk_costaccount();
			   List li=(List) getDao().retrieveByClause(CostAccountVO.class, " isnull(dr,0)=0 and pk_costaccount='"+pk_costaccount+"'");
			   if(li==null || li.size()==0)
				   continue;
			   CostAccountVO ahvo=(CostAccountVO) li.get(0);
			   abillvos[i]=new AggCostAccountVO();
			   abillvos[i].setParentVO(ahvo);
			   abillvos[i].setChildrenVO(abvos);
		   }
		return abillvos;		
	}
	/**
	 * 保存单据
	 * @param orderVos
	 * @param pk_billtype
	 * @param busdate
	 * @throws Exception
	 */
	public void saveBill(AggregatedValueObject[] orderVos ,String pk_billtype,String busdate) throws Exception{
		if(orderVos==null || orderVos.length==0)
			return;
		PfUtilBO pfbo = getPfBO();
		for (AggregatedValueObject bill : orderVos) {
			if(bill==null || bill.getParentVO()==null || bill.getChildrenVO()==null || bill.getChildrenVO().length==0)
				continue;
			pfbo.processAction("WRITE",
					pk_billtype,busdate, null, bill, null);
		}	
	}
	public void setBillInfors(AggregatedValueObject[] billvos,String pk_billtype,PfParameterVO paraVo,boolean isSetSource) throws BusinessException{
		if(billvos==null || billvos.length==0)
			return;
		for (int i = 0; i < billvos.length; i++) {
			AggregatedValueObject bill=billvos[i];
			String sourcetype=null;
			String sourbillno=null;
			String sourbillid=null;
			if (bill == null || bill.getParentVO() == null)
				continue;
			String billno = getHyBO().getBillNo(pk_billtype,
					(String) bill.getParentVO().getAttributeValue("pk_corp"),
					null, null);
			sourcetype=(String) bill.getParentVO().getAttributeValue("pk_billtype");
			sourbillno=(String) bill.getParentVO().getAttributeValue("vbillno");
			sourbillid=(String) bill.getParentVO().getPrimaryKey();
			bill.getParentVO().setAttributeValue("pk_billtype", pk_billtype);
			bill.getParentVO().setPrimaryKey(null);
			bill.getParentVO().setStatus(VOStatus.NEW);
			bill.getParentVO().setAttributeValue("vbillno", billno);
			bill.getParentVO().setAttributeValue("voperatorid",
					paraVo.m_operator);
			bill.getParentVO().setAttributeValue("dbilldate",
					new UFDate(paraVo.m_currentDate));
			bill.getParentVO().setAttributeValue("dmakedate",
					new UFDate(paraVo.m_currentDate));
			//设置单据状态，设空审批信息
//			public String vapproveid;
//			public UFDate dapprovedate;
//			public String vapprovenote;
//			vbillstatus
			bill.getParentVO().setAttributeValue("vapproveid", null);
			bill.getParentVO().setAttributeValue("dapprovedate", null);
			bill.getParentVO().setAttributeValue("vapprovenote", null);
			bill.getParentVO().setAttributeValue("vbillstatus", IBillStatus.FREE);
			
			SuperVO[] tbvos = (SuperVO[]) bill.getChildrenVO();
			if (tbvos != null && tbvos.length > 0) {
				for (int j = 0; j < tbvos.length; j++) {
					if(isSetSource)
					setSourceInfor(sourcetype,sourbillno,sourbillid,tbvos[j]);
					tbvos[j].setPrimaryKey(null);
					tbvos[j].setStatus(VOStatus.NEW);
					tbvos[j].setAttributeValue("pk_costaccount", null);
				}
			}
		}
	}
	private void setSourceInfor(String sourcetype,String sourbillno, String sourbillid,SuperVO vo) {
		//来源
		vo.setAttributeValue("vlastbilltype", sourcetype);
		vo.setAttributeValue("vlastbillid", sourbillid);
		vo.setAttributeValue("vlastbillrowid", vo.getPrimaryKey());
		vo.setAttributeValue("vlastbillcode", sourbillno);
		//源头
		vo.setAttributeValue("csourcebillcode", vo.getAttributeValue("csourcebillcode"));
		vo.setAttributeValue("vsourcebilltype", vo.getAttributeValue("vsourcebilltype"));
		vo.setAttributeValue("vsourcebillid", vo.getAttributeValue("vsourcebillid"));
		vo.setAttributeValue("vsourcebillid", vo.getAttributeValue("vsourcebillid"));
	}
	/**
	 * 删除单据
	 * @throws Exception
	 */
	public void deleteBillVO(String pk_billtype,String busidate,AggregatedValueObject[] abillvos)throws Exception{
		//调用删除脚本
		if (abillvos == null || abillvos.length == 0) {
			return;
		}       
		PfUtilBO pfbo = getPfBO();
		for (AggregatedValueObject bill : abillvos) {	
			if(bill==null || bill.getParentVO()==null || bill.getChildrenVO()==null || bill.getChildrenVO().length==0){
				continue;
			}				
			if(PuPubVO.getInteger_NullAs(bill.getParentVO().getAttributeValue("vbillstatus"), -1)!=IBillStatus.FREE){
				String billno=PuPubVO.getString_TrimZeroLenAsNull(bill.getParentVO().getAttributeValue("vbillno"));
				throw new BusinessException(" 单据号为["+billno+"] 单据已经提交或审批 不能删除");
			}
			pfbo.processAction("DELETE",pk_billtype, busidate, null, bill, null);
		}		
	}
	/**
	 * 根据公司查询 核算账簿
	 * @param pk_corp
	 * @return
	 * @throws DAOException 
	 */
	public List<String> getAccountBookByCorp(String pk_corp) throws DAOException{
	   String sql=" select pk_glbook from bd_glbook  where  bd_glbook.pk_glbook in (select b.pk_glbook from " +
								" bd_glorg h join bd_glorgbook b on h.pk_glorg=b.pk_glorg " +
								" where nvl(h.dr,0)=0 and nvl(b.dr,0)=0 and h.pk_entityorg='"+pk_corp+"')";
	   List list=(List<String>) getDao().executeQuery(sql, new ArrayListProcessor());
	   return list;
	}
	/**
	 * 按公司+核算账簿+数据来源+是否末级+是否公用成本+是否封存 查询成本要素
	 * @param pk_corp
	 * @param pk_accoutbook
	 * @param data_source_jx
	 * @param isFinalStage
	 * @param isPubCost
	 * @return
	 * @throws Exception 
	 */
	public CostelementBVO[] doQueryCostElement(String pk_corp,
			String pk_accoutbook, Integer data_source, UFBoolean isFinalStage,
			UFBoolean isPubCost,UFBoolean isFlag) throws Exception {
		StringBuffer sql=new StringBuffer();
		sql.append(" select  ");
		sql.append(" b.*");
		sql.append(" from xccb_costelement h");
		sql.append(" join xccb_costelement_b b");
		sql.append(" on h.pk_costelement=b.pk_costelement ");
		sql.append(" where ");
		sql.append("     isnull(h.dr,0)=0");
		sql.append(" and isnull(b.dr,0)=0");
		if(data_source!=null&&data_source>0){
		  sql.append(" and h.datasource="+data_source);
		}
		if(isFinalStage!=null){
		  if(isFinalStage.booleanValue()==true){	
		     sql.append(" and h.reserve14='Y'");
		  }else{
			 sql.append(" and coalesce(h.reserve14,'N')='N'"); 
		  }
		}
		if(isFlag!=null){
		  if(isFlag.booleanValue()==true){	
			 sql.append(" and h.isclose='Y'");
		  }else{
			 sql.append(" and coalesce(h.isclose,'N')='N'"); 
		  }
		}
		if(pk_corp!=null && pk_corp.length()>0){
		     sql.append(" and h.pk_corp='"+pk_corp+"'");
		}
		if(pk_accoutbook!=null && pk_accoutbook.length()>0){
			sql.append(" and h.pk_accoutbook='"+pk_accoutbook+"'");
		}
		List<CostelementBVO> list=(List<CostelementBVO>) getDao().executeQuery(sql.toString(), new BeanListProcessor(CostelementBVO.class));
		if(list==null || list.size()==0)
			return null;
		//设置公司和账簿
		for(int i=0;i<list.size();i++){
			CostelementBVO vo=list.get(i);
			vo.setReserve1(pk_accoutbook);
			vo.setReserve2(pk_corp);
		}
		CostelementBVO[] costvos=getCostElementByIsPubCost(list,isPubCost);		
		return costvos;
	}
	/**
	 * 根据是否公用成本 过滤集合中的成本要素
	 * @param list
	 * @param isPubCost
	 * @return
	 * @throws Exception 
	 */
	public CostelementBVO[] getCostElementByIsPubCost(
			List<CostelementBVO> list, UFBoolean isPubCost) throws Exception {
		if(list==null || list.size()==0)
			return null;
		if(isPubCost ==null)
			return list.toArray(new CostelementBVO[0]);
	    List<CostelementBVO> nlist=new ArrayList<CostelementBVO>();
		for(int i=0;i<list.size();i++){
			CostelementBVO nvo=list.get(i);
			if(isPubCostElement(nvo)==isPubCost.booleanValue()){
				nlist.add(nvo);
			}
		}		
		if(nlist==null || nlist.size()==0)
			return null;
		return nlist.toArray(new CostelementBVO[0]);
	}
	/**
	 * 判断一个成本要素是否是 公用成本要素
	 * @param nvo
	 * @return
	 * @throws Exception 
	 */
	public boolean isPubCostElement(CostelementBVO nvo) throws Exception {
		if(nvo==null)
			throw new Exception("要判断的成本要素为空");		
		//成本要素主键
		String pk_h=PuPubVO.getString_TrimZeroLenAsNull(nvo.getPk_costelement());
		String sql=" select count(0) from xccb_costelement_b1 h where isnull(h.dr,0)=0 and h.pk_costelement ='"+pk_h+"'";
		Integer count=PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor()), -1);
		if(count>0){
			return true;
		}
		return false;
	}
	/**
	 * 根据成本要素归集成本  
	 * @param costvos
	 * @param isPubCoust
	 * @return
	 * @throws Exception 
	 */
	public SumDealVO[] doCollectionCostByCostElement(CostelementBVO[] costvos,String periods,
			Integer dataSource) throws Exception {
		if(costvos==null || costvos.length==0)
			return null;
		if(dataSource ==null){
			throw new Exception("数据来源为空");
		}
		if(dataSource.intValue()==Xewcbpubconst.data_source_zz){
			return doCollectionCostZZ(costvos,periods);
		}
		if(dataSource.intValue()==Xewcbpubconst.data_source_jx){
			return null;
		}else{
			throw new Exception("未知的数据来源");
		}
	}
	/**
	 * 根据数据来源为 井巷工程的成本要素 归集成本
	 * @param costvos
	 * @throws Exception 
	 */
	public SumDealVO[] doCollectionCostJX(CostelementBVO[] costvos,String periods) throws Exception {
		if(costvos==null || costvos.length==0)
			return null;
		//得到井巷工程 归集作业的成本要素
		CostelementBVO[]  costvos1=getCostElementVO_ZY(costvos);
		//得到井巷工程 归集材料的成本要素
		CostelementBVO[]  costvos2=getCostElementVO_CL(costvos);
		//井巷工程作业成本归集
		SumDealVO[] dvos1=doCollectionCostJX_ZY(costvos1,periods);
		//井巷工程材料成本归集
		SumDealVO[] dvos2=doCollectionCostJX_CL(costvos2,periods);
	    if(dvos1==null || dvos1.length==0)
	    	return dvos2;
	    if(dvos2==null || dvos2.length==0)
	    	return dvos1;
	    List<SumDealVO>  list=new ArrayList<SumDealVO>();	 
	    for(int i=0;i<dvos1.length;i++){
	    	list.add(dvos1[i]);
	    }
	    for(int i=0;i<dvos2.length;i++){
	    	list.add(dvos2[i]);
	    }
	    
		return list.toArray(new SumDealVO[0]);
	}
	private SumDealVO[] doCollectionCostJX_CL(CostelementBVO[] costvos,
			String periods) throws Exception {		
		List<String> sqls=new ArrayList<String>();
 		for(int i=0;i<costvos.length;i++){
 			CostelementBVO costvo=costvos[i];
 			valuteCostVO(costvo,Xewcbpubconst.data_source_jx);
 			String period=periods;
 			if(periods==null || periods.length()==0){
 				throw new Exception("会计期间为空");
 			}
 			String pk_glbook=costvo.getReserve1();//核算账簿
 			String pk_corp=costvo.getReserve2();//公司	
 			String pk_invcl=costvo.getPk_invcl();//得到存货分类
 			String invclcode=getInvclCodeByPk(pk_invcl);//得到存货分类编码
 			String pk_invmandoc=costvo.getPk_invmandoc();//得到存货
  			String sql=getQuerySqlJX_CL(pk_corp, pk_glbook, period,pk_invmandoc,invclcode);
 			sqls.add(sql);
		}
 		if(sqls==null||sqls.size()==0){
 			return null;
 		}
		List<ReportBaseVO[]> listvos=getDMO().queryVOBySql(sqls.toArray(new String[0]));
		//设置工作中心
		setWorkCenters(listvos, Xewcbpubconst.data_source_jx);
		//设置成本要素
		setCostElement(costvos,listvos);
		//创建汇总处理vo
		SumDealVO[] dvos=createSumDealVO(listvos,"nc.bs.pf.changedir.CHGJXCLTODealVO");
		return dvos;
	}

	/**
	 * 井巷管理工程 作业成本的归集
	 * @param costvos1
	 * @param periods
	 * @throws Exception 
	 */
	public SumDealVO[] doCollectionCostJX_ZY(CostelementBVO[] costvos, String periods) throws Exception {
		
		List<String> sqls=new ArrayList<String>();
 		for(int i=0;i<costvos.length;i++){
 			CostelementBVO costvo=costvos[i];
 			valuteCostVO(costvo,Xewcbpubconst.data_source_jx);
 			String period=periods;
 			if(periods==null || periods.length()==0){
 				throw new Exception("会计期间为空");
 			}
 			String pk_glbook=costvo.getReserve1();//核算账簿
 			String pk_corp=costvo.getReserve2();//公司	
 			String pk_invcl=costvo.getPk_invcl1();//作业分类	
 			String invclcode=getInvclCodeByPk(pk_invcl);//得到作业编码 
 			String pk_invmandoc=costvo.getPk_invmandoc1();//作业
  			String sql=getQuerySqlJX_ZY(pk_corp, pk_glbook, period,pk_invmandoc,invclcode);
 			sqls.add(sql);
		}
 		if(sqls==null || sqls.size()==0){
 			return null;
 		}
		List<ReportBaseVO[]> listvos=getDMO().queryVOBySql(sqls.toArray(new String[0]));
		setWorkCenters(listvos, Xewcbpubconst.data_source_jx);
		//设置成本要素
		setCostElement(costvos,listvos);
		//创建汇总处理vo
		SumDealVO[] dvos=createSumDealVO(listvos,"nc.bs.pf.changedir.CHGJXZYTODealVO");
		return dvos;
	}
    public String getQuerySqlJX_ZY(String pk_corp, String pk_glbook,
			String period, String pk_invmandoc, String invclcode) throws InvalidAccperiodExcetion {
		StringBuffer  sql=new StringBuffer();		
		sql.append(" select  ");
		sql.append("   h.pk_corp ,");//公司
//		for(int i=0;i<ProAcceptVO.workcenters.length;i++){
//			sql.append(" "+ProAcceptVO.workcenters[i]+" ,");
//		}
		sql.append("   h.pk_cubasdoc  ,");//客商基本id
		sql.append("   h.pk_cave ,");//洞口
		sql.append("   h.pk_deptdoc ,");//部门
		sql.append("   h.pk_cumandoc  ,");//客商管理id
		sql.append("   h.pk_proaccept ,");//主键
		sql.append("   h.pk_minarea ,");//矿区
		sql.append("   h.pk_billtype,");//单据类型
		sql.append("   b.pk_proaccept_b ,");//子表主键
		sql.append("   b.pk_procl ,");//工程类别
		sql.append("   b.pk_project, ");//工程管理id
		sql.append("   b.pk_jobbasfil,");//工程基本id
		sql.append("   b.pk_invbasdoc ,");//作业基本id
		sql.append("   b.pk_invmandoc ,");//作业管理id
		sql.append("   b.hauldis ,");//
		sql.append("   b.heiprice ,");//
		sql.append("   b.num ,");//工作量
		sql.append("   b.basprice ,");//
		sql.append("   b.artprice ,");//	
		sql.append("   b.hauprice ,");//			
		sql.append("   b.chuprice ,");//		
		sql.append("   b.heightdiff, ");//	
		sql.append("   b.otherprice ,");//
		sql.append("   b.finprice ,");//最终单价	
		sql.append("   b.endmny ,");//金额	
		sql.append("   b.sermonth , ");//服务月限		
		sql.append("   b.proquality ");//工程质量		
		sql.append("  from  ");
		sql.append("  xew_proaccept h ");
		sql.append("  join xew_proaccept_b b ");
		sql.append("  on h.pk_proaccept=b.pk_proaccept ");
		sql.append("  where isnull(h.dr,0)=0");
		sql.append("  and isnull(b.dr,0)=0 ");
		AccountCalendar ac=AccountCalendar.getInstanceByAccperiodMonth(period);
		AccperiodmonthVO mothvo=ac.getMonthVO();
		String sdate=mothvo.getBegindate().toString();
		String edate=mothvo.getEnddate().toString();
		sql.append("  and h.dbilldate >='"+sdate+"'");
		sql.append("  and h.dbilldate <='"+edate+"'");
		sql.append("  and h.vbillstatus="+IBillStatus.CHECKPASS);
		sql.append("  and h.pk_corp='"+pk_corp+"' ");
		if(pk_invmandoc!=null && pk_invmandoc.length()>0){
			sql.append("  and b.pk_invmandoc='"+pk_invmandoc+"' ");
		}
		if(invclcode!=null && invclcode.length()>0){
			sql.append("  and b.pk_invmandoc in (select h.pk_invmandoc from bd_invmandoc h " +
					" join bd_invbasdoc b " +
					" on h.pk_invbasdoc = b.pk_invbasdoc " +
					" join bd_invcl c  " +
					" on b.pk_invcl = c.pk_invcl " +
					" where isnull(h.dr, 0) = 0  " +
					" and isnull(b.dr, 0) = 0 " +
					" and isnull(c.dr, 0) = 0 " +
					" and c.invclasscode like '"+invclcode+"%')");
		}	    
		return sql.toString();
	}

	public String getInvclCodeByPk(String pk_invcl) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("invclasscode->getColValue(bd_invcl,invclasscode,pk_invcl,pk_invcl)",
				new String[]{"pk_invcl"}, new String[]{pk_invcl}));
	}
	public String getInvclNameByPk(String pk_invcl) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("invclassname->getColValue(bd_invcl,invclassname,pk_invcl,pk_invcl)",
				new String[]{"pk_invcl"}, new String[]{pk_invcl}));
	}

	public String getQuerySqlJX_CL(String pk_corp, String pk_glbook,
			String period, String pk_invmandoc, String invclcode) throws InvalidAccperiodExcetion {
		StringBuffer  sql=new StringBuffer();		
		sql.append(" select  ");
		sql.append("   h.pk_corp ,");//公司
		sql.append("   h.pk_cubasdoc  ,");//客商基本id
		sql.append("   h.pk_cave ,");//洞口
//		for(int i=0;i<ProAcceptVO.workcenters.length;i++){
//			sql.append(" "+ProAcceptVO.workcenters[i]+" ,");
//		}
		sql.append("   h.pk_deptdoc ,");//部门
		sql.append("   h.pk_cumandoc  ,");//客商管理id
		sql.append("   h.pk_proaccept ,");//主键
		sql.append("   h.pk_minarea ,");//矿区
		sql.append("   h.pk_billtype,");//单据类型
		sql.append("   b.pk_proaccept_b ,");//子表主键
		sql.append("   b.pk_procl ,");//工程类别
		sql.append("   b.pk_project, ");//工程管理id
		sql.append("   b.pk_jobbasfil,");//工程基本id
		sql.append("   b.pk_invbasdoc ,");//作业基本id
		sql.append("   b.pk_invmandoc ,");//作业管理id
		sql.append("   b.hauldis ,");//
		sql.append("   b.heiprice ,");//
		sql.append("   b.num ,");//工作量
		sql.append("   b.basprice ,");//
		sql.append("   b.artprice ,");//	
		sql.append("   b.hauprice ,");//			
		sql.append("   b.chuprice ,");//		
		sql.append("   b.heightdiff ,");//	
		sql.append("   b.otherprice ,");//
		sql.append("   b.finprice ,");//最终单价	
		sql.append("   b.endmny ,");//金额	
		sql.append("   b.sermonth ,");//服务月限		
		sql.append("   b.proquality ,");//工程质量	
		sql.append("   c.pk_proaccept_bb ,");//	材料主键	
		sql.append("   c.pk_invbasdoc pk_invbasdoc1 ,");//	材料 id
		sql.append("   c.pk_invmandoc pk_invmandoc2 ,");//材料管理id
		sql.append("   c.price ,");//材料单价
		sql.append("   c.num num1 ,");//材料数量
		sql.append("   c.mny  ");//材料金额		
		sql.append("  from  ");
		sql.append("  xew_proaccept h ");
		sql.append("  join xew_proaccept_b b ");
		sql.append("  on h.pk_proaccept=b.pk_proaccept ");
		sql.append("  join xew_proaccept_bb c");
		sql.append("  on b.pk_proaccept_b=c.pk_proaccept_b");
		sql.append("  where isnull(h.dr,0)=0");
		sql.append("  and isnull(b.dr,0)=0 ");
		sql.append("  and isnull(c.dr,0)=0");
		AccountCalendar ac=AccountCalendar.getInstanceByAccperiodMonth(period);
		AccperiodmonthVO mothvo=ac.getMonthVO();
		String sdate=mothvo.getBegindate().toString();
		String edate=mothvo.getEnddate().toString();
		sql.append("  and h.dbilldate >='"+sdate+"'");
		sql.append("  and h.dbilldate <='"+edate+"'");
		sql.append("  and h.vbillstatus="+IBillStatus.CHECKPASS);
		sql.append("  and h.pk_corp='"+pk_corp+"' ");
		if(pk_invmandoc!=null && pk_invmandoc.length()>0){
			sql.append("  and c.pk_invmandoc='"+pk_invmandoc+"' ");
		}
		if(invclcode!=null && invclcode.length()>0){
			sql.append("  and c.pk_invmandoc in (select h.pk_invmandoc from bd_invmandoc h " +
					" join bd_invbasdoc b " +
					" on h.pk_invbasdoc = b.pk_invbasdoc " +
					" join bd_invcl c  " +
					" on b.pk_invcl = c.pk_invcl " +
					" where isnull(h.dr, 0) = 0  " +
					" and isnull(b.dr, 0) = 0 " +
					" and isnull(c.dr, 0) = 0 " +
					" and c.invclasscode like '"+invclcode+"%')");
		}
		return sql.toString();
	}

	/**
     * 得到井巷工程归集作业的材料要素
     * @param costvos
     * @return
     */
	public CostelementBVO[] getCostElementVO_CL(CostelementBVO[] costvos) {
		if(costvos==null || costvos.length==0){
			return null;
		}
		List<CostelementBVO> list=new ArrayList<CostelementBVO>();
		for(int i=0;i<costvos.length;i++){
			CostelementBVO vo=costvos[i];
			if(vo.getPk_invcl()!=null && vo.getPk_invcl().length()>0){
				list.add(vo);
				continue;
			}
			if(vo.getPk_invmandoc()!=null && vo.getPk_invmandoc().length()>0){
				list.add(vo);
				continue;
			}
		}		
		return list.toArray(new CostelementBVO[0]);
	}
    /**
     * 得到井巷工程归集作业的成本要素
     * @param costvos
     * @return
     */
	public CostelementBVO[] getCostElementVO_ZY(CostelementBVO[] costvos) {
		if(costvos==null || costvos.length==0){
			return null;
		}
		List<CostelementBVO> list=new ArrayList<CostelementBVO>();
		for(int i=0;i<costvos.length;i++){
			CostelementBVO vo=costvos[i];
			if(vo.getPk_invcl1()!=null && vo.getPk_invcl1().length()>0){
				list.add(vo);
				continue;
			}
			if(vo.getPk_invmandoc1()!=null && vo.getPk_invmandoc1().length()>0){
				list.add(vo);
				continue;
			}
		}		
		return list.toArray(new CostelementBVO[0]);
	}

	/**
	 * 根据数据来源为  总账的成本要素 归集成本
	 * @param costvos
	 * @throws Exception 
	 */
	public SumDealVO[] doCollectionCostZZ(CostelementBVO[] costvos,String periods) throws Exception {
		if(costvos==null || costvos.length==0)
			return null;
		List<String> sqls=new ArrayList<String>();
 		for(int i=0;i<costvos.length;i++){
 			CostelementBVO costvo=costvos[i];
 			valuteCostVO(costvo,Xewcbpubconst.data_source_zz);
 			String period=periods;
 			if(periods==null || periods.length()==0){
 				throw new Exception("会计期间为空");
 			}
 			String pk_glbook=costvo.getReserve1();//核算账簿
 			String pk_corp=costvo.getReserve2();//公司
 			String pk_accsubj=costvo.getPk_accountsub();//会计科目
 			String accscode=getAccountSubCodeByPk(pk_accsubj);//会计科目编码
 			String sql=getQuerySqlZZ(pk_corp, pk_glbook, period, accscode);
 			sqls.add(sql);
		}
		List<ReportBaseVO[]> listvos=getDMO().queryVOBySql(sqls.toArray(new String[0]));
		//设置辅助核算
		setWorkCenters(listvos, Xewcbpubconst.data_source_zz);
		//设置成本要素
		setCostElement(costvos,listvos);
		//创建汇总处理vo
		SumDealVO[] dvos=createSumDealVO(listvos,"nc.bs.pf.changedir.CHGZZPZTODealVO");
		//filter only factory depptment
		SumDealVO[] dvos1=filterFacDeptDatas(dvos);
		return dvos1;
	}
	/**
	 * filter only factory depptment
	 * @param dvos
	 * @return
	 * @throws DAOException 
	 */
	public SumDealVO[] filterFacDeptDatas(SumDealVO[] dvos) throws BusinessException {
		if(dvos==null || dvos.length==0){
			return null;
		}
       //get factory deptment datas
		String pk_corp=dvos[0].getPk_corp();
		List fvos=(List) getDao().retrieveByClause(FactoryVO.class, " isnull(dr,0)=0 and pk_corp='"+pk_corp+"'");
	    if(fvos==null || fvos.size()==0){
	    	throw new BusinessException("请设置选厂档案");
	    }
		//set filter SumDealVO
	    List<SumDealVO> slist=new ArrayList<SumDealVO>();
	    for(int i=0;i<dvos.length;i++){
	    	SumDealVO dvo=dvos[i];
	    	if(isExist(dvo,fvos)){
	    		slist.add(dvo);
	    	}
	    }
		return slist.toArray(new SumDealVO[0]);
	}
	/**
	 * judge dvo  and fvos deptparment is equal
	 * @param dvo
	 * @param fvos
	 * @return
	 */
	public boolean isExist(SumDealVO dvo, List fvos) {
		if(dvo==null){
			return false;
		}
		
		if(fvos==null || fvos.size()==0){
			return false;
		}
		
		for(int i=0;i<fvos.size();i++){
			String pk_deptdoc=dvo.getPk_defdoc1();
			if(pk_deptdoc==null){
				return false;
			}
	       FactoryVO  fvo=(FactoryVO) fvos.get(i);
			if(pk_deptdoc.equals(fvo.getPk_deptdoc())){
				return true;
			}
			
		}
			
		return false;
	}
	public SumDealVO[] createSumDealVO(List<ReportBaseVO[]> listvos,String chgclass) throws Exception {
		List<ReportBaseVO> list=new ArrayList<ReportBaseVO>();
		if(listvos==null || listvos.size()==0)
			return null;
		for(int i=0;i<listvos.size();i++){
			ReportBaseVO[] vos=listvos.get(i);
			if(vos==null || vos.length==0)
				continue;
			for(int j=0;j<vos.length;j++){
				list.add(vos[j]);
			}
		}
		SumDealVO[] dealvos=(SumDealVO[]) SingleVOChangeDataBsTool.runChangeVOAry(list.toArray(new ReportBaseVO[0]), SumDealVO.class, 
				chgclass);		
		return dealvos;
	}

	public void setCostElement(CostelementBVO[] costvos, List<ReportBaseVO[]> listvos) {
		if(costvos==null || costvos.length==0)
			return ;
		if(listvos==null || listvos.size()==0)
			return;
		for(int i=0;i<costvos.length;i++){
			CostelementBVO cvo=costvos[i];
			String pk_costelement=cvo.getPk_costelement();
			ReportBaseVO[] bvos=listvos.get(i);
			if(bvos==null|| bvos.length==0){
				continue;
			}
			for(int j=0;j<bvos.length;j++){
				bvos[j].setAttributeValue("pk_costelement", pk_costelement);
				bvos[j].setAttributeValue("pk_accoutbook", cvo.getReserve1());
			}
		}		
	}
    /**
     * 设置凭证的辅助核算
     * @param listvos
     * @throws Exception 
     */
	public void setWorkCenters(List<ReportBaseVO[]> listvos,Integer datasource) throws Exception {
		if(listvos==null || listvos.size()==0){
			return;
		}
		for(int i=0;i<listvos.size();i++){
			ReportBaseVO[] vos=listvos.get(i);
			if(vos==null || vos.length==0){
				continue;
			}else{
				for(int j=0;j<vos.length;j++){
				  setWorkCenter(vos[j],datasource);
				}
			}
		}
		
	}
	/**
	 * 设置工作中心  根据数据来源
	 * @param reportBaseVO
	 * @param datasource
	 * @throws Exception 
	 */
    public void setWorkCenter(ReportBaseVO vo, Integer datasource) throws Exception {
         if(datasource.intValue()==Xewcbpubconst.data_source_jx){
        	 setWorkCenterJX(vo);
         }else if(datasource.intValue()==Xewcbpubconst.data_source_zz){
        	 setWorkCenterZZ(vo);
         }else{
        	 throw new Exception("未知的数据来源");
         }
    	
	}

	private void setWorkCenterJX(ReportBaseVO vo) throws SystemException, SQLException, NamingException, BusinessException {
		if(vo==null){
			return;
		}
		//这段代码是 比较灵活的来去 工作中心  单不好维护 所以暂时不用了
//		String pk_billtype=PuPubVO.getString_TrimZeroLenAsNull(vo.getAttributeValue("pk_billtype"));
//		String[] work_bas_names=SumDealVO.workcenters_basedoc_name;//获得工作中心 档案的名字
//		String[] workcenters=SumDealVO.workcenters;//获得工作中心  在汇总vo中注册字段
//		for(int i=0;i<work_bas_names.length;i++){
//			String name=work_bas_names[i];
//			String fieldname=getFieldCodeByName(pk_billtype,name);
//			vo.setAttributeValue(workcenters[i], vo.getAttributeValue(fieldname));
//		}  
//		String[] work_bas_names=SumDealVO.workcenters_basedoc_name;//获得工作中心 档案的名字
		String[] workcenters=SumDealVO.workcenters;//获得工作中心  在汇总vo中注册字段
//		String[] aworkcenters=ProAcceptVO.workcenters1;//验收单工作中心
//		for(int i=0;i<aworkcenters.length;i++){
//			String name=aworkcenters[i];
//			vo.setAttributeValue(workcenters[i], vo.getAttributeValue(name));
//		} 
	}
    
	public String getFieldCodeByName(String pk_billtype,String name) throws DAOException {
       String sql=" select itemkey from pub_billtemplet h join pub_billtemplet_b b " +
       		" on h.pk_billtemplet=b.pk_billtemplet where isnull(h.dr,0)=0 " +
       		" and isnull(b.dr,0)=0 and h.pk_billtypecode='"+pk_billtype+"' and b.reftype='"+name+"'";    
       String fieldcode=PuPubVO.getString_TrimZeroLenAsNull(getDao().executeQuery(sql, new ColumnProcessor()));		
		return fieldcode;
	}

	/**
     * 设置来自总账的数据的 工作中心
     * @param vo
     * @throws NamingException 
     * @throws SQLException 
     * @throws SystemException 
     * @throws BusinessException 
     */
	public void setWorkCenterZZ(ReportBaseVO vo) throws SystemException, SQLException, NamingException, BusinessException {
		if(vo==null){
			return;
		}
	    ReportBaseVO[] assvos=getDMO().queryVOBySql(getQuerySqlAssisZZ(PuPubVO.getString_TrimZeroLenAsNull(vo.getAttributeValue("assid"))));
		if(assvos==null || assvos.length==0)
			return ;
		String[] work_bas_names=SumDealVO.workcenters_basedoc_name;//获得工作中心 档案的名字
		String[] workcenters=SumDealVO.workcenters;//获得工作中心  在汇总vo中注册字段
		for(int i=0;i<work_bas_names.length;i++){
			String pk_bdinfo=getBdinfoPkByName(work_bas_names[i]);
			if(pk_bdinfo==null || pk_bdinfo.length()==0){
				continue;
			}
			String checkvalue=null;
			for(int j=0;j<assvos.length;j++){
				if(pk_bdinfo.equals(assvos[j].getAttributeValue("checktype"))){
					checkvalue=PuPubVO.getString_TrimZeroLenAsNull(assvos[j].getAttributeValue("checkvalue"));
					vo.setAttributeValue(workcenters[i], checkvalue);
					break;
				}
			}			
		}
	}
	/**
	 * 获得系统注册档案主键
	 * @param string
	 * @return
	 * @throws BusinessException 
	 */
	public String getBdinfoPkByName(String bdname) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("pk_bdinfo->getColValue(bd_bdinfo,pk_bdinfo,bdname,bdname)", 
				new String[]{"bdname"}, new String[]{bdname}));
	}
	/**
	 * 获得系统注册档案主键
	 * @param string
	 * @return
	 * @throws BusinessException 
	 */
	public String getProClPkByProBasPk(String pk_probas) throws BusinessException {
		return  PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("def20->getColValue(bd_jobbasfil,def20,pk_jobbasfil,pk_jobbasfil)", 
				new String[]{"pk_jobbasfil"}, new String[]{pk_probas}));
    	
	}
	public static String  defnum="";
	/**
	 * 
	 * @作者：mlr
	 * @说明：完达山物流项目 
	 *  成本分配 获得系统设置 保留小数位数
	 * @时间：2011-4-20上午11:57:57
	 * @return
	 */
	public static  String getDefaultNum(String corp){
		if(PuPubVO.getString_TrimZeroLenAsNull(defnum)!=null)
			return defnum;
		ISysInitQry sysinitQry = (ISysInitQry) NCLocator.getInstance().lookup(ISysInitQry.class.getName());
		try {
			SysInitVO vo =sysinitQry.queryByParaCode(corp, "XEW00");
			if(vo != null){
				defnum = vo.getValue();
			}else{
				return "4";
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			System.out.println("获取参数WDS00失败");
			return "4";
		}
		return defnum;
	}
	/**
	 * 根据辅助核算id  查询辅助核算
	 * @param assid
	 * @return
	 */
    public String getQuerySqlAssisZZ(String assid){
    	StringBuffer sql=new StringBuffer();
    	sql.append(" select  ");
    	sql.append(" checktype ,");//辅助核算类型
    	sql.append(" checkvalue,");//辅助核算内容
    	sql.append(" freevalueid ,");//辅助核算id
    	sql.append(" pk_freevalue ,");//辅助核算主键
    	sql.append(" valuecode, ");//核算内容编码
    	sql.append(" valuename ");//核算内容名称
    	sql.append(" from ");
    	sql.append(" gl_freevalue ");
    	sql.append(" where isnull(dr,0)=0");
    	sql.append(" and freevalueid='"+assid+"'");    	
    	return sql.toString();
    }
	/**
	 * 成本要素数据合法性校验
	 * @param costvo
	 * @param data_source_zz
	 * @throws Exception 
	 */
	public void valuteCostVO(CostelementBVO costvo, Integer data_source_zz) throws Exception {
		if(data_source_zz.intValue()==Xewcbpubconst.data_source_zz){
 	
 			valuteCostVOZZ(costvo);
 			
		}else if(data_source_zz.intValue()==Xewcbpubconst.data_source_jx){
			valuteCostVOJX(costvo);
		}else{
			throw new Exception("未知的数据来源");
		}
	}
	
	private void valuteCostVOJX(CostelementBVO costvo) throws Exception {
		if(costvo==null){
			throw new Exception("来自总账的成本要素对象为空");
		}
		String pk_glbook=costvo.getReserve1();
		if(pk_glbook==null || pk_glbook.length()==0){
			String costcode=getCostCodeByPk(costvo.getPk_costelement());
			String costname=getCostNameByPk(costvo.getPk_costelement());
			throw new Exception("成本要素定义 编码为：['"+costcode+"'],名称为：['"+costname+"'] 核算账簿为空 ");
		}	
		String pk_corp=costvo.getReserve2();
		if(pk_corp==null || pk_corp.length()==0){
			String costcode=getCostCodeByPk(costvo.getPk_costelement());
			String costname=getCostNameByPk(costvo.getPk_costelement());
			throw new Exception("成本要素定义 编码为：['"+costcode+"'],名称为：['"+costname+"'] 公司为空 ");
		}

	}

	public  void valuteCostVOZZ(CostelementBVO costvo) throws Exception {
		if(costvo==null){
			throw new Exception("来自总账的成本要素对象为空");
		}
		String pk_glbook=costvo.getReserve1();
		if(pk_glbook==null || pk_glbook.length()==0){
			String costcode=getCostCodeByPk(costvo.getPk_costelement());
			String costname=getCostNameByPk(costvo.getPk_costelement());
			throw new Exception("成本要素定义 编码为：['"+costcode+"'],名称为：['"+costname+"'] 核算账簿为空 ");
		}	
		String pk_corp=costvo.getReserve2();
		if(pk_corp==null || pk_corp.length()==0){
			String costcode=getCostCodeByPk(costvo.getPk_costelement());
			String costname=getCostNameByPk(costvo.getPk_costelement());
			throw new Exception("成本要素定义 编码为：['"+costcode+"'],名称为：['"+costname+"'] 公司为空 ");
		}
		String pk_accsubj=costvo.getPk_accountsub();
		if(pk_accsubj==null || pk_accsubj.length()==0){
			String costcode=getCostCodeByPk(costvo.getPk_costelement());
			String costname=getCostNameByPk(costvo.getPk_costelement());
			throw new Exception("成本要素定义 编码为：['"+costcode+"'],名称为：['"+costname+"'] 数据来源定义的会计科目存在空值");
		}
		String accscode=getAccountSubCodeByPk(pk_accsubj);
		if(accscode==null || accscode.length()==0){
			String costcode=getCostCodeByPk(costvo.getPk_costelement());
			String costname=getCostNameByPk(costvo.getPk_costelement());
			throw new Exception("成本要素定义 编码为：['"+costcode+"'],名称为：['"+costname+"'] 数据来源定义存在没有找到会计科目编码的科目");
		}	
	}
	public String getAccountSubCodeByPk(String pk_accsubj) throws BusinessException {        
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("subjcode->getColValue(bd_accsubj,subjcode ,pk_accsubj ,pk_accsubj )",
				new String[]{"pk_accsubj"}, new String[]{pk_accsubj}));
	}
	public  String getCostCodeByPk(String costpk) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("costcode->getColValue(xccb_costelement,costcode ,pk_costelement ,pk_costelement )",
				new String[]{"pk_costelement"}, new String[]{costpk}));
		
	}
	public  String getCostCodeByPkClient(String costpk) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomularClient("costcode->getColValue(xccb_costelement,costcode ,pk_costelement ,pk_costelement )",
				new String[]{"pk_costelement"}, new String[]{costpk}));
		
	}
	public  String getAccountBookByPk(String costpk) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("pk_accoutbook->getColValue(xccb_costelement,pk_accoutbook ,pk_costelement ,pk_costelement )",
				new String[]{"pk_costelement"}, new String[]{costpk}));
		
	}
	
	public  String getCostNameByPk(String costpk) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("costname->getColValue(xccb_costelement,costname ,pk_costelement ,pk_costelement )",
				new String[]{"pk_costelement"}, new String[]{costpk}));
		
	}
	/**
	 * 获得查询总账凭证的sql
	 * @return
	 * @throws InvalidAccperiodExcetion 
	 */
	public String getQuerySqlZZ(String pk_corp,String pk_glbook,String period,String accscode) throws InvalidAccperiodExcetion{
		StringBuffer  sql=new StringBuffer();		
		sql.append(" select  ");
		sql.append("   h.pk_corp ,");//公司
		sql.append("   h.pk_glbook ,");//核算账簿
		sql.append("   h.pk_glorg  ,");//会计主体
		sql.append("   h.pk_glorgbook ,");//主体账簿
		sql.append("   h.pk_sob ,");//账簿主键
		sql.append("   h.prepareddate  ,");//制单日期
		sql.append("   h.signdate ,");//签字日期
		sql.append("   h.tallydate ,");//记账日期
		sql.append("   h.signflag ,");//签字标志
		sql.append("   h.period ,");//会计期间
		sql.append("   h.pk_voucher ,");//凭证主键
		sql.append("   b.pk_detail ,");//分录主键
		sql.append("   b.localdebitamount ,");//本币借方发生额
		sql.append("   b.localcreditamount, ");//本币贷方发生额
		sql.append("   b.nov vreserve3,");//凭证编码
		sql.append("   b.pk_accsubj ,");//科目主键
		sql.append("   b.checkno ,");//票据编码
		sql.append("   b.checkdate ,");//票据日期
		sql.append("   b.assid ,");//辅助核算
		sql.append("   b.explanation ,");//摘要内容
		sql.append("   b.pk_currtype ");//币种	
		sql.append("  from  ");
		sql.append("  gl_voucher h ");
		sql.append("  join gl_detail b ");
		sql.append("  on h.pk_voucher=b.pk_voucher ");
		sql.append("  where isnull(h.dr,0)=0");
		sql.append("  and isnull(b.dr,0)=0 ");
		AccountCalendar ac=AccountCalendar.getInstanceByAccperiodMonth(period);	
		sql.append("  and h.period='"+	ac.getMonthVO().getMonth()+"'");
		///sql.append("  and b.localdebitamount > 0 ");
		sql.append("  and b.direction='D'");//借贷 方向为借方
		sql.append("  and h.year='"+ac.getYearVO().getPeriodyear()+"'");
		sql.append("  and pk_checked is not null ");//审核通过的
		sql.append("  and h.pk_glbook='"+pk_glbook+"'");
		sql.append("  and h.pk_corp='"+pk_corp+"' ");
		sql.append("  and b.pk_accsubj in (select pk_accsubj  from bd_accsubj where subjcode like '"+accscode+"%'  )");		
		return sql.toString();
	}
	/**
	 * 按工作中心维度 汇总成本
	 * @param dealvos
	 * @return
	 */
	public SumDealVO[] combinDealVO(SumDealVO[] dealvos){
		if(dealvos==null || dealvos.length==0)
			return null;
		SumDealVO[] dvos=(SumDealVO[]) CombinVO.combinData(dealvos, SumDealVO.combinConds,SumDealVO.combinFields , SumDealVO.class);		
		return dvos;
	}
    /**
     * 根据工作中信息 构建查询条件
     * @throws Exception 
     */
	public String getWorkCenterSql(SuperVO sourcevo,String[] sworkcenters,String[] dworkcenters) throws Exception{
		if(sourcevo==null ){
			throw new Exception("来源数据为空");
		}
		if(sworkcenters==null || sworkcenters.length==0 || dworkcenters==null || dworkcenters.length==0){
			throw new Exception("来源或目的工作中心 不能为空");
		}	
		if(sworkcenters.length !=dworkcenters.length){
			throw new Exception("来源和目的工作中定义元素个数不一致");
		}
		String wsql=new String();
		for(int i=0;i<sworkcenters.length;i++){
			String cpk=PuPubVO.getString_TrimZeroLenAsNull(sourcevo.getAttributeValue(sworkcenters[i]));
			if(cpk!=null){
				if(dworkcenters[i].equals("h.pk_factory")){
					cpk=getFactoryByDeptdoc(cpk);
				}
				wsql=wsql+"  and "+dworkcenters[i]+" = '"+cpk+"'";
			}else{
				wsql=wsql+"  and "+dworkcenters[i]+" is null ";
			}
		}	
		return wsql;
	}
	/**
	 * 根据部门从选厂档案找到对应选厂
	 * @param cpk
	 * @return
	 * @throws DAOException 
	 */
	public String getFactoryByDeptdoc(String cpk) throws DAOException {
		String sql=" select pk_factory from xcgl_factory where pk_deptdoc='"+cpk+"' and isnull(dr,0)=0";
		String str=PuPubVO.getString_TrimZeroLenAsNull(getDao().executeQuery(sql, new ColumnProcessor()));		
		return str;
	}
	/**
	 * 根据日期获得会计月
	 * @param date
	 * @return
	 * @throws InvalidAccperiodExcetion 
	 */
	public AccperiodmonthVO getMonthVOByDate(UFDate date,String pk_accountbook) throws InvalidAccperiodExcetion{
		if(date==null || date.toString().length()==0)
			return null;
		AccountCalendar ac=AccountCalendar.getInstance();
		if(pk_accountbook!=null && pk_accountbook.length()>0)
			ac=AccountCalendar.getInstanceByGlbook(pk_accountbook);
		ac.setDate(date);
		AccperiodmonthVO mothvo=ac.getMonthVO();
		return mothvo;
	}
	
	/**
	 * 根据 wsql 查询工程验收单
	 * @param wsql
	 * @return
	 */
	public String getQuerySqlAcc(String wsql) {
		StringBuffer sql=new StringBuffer();
		sql.append(" select  ");
		sql.append("   h.*,");//公司
		sql.append("   b.*");
		sql.append(" from xcgl_general_h h ");
		sql.append(" join xcgl_general_b b ");
		sql.append(" on h.pk_general_h = b.pk_general_h ");
		sql.append(" where isnull(h.dr,0)=0 ");
		sql.append(" and isnull(b.dr,0)=0 ");
		sql.append(" and h.vbillstatus=1");
		sql.append(" and h.pk_billtype='"+PubBillTypeConst.billtype_Generalout+"'");
		sql.append(wsql);		
		return sql.toString();
	}
	/**
	 * 根据 wsql 查询核算单
	 * @param wsql
	 * @return
	 */
	public String getQueryAccountSql(String wsql) {
		StringBuffer sql=new StringBuffer();
		String[] workces=CostAccountVO.workcenters;
		sql.append(" select  ");
		sql.append(" h.pk_corp ,");//公司
		sql.append(" h.pk_costaccount ,");
		sql.append(" h.vbillstatus,");//单据状态
		sql.append(" h.pk_accoutbook ,");
		sql.append(" h.pk_deptdoc ,");
		sql.append(" h.pk_minearea ,");
		if(workces!=null || workces.length>0){
			for(int i=0;i<workces.length;i++){
			  sql.append(" "+workces[i]+" ,");
			}
		}
        sql.append("  b.worknum,");
        sql.append("  b.pk_project,");
        sql.append("  b.pk_jobbasfil,");
        sql.append("  b.nmy,");
        sql.append("  b.pk_costelement,");
        sql.append("  b.pk_cumandoc,");
        sql.append("  b.pk_cubasdoc,");
        sql.append("  b.pk_invmandoc,");
        sql.append("  b.pk_invbasdoc");
		sql.append(" from xccb_costaccount h ");
		sql.append(" join xccb_costaccount_b b ");
		sql.append(" on h.pk_costaccount = b.pk_costaccount ");
		sql.append(" where isnull(h.dr,0)=0 ");
		sql.append(" and isnull(b.dr,0)=0 ");
		if(wsql!=null&&wsql.length()>0)
		sql.append(wsql);		
		return sql.toString();
	}
	
	
	public String getProCodeByPk(String pk_basid) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("jobcode->getColValue(bd_jobbasfil,jobcode,pk_jobbasfil,pk_jobbasfil)", 
				new String[]{"pk_jobbasfil"}, new String[]{pk_basid}));
	}
	
	public String getProSerMonthCodeByPk(String pk_basid) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("jobcode->getColValue(bd_jobbasfil,def10,pk_jobbasfil,pk_jobbasfil)", 
				new String[]{"pk_jobbasfil"}, new String[]{pk_basid}));
	}
	
	public String getProSerMonthCodeByPkClient(String pk_basid) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomularClient("jobcode->getColValue(bd_jobbasfil,def10,pk_jobbasfil,pk_jobbasfil)", 
				new String[]{"pk_jobbasfil"}, new String[]{pk_basid}));
	}
	
	
	public String getProPkByCode(String jobcode) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("jobcode->getColValue(bd_jobbasfil,pk_jobbasfil,jobcode,jobcode)", 
				new String[]{"jobcode"}, new String[]{jobcode}));
	}
	
	public String getProNameByPk(String pk_basid) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("jobname->getColValue(bd_jobbasfil,jobname,pk_jobbasfil,pk_jobbasfil)", 
				new String[]{"pk_jobbasfil"}, new String[]{pk_basid}));
	}
	/**
	 * 根据工程类别主键获得工程类别编码
	 * @param pk_procl
	 * @return
	 * @throws BusinessException 
	 */
	public String getProclCodeBypPk(String pk_procl) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("vcode->getColValue(xew_workscategory,vcode,pk_workscategory ,pk_workscategory )", 
				new String[]{"pk_workscategory"}, new String[]{pk_procl}));
	}
	public String getProclNameBypPk(String pk_procl) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("vcode->getColValue(xew_workscategory,vname,pk_workscategory ,pk_workscategory )", 
				new String[]{"pk_workscategory"}, new String[]{pk_procl}));
	}
	/**
	 * 根据存货档案主键获得单价
	 * @param pk_invmandoc
	 * @return
	 * @throws BusinessException 
	 */
	public String getInvPriceBypPk(String pk_invmandoc) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("price->getColValue(bd_invmandoc,costprice, pk_invmandoc, pk_invmandoc )", 
				new String[]{"pk_invmandoc"}, new String[]{pk_invmandoc}));
	}
	public String getInvcodeByPk(String pk_invbasdoc) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("invcode->getColValue(bd_invbasdoc,invcode, pk_invbasdoc , pk_invbasdoc  )", 
				new String[]{"pk_invbasdoc "}, new String[]{pk_invbasdoc}));
	}
	public String getInvNameByPk(String pk_invbasdoc) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("invname->getColValue(bd_invbasdoc,invname, pk_invbasdoc , pk_invbasdoc  )", 
				new String[]{"pk_invbasdoc "}, new String[]{pk_invbasdoc}));
	}
	public String getBookCodeByPk(String pk_accountbook) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("code->getColValue(bd_glbook,code,pk_glbook,pk_glbook)", 
				new String[]{"pk_glbook "}, new String[]{pk_accountbook}));
	}
	public String getBookNameByPk(String pk_accountbook) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("name->getColValue(bd_glbook,name,pk_glbook,pk_glbook)", 
				new String[]{"pk_glbook "}, new String[]{pk_accountbook}));
	}
	public String getPkinvclBypPk(String pk_invmandoc) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("pk_invcl->getColValue(bd_invbasdoc,pk_invcl, pk_invbasdoc, pk_invmandoc )", 
				new String[]{"pk_invmandoc"}, new String[]{pk_invmandoc}));
	}
	/**
	 * 将 sour的属性  复制到  dest中 
	 * @param sour
	 * @param dest
	 * @param sid //追加标示符
	 */
	public void BeanAttrCopy(CircularlyAccessibleValueObject sour, CircularlyAccessibleValueObject dest,String sid) {
		if(sour==null || dest==null)
			return;
		String[] attrnames=sour.getAttributeNames();
		if(attrnames==null || attrnames.length==0)
			return;
		for(int i=0;i<attrnames.length;i++){
			if(sid==null || sid.length()==0){
			    dest.setAttributeValue(attrnames[i],sour.getAttributeValue(attrnames[i]));	
			}else{
				dest.setAttributeValue(attrnames[i]+sid,sour.getAttributeValue(attrnames[i]));	
			}
		}
	}
	
    /**
     * 查看两个成本要素是否存在交叉
     * @param pk_costelement1
     * @param pk_costelement2
     * @return
     * @throws BusinessException 
     */
	public boolean isCostelementCross(String pk_costelement1, String pk_costelement2) throws BusinessException {
		String invclcode=getCostCodeByPk(pk_costelement1);
		String invclcode1=getCostCodeByPk(pk_costelement2);
		if(invclcode.startsWith(invclcode1)){
			return true;
		}
		if(invclcode1.startsWith(invclcode)){
		    return true;	
		}
		return false;
	}
	/**
	 * 根据会计账簿查询固定资产和总账接口
	 * @param pk_billtype
	 * @param vlastbillid
	 * @return
	 * @throws DAOException 
	 */
	public List queryDataFitering(String pk_accountbook,String pk_corp) throws DAOException{
//		String sql=" select b.* from XEW_DATAFILTERING b where isnull(b.dr,0)=0 " +
//				" and  b.pk_accoutbook='"+pk_accountbook+"' and b.pk_corp='"+pk_corp+"'";
//		List list=(List) getDao().executeQuery(sql, new BeanListProcessor(DataFilteringVO.class));
//		return list;
		return null;
	}
	
    /**
     * 查看两个工程类别是否存在交叉
     * @param pk_workscategory1
     * @param pk_workscategory2
     * @return
     * @throws BusinessException 
     */
	public boolean isWorksCategoryCross(String pk_workscategory1, String pk_workscategory2) throws BusinessException {
		String invclcode=getProclCodeBypPk(pk_workscategory1);
		String invclcode1=getProclCodeBypPk(pk_workscategory2);
		if(invclcode.startsWith(invclcode1)){
			return true;
		}
		if(invclcode1.startsWith(invclcode)){
		    return true;	
		}
		return false;
	}
	/**
	 * 根据项目类型编码获得项目类型主键
	 * @param protype
	 * @return
	 * @throws BusinessException 
	 */
	public String getpk_protypeByCode(String protype) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("pk_jobtype->getColValue(bd_jobtype,pk_jobtype,jobtypecode ,jobtypecode )", 
				new String[]{"jobtypecode"}, new String[]{protype}));
	}
	/**
	 * 根据项目类型编码获得项目类型主键
	 * @param protype
	 * @return
	 * @throws BusinessException 
	 */
	public String getpk_protypeByCodeClient(String protype) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomularClient("pk_jobtype->getColValue(bd_jobtype,pk_jobtype,jobtypecode ,jobtypecode )", 
				new String[]{"jobtypecode"}, new String[]{protype}));
	}
	
	/**
	 * 根据项目类型编码获得项目类型编码
	 * @param protype
	 * @return
	 * @throws BusinessException 
	 */
	public String getjobtypenameByCode(String protype) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("pk_jobtype->getColValue(bd_jobtype,jobtypename,jobtypecode ,jobtypecode )", 
				new String[]{"jobtypecode"}, new String[]{protype}));
	}
	/**
	 * 根据项目类型编码获得项目类型编码规则
	 * @param protype
	 * @return
	 * @throws BusinessException 
	 */
	public String getjobtypeCdgzByCode(String protype) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("jobclclass ->getColValue(bd_jobtype,jobclclass,jobtypecode ,jobtypecode )", 
				new String[]{"jobtypecode"}, new String[]{protype}));
	}
	/**
	 * 根据项目类型编码获得项目类型编码规则
	 * @param protype
	 * @return
	 * @throws BusinessException 
	 */
	public String getjobtypeCdgzByCodeClient(String protype) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomularClient("jobclclass ->getColValue(bd_jobtype,jobclclass,jobtypecode ,jobtypecode )", 
				new String[]{"jobtypecode"}, new String[]{protype}));
	}
	/**
	 * 根据项目类型编码获得项目类型编码
	 * @param protype
	 * @return
	 * @throws BusinessException 
	 */
	public String getjobtypenameByCodeClient(String protype) throws BusinessException {
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomularClient("pk_jobtype->getColValue(bd_jobtype,jobtypename,jobtypecode ,jobtypecode )", 
				new String[]{"jobtypecode"}, new String[]{protype}));
	}
	
	
	
	
	
	
	
	
	
	
	
	

}
