package nc.vo.pub.formulaset;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import nc.vo.logging.Debug;
import nc.vo.pub.formulaset.FormulaAnalyser;
import nc.vo.pub.formulaset.FormulaConvertor;
import nc.vo.pub.formulaset.FormulaCustomFunctionCache;
import nc.vo.pub.formulaset.FormulaThread;
import nc.vo.pub.formulaset.IFormulaAnalyser;
import nc.vo.pub.formulaset.IFormulaConvertor;
import nc.vo.pub.formulaset.VarryVO;
import nc.vo.pub.formulaset.core.EvaluatorVisitor;
import nc.vo.pub.formulaset.function.ICustomFunction;
import nc.vo.pub.formulaset.function.NcCustomFunction;
import nc.vo.pub.formulaset.function.PostfixMathCommandI;
import nc.vo.pub.formulaset.jep.JEPExpression;
import nc.vo.pub.formulaset.jep.JEPExpressionParser;
import nc.vo.pub.formulaset.model.FormulaError;
import nc.vo.pub.formulaset.util.BooleanObject;
import nc.vo.pub.formulaset.util.FormulaParseLogger;
import nc.vo.pub.formulaset.util.FormulaUtils;
import nc.vo.pub.formulaset.util.NullZeroNumber;
import nc.vo.pub.formulaset.util.ScaleManager;
import nc.vo.pub.formulaset.util.StringUtil;
import nc.vo.pub.lang.UFDouble;


/**
 * 公式解析器父类，所有的公式调用的入口类 <br><br>
 * 建议一个模块，请维护一个自己单一的公式解析器，以免解析器初始化代价<br>
 * 公式解析器的使用方法:<br><code>
 * 
 *       String formula = "a->charAt(var,4)"; <br>
 *       FormulaParseFather f = new FormulaParse();<br>
 *       f.setExpress(formula);<br>
 *       f.addVariable(var,"teststring");<br>
 *       String result = f.getValue(); </code><p>
 * 
 * 或者：<p><code>
 *      String[] formulas = new String[]{...};<br>
 *      FormulaParseFather f = new FormulaParse();<br>
 *      f.addVariable("cch",new Double(56));<br>
 *      f.setExpressArray(formulas);<br>
 *      VarryVO[] varrys = f.getVarryArray();  <br>
 *      f.addVariable("var",new String[]{"a","b","c"});
 *      String[][] resArray = f.getValueSArray();<br>
 *      </code><p>
 * <b>注意： 公式解析器的初始化相对来说是耗时操作，所以对于<font color=red>前台</font>公式解析器请不要每次都创建，
 * <br>尤其不要在循环里创建，以免引起效率问题，建议每个模块用一个实例</b><br>
 * 如果在较长周期内使用一个实例，可以使用garbage()方法回收变量(@sinceV56)
 * <br><b>后台使用时，在同一线程里，可以使用同一解析器实例。     
 * @author cch (cch@ufida.com.cn)
 * 2006-2-24-17:09:34
 * 
 */
abstract public class FormulaParseFather
{
	/**
	 * The module ID which owns current parser.
	 * Null means this parser is a common one.
	 */
	private String m_ownModuleID = null;
	
    /**
     * 公式个数
     */
    protected int m_expressCount; 

    /**
     * 存贮原始公式
     */
    protected String[] m_formulas; 
    
    /**
     * 动态扩展后的公式，有些公式如：
     * a,b,c->getColsValue("table","col1","col2","col3","colname",pkvars)
     * 扩展后变为：
     * a->getColsValue("table","col1","col2","col3","colname",pkvars)
     * b->getColsValue("table","col1","col2","col3","colname",pkvars)
     * c->getColsValue("table","col1","col2","col3","colname",pkvars)
     */
    protected String[] m_extendedFormulas;

    /**
     * 每个参数值的个数,所有公式都一样
     */
    protected int valueNum = 1; 

    /**
     * 公式拆分后的结果
     */
    protected List formulavos = null; 

    /**
     * 错误信息
     */
    private FormulaError m_error = new FormulaError(); 

    /**
     * 转换器
     */
    private IFormulaConvertor m_convertor = new FormulaConvertor(this); 

    private ScaleManager m_scaleManager = new ScaleManager(); //精度管理器

    private final int OBJECTTYPE = 1; //按对象传递

    private final int STRINGTYPE = 0; //按字符串传递

    private int m_inputArgType = 0;

    /**
     * 记录公式解析错误
     */
    private boolean m_bParseError = false; 
    
    /**
     * null可否当做0
     */
    private boolean m_bNullAsZero = false; 

    /* #################用于修改多线程多执行器示例#########Begin########## */

    /**
     * 自定义函数信息
     */
    private FormulaCustomFunctionCache m_customfunctionCache = new FormulaCustomFunctionCache();

    /**
     * JEP公式解析器
     */
    private JEPExpressionParser m_jepParser = null;
    
    /**
     * 每一个线程独立使用的公式运算visitor
     */
    private EvaluatorVisitor m_evaluator = new EvaluatorVisitor();
    
    /**
     * 解析器可以临时改变的信息池
     */
    private Map m_parserInfoPool = null;
    
    //缓存节点树
    private Map m_expressionCache = null;
    
    //为了提高效率，当公式中不含有数据库查询函数时，仅仅做一次后置转换
    private boolean m_doPostConvert = true;
    
    //统一用一个分析器,提高效率
    IFormulaAnalyser analyser = new FormulaAnalyser(this,getConvertor()); //拆分转换公式
    
    private final static int LRUSIZE = 1000;
    
    private static class LRUMap<K, V> extends LinkedHashMap<K, V> {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public LRUMap(int initSize) {
            super(initSize, 0.75f, true);
        }

        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            if (size() > LRUSIZE)
                return true;
            else
                return false;
        }
    }
    
    /**
     * 取得ExpressionMap
     * @return
     */
    protected Map getExpressionCache()
    {
        if(m_expressionCache == null)
            m_expressionCache = new LRUMap<String, JEPExpression>(100);
        return m_expressionCache;
    }
    
    /**
     * 将JEPExpression加入到缓存
     * @param exphashcode hash码
     * @param expression  已经解析完毕的JEP表达式(节点树)
     */
    public void addToExpressionCache(String formula,JEPExpression expression)
    {
        getExpressionCache().put(formula,expression);
    }
    
    /**
     * 取得公式
     * @param exphashcode  hash码
     * @return
     */
    public JEPExpression getExpressionFromCache(String formula)
    {
        return (JEPExpression)getExpressionCache().get(formula);
    }
    
    
    /**
     * 是否需要做后置变换
     * @return
     */
    public boolean isNeedDoPostConvert()
    {
        return m_doPostConvert;
    }
    
    /**
     * 设置是否需要做后置变换
     * @param postConvert
     */
    public void setNeedDoPostConvert(boolean postConvert)
    {
        m_doPostConvert = postConvert;
    }
    
    /**
     * 得到所以自定义函数
     * @return Returns the m_customfunctionmap.
     */
    public FormulaCustomFunctionCache getCustomfunctionCache()
    {
        return m_customfunctionCache;
    }
    
    /**
     * 返回JEPParser，内置的JEP解析器
     * @return Returns the m_jepParser.
     */
    public JEPExpressionParser getJepParser()
    {
        return m_jepParser;
    }

    /* #################用于修改多线程多执行器示例########End########### */

    /**
     * 数据库查询的缓存类型
     */
    protected int m_cacheType = INT_MIX_CACHE;
    public final static int INT_BD_CACHE = 0;
    public final static int INT_FOREDB_CACHE = 1;
    public final static int INT_MIX_CACHE = 2;

    /**
     * 设置缓存类型,由于V5版废除了老版BDCACHE,所以此处已经不需要设置了
     * @param cacheType
     * @deprecated
     */
    public void setCacheType(int cacheType)
    {
        m_cacheType = cacheType;
    }

    /**
     * 得到缓存类型,由于V5版废除了老版BDCACHE,所以此处已经不需要设置了
     * @param cacheType
     * @deprecated
     */
    public int getCacheType()
    {
        return m_cacheType;
    }

    protected ScaleManager getScaleManager()
    {
        return m_scaleManager;
    }

    
    /**
     * 构造函数，做一些公式解析器的初始化操作
     */
    public FormulaParseFather()
    {
        //清除解析过程中的各种信息，重新初始化
        initEnvironment();
    }
    
    /**
     * 构造函数，做一些公式解析器的初始化操作
     * add by cch on 2007-05-22
     * @param ownModuleID 所属模块标识，用于区分不同模块的公式解析器，<br>
     * 区分的目的是为了能针对不同模块的解析器做不同的初始化，比如自定义函数。
     */
    public FormulaParseFather(String ownModuleID)
    {
        //清除解析过程中的各种信息，重新初始化
    	m_ownModuleID = ownModuleID;
        initEnvironment();
    }
    
    /**
     * @return 所属模块标识
     */
    public String getOwnModuleID() {
		return m_ownModuleID;
	}

	/**
     * 创建错误管理中心
     * 
     * @return 错误对象
     */
    public FormulaError getErrorVO()
    {
        if (null == m_error)
            m_error = new FormulaError();
        return m_error;
    }

    /**
     * 得到错误信息
     * 
     * @return 描述错误的字符串
     */
    public String getError()
    {
        return getErrorVO().getError();
    }

    protected IFormulaConvertor getConvertor()
    {
        return m_convertor;
    }
    
    /**
     * 设置需要运算的公式，有了公式解析器实例之后一般调用此方法, 此方法用于设置单个公式
     * 
     * @param expr 公式字符串
     * @return 设置公式的同时，解析器会做解析，返回值表示所设置的公式是否正确
     * @see public boolean setExpressArray(String[] newExpress)
     */
    public boolean setExpress(String expr)
    {
        return setExpressArray(new String[] { expr });
    }


    /**
     * 设置需要运算的公式，有了公式解析器实例之后一般调用此方法
     * @param newExpress 公式字符串数组
     * @return 设置公式的同时，解析器会做解析，返回值表示所设置的公式是否正确
     * @see public boolean setExpress(String expr)
     */
    public boolean setExpressArray(String[] newExpress)
    {
    	
    	valueNum = 1;
        //公式调用次数
//        FormulaCallInfo.getInstance().addParsenum();
        FormulaParseLogger.debug("开始解析公式:" + Arrays.asList(newExpress));
        m_bParseError = false;
        getErrorVO().clearError();
        if (newExpress==null||newExpress.length==0)
            return true;
        //存贮原始信息
//        m_expressCount = newExpress.length;
        m_formulas = newExpress;
        
        //查看当前线程是否有当前公式执行器
	    registerParserToCurrentThread();

        //进行处理
        formulavos = analyser.split(newExpress); //拆分结果
        m_expressCount = formulavos.size();
        if(m_expressCount>m_formulas.length)
        {
	        m_extendedFormulas = new String[m_expressCount];
	        for (int i = 0; i < m_expressCount; i++) {
	        	m_extendedFormulas[i] = ((JEPExpression)formulavos.get(i)).getExpression();
			}                                
        }
        else
        	m_extendedFormulas = m_formulas;
                                        
        String errorinfo = getError();
        if (errorinfo != null && errorinfo.length() != 0)
        {
        	FormulaParseLogger.error("公式解析错误:\n" + getErrorVO().getError());
            getErrorVO().clearError();
            m_bParseError = true;
            FormulaThread.release();
            return false;
        }
        FormulaParseLogger.debug("解析公式结束!");
        FormulaThread.release();
        return true;
    }

    /**
     * 检查公式是否合法，供外部调用
     * 
     * @param formula 需要检查的公式
     * @return 是否合法
     * @see checkExpressArray(String[] formulas)
     */
    public boolean checkExpress(String formula)
    {
        //检查是否有错误
        return checkExpressArray(new String[] { formula });
    }

    /**
     * 检查公式是否合法，供外部调用
     * 
     * @param formulas 需要检查的公式字符串数组
     * @return 是否合法
     * @see checkExpress(String formula)
     */
    public boolean checkExpressArray(String[] formulas)
    {
    	getErrorVO().clearError();
        //公式调用次数
//        FormulaCallInfo.getInstance().addChecknum();
        
        FormulaParseLogger.debug("开始检查公式:" + Arrays.asList(formulas));
        if (formulas==null||formulas.length==0) //如果公式为空，认为正确
            return true;
        //查看当前线程是否有当前公式执行器
	    registerParserToCurrentThread();

        //进行处理
        analyser.split(formulas); //拆分结果
        String errorinfo = getError();
        if (errorinfo != null && errorinfo.length() != 0)
        {
        	FormulaParseLogger.error("公式不正确，发现错误：\n" + errorinfo);
            getErrorVO().clearError();
            FormulaThread.release();
            return false;
        }
        FormulaParseLogger.debug("公式检查结果：正确!");
        FormulaThread.release();
        return true;
    }

    /**
     * 检查公式
     * 
     * @return
     */
    public boolean check()
    {
        //检查是否有错误
        if (getErrorVO().getError() != null)
            return false;
        else
            return true;
    }
    /**
     * mlr add
     * 取得公式计算结果，一般设置完公式表达式，设置好相应参数后便可调用此方法得到公式的值
     * <br><font color=red><b>公式结果以对象方式返回</b></font>
     * 
     * @return 二维数组，第一维对应公式数组的个数，第二维对应参数的长度<p>
     * 比如有公式:<br>
     * "a->b+c;d->4*a" <br>
     * 其中b=[1,2,3,4],c=[2,2,2,2]<br>
     * 那么返回值为一个2*4的二维数组，每一个元素为Integer：<br>
     * 第一维为a = [3,4, 5, 6]<br>
     * 第二维为c = [4,8,12,16]
     * @see public String[][] getValueSArray()
     */
    public Object[][] getValueOArray1()
    {
        //公式调用次数
//        FormulaCallInfo.getInstance().addComputenum();
        
        //判断公式有无为空
        if(m_formulas == null || m_formulas.length == 0)
        {
        	FormulaParseLogger.error("在公式求值前需要先设置公式表达式!");
            return null;
        }
        //公式返回值，如果为空，返回公式本身
        m_expressCount = formulavos.size();
        Object[][] result = new Object[m_expressCount][];
        
        FormulaParseLogger.debug("开始取公式的值...");
        //检查是否有错误
        if (m_bParseError)
        {
            reStoreFormulaSet(); //还原公式信息
            for (int i = 0; i < m_expressCount; i++)
            {
                result[i] = new Object[] {m_extendedFormulas[i]};
            }
            return result;
        }
        
        //查看当前线程是否有当前公式执行器
	    registerParserToCurrentThread();
        
        //计算公式的值，同时更新参数表
        for (int i=0;i<formulavos.size();)
        {
            JEPExpression exp = (JEPExpression) formulavos.get(i);
            Object resobj = (exp==null)?null:exp.getResult1();
            if (resobj == null)
            {
            	FormulaParseLogger.debug("公式["+(exp==null?"":exp.getExpression())+"]"+"执行结果为null");
                //打出计算过程中的错误
                if(exp!=null && exp.getErrorInfo()!=null){
                	//2007.11.12,append the error info of expression  to Formula error 
                	getErrorVO().appendError("\n"+exp.getErrorInfo());
                	FormulaParseLogger.error(exp.getErrorInfo());
                }
                result[i++] = null;
                continue;
            }
            else if (resobj instanceof List || resobj instanceof Vector) //如果是List的形式
            {
            	if(FormulaParseLogger.isLogOn())
            		FormulaParseLogger.debug("公式["+exp.getExpression()+"]"+"执行结果为:"+resobj);
                List rowvalues = (List) resobj;
                convertListToObjectArray(rowvalues, result, i, exp);
                updateVariableMap(exp.getLeftName(), rowvalues); //更新参数表
                i++;
            } 
			else
			{
			    result[i] = new Object[1];
			    result[i][0] = resobj;
			    updateVariableMap(exp.getLeftName(), resobj);
			    i++;
			    if(FormulaParseLogger.isLogOn())
			    	FormulaParseLogger.debug("公式["+exp.getExpression()+"]"+"执行结果为:"+resobj);
			}
			
        }
        //对非法公式返回的NULL值进行处理
        result = dealResultValue(result);
        if (getError() != null)
        	FormulaParseLogger.error(getError());
        reStoreFormulaSet(); //还原公式信息
        FormulaThread.release();
        return result;
    }

    /**
     * 取得公式计算结果，一般设置完公式表达式，设置好相应参数后便可调用此方法得到公式的值
     * <br><font color=red><b>公式结果以对象方式返回</b></font>
     * 
     * @return 二维数组，第一维对应公式数组的个数，第二维对应参数的长度<p>
     * 比如有公式:<br>
     * "a->b+c;d->4*a" <br>
     * 其中b=[1,2,3,4],c=[2,2,2,2]<br>
     * 那么返回值为一个2*4的二维数组，每一个元素为Integer：<br>
     * 第一维为a = [3,4, 5, 6]<br>
     * 第二维为c = [4,8,12,16]
     * @see public String[][] getValueSArray()
     */
    public Object[][] getValueOArray()
    {
        //公式调用次数
//        FormulaCallInfo.getInstance().addComputenum();
        
        //判断公式有无为空
        if(m_formulas == null || m_formulas.length == 0)
        {
        	FormulaParseLogger.error("在公式求值前需要先设置公式表达式!");
            return null;
        }
        //公式返回值，如果为空，返回公式本身
        m_expressCount = formulavos.size();
        Object[][] result = new Object[m_expressCount][];
        
        FormulaParseLogger.debug("开始取公式的值...");
        //检查是否有错误
        if (m_bParseError)
        {
            reStoreFormulaSet(); //还原公式信息
            for (int i = 0; i < m_expressCount; i++)
            {
                result[i] = new Object[] {m_extendedFormulas[i]};
            }
            return result;
        }
        
        //查看当前线程是否有当前公式执行器
	    registerParserToCurrentThread();
        
        //计算公式的值，同时更新参数表
        for (int i=0;i<formulavos.size();)
        {
            JEPExpression exp = (JEPExpression) formulavos.get(i);
            Object resobj = (exp==null)?null:exp.getResult();
            if (resobj == null)
            {
            	FormulaParseLogger.debug("公式["+(exp==null?"":exp.getExpression())+"]"+"执行结果为null");
                //打出计算过程中的错误
                if(exp!=null && exp.getErrorInfo()!=null){
                	//2007.11.12,append the error info of expression  to Formula error 
                	getErrorVO().appendError("\n"+exp.getErrorInfo());
                	FormulaParseLogger.error(exp.getErrorInfo());
                }
                result[i++] = null;
                continue;
            }
            else if (resobj instanceof List || resobj instanceof Vector) //如果是List的形式
            {
            	if(FormulaParseLogger.isLogOn())
            		FormulaParseLogger.debug("公式["+exp.getExpression()+"]"+"执行结果为:"+resobj);
                List rowvalues = (List) resobj;
                convertListToObjectArray(rowvalues, result, i, exp);
                updateVariableMap(exp.getLeftName(), rowvalues); //更新参数表
                i++;
            } 
			else
			{
			    result[i] = new Object[1];
			    result[i][0] = resobj;
			    updateVariableMap(exp.getLeftName(), resobj);
			    i++;
			    if(FormulaParseLogger.isLogOn())
			    	FormulaParseLogger.debug("公式["+exp.getExpression()+"]"+"执行结果为:"+resobj);
			}
			
        }
        //对非法公式返回的NULL值进行处理
        result = dealResultValue(result);
        if (getError() != null)
        	FormulaParseLogger.error(getError());
        reStoreFormulaSet(); //还原公式信息
        FormulaThread.release();
        return result;
    }
    
    /**
     * 取得多个公式的值,和getValueOArray()不同的是，此方法将返回结果都转换为String类型<br>
     * V30遗留方法，不推荐使用
     * @return
     * @see public Object[][] getValueOArray()
     */
    public String[][] getValueSArray()
    {
        //计算公式的值
        Object[][] oresult = getValueOArray();
        if (oresult == null || oresult.length == 0)
            return null;
        String[][] result = new String[m_expressCount][];
        for (int i = 0; i < oresult.length; i++)
        {
            Object[] objects = oresult[i];
            if (objects != null)
            {
                result[i] = new String[objects.length];
                for (int j = 0; j < objects.length; j++)
                {
                    Object object = objects[j];
                    if (object == null)
                        result[i][j] = "";
                    else
                        result[i][j] = object.toString();
                }
            }
        }
        return result;
    }

    /**
     * add mlr
     * 取得多个公式的值,和getValueOArray()不同的是，此方法将返回结果都转换为String类型<br>
     * V30遗留方法，不推荐使用
     * @return
     * @see public Object[][] getValueOArray()
     */
    public String[][] getValueSArray1()
    {
        //计算公式的值
        Object[][] oresult = getValueOArray1();
        if (oresult == null || oresult.length == 0)
            return null;
        String[][] result = new String[m_expressCount][];
        for (int i = 0; i < oresult.length; i++)
        {
            Object[] objects = oresult[i];
            if (objects != null)
            {
                result[i] = new String[objects.length];
                for (int j = 0; j < objects.length; j++)
                {
                    Object object = objects[j];
                    if (object == null)
                        result[i][j] = "";
                    else
                        result[i][j] = object.toString();
                }
            }
        }
        return result;
    }
    
    /**
     * 
     */
    private void registerParserToCurrentThread()
    {
        FormulaThread.registerJepParser(this);
        FormulaThread.registerEvVisitor(m_evaluator);
    }

    /**
     * 对返回值进行处理
     * 
     * @param result
     */
    protected Object[][] dealResultValue(Object[][] result)
    {
        if (result == null)
            return null;
        boolean bAllNull = true;
        
        //求最大值
        for (int i = 0; i < result.length; i++) {
			if(result[i]!=null && result[i].length>valueNum)
				valueNum = result[i].length;
		}
        
        for (int i = 0; i < result.length; i++)
        {
            //空值的处理
            if (result[i] == null || result[i].length == 0)
            {
                result[i] = new Object[valueNum];
            }
            //对常量的长度进行调整
            else if (result[i].length == 1 && result[i].length < valueNum)
            {
                Object saveobj = result[i][0];
                result[i] = new Object[valueNum];
                for (int j = 0; j < valueNum; j++)
                {
                    result[i][j] = saveobj;
                }
                bAllNull = false; //有非空值
            }
            else
            {
                bAllNull = false;
            }

            //对参数返回值类型进行修正
            JEPExpression exp = (JEPExpression) formulavos.get(i);
            if (result[i] != null && result[i].length > 0)
            {
                for (int j = 0; j < result[i].length; j++)
                {
                    Object obj = result[i][j];
                    if (FormulaUtils.isParamNull(obj)) //参数为null的恢复
                    {
                            obj = null;
                    }
                    else if(obj instanceof NullZeroNumber)
                    {
                        obj = new Double(0);
                    }
                    else if (obj instanceof BooleanObject)
                        obj = ((BooleanObject) obj).toBoolean();

                    //精度处理--去掉，如果对double转UFDouble，0会丢失。
                    if (obj instanceof Double)
                    {
                        //处理Double精度
                        obj = getScaleManager().getScaledValue(
                                exp.getLeftName(), (Double) obj);
                    }
                    else if (obj instanceof UFDouble)
                    {
                        //处理UFdouble精度
                        obj = getScaleManager().getScaledValue(
                                exp.getLeftName(), (UFDouble) obj);
                    }
                    result[i][j] = obj;
                }

            }
        }
        //NULL值处理
        if(bAllNull) result = null;
        return result;
    }

    /**
     * 初始化公式执行器
     */
    protected void initEnvironment()
    {
    	//init variable
    	m_jepParser = new JEPExpressionParser(getOwnModuleID());
        //精度设置
        getScaleManager().setDefaultRoudingUp(BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 将list转为数据
     * 
     * @param rowvalues
     * @param result
     * @param i
     * @param exp
     * @return
     */
    private void convertListToObjectArray(List rowvalues, Object[][] result,
            int i, JEPExpression exp)
    {
        int rowsize = rowvalues.size();
		if (rowvalues == null || rowsize == 0)
        {
            getErrorVO().appendError(FormulaError.ERR_ILLEGALRESULT);
            return;
        }
        //得到公式结果的参数长度，用于将空值赋值返回
        valueNum = rowsize;
        result[i] = new Object[rowsize];
        for (int j = 0; j < rowsize; j++)
        {
            Object o = rowvalues.get(j);
            result[i][j] = o;
        }
        return;
    }

    /**
     * 更新参数表
     * 
     * @param rowvalues
     */
    protected synchronized void updateVariableMap(String varname, Object rowvalues)
    {
        m_jepParser.addVariable(varname, rowvalues);
    }

    /**
     * 此方法用于返回单个公式且参数不是数组情况下的运算结果，以对象方式返回<br>
     * 如果公式是多个，则只返回第一个<br>
     * 如果参数为数组，也只返回对应于第一个参数的运算结果<br>
     * 
     * @return 公式运算结果，具体返回的对象类型取决于公式<br>
     * 如果是UFDouble互相运算那么，返回的则是UFDouble,<br>
     * 如果条件表达式，则返回的是Boolean等
     * @see public String getValue()
     */
    public Object getValueAsObject()
    {
        Object[] re = getValueO();
        if (re == null || re.length == 0)
            return null;
        return re[0];
    }

    /**
     * 此方法用于返回单个公式且参数不是数组情况下的运算结果,以字符串方式返回<br>
     * 如果公式是多个，则只返回第一个<br>
     * 如果参数为数组，也只返回对应于第一个参数的运算结果<br>
     * 
     * @return 公式运算结果,均已转换为字符串
     * @see public Object getValueAsObject()
     */
    public String getValue()
    {
        String[] re = getValueS();
        if (re == null || re.length == 0)
            return null;
        return re[0];
    }

    /**
     * 此方法用于返回单个公式的运算结果,以对象方式返回<br>
     * 如果公式是多个，则只返回第一个<br>
     * 
     * @return 公式运算结果，具体返回的对象类型取决于公式<br>
     * 如果是UFDouble互相运算那么，返回的则是UFDouble,<br>
     * 如果条件表达式，则返回的是Boolean等
     * 
     * @see public Object getValueAsObject()
     */
    public Object[] getValueO()
    {
        Object[][] res = getValueOArray();
        if (res == null || res.length == 0)
            return null;
        return res[0];
    }

    /**
     * 此方法用于返回单个公式的运算结果,以字符串方式返回<br>
     * 如果公式是多个，则只返回第一个<br>
     * 
     * @return
     * @see public String getValue()
     */
    public String[] getValueS()
    {
        String[][] res = getValueSArray();
        if (res == null || res.length == 0)
            return null;
        return res[0];
    }

    /**
     * 设置参数表,V3老接口，不推荐使用，需要废弃
     * 建议用:<br><br>
     * <font color=red>public void setDataSArray(Map keyListMap)</font> 或者：<p>
     * <font color=red>public void addVariable(String name, Object value)</font> 代替.
     * @param varryDataArray java.util.Hashtable[]
     * @deprecated 
     */
    public void setDataSArray(java.util.Hashtable[] varryDataArray)
    {
        m_inputArgType = STRINGTYPE;
        for (int i = 0; i < varryDataArray.length; i++)
        {
            Map hashtable = varryDataArray[i];
            addVariablesFromMap(hashtable);
        }
    }

    /**
     * 设置参数表 ,按Objects方式传入参数(可为UFDouble,Integer,String等等)<br>
     * 下面演示如何使用(其中f为公式解析器实例)：<p><code>
     *  List v1 = new ArrayList();<br>
        v1.add("1001AA10000000000DZY"); //row value<br>
        v1.add("1001AA10000000000DZY"); //row value<br>
        v1.add("1001AA10000000000DZY"); //row value<br>
        Map map = new HashMap();<br>
        map.put("pk_deptdoc", v1);<br>
        f.setDataSArray(map);<br></code>
     * @param keyListMap 存贮参数的hashmap
     */
    public void setDataSArray(Map keyListMap)
    {
        m_inputArgType = OBJECTTYPE;
        addVariablesFromMap(keyListMap);
    }

    /**
     * 从外部接受参数
     * 
     * @param keyListMap
     */
    private void addVariablesFromMap(Map keyListMap)
    {
        //更新参数表
        //m_variablemap.update(null, null);
        for (Iterator iterator = keyListMap.keySet().iterator(); iterator
                .hasNext();)
        {
            boolean berror = false;
            String colName = (String) iterator.next();
            Object tempvalues = keyListMap.get(colName);
            List values = null;
            if (tempvalues instanceof List)
            {
                values = (List) tempvalues;
            }
            else if (tempvalues instanceof Object[])
            {
                values = new ArrayList();
                values.addAll(Arrays.asList((Object[]) tempvalues));
            }
            else
            {
                //如果传入的参数类型不支持
                berror = true;
            }

            //如果传入的参数长度为0
            if (values == null || values.size() == 0 || berror)
            {
                //更新变量表
                updateVariableMap(colName, null);
            }
            else
            {
                valueNum = values.size();
                //对类型做检查，UFDOUBLE转换为Double
                convertDataToKnownType(values);
                //更新变量表
                updateVariableMap(colName, values);
            }

        }
    }

    /**
     * 为公式增加变量,最方便的增加变量的方法，变量可以为简单对象，<br>
     * 也可以为数组或者List<p>
     * 使用方法：<br><code>
     *  List deptcode = new ArrayList();<br>
        deptcode.add("dept003");<br>
        f.addVariable("deptcode",deptcode);<br></code>
     * @param name 变量名
     * @param value 变量值 支持各种基本类型及数组，List等
     * 
     */
    public void addVariable(String name, Object value)
    {
        m_inputArgType = OBJECTTYPE;
        updateVariableMap(name, convertDataToKnownType(value));
    }

    /**
     * 为公式注册自定义函数
     * 
     * @param name 函数名
     * @param value 函数的实现类实例,实现PostfixMathCommandI接口的类
     */
    public void addFunction(String name, PostfixMathCommandI value)
    {
    	if(!nc.vo.jcom.lang.StringUtil.isEmpty(name))
    		m_jepParser.addFunction(name.toUpperCase(), value);
    }

    /**
     * 将传入的对象做一定的转换，用于特殊值的处理以及兼容NC30老的传值方式
     * 
     * @param obj
     */
    protected Object convertDataToKnownType(Object obj)
    {
        try
        {
            if (obj == null)
            {
                return null;
            }
            else if (obj instanceof Object[])
            {
            	List newlist = new ArrayList();
            	Object[] objs = (Object[])obj;
            	for (int i = 0; i < objs.length; i++)
				{
            		newlist.add(convertDataToKnownType(objs[i]));
				}
            	return newlist;
            }
            else if (obj instanceof List)
            {
                return dealObjectOfList((List)obj);
            }
            else if (obj instanceof Boolean)
            {
                return new BooleanObject((Boolean) obj); //增加Boolean型处理
            }
            else if (obj instanceof String && m_inputArgType == STRINGTYPE) //可能以老的方式传进来
            {
                String str = obj.toString();
                return StringUtil.getValueFromString(str);
            }
            return obj;
        }
        catch (ClassCastException e)
        {
            getErrorVO().appendError(FormulaError.ERR_ARGTYPEDIFFER);
            FormulaParseLogger.error("传入对象类型错误!",e);
        }
        catch (Exception e)
        {
        	FormulaParseLogger.error("对传入对象进行类型转换时出现错误!",e);
        }
        return null;
    }

	private Object dealObjectOfList(List obj)
	{
		List inlist = (List) obj;
		//如果传入的参数为ArrayList并且为空
		if (inlist.size() == 0)
		    return null;
		for (int i = 0; i < inlist.size(); i++)
		{
		    Object o = inlist.get(i);
		    inlist.set(i, convertDataToKnownType(o));
		}
		return inlist;
	}



    /**
     * 将传入的List做一定的转换
     * 
     * @param srclist
     */
    private void convertDataToKnownType(List srclist)
    {
        for (int i = 0; i < srclist.size(); i++)
        {
            Object obj = srclist.get(i);
            srclist.set(i, convertDataToKnownType(obj));
        }
    }

    /**
     * 得到变量信息
     * 
     * @return 变量描述类型VarryVO
     */
    public VarryVO getVarry()
    {
        VarryVO[] varvos = getVarryArray();
        if (varvos != null)
            return varvos[0];
        return null;
    }

    /**
     * 得到指定公式里的变量信息
     * 
     * @return 变量描述类型VarryVO
     */
    public VarryVO getVarry(String formula)
    {
        int npos = -1;
        if (m_formulas != null)
        {
            for (int i = 0; i < m_formulas.length; i++)
            {
                if (m_formulas[i].equals(formula))
                {
                    npos = i;
                    break;
                }
            }
        }
        if (npos == -1) //没有设置过公式或者公式里没找到
        {
            setExpress(formula);
            npos = 0;
        }
        VarryVO[] varvos = getVarryArray();
        if (varvos != null)
        {
            return varvos[npos];
        }
        return null;
    }

    /**
     * 得到多行公式中所有的变量信息
     * 
     * @return VarryVO[]
     */
    public VarryVO[] getVarryArray()
    {
    	FormulaParseLogger.debug("开始从公式中取得变量...");
        VarryVO[] resvos = new VarryVO[m_expressCount];
        if (m_formulas == null || formulavos == null || formulavos.size()==0)
            return null;
        for (int i = 0; i < m_expressCount; i++)
        {
            if (formulavos.get(i) != null)
            {
                resvos[i] = ((JEPExpression) formulavos.get(i)).getVarryVO();
//                FormulaParseLogger.debug("公式变量" + i + Arrays.asList(resvos[i].getVarry()));
            }
        }
        FormulaParseLogger.debug("返回公式中的变量!");
        return resvos;
    }

    /**
     * 往公式解析器注册自定义函数信息，用外部已有类的方法来注册函数
     * 
     * @param className 类名
     * @param methodName 方法名
     * @param returnType 返回值类型
     * @param argTypes 参数类型数组
     */
    public void setSelfMethod(String className, String methodName,
            Class returnType, Class[] argTypes)
    {
       setSelfMethod(className, methodName, returnType, argTypes,true);
    }

    /**
     * 注册函数到JEP
     * 
     * @param methodName
     * @param className
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private void addFunctionToJepParser(String methodName, String className,boolean isNullAsZero)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException
    {
        Object obj = Class.forName(className).newInstance();
        if (obj instanceof ICustomFunction)
            m_jepParser.addFunction(methodName, (PostfixMathCommandI) obj);
        else
            m_jepParser.addFunction(methodName, new NcCustomFunction(isNullAsZero));
    }

    /**
     * 设置自定义函数信息。 创建日期：(2001-7-9 15:01:50)
     * 
     * @param className java.lang.String
     * @param methodName java.lang.String
     * @param returnType java.lang.Class
     * @param argTypes ava.lang.Class[]
     * @param argValues java.lang.Object[]
     */
    public void setSelfMethod(String className, String methodName,
            Class returnType, Class[] argTypes, Object[] argValues)
    {
        try
        {
            addToCustomFunctionCache(methodName, className, methodName,argTypes, returnType);
            //加载到JEP
            addFunctionToJepParser(methodName, className,true);
        }
        catch (Exception e)
        {
        	FormulaParseLogger.error(e.getMessage(),e);
            getErrorVO().appendError(FormulaError.ERR_CLASSNOTFOUND);
        }

    }
    
    public void setSelfMethod(String className, String methodName,
            Class returnType, Class[] argTypes,boolean isNullAsZero)
    {
        try
        {
            addToCustomFunctionCache(methodName, className, methodName,argTypes, returnType);
            //加载到JEP
            addFunctionToJepParser(methodName, className,isNullAsZero);
        }
        catch (Exception e)
        {
        	FormulaParseLogger.error(e.getMessage(),e);
            getErrorVO().appendError(FormulaError.ERR_CLASSNOTFOUND);
        }

    }
    

    /**
     * 设置自定义函数信息。 创建日期：(2001-7-9 15:01:50)
     * 
     * @param method
     *            java.lang.String
     * @param className
     *            java.lang.String
     * @param methodName
     *            java.lang.String
     * @param returnType
     *            java.lang.Class
     * @param argTypes
     *            java.lang.Class[]
     */
    public void setSelfMethod(String method, String className,
            String methodName, Class returnType, Class[] argTypes)
    {
        try
        {
            addToCustomFunctionCache(method, className, methodName, argTypes,returnType);
            //加载到JEP
            addFunctionToJepParser(method, className,true);
        }
        catch (Exception e)
        {
            FormulaParseLogger.error(e.getMessage(),e);
            getErrorVO().appendError(FormulaError.ERR_CLASSNOTFOUND);
        }
    }

    /**
     * 加到自定义函数缓存里
     * 
     * @param method
     * @param className
     * @param methodName
     * @param argTypes
     * @param returnType
     */
    private void addToCustomFunctionCache(String method, String className,
            String methodName, Class[] argTypes, Class returnType)
    {
        m_customfunctionCache.addClassName(method, className);
        m_customfunctionCache.addArgType(method, argTypes);
        m_customfunctionCache.addReturnType(method, returnType);
        m_customfunctionCache.addCallMethod(method, methodName);
    }

    /**
     * 设置返回结果的精度
     * 
     * @param scale
     */
    public void setScale(int scale)
    {
        getScaleManager().setDefaultScale(scale);
    }

    /**
     * 设置返回结果的精度
     * 
     * @param scale
     */
    public void setScale(int scale, int roundingup)
    {
        getScaleManager().setDefaultScale(scale);
        getScaleManager().setDefaultRoudingUp(roundingup);
    }

    /**
     * 设置变量的返回精度
     * 
     * @param varname
     * @param scale
     */
    public void setScale(String varname, int scale)
    {
        getScaleManager().addVarScale(varname, scale);
    }

    /**
     * 设置变量的返回精度
     * 
     * @param varname
     * @param scale
     */
    public void setScale(String varname, int scale, int roundingup)
    {
        getScaleManager().addVarScale(varname, scale, roundingup);
    }

    /**
     * 此处插入方法说明。 功能：设置参数表 创建日期：(2001-4-25 8:51:15)
     * 
     * @param varryData
     *            java.util.Hashtable
     */
    public void setDataS(java.util.Hashtable varryData)
    {
        setDataSArray(new java.util.Hashtable[] { varryData });
    }

    /**
     * 设置公式变量,V3老接口，不建议再使用<br>
     * 请使用:<br>
     * public void addVariable(String name, Object value) 代替<br>
     * 
     * @param varryData java.util.Hashtable
     * @deprecated
     */
    public void setData(java.util.Hashtable varryData)
    {
        if (varryData != null)
        {
            Enumeration enu = varryData.keys();

            if (enu != null)
            {
                String key = "";
                while (enu.hasMoreElements())
                {
                    try
                    {
                        key = (String) enu.nextElement();
                        Object invalue = varryData.get(key);
                        Object[] canShu = null;
                        if (invalue instanceof String)
                        {
                            canShu = new String[1];
                            canShu[0] = invalue;
                        }
                        else
                            canShu = (Object[]) invalue;
                        varryData.put(key, canShu); //设置参数表
                    }
                    catch (Exception ex)
                    {
                    	FormulaParseLogger.error(ex.getMessage(),ex);
                        getErrorVO().appendError("提示：赋值有误！参数:" + key);
                    }
                }
            }
        }
        setDataS(varryData);
    }

    /**
     * NULL是否当0用。 创建日期：(2001-7-9 15:13:42)
     * 
     * @return boolean
     */
    public boolean isNullAsZero()
    {
        return m_bNullAsZero;
    }

    /**
     * 设置NULL是否可以当0用。 创建日期：(2001-7-9 15:13:42)
     * 
     * @param newNullAsZero
     *            boolean
     */
    public void setNullAsZero(boolean newNullAsZero)
    {
        m_bNullAsZero = newNullAsZero;
    }

    /**
     * 将字符串两边加",使之成为真字符串
     * V3老方法，不建议再使用,兼容NC30接口
     * @param st
     * @return
     * @deprecated 
     * @see StringUtil.toString(st)
     */
    public String toString(String st)
    {
        return StringUtil.toString(st);
    }

    
    /**
     * 将字符串两边加",使之成为真字符串
     * V3老方法，不建议再使用,兼容NC30接口
     * @param st
     * @return
     * @deprecated 
     * @see StringUtil.toString(st)
     */
    public String[] toString(String st[])
    {
        if (st == null || st.length <= 0)
            return null;

        String[] reSt = new String[st.length];
        for (int i = 0; i < st.length; i++)
        {
            reSt[i] = toString(st[i]);
        }
        return reSt;
    }
    
    /**
     * 给公式建立还原点
     * 如果可以的话，应该统一管理这些信息
     *
     */
    public void buildReStorePoint()
    {
    	FormulaParseLogger.debug("给公式解析器建立还原点...");
        if(m_parserInfoPool == null)
            m_parserInfoPool = new HashMap();
        //加入需要保留的信息
        m_parserInfoPool.put("nullaszero",new Boolean(isNullAsZero()));
        m_parserInfoPool.put("cachetype",new Integer(getCacheType()));
        m_parserInfoPool.put("nodb",new Boolean(isNeedDoPostConvert()));
    }
    
    /**
     * 还原公式信息
     *
     */
    public void reStoreFormulaSet()
    {
    	FormulaParseLogger.debug("还原公式信息...");
        if(m_parserInfoPool == null)
            return;
        Boolean nullaszero = (Boolean)m_parserInfoPool.get("nullaszero");
        if(nullaszero != null)
            setNullAsZero(nullaszero.booleanValue());
        Integer cachetype = (Integer)m_parserInfoPool.get("cachetype");
        if(cachetype != null)
            setCacheType(cachetype.intValue());
        Boolean nodb = (Boolean)m_parserInfoPool.get("nodb");
        if(nodb != null)
            setNeedDoPostConvert(nodb.booleanValue());
        Debug.setDebuggable(false);
    }

	/**
	 * 回收变量内存
	 * @since V56
	 */
	public void garbage() {
		//回收变量内存
        VarryVO[] varries = getVarryArray();
		if(varries!=null)
		{
			for (int i = 0; i < varries.length; i++) {
				String[] vars = varries[i].getVarry();
				if(vars!=null)
				{
					for (int j = 0; j < vars.length; j++) {
						getJepParser().getVariables().put(vars[j],null);
					}
				}
				getJepParser().getVariables().put(varries[i].getFormulaName(),null);
			}
		}
	}
    
    /**
     * 得到多语言编码,后台默认为简体中文,分离前台代码
     * @return
     */
    public String getLangCode()
    {
    	return "simpchn";
    }
    
    /**
     * V3老方法，不建议再使用
     * @return
     * @deprecated
     */
    public String getStarts()
    {
        return StringUtil.START_ST;
    }

    /**
     * V3老方法，不建议再使用
     * @return
     * @deprecated
     */
    public String getEnds()
    {
        return StringUtil.END_ST;
    }
    
    /**
     * 替换公式里的变量并得到替换后的公式字符串
     * @param replacedFormula - 带替换的公式字符串
     * @param varnames - map<原变量名,替换变量名>
     * @return
     */
    public String replaceVariableInExpression(String replacedFormula,Map varnames)
    {
    	StringBuffer resultStr = new StringBuffer();
    	String[] formulas = replacedFormula.split(";");
    	setExpressArray(formulas);
    	VarryVO[] varries = getVarryArray();
    	for (int i = 0; i < formulavos.size(); i++) {
    		if(!varries[i].getFormulaName().startsWith("NC_FORMULA_VAR"))
    			resultStr.append(varries[i].getFormulaName()).append("->");
    		resultStr.append(((JEPExpression)formulavos.get(i)).replaceVariables(varnames));
    		if(i!=formulavos.size()-1)
    			resultStr.append(";");
		}
    	return resultStr.toString();
    }
}