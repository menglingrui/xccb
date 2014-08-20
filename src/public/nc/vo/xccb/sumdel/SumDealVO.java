package nc.vo.xccb.sumdel;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDouble;
/**
 * 成本汇总处理vo
 * @author mlr
 */
public class SumDealVO extends SuperVO{
	private static final long serialVersionUID = -4885956507051397137L;
//	/**
//	 * 工作中心  工作中心是成本分摊的维度，成本汇总的维度  目前已用前两个  分别为  部门和矿区  后面的为预留字段 不参与汇总维度和分摊维度
//	 */
//    public static String[] workcenters={"pk_defdoc1","pk_defdoc2",
//    		"pk_defdoc11","pk_defdoc12","pk_defdoc13","pk_defdoc14","pk_defdoc15","pk_defdoc16","pk_defdoc17","pk_defdoc18"};//
//    /**
//     * 工作中心引用的基础档案 名称
//     */
//    public static String[] workcenters_basedoc_name={"部门档案","矿区"};//
    
	/**
	 * 工作中心  工作中心是成本分摊的维度，成本汇总的维度  目前已用前两个  分别为  部门和矿区  后面的为预留字段 不参与汇总维度和分摊维度
	 */
    public static String[] workcenters={"pk_defdoc1",
    		"pk_defdoc11","pk_defdoc12","pk_defdoc13","pk_defdoc14","pk_defdoc15","pk_defdoc16","pk_defdoc17","pk_defdoc18"};//
    /**
     * 工作中心引用的基础档案 名称
     */
    public static String[] workcenters_basedoc_name={"部门档案"};//
    
    
    /**
     * 汇总字段
     */
    public static String[] combinFields={"mny"};
    /**
     * 汇总维度 工作中心+成本要素+公司+账簿
     */
    public static String[] combinConds={"pk_defdoc1","pk_defdoc2","pk_defdoc11",
    		"pk_defdoc11","pk_defdoc12","pk_defdoc13","pk_defdoc14","pk_defdoc15","pk_defdoc16","pk_defdoc17","pk_defdoc18","pk_costelement","pk_accoutbook"};
    	
    private String pk_corp;//公司
	
	private String pk_accoutbook;//核算账簿
	
	private String  pk_costelement;//成本要素
	
	private Integer  datasource;//数据来源     0井巷工程      1总账
	
	private Integer  datainfor;//来源信息   0工程验收单   1总账凭证
	
	private String   datasourceid1;//来源内容ID(一般指来源主表id)
	
	private String   datasourceid2;//来源内容ID(一般指来源子表id)
	
	private String   datasourceid3;//来源内容ID(一般指来源孙表id 或 多子表的表id)
	
	private String   dataname;//来源内容
	
	private UFDouble   mny;//金额
	
	private UFBoolean issum;//是否已经汇总
	
	public String getPk_defdoc11() {
		return pk_defdoc11;
	}

	public void setPk_defdoc11(String pk_defdoc11) {
		this.pk_defdoc11 = pk_defdoc11;
	}

	public String getPk_defdoc12() {
		return pk_defdoc12;
	}

	public void setPk_defdoc12(String pk_defdoc12) {
		this.pk_defdoc12 = pk_defdoc12;
	}

	public String getPk_defdoc13() {
		return pk_defdoc13;
	}

	public void setPk_defdoc13(String pk_defdoc13) {
		this.pk_defdoc13 = pk_defdoc13;
	}

	public String getPk_defdoc14() {
		return pk_defdoc14;
	}

	public void setPk_defdoc14(String pk_defdoc14) {
		this.pk_defdoc14 = pk_defdoc14;
	}

	public String getPk_defdoc15() {
		return pk_defdoc15;
	}

	public void setPk_defdoc15(String pk_defdoc15) {
		this.pk_defdoc15 = pk_defdoc15;
	}

	public String getPk_defdoc16() {
		return pk_defdoc16;
	}

	public void setPk_defdoc16(String pk_defdoc16) {
		this.pk_defdoc16 = pk_defdoc16;
	}

	public String getPk_defdoc17() {
		return pk_defdoc17;
	}

	public void setPk_defdoc17(String pk_defdoc17) {
		this.pk_defdoc17 = pk_defdoc17;
	}

	public String getPk_defdoc18() {
		return pk_defdoc18;
	}

	public void setPk_defdoc18(String pk_defdoc18) {
		this.pk_defdoc18 = pk_defdoc18;
	}

	public String pk_defdoc1;//部门
	public String pk_defdoc2;//矿区
	public String pk_defdoc11;//预留 存放凭证辅助核算信息
	public String pk_defdoc12;//预留 存放凭证辅助核算信息
	public String pk_defdoc13;//预留 存放凭证辅助核算信息
	public String pk_defdoc14;//预留 存放凭证辅助核算信息
	public String pk_defdoc15;//预留 存放凭证辅助核算信息
	public String pk_defdoc16;//预留 存放凭证辅助核算信息
	public String pk_defdoc17;//预留 存放凭证辅助核算信息
	public String pk_defdoc18;//预留 存放凭证辅助核算信息
	
	public String pk_defdoc3;
	public String pk_defdoc4;
	public String pk_defdoc5;
	
	public String vdef1;
	public String vdef2;
	public String vdef3;
	public String vdef4;
	public String vdef5;
	public String vdef6;
	
	public String vreserve1;
	public String vreserve2;
	public String vreserve3;
	
	
	public UFDouble nreserve1;
	public UFDouble nreserve2;
	public UFDouble nreserve3;
	public UFDouble nreserve4;
	public UFDouble nreserve5;
	
	public UFBoolean ureserve1;
	public UFBoolean ureserve2;
	public UFBoolean ureserve3;
	
	


	public String getVdef1() {
		return vdef1;
	}

	public void setVdef1(String vdef1) {
		this.vdef1 = vdef1;
	}

	public String getVdef2() {
		return vdef2;
	}

	public void setVdef2(String vdef2) {
		this.vdef2 = vdef2;
	}

	public String getVdef3() {
		return vdef3;
	}

	public void setVdef3(String vdef3) {
		this.vdef3 = vdef3;
	}

	public String getVdef4() {
		return vdef4;
	}

	public void setVdef4(String vdef4) {
		this.vdef4 = vdef4;
	}

	public String getVdef5() {
		return vdef5;
	}

	public void setVdef5(String vdef5) {
		this.vdef5 = vdef5;
	}

	public String getVdef6() {
		return vdef6;
	}

	public void setVdef6(String vdef6) {
		this.vdef6 = vdef6;
	}

	public String getDatasourceid1() {
		return datasourceid1;
	}

	public void setDatasourceid1(String datasourceid1) {
		this.datasourceid1 = datasourceid1;
	}

	public String getDatasourceid2() {
		return datasourceid2;
	}

	public void setDatasourceid2(String datasourceid2) {
		this.datasourceid2 = datasourceid2;
	}

	public String getDatasourceid3() {
		return datasourceid3;
	}

	public void setDatasourceid3(String datasourceid3) {
		this.datasourceid3 = datasourceid3;
	}

	public UFBoolean getIssum() {
		return issum;
	}

	public void setIssum(UFBoolean issum) {
		this.issum = issum;
	}

	public String getPk_defdoc1() {
		return pk_defdoc1;
	}

	public void setPk_defdoc1(String pk_defdoc1) {
		this.pk_defdoc1 = pk_defdoc1;
	}

	public String getPk_defdoc2() {
		return pk_defdoc2;
	}

	public void setPk_defdoc2(String pk_defdoc2) {
		this.pk_defdoc2 = pk_defdoc2;
	}

	public String getPk_defdoc3() {
		return pk_defdoc3;
	}

	public void setPk_defdoc3(String pk_defdoc3) {
		this.pk_defdoc3 = pk_defdoc3;
	}

	public String getPk_defdoc4() {
		return pk_defdoc4;
	}

	public void setPk_defdoc4(String pk_defdoc4) {
		this.pk_defdoc4 = pk_defdoc4;
	}

	public String getPk_defdoc5() {
		return pk_defdoc5;
	}

	public void setPk_defdoc5(String pk_defdoc5) {
		this.pk_defdoc5 = pk_defdoc5;
	}

	public String getVreserve1() {
		return vreserve1;
	}

	public void setVreserve1(String vreserve1) {
		this.vreserve1 = vreserve1;
	}

	public String getVreserve2() {
		return vreserve2;
	}

	public void setVreserve2(String vreserve2) {
		this.vreserve2 = vreserve2;
	}

	public String getVreserve3() {
		return vreserve3;
	}

	public void setVreserve3(String vreserve3) {
		this.vreserve3 = vreserve3;
	}

	public UFDouble getNreserve1() {
		return nreserve1;
	}

	public void setNreserve1(UFDouble nreserve1) {
		this.nreserve1 = nreserve1;
	}

	public UFDouble getNreserve2() {
		return nreserve2;
	}

	public void setNreserve2(UFDouble nreserve2) {
		this.nreserve2 = nreserve2;
	}

	public UFDouble getNreserve3() {
		return nreserve3;
	}

	public void setNreserve3(UFDouble nreserve3) {
		this.nreserve3 = nreserve3;
	}

	public UFDouble getNreserve4() {
		return nreserve4;
	}

	public void setNreserve4(UFDouble nreserve4) {
		this.nreserve4 = nreserve4;
	}

	public UFDouble getNreserve5() {
		return nreserve5;
	}

	public void setNreserve5(UFDouble nreserve5) {
		this.nreserve5 = nreserve5;
	}

	public UFBoolean getUreserve1() {
		return ureserve1;
	}

	public void setUreserve1(UFBoolean ureserve1) {
		this.ureserve1 = ureserve1;
	}

	public UFBoolean getUreserve2() {
		return ureserve2;
	}

	public void setUreserve2(UFBoolean ureserve2) {
		this.ureserve2 = ureserve2;
	}

	public UFBoolean getUreserve3() {
		return ureserve3;
	}

	public void setUreserve3(UFBoolean ureserve3) {
		this.ureserve3 = ureserve3;
	}

	public String getPk_corp() {
		return pk_corp;
	}

	public void setPk_corp(String pk_corp) {
		this.pk_corp = pk_corp;
	}

	public String getPk_accoutbook() {
		return pk_accoutbook;
	}

	public void setPk_accoutbook(String pk_accoutbook) {
		this.pk_accoutbook = pk_accoutbook;
	}

	public String getPk_costelement() {
		return pk_costelement;
	}

	public void setPk_costelement(String pk_costelement) {
		this.pk_costelement = pk_costelement;
	}

	public Integer getDatasource() {
		return datasource;
	}

	public void setDatasource(Integer datasource) {
		this.datasource = datasource;
	}

	public Integer getDatainfor() {
		return datainfor;
	}

	public void setDatainfor(Integer datainfor) {
		this.datainfor = datainfor;
	}

	public String getDataname() {
		return dataname;
	}

	public void setDataname(String dataname) {
		this.dataname = dataname;
	}

	public UFDouble getMny() {
		return mny;
	}

	public void setMny(UFDouble mny) {
		this.mny = mny;
	}

	@Override
	public String getPKFieldName() {
		return null;
	}

	@Override
	public String getParentPKFieldName() {
		return null;
	}

	@Override
	public String getTableName() {
		return null;
	}

}
