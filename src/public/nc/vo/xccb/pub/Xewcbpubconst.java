package nc.vo.xccb.pub;
/**
 * 西尔维井巷工程常量类
 * @author mlr
 *
 */
public class Xewcbpubconst {
	public static String module="xccb";
	/**
	 * 币种档案-人民币 主键
	 */
	public static String pk_currency="00010000000000000001";
	/**
	 * 成本要素定义 数据来源井巷工程管理
	 */
	public static Integer data_source_jx=0;
	/**
	 * 成本要素定义 数据来源总账
	 */
	public static Integer data_source_zz=1;
	/**
	 * 成本要素定义 功能节点号
	 */
	public static String node_code_costelement="2002AC023010";
	/**
	 * 成本要素定义_模版类型
	 */
	public static String bill_code_costelement="CB01";
	/**
	 * 成本要素定义-会计账簿模版 类型  
	 */
	public static String node_code_costelement_1="CB02";
	/**
	 * 成本核算单（会计平台) 功能节点
	 */
	public static String node_code_costaccount1="2002AA4003";
	/**
	 * 成本核算单（会计平台) 单据类型
	 */
	public static String bill_code_costaccount1="XEW2";
	/**
	 * 成本核算单（固定资产) 单据类型
	 */
	public static String bill_code_costaccount2="XEW3";
	/**
	 * 固定资产模块  新增固定资产审批单
	 */
	public static String bill_code_fixed_assets="HN";
	/**
	 * 工程项目建立 功能节点号
	 */
	public static String node_code_procreate="2002AA0301";
	/**
	 * 工程项目建立 单据类型
	 */
	public static String bill_code_procreate="XEW4";
	/**
	 * 工程项目建立 采掘 单据类型
	 */
	public static String bill_code_procreate1="XEWO";
	/**
	 * 工程项目建立 采掘 功能注册
	 */
	public static String node_code_procreate1="2002AA0302";
	/**
	 * 井巷工程验收单 功能节点号
	 */
	public static String node_code_proaccept="2002AA1001";
	/**
	 * 井巷工程验收单 参照查询模版节点标示
	 */
	public static String yuery_code_proaccept="XEW5REF";
	/**
	 * 井巷工程验收单  单据类型
	 */
	public static String bill_code_proaccept="XEW5";
	/**
	 * 井巷工程验收单-使用材料录入  单据类型
	 */
	public static String bill_code_proaccept_materials="XEW6";
	/**
	 * 作业顶级类别
	 */
	public static String oberate_work_cl="00";
	/**
	 * 存货顶级类别
	 */
	public static String invcl="01";	
	/**
	 * 公用成本汇总处理单  模版类型
	 */
	public static String bill_code_pubsumdeal="CB03";
	/**
	 * 公用成本汇总处理单 功能节点号
	 */
	public static String node_code_pubsumdeal="2002AC02301510";

	/**
	 * 矿脉档案 功能节点号
	 */
	public static String node_code_lode="2002AA0501";
	/**
	 * 硐口档案 功能节点号
	 */
	public static String node_code_hole="2002AA0503";
	/**
	 * 中段档案 功能节点号
	 */
	public static String node_code_middlepart="2002AA0502";
	/**
	 * 工程类别档案 功能节点号
	 */
	public static String node_code_workscategory="2002AA0505";
	/**
	 * 定额单 功能节点号
	 */
	public static String node_code_quota="2002AA0506";
	/**
	 * 巷道采场绑定 功能节点号
	 */
	public static String node_code_quota1="2002AA0510";
	
    /**
     * 公用成本汇总单 单据类型
     */
	public static String bill_code_costsum="CB04";
	   /**
     * 公用成本汇总单 功能节点
     */
	public static String node_code_costsum="2002AC02301515";
	
	/**
	 * 钻探工程验收单  功能节点号
	 */
	public static String node_code_drillacceptance="2002AA1002";
	
	/**
	 * 钻探工程验收单  单据类型
	 */
	public static String bill_code_drillacceptance="XEW9";
	/**
	 *公用成本核算单 单据类型
	 */
	public static String bill_code_costaccount4="CB05";
	/**
	 *公用成本核算单 功能节点号
	 */
	public static String node_code_costaccount4="2002AC02301520";
	/**
	 *直接成本核算单 功能节点号
	 */
	public static String node_code_costaccount5="2002AA2001";
	/**
	 *直接成本核算单 单据类型
	 */
	public static String bill_code_costaccount5="XEWB";
	/**
	 *成本核算单 功能节点号
	 */
	public static String node_code_costaccount6="2002AA4002";
	/**
	 *成本核算单 单据类型
	 */
	public static String bill_code_costaccount6="XEWC";
	/**
	 *与固定产的接口(数据传输) 功能节点号
	 */
	public static String node_code_datatransmission="2002AA5002";
	/**
	 *与固定资产和总账接口(数据过滤) 功能节点号
	 */
	public static String node_code_datafiltering="2002AA5001";
	/**
	 *成本核算单调整单 单据类型
	 */
	public static String bill_code_costaccount7="XEWD";
	/**
	 *成本核算单调整单 功能节点号
	 */
	public static String node_code_costaccount7="2002AA2002";
	
	/**
	 * 项目类型 井巷工程编码
	 */
	public static String protype="05";
	
	/**
	 * 井巷工程验收单-材料明细  单据类型
	 */
	public static String bill_code_proaccept_query="XEWE";
	/**
	 * 井巷工程验收单-材料汇总  单据类型
	 */
	public static String bill_code_proaccept_query1="XEWK";
	/**
	 * 井巷工程验收单-查看备注  单据类型
	 */
	public static String bill_code_proaccept_query2="XEWL";
	/**
	 * 井巷工程验收单-查看工程量  单据类型
	 */
	public static String bill_code_proaccept_query3="XEWP";
	
	/**
	 * 钻探工程验收单-材料明细  单据类型
	 */
	public static String bill_code_drllacceptance_query="XEWF";
	/**
	 * 公用成本汇总单-分配明细  单据类型
	 */
	public static String bill_code_costaccount3_query="CB06";
	/**
	 * 固定资产成本汇总处理单  模版类型
	 */
	public static String bill_code_pubsumdeal1="XEWH";
	/**
	 * 固定资产成本汇总处理单 功能节点号
	 */
	public static String node_code_pubsumdeal1="2002AA4010";
	/**
	 * 井巷工程验收单  传成本核算 单据类型 
	 */
	public static String bill_code_proaccept1="XEWI";
	/**
	 * 钻探工程验收单 传成本  功能节点号
	 */
	public static String node_code_drillacceptance1="2002AA2020";
	/**
	 * 钻探工程验收单 传成本 单据类型
	 */
	public static String bill_code_drillacceptance1="XEWJ";
	/**
	 * 取消传固定资产 节点号
	 */
	public static String node_code_cancdeSendFa="2002AA4020";
	/**
	 * 固定资产 新增资产审批单  存放会计账簿
	 */
	public static String fa_pk_accountbook="def19";
	/**
	 * 固定资产 新增资产审批单  存放数据来源  是否来源井巷工程的 新增资产审批单
	 */
	public static String fa_jxgc="def20";
   
	
	/**
	 * 井巷工程验收单 历史版本 单据类型
	 */
	public static String bill_code_proaccept2="XEWM";
	/**
	 * 钻探工程验收单 历史版本 单据类型
	 */
	public static String bill_code_drillacceptance2="XEWN";
	/**
	 * 自定义项档案  服务月限
	 */
	public static String defsername="服务月限";
	
	
	
	
	
	
	
}
