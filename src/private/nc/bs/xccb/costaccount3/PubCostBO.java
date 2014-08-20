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
 * ���óɱ����䴦�� ��̨������
 * @author mlr
 */
public class PubCostBO {
    /**
     * ����
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
	 * ȡ���ɱ�����
	 * @throws Exception 
	 * @param infor  infor(0)=��ǰ����  infor(1)=��ǰ����Ա  infor(3)=��˾
	 */
	public CostAccoutBVO[] doCancelAllo(CostAccountVO vo,CostAccoutBVO[] vos,String[] infor)throws Exception{
		if(vos==null || vos.length==0)
			throw new Exception("����Ϊ��");
		if(infor==null || infor.length==0)
			throw new Exception("��½��ϢΪ��");
		if(vo==null)
			throw new Exception("��ͷ��ϢΪ��");	
		for(int i=0;i<vos.length;i++){
			String wsql=" 1=1 ";
			CostAccoutBVO bvo=vos[i];
			wsql=wsql+" and vlastbillrowid='"+bvo.getPrimaryKey()+"' and isnull(dr,0)=0 ";
			List list=(List) getDao().retrieveByClause(CostAccoutBVO.class, wsql);
			if(list==null|| list.size()==0){
				continue;
			}
			//�鿴�Ƿ�������̬
			for(int j=0;j<list.size();j++){
				CostAccoutBVO nvo=(CostAccoutBVO) list.get(j);
				Integer vbillstate=getVbillState(nvo);
				if(vbillstate.intValue()!=IBillStatus.FREE){
					String billno=getVbillNo(nvo);
					throw new Exception("���ε��� �����ݺ�Ϊ["+billno+"] �����Ѿ��������ύ");
				}
			}
			//��������ɾ����־
			for(int j=0;j<list.size();j++){
				CostAccoutBVO nvo=(CostAccoutBVO) list.get(j);
				String sql1=" update xccb_costaccount_b set xccb_costaccount_b.dr=1 where xccb_costaccount_b.pk_costaccount_b='"+nvo.getPrimaryKey()+"'";
				//ִ��ɾ�����α������
				getDao().executeUpdate(sql1);
		
			}
			//�鿴�Ƿ���ڱ���Ϊ�յ����ε��� ����ɾ��
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
		 * ���÷����־
		 */
		for(int i=0;i<vos.length;i++){
			vos[i].setUreserve1(new UFBoolean(false));
		}
		getDao().updateVOArray(vos);
		return vos;
	}
	/**
	 * ȡ�õ��ݱ��
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
	 * ȡ�ñ�ͷ����״̬
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
	 * �ɱ�����
	 * @throws Exception 
	 * @param infor  infor(0)=��ǰ����  infor(1)=��ǰ����Ա  infor(3)=��˾
	 */
	public CostAccoutBVO[] doCostAllo(CostAccountVO vo,CostAccoutBVO[] vos,String[] infor) throws Exception{
		if(vo==null )
			throw new Exception("���������� ��ͷΪ��");
        if(vos==null || vos.length==0){
        	throw new Exception("���������ݵı���Ϊ��");
        }
    	   /**
         * ����˲�
         */
        String pk_accoutbook=vo.getPk_accoutbook();
        /**
         * ȡ�óɱ�����vo�Ĺ�������
         */
        String[] costworkcenters=CostAccountVO.workcenters1; 
        /**
         * ȡ�� ore mine vo�Ĺ�������
         */
        String[] accworcenters=GenPrcOutHVO.workcenters;
        /**
         * ��ŷ�̯��ĳɱ�
         */
        List<ReportBaseVO> alllist=new ArrayList<ReportBaseVO>();
    
        for(int i=0;i<vos.length;i++){   
            /**
             * ��ѯ�Ĺ������յ�
             */
            List<ReportBaseVO> provos=new ArrayList<ReportBaseVO>();
        	String wsql=getWhereSql(vo,costworkcenters,accworcenters);         
        	/**
    		 * ȡ�óɱ�����
    		 */
    		List divs=(List) getTool().getDao().retrieveByClause(CostelementB1VO.class, " pk_costelement = '"+vos[i].getPk_costelement()+"' and isnull(dr,0)=0");
    		if(divs==null || divs.size()==0){
    			String costcode=getTool().getCostCodeByPk(vos[i].getPk_costelement());
    			String costname=getTool().getCostNameByPk(vos[i].getPk_costelement());
    			throw new BusinessException("�ɱ�Ҫ�ر���Ϊ:['"+costcode+"'] ,����Ϊ:['"+costname+"'] �ĳɱ�Ҫ�� û�ж���ɱ�����");
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
                 * �õ���ѯ���յ�sql
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
             * ����ɱ�
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
         * �������ĳɱ�ת��Ϊ����
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
				String  pk_invbasdoc=vo.getPk_invbasdoc();//��ҵ
				String  pK_clbasdoc=vo.getVreserve3();//����
				String  pk_pro=vo.getPk_jobbasfil();//����
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
         * ���ù������Ĺ��� ��������sql
         */
        String wsql=getTool().getWorkCenterSql(vo, costworkcenters, accworcenters);      
        /**
         * �����������
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
	 * ������Դ��Ϣ
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
	 * ���óɱ�Ҫ��
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
	 * ���û���˲�
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
	 * ���óɱ��Ƿ��Ѿ������־
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
	 * �����ĳɱ�����  ���ɷ�̯�ɱ����㵥
	 * @param vo 
	 * @param costvos
	 * @param  infor(0)=��ǰ����  infor(1)=��ǰ����Ա  infor(3)=��˾
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
	 * ���÷�������
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
			throw new BusinessException("[��������] �Զ������û�н���");
		}
		
		String ser1_pk=PuPubVO.getString_TrimZeroLenAsNull(
				getDao().executeQuery("select pk_defdoc from bd_defdoc d where d.pk_defdoclist='"+pk_def+"' and d.doccode='ser1' ", new ColumnProcessor()));
		String ser2_pk=PuPubVO.getString_TrimZeroLenAsNull(
				getDao().executeQuery("select pk_defdoc from bd_defdoc d where d.pk_defdoclist='"+pk_def+"' and d.doccode='ser2' ", new ColumnProcessor()));
	    if(ser1_pk==null){
	    	throw new BusinessException("[��������] �Զ������,���������е������������[ser1] ����[һ�꼰һ��һ��]");   
	    }
	    if(ser2_pk==null){
	    	throw new BusinessException("[��������] �Զ������,���������е������������[ser2] ����[һ������]");   
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
	 * ���÷�̯�ɱ����㵥����ű�
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
	 * �����̯�ɱ����㵥vo   
	 * 
	 * �鿴 ��ǰ����ڼ� ͬһ�������� �Ƿ��Ѿ����ڹ��óɱ����㵥
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
	         * ���ù������Ĺ��� ��������sql
	         */
	         wsql=wsql + getTool().getWorkCenterSql(hvo, costworkcenters,costworkcenters);	
				wsql=wsql+" and pk_corp='"+hvo.getPk_corp()+"'";
				wsql=wsql+" and isnull(dr,0)=0";
				AccperiodmonthVO mothvo=getTool().getMonthVOByDate(hvo.getDbilldate(),hvo.getPk_accoutbook());
				if(mothvo==null){
				   throw new BusinessException("�����Ϊ��");	
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
	 * ���ݳɱ���� ���� �ɱ�  ��������Ϊ �ɱ�Ҫ�����õĳɱ�����
	 * @param costAccoutBVO
	 * @param avos
	 * @throws Exception 
	 */
	public ReportBaseVO[] costAllo(List divs,CostAccoutBVO costvo, ReportBaseVO[] avos) throws Exception {	
		CostelementB1VO dvo=(CostelementB1VO) divs.get(0);
		String diver=dvo.getCostdriver();//ȡ�óɱ�����
		avos=calDriverValue(diver,avos,dvo);
		UFDouble drviersum=calDriverSum(avos);
		costAllo(costvo.getNmy(),drviersum,avos);
		return avos;
	}
	/**
	 * ���ݳɱ� ��������  ���гɱ�����
	 * �����㷨��
	 * ������=�ɱ�������(allonmy)*�ɱ�����(��������vo�ж���ĳɱ�����:costdrivervale)/�ɱ���������(drviersum)
	 * 
	 * ���һ�� ���䣺 �ɱ�������(allonmy)-�ѷ�����
	 * 
	 * @param drviersum
	 * @param avos
	 * @throws BusinessException 
	 */
	public void costAllo(UFDouble allonmysum,UFDouble drviersum, ReportBaseVO[] avos) throws BusinessException {
		if(allonmysum==null || allonmysum.isTrimZero()){
			throw new BusinessException("������Ϊ0 ���÷���");
		}
		if(drviersum==null || drviersum.isTrimZero()){
			throw new BusinessException("�ɱ������Ϊ0 �޷�����");
		}
		if(avos==null || avos.length==0)
			throw new BusinessException("��̯����ҵΪ��");
		String dvname=GenPrcOutBVO.costdrivervale;
		String nmyname=GenPrcOutBVO.costallonmy;
		/**
		 * ����Ѿ�����Ľ��
		 */
		UFDouble allnmysum1=new UFDouble();
		for(int i=0;i<avos.length;i++){
			ReportBaseVO zvo=avos[i];
			//ȡ�ö���
			UFDouble  dv=PuPubVO.getUFDouble_NullAsZero(zvo.getAttributeValue(dvname));
			//���㶯��ռ�õİٷֱ�
			UFDouble pervlaue=dv.div(drviersum);
			//���������
			UFDouble allonmy=allonmysum.multiply(pervlaue);
			//ȡ�ý�λ��λ��
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
	 * ����ɱ�����ֵ�ĺ�
	 * @param avos
	 * @return
	 * @throws BusinessException 
	 */
	public UFDouble calDriverSum(ReportBaseVO[] avos) throws BusinessException {
		if(avos==null || avos.length==0)
			throw new BusinessException("��̯����ҵΪ��");
		UFDouble dsum=new UFDouble(0.0);
		String field=GenPrcOutBVO.costdrivervale;
		for(int i=0;i<avos.length;i++){
			UFDouble dv=PuPubVO.getUFDouble_NullAsZero(avos[i].getAttributeValue(field));
			dsum=dsum.add(dv);
		}
		return dsum;
	}
	/**
	 *���ݳɱ����� ���㶯��ֵ
	 * @param vo �ɱ�Ҫ���гɱ���������vo
	 * @param diver �����ĳɱ�����ֵ
	 * @param avos ҵ������
	 * @throws Exception 
	 */
	public ReportBaseVO[] calDriverValue(String diver, ReportBaseVO[] avos,CostelementB1VO vo) throws Exception {
		/**
		 * �������ù�����  ��ΪĿǰû�м��� ��ʽ�༭�� ��  ��ʽ���������� ������ʱ����ôд
		 */
		if(avos==null || avos.length==0)
			throw new BusinessException("��ҵ������");
		List<ReportBaseVO> nlist=new ArrayList<ReportBaseVO>();
		String drivername=GenPrcOutBVO.costdrivervale;
		String expressCode=vo.getCostdriver();//��ʽ���ʽ
		String expressName=vo.getReserve1();//��ʽ���ʽ��������
		List<UFDouble> list=getExecFomularTool().doCalcStart(expressCode, expressName, avos);		
		for(int i=0;i<avos.length;i++){
			/**
			 * ���ö���ֵ
			 */
			if(PuPubVO.getUFDouble_NullAsZero(list.get(i)).doubleValue()>0){
				nlist.add(avos[i]);
			}
			avos[i].setAttributeValue(drivername, PuPubVO.getUFDouble_NullAsZero(list.get(i)));
		}
		if(nlist.size()==0){
			throw new BusinessException("��ҵ������");
		}
		return nlist.toArray(new ReportBaseVO[0]);
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
