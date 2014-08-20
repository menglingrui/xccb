package nc.bs.xccb.costaccount3;
import java.util.ArrayList;
import java.util.List;

import nc.bd.accperiod.AccountCalendar;
import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.framework.common.NCLocator;
import nc.bs.trade.business.HYPubBO;
import nc.bs.zmpub.formula.calc.DoCalc;
import nc.bs.zmpub.pub.tool.SingleVOChangeDataBsTool;
import nc.itf.uap.pf.IPFBusiAction;
import nc.jdbc.framework.processor.ColumnProcessor;
import nc.jdbc.framework.util.SQLHelper;
import nc.ui.scm.util.ObjectUtils;
import nc.vo.bd.period2.AccperiodmonthVO;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDouble;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.trade.pub.IBillStatus;
import nc.vo.xccb.costaccount.AggCostAccountVO;
import nc.vo.xccb.costaccount.CostAccountVO;
import nc.vo.xccb.costaccount.CostAccoutBVO;
import nc.vo.xccb.costelement.CostelementB1VO;
import nc.vo.xccb.pub.XewcbPubTool;
import nc.vo.xccb.pub.Xewcbpubconst;
import nc.vo.xcgl.genprcessout.GenPrcOutBVO;
import nc.vo.xcgl.genprcessout.GenPrcOutHVO;
import nc.vo.zmpub.pub.report.ReportBaseVO;
import nc.vo.zmpub.pub.tool.ZmPubTool;
/**
 * 公用成本分配处理 后台工作类
 * @author mlr
 */
public class PubCostBO {
    /**
     * 设置
     */
	public int scale=4;
	private BaseDAO dao = null;
	
	public BaseDAO getDao() {
		if (dao == null) {
			dao = new BaseDAO();
		}
		return dao;
	}

	private XewcbPubTool tool;

	public XewcbPubTool getTool() {
		if (tool == null) {
			tool = new XewcbPubTool();
		}
		return tool;
	}
	private static DoCalc ftool=null;
	private static DoCalc getExecFomularTool(){
		if(ftool==null){
			ftool=new DoCalc();
		}
		return ftool;
	}
	/**
	 * 取消成本分配
	 * @throws Exception 
	 * @param infor  infor(0)=当前日期  infor(1)=当前操作员  infor(3)=公司
	 */
	public CostAccoutBVO[] doCancelAllo(CostAccountVO vo,CostAccoutBVO[] vos,String[] infor)throws Exception{
		if(vos==null || vos.length==0)
			throw new Exception("数据为空");
		if(infor==null || infor.length==0)
			throw new Exception("登陆信息为空");
		if(vo==null)
			throw new Exception("表头信息为空");	
		for(int i=0;i<vos.length;i++){
			String wsql=" 1=1 ";
			CostAccoutBVO bvo=vos[i];
			wsql=wsql+" and vlastbillrowid='"+bvo.getPrimaryKey()+"' and isnull(dr,0)=0 ";
			List list=(List) getDao().retrieveByClause(CostAccoutBVO.class, wsql);
			if(list==null|| list.size()==0){
				continue;
			}
			//查看是否是自由态
			for(int j=0;j<list.size();j++){
				CostAccoutBVO nvo=(CostAccoutBVO) list.get(j);
				Integer vbillstate=getVbillState(nvo);
				if(vbillstate.intValue()!=IBillStatus.FREE){
					String billno=getVbillNo(nvo);
					throw new Exception("下游单据 ，单据号为["+billno+"] 单据已经审批或提交");
				}
			}
			//设置下游删除标志
			for(int j=0;j<list.size();j++){
				CostAccoutBVO nvo=(CostAccoutBVO) list.get(j);
				String sql1=" update xccb_costaccount_b set xccb_costaccount_b.dr=1 where xccb_costaccount_b.pk_costaccount_b='"+nvo.getPrimaryKey()+"'";
				//执行删除下游表体操作
				getDao().executeUpdate(sql1);
		
			}
			//查看是否存在表体为空的下游单据 进行删除
			CostAccoutBVO[] nbvos=(CostAccoutBVO[]) list.toArray(new CostAccoutBVO[0]);
			for(int j=0;j<nbvos.length;j++){
				CostAccoutBVO bvo1=nbvos[j];
				String sql=" select count(*) from xccb_costaccount_b h where isnull(h.dr,0)=0 and h.pk_costaccount='"+bvo1.getPk_costaccount()+"'";
				Integer count=PuPubVO.getInteger_NullAs(getDao().executeQuery(sql, new ColumnProcessor()), -1);
				if(count==0){
					String sql1=" update xccb_costaccount set xccb_costaccount.dr=1 where xccb_costaccount.pk_costaccount='"+bvo1.getPk_costaccount()+"'";
					getDao().executeUpdate(sql1);
				}
			}
		}	
		/**
		 * 设置分配标志
		 */
		for(int i=0;i<vos.length;i++){
			vos[i].setUreserve1(new UFBoolean(false));
		}
		getDao().updateVOArray(vos);
		return vos;
	}
	/**
	 * 取得单据编号
	 * @param nvo
	 * @return
	 * @throws BusinessException 
	 */
	public String getVbillNo(CostAccoutBVO nvo) throws BusinessException {
		if(nvo==null)
			return null;
		return PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular(
				"vbillno->getColValue(xccb_costaccount,vbillno,pk_costaccount,pk_costaccount)",
				new String[]{"pk_costaccount"}, 
				new String[]{nvo.getPk_costaccount()}));
	}
	/**
	 * 取得表头单据状态
	 * @param nvo
	 * @return
	 * @throws BusinessException 
	 */
	public Integer getVbillState(CostAccoutBVO nvo) throws BusinessException {
		if(nvo==null)
			return null;
		return PuPubVO.getInteger_NullAs((ZmPubTool.execFomular(
				"vbillstatus->getColValue(xccb_costaccount,vbillstatus,pk_costaccount,pk_costaccount)",
				new String[]{"pk_costaccount"}, 
				new String[]{nvo.getPk_costaccount()})), -1);
	}
	/**
	 * 成本分配
	 * @throws Exception 
	 * @param infor  infor(0)=当前日期  infor(1)=当前操作员  infor(3)=公司
	 */
	public CostAccoutBVO[] doCostAllo(CostAccountVO vo,CostAccoutBVO[] vos,String[] infor) throws Exception{
		if(vo==null )
			throw new Exception("待分配数据 表头为空");
        if(vos==null || vos.length==0){
        	throw new Exception("待分配数据的表体为空");
        }
    	   /**
         * 会计账簿
         */
        String pk_accoutbook=vo.getPk_accoutbook();
        /**
         * 取得成本核算vo的工作中心
         */
        String[] costworkcenters=CostAccountVO.workcenters1; 
        /**
         * 取得 ore mine vo的工作中心
         */
        String[] accworcenters=GenPrcOutHVO.workcenters;
        /**
         * 存放分摊后的成本
         */
        List<ReportBaseVO> alllist=new ArrayList<ReportBaseVO>();
    
        for(int i=0;i<vos.length;i++){   
            /**
             * 查询的工程验收单
             */
            List<ReportBaseVO> provos=new ArrayList<ReportBaseVO>();
        	String wsql=getWhereSql(vo,costworkcenters,accworcenters);         
        	/**
    		 * 取得成本动因
    		 */
    		List divs=(List) getTool().getDao().retrieveByClause(CostelementB1VO.class, " pk_costelement = '"+vos[i].getPk_costelement()+"' and isnull(dr,0)=0");
    		if(divs==null || divs.size()==0){
    			String costcode=getTool().getCostCodeByPk(vos[i].getPk_costelement());
    			String costname=getTool().getCostNameByPk(vos[i].getPk_costelement());
    			throw new BusinessException("成本要素编码为:['"+costcode+"'] ,名称为:['"+costname+"'] 的成本要素 没有定义成本动因");
    		}
    		for(int n=0;n<divs.size();n++){
    			CostelementB1VO cvo=(CostelementB1VO) divs.get(n);
    			String pk_procl=cvo.getPk_projectcl();
    			String swsql="";
    			if(pk_procl==null || pk_procl.length()==0){
    				swsql=wsql;
    			}else{
    			   String proclcode=getTool().getProclCodeBypPk(pk_procl);
    			   swsql=wsql+" and  b.pk_procl in ( select pk_workscategory from xew_workscategory h where h.vcode like '"+proclcode+"%')";  
    			}
    			 /**
                 * 得到查询验收单sql
                 */
                String sql=getTool().getQuerySqlAcc(swsql);
                ReportBaseVO[] prvos=getTool().getDMO().queryVOBySql(sql);
                if(prvos!=null || prvos.length>0){
                	for(int f=0;f<prvos.length;f++){
                		provos.add(prvos[f]);
                	}
                }
    		}        
            /**
             * 分配成本
             */
        	ReportBaseVO[] nvos=costAllo(divs,vos[i],(ReportBaseVO[])ObjectUtils.serializableClone(provos.toArray(new ReportBaseVO[0])));
        	if(nvos==null || nvos.length==0){
        		continue;
        	}
        	setCostElement(vos[i].getPk_costelement(),nvos);
        	//setClinfor(nvos);
        	setSourceInfor(vo,vos[i],nvos);
        	for(int j=0;j<nvos.length;j++){
        		alllist.add(nvos[j]);
        	}       	
        }  
        /**
         * 将分配后的成本转化为数组
         */
        ReportBaseVO[] costvos=alllist.toArray(new ReportBaseVO[0]);
        setAccountBook(pk_accoutbook,costvos);
        deal(vo,costvos,infor);
        setAlloID(vos);
		return vos;
	}
	private void setClinfor(CostAccoutBVO[] vos) throws BusinessException {
		if(vos!=null&&vos.length>0){
			for(int i=0;i<vos.length;i++){
				CostAccoutBVO vo=vos[i];
				String  pk_invbasdoc=vo.getPk_invbasdoc();//作业
				String  pK_clbasdoc=vo.getVreserve3();//材料
				String  pk_pro=vo.getPk_jobbasfil();//工程
				String  pk_cl=getTool().getPkinvclBypPk(pk_invbasdoc);
				String  pk_cl1=getTool().getPkinvclBypPk(pK_clbasdoc);
				String  pk_procl=getTool().getProClPkByProBasPk(pk_pro);
				vo.setPk_defdoc3(pk_cl);
				vo.setPk_defdoc4(pk_cl1);
				vo.setPk_defdoc5(pk_procl);			
			}
		}
	}
	private String getWhereSql(CostAccountVO vo, String[] costworkcenters, String[] accworcenters) throws Exception {
		  /**
         * 利用工作中心构建 过滤条件sql
         */
        String wsql=getTool().getWorkCenterSql(vo, costworkcenters, accworcenters);      
        /**
         * 补填过滤条件
         */
        wsql=wsql+" and h.pk_corp='"+vo.getPk_corp()+"' ";
        UFDate dbilldate=vo.getDbilldate();
        AccountCalendar ca=AccountCalendar.getInstance();
        ca.setDate(dbilldate);
        AccperiodmonthVO mon=ca.getMonthVO();
        String sdate=mon.getBegindate().toString();
        String edate=mon.getEnddate().toString();
        wsql=wsql+" and h.dbilldate >= '"+sdate+"'";
        wsql=wsql+" and h.dbilldate <= '"+edate+"'";  
        return wsql;
	}
	/**
	 * 设置来源信息
	 * @param vo 
	 * @param costAccoutBVO
	 * @param nvos
	 */
	public void setSourceInfor(CostAccountVO vo, CostAccoutBVO cvo, ReportBaseVO[] nvos) {
	   if(cvo==null){
		   return;
	   }
	   if(nvos==null|| nvos.length==0){
		   return;
	   }
	   for(int i=0;i<nvos.length;i++){
		   nvos[i].setAttributeValue("pk_costaccount", cvo.getPk_costaccount());
		   nvos[i].setAttributeValue("pk_costaccount_b", cvo.getPk_costaccount_b());
		   nvos[i].setAttributeValue("vbillno", vo.getVbillno());
	   }
	}
	/**
	 * 设置成本要素
	 * @param pk_costelement
	 * @param nvos
	 */
	public void setCostElement(String pk_costelement, ReportBaseVO[] nvos) {
		if(nvos==null || nvos.length==0)
			return;
		for(int i=0;i<nvos.length;i++){
			nvos[i].setAttributeValue("pk_costelement", pk_costelement);
		}
		
	}
	/**
	 * 设置会计账簿
	 * @param pk_accoutbook
	 * @param costvos
	 */
	public void setAccountBook(String pk_accoutbook, ReportBaseVO[] costvos) {
		if(costvos==null|| costvos.length==0)
			return;
		for(int i=0;i<costvos.length;i++){
			costvos[i].setAttributeValue("pk_accoutbook", pk_accoutbook);
		}
		
	}
	/**
	 * 设置成本是否已经分配标志
	 * @param vos
	 * @throws DAOException 
	 */
	public void setAlloID(CostAccoutBVO[] vos) throws DAOException {
		if(vos==null || vos.length==0)
			return;
		for(int i=0;i<vos.length;i++){
			vos[i].setUreserve1(new UFBoolean(true));
		}
		getDao().updateVOArray(vos);
	}
	/**
	 * 分配后的成本处理  生成分摊成本核算单
	 * @param vo 
	 * @param costvos
	 * @param  infor(0)=当前日期  infor(1)=当前操作员  infor(3)=公司
	 * @throws Exception 
	 */
	public void deal(CostAccountVO vo, ReportBaseVO[] costvos, String[] infor) throws Exception {
		if(vo==null )
			return;
		if(costvos==null || costvos.length==0)
			return ;
		AggCostAccountVO  nbillvo=new AggCostAccountVO();
		
		CostAccountVO[] headvos=(CostAccountVO[]) SingleVOChangeDataBsTool.runChangeVOAry(new CostAccountVO[]{vo}, CostAccountVO.class, "nc.bs.pf.changedir.CHGCB04TOCB05headVO");
		CostAccoutBVO[] bodycostvos=(CostAccoutBVO[]) SingleVOChangeDataBsTool.runChangeVOAry(costvos, CostAccoutBVO.class, "nc.bs.pf.changedir.CHGCB04TOCB05bodyVO");		
		nbillvo=new AggCostAccountVO();
		HYPubBO bo=new HYPubBO();
		String billno=bo.getBillNo(Xewcbpubconst.bill_code_costaccount4, (String) headvos[0].getAttributeValue("pk_corp"), null, null);
		headvos[0].setAttributeValue("vbillno", billno);
		headvos[0].setAttributeValue("voperatorid", infor[1]);
		headvos[0].setAttributeValue("dbilldate", new UFDate(infor[0]));
		headvos[0].setAttributeValue("dmakedate", new UFDate(infor[0]));
		nbillvo.setParentVO(headvos[0]);
		setClinfor(bodycostvos);
		nbillvo.setChildrenVO(bodycostvos);	
		setSerMonth(new AggCostAccountVO[]{nbillvo});
		saveCost(new AggCostAccountVO[]{nbillvo});
	}
	/**
	 * 设置服务月限
	 * @param orderVos
	 * @throws BusinessException 
	 */
	private void setSerMonth(AggregatedValueObject[] orderVos) throws BusinessException {
		if(orderVos==null||orderVos.length==0)
			return;
		String defname=Xewcbpubconst.defsername;
		String pk_def=PuPubVO.getString_TrimZeroLenAsNull(ZmPubTool.execFomular("pk_defdoclist->getColValue(bd_defdoclist,pk_defdoclist,doclistname,doclistname)",
				new String[]{"doclistname"}, new String[]{defname}));
		if(pk_def==null){
			throw new BusinessException("[服务月限] 自定义项档案没有建立");
		}
		
		String ser1_pk=PuPubVO.getString_TrimZeroLenAsNull(
				getDao().executeQuery("select pk_defdoc from bd_defdoc d where d.pk_defdoclist='"+pk_def+"' and d.doccode='ser1' ", new ColumnProcessor()));
		String ser2_pk=PuPubVO.getString_TrimZeroLenAsNull(
				getDao().executeQuery("select pk_defdoc from bd_defdoc d where d.pk_defdoclist='"+pk_def+"' and d.doccode='ser2' ", new ColumnProcessor()));
	    if(ser1_pk==null){
	    	throw new BusinessException("[服务月限] 自定义项档案,档案内容中档案编码必须用[ser1] 代表[一年及一年一下]");   
	    }
	    if(ser2_pk==null){
	    	throw new BusinessException("[服务月限] 自定义项档案,档案内容中档案编码必须用[ser2] 代表[一年以上]");   
	    }		
		for(int i=0;i<orderVos.length;i++){
		    if(orderVos[i]!=null&&orderVos[i].getChildrenVO()!=null&&orderVos[i].getChildrenVO().length>0){
		    	CostAccoutBVO[]  vos=(CostAccoutBVO[]) orderVos[i].getChildrenVO();
		    	if(vos!=null&&vos.length>0){
		    		for(int j=0;j<vos.length;j++){
		    		 //  	Integer sermonth=PuPubVO.getInteger_NullAs(vos[j].getPk_defdoc2(), 0);
		    		 	String  pk_baspr=PuPubVO.getString_TrimZeroLenAsNull(vos[j].getPk_jobbasfil()); 
		    		 	UFDouble sermonth=PuPubVO.getUFDouble_NullAsZero(getTool().getProSerMonthCodeByPk(pk_baspr));
		    		
				    	if(sermonth.doubleValue()<=12){
				    		vos[j].setPk_defdoc1(ser1_pk);
				    	}else{
				    		vos[j].setPk_defdoc1(ser2_pk);
				    	}
		    		}
		    	}	    	
		    }			
		}
	}
	/**
	 * 调用分摊成本核算单保存脚本
	 * @param accbillvos
	 * @throws BusinessException 
	 */
    public void saveCost(AggCostAccountVO[] accbillvos ) throws BusinessException {    	
    	if(accbillvos==null || accbillvos.length==0)
    		return;
    	UFDate dbilldate=PuPubVO.getUFDate(accbillvos[0].getParentVO().getAttributeValue("dmakedate"));
		IPFBusiAction bsBusiAction = (IPFBusiAction) NCLocator.getInstance().lookup(IPFBusiAction.class.getName());
		for(int i=0;i<accbillvos.length;i++){
			bsBusiAction.processAction("WRITE",Xewcbpubconst.bill_code_costaccount4,dbilldate.toString(),null,accbillvos[i], null,null);	
		}	
	}
    /**
	 * 处理分摊成本核算单vo   
	 * 
	 * 查看 当前会计期间 同一工作中心 是否已经存在公用成本核算单
	 *
	 * @param accbillvos
	 * @throws Exception 
	 */
	public void dealConstVO(AggCostAccountVO[] accbillvos) throws Exception {
		if(accbillvos==null || accbillvos.length==0)
			return;
		for(int i=0;i<accbillvos.length;i++){
			AggCostAccountVO billvo=accbillvos[i];
			CostAccountVO hvo=(CostAccountVO) billvo.getParentVO();
			CostAccoutBVO[] bvos=(CostAccoutBVO[]) billvo.getChildrenVO();
		    String[] costworkcenters=CostAccountVO.workcenters1;
		    String wsql=" 1=1 ";
	        /**
	         * 利用工作中心构建 过滤条件sql
	         */
	         wsql=wsql + getTool().getWorkCenterSql(hvo, costworkcenters,costworkcenters);	
				wsql=wsql+" and pk_corp='"+hvo.getPk_corp()+"'";
				wsql=wsql+" and isnull(dr,0)=0";
				AccperiodmonthVO mothvo=getTool().getMonthVOByDate(hvo.getDbilldate(),hvo.getPk_accoutbook());
				if(mothvo==null){
				   throw new BusinessException("会计月为空");	
				}
				String sdate=mothvo.getBegindate().toString();
				String edate=mothvo.getEnddate().toString();
				wsql =wsql+"  and dbilldate >='"+sdate+"'";
				wsql =wsql+"  and dbilldate <='"+edate+"'";
				wsql=wsql+" and pk_billtype='"+Xewcbpubconst.bill_code_costaccount4+"'";
		    List	list=(List) getDao().retrieveByClause(CostAccountVO.class, wsql);
		    if(list==null || list.size()==0)
		    	continue;
		    CostAccountVO  ohvo=(CostAccountVO) list.get(0);
		    if(ohvo.getVbillstatus()!=IBillStatus.FREE){
		        continue;
		    }
		    accbillvos[i].setParentVO(ohvo);
		    String pk_h=ohvo.getPrimaryKey();
		    List list1=(List) getDao().retrieveByClause(CostAccoutBVO.class, " pk_costaccount ='"+pk_h+"' and isnull(dr,0)=0 ");
		    List<CostAccoutBVO> zlist=new ArrayList<CostAccoutBVO>();
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
		    			bvo.setPk_costaccount(ovo.getPk_costaccount());
		    			bvo.setStatus(VOStatus.NEW);
		    			zlist.add(bvo);
		    		}
		    	}
		    }
		    accbillvos[i].setChildrenVO((CircularlyAccessibleValueObject[]) zlist.toArray(new CostAccoutBVO[0]));				
		}		
	}
	/**
	 * 跟据成本金额 分配 成本  分配依据为 成本要素设置的成本动因
	 * @param costAccoutBVO
	 * @param avos
	 * @throws Exception 
	 */
	public ReportBaseVO[] costAllo(List divs,CostAccoutBVO costvo, ReportBaseVO[] avos) throws Exception {	
		CostelementB1VO dvo=(CostelementB1VO) divs.get(0);
		String diver=dvo.getCostdriver();//取得成本动因
		avos=calDriverValue(diver,avos,dvo);
		UFDouble drviersum=calDriverSum(avos);
		costAllo(costvo.getNmy(),drviersum,avos);
		return avos;
	}
	/**
	 * 根据成本 动因量和  进行成本分配
	 * 分配算法：
	 * 分配金额=成本分配金额(allonmy)*成本动因(工程验收vo中定义的成本动因:costdrivervale)/成本动因量和(drviersum)
	 * 
	 * 最后一个 分配： 成本分配金额(allonmy)-已分配金额
	 * 
	 * @param drviersum
	 * @param avos
	 * @throws BusinessException 
	 */
	public void costAllo(UFDouble allonmysum,UFDouble drviersum, ReportBaseVO[] avos) throws BusinessException {
		if(allonmysum==null || allonmysum.isTrimZero()){
			throw new BusinessException("分配金额为0 不用分配");
		}
		if(drviersum==null || drviersum.isTrimZero()){
			throw new BusinessException("成本动因和为0 无法分配");
		}
		if(avos==null || avos.length==0)
			throw new BusinessException("分摊的作业为空");
		String dvname=GenPrcOutBVO.costdrivervale;
		String nmyname=GenPrcOutBVO.costallonmy;
		/**
		 * 存放已经分配的金额
		 */
		UFDouble allnmysum1=new UFDouble();
		for(int i=0;i<avos.length;i++){
			ReportBaseVO zvo=avos[i];
			//取得动因
			UFDouble  dv=PuPubVO.getUFDouble_NullAsZero(zvo.getAttributeValue(dvname));
			//计算动因占用的百分比
			UFDouble pervlaue=dv.div(drviersum);
			//计算分配金额
			UFDouble allonmy=allonmysum.multiply(pervlaue);
			//取得进位的位数
			int denum=4;
			String delnum=XewcbPubTool.getDefaultNum(SQLHelper.getCorpPk());
			if(delnum!=null&&delnum.length()>0){
				denum=Integer.parseInt(delnum);
			}                                                                                                             
			allonmy=allonmy.setScale(denum, UFDouble.ROUND_UP);	
			zvo.setAttributeValue(nmyname, allonmy);
			if(i==avos.length-1){
				allonmy=allonmysum.sub(allnmysum1);
				zvo.setAttributeValue(nmyname, allonmy);
			}else{
				allnmysum1=allnmysum1.add(allonmy);
			}
		}		
	}
	/**
	 * 计算成本动因值的和
	 * @param avos
	 * @return
	 * @throws BusinessException 
	 */
	public UFDouble calDriverSum(ReportBaseVO[] avos) throws BusinessException {
		if(avos==null || avos.length==0)
			throw new BusinessException("分摊的作业为空");
		UFDouble dsum=new UFDouble(0.0);
		String field=GenPrcOutBVO.costdrivervale;
		for(int i=0;i<avos.length;i++){
			UFDouble dv=PuPubVO.getUFDouble_NullAsZero(avos[i].getAttributeValue(field));
			dsum=dsum.add(dv);
		}
		return dsum;
	}
	/**
	 *根据成本动因 计算动因值
	 * @param vo 成本要素中成本动因配置vo
	 * @param diver 计算后的成本动因值
	 * @param avos 业务数据
	 * @throws Exception 
	 */
	public ReportBaseVO[] calDriverValue(String diver, ReportBaseVO[] avos,CostelementB1VO vo) throws Exception {
		/**
		 * 这里先用工作量  因为目前没有加上 公式编辑器 和  公式继续工具类 所以暂时先这么写
		 */
		if(avos==null || avos.length==0)
			throw new BusinessException("无业务数据");
		List<ReportBaseVO> nlist=new ArrayList<ReportBaseVO>();
		String drivername=GenPrcOutBVO.costdrivervale;
		String expressCode=vo.getCostdriver();//公式表达式
		String expressName=vo.getReserve1();//公式表达式中文名称
		List<UFDouble> list=getExecFomularTool().doCalcStart(expressCode, expressName, avos);		
		for(int i=0;i<avos.length;i++){
			/**
			 * 设置动因值
			 */
			if(PuPubVO.getUFDouble_NullAsZero(list.get(i)).doubleValue()>0){
				nlist.add(avos[i]);
			}
			avos[i].setAttributeValue(drivername, PuPubVO.getUFDouble_NullAsZero(list.get(i)));
		}
		if(nlist.size()==0){
			throw new BusinessException("无业务数据");
		}
		return nlist.toArray(new ReportBaseVO[0]);
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
