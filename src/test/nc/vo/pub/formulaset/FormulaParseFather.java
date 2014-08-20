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
 * ��ʽ���������࣬���еĹ�ʽ���õ������ <br><br>
 * ����һ��ģ�飬��ά��һ���Լ���һ�Ĺ�ʽ�������������������ʼ������<br>
 * ��ʽ��������ʹ�÷���:<br><code>
 * 
 *       String formula = "a->charAt(var,4)"; <br>
 *       FormulaParseFather f = new FormulaParse();<br>
 *       f.setExpress(formula);<br>
 *       f.addVariable(var,"teststring");<br>
 *       String result = f.getValue(); </code><p>
 * 
 * ���ߣ�<p><code>
 *      String[] formulas = new String[]{...};<br>
 *      FormulaParseFather f = new FormulaParse();<br>
 *      f.addVariable("cch",new Double(56));<br>
 *      f.setExpressArray(formulas);<br>
 *      VarryVO[] varrys = f.getVarryArray();  <br>
 *      f.addVariable("var",new String[]{"a","b","c"});
 *      String[][] resArray = f.getValueSArray();<br>
 *      </code><p>
 * <b>ע�⣺ ��ʽ�������ĳ�ʼ�������˵�Ǻ�ʱ���������Զ���<font color=red>ǰ̨</font>��ʽ�������벻Ҫÿ�ζ�������
 * <br>���䲻Ҫ��ѭ���ﴴ������������Ч�����⣬����ÿ��ģ����һ��ʵ��</b><br>
 * ����ڽϳ�������ʹ��һ��ʵ��������ʹ��garbage()�������ձ���(@sinceV56)
 * <br><b>��̨ʹ��ʱ����ͬһ�߳������ʹ��ͬһ������ʵ����     
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
     * ��ʽ����
     */
    protected int m_expressCount; 

    /**
     * ����ԭʼ��ʽ
     */
    protected String[] m_formulas; 
    
    /**
     * ��̬��չ��Ĺ�ʽ����Щ��ʽ�磺
     * a,b,c->getColsValue("table","col1","col2","col3","colname",pkvars)
     * ��չ���Ϊ��
     * a->getColsValue("table","col1","col2","col3","colname",pkvars)
     * b->getColsValue("table","col1","col2","col3","colname",pkvars)
     * c->getColsValue("table","col1","col2","col3","colname",pkvars)
     */
    protected String[] m_extendedFormulas;

    /**
     * ÿ������ֵ�ĸ���,���й�ʽ��һ��
     */
    protected int valueNum = 1; 

    /**
     * ��ʽ��ֺ�Ľ��
     */
    protected List formulavos = null; 

    /**
     * ������Ϣ
     */
    private FormulaError m_error = new FormulaError(); 

    /**
     * ת����
     */
    private IFormulaConvertor m_convertor = new FormulaConvertor(this); 

    private ScaleManager m_scaleManager = new ScaleManager(); //���ȹ�����

    private final int OBJECTTYPE = 1; //�����󴫵�

    private final int STRINGTYPE = 0; //���ַ�������

    private int m_inputArgType = 0;

    /**
     * ��¼��ʽ��������
     */
    private boolean m_bParseError = false; 
    
    /**
     * null�ɷ���0
     */
    private boolean m_bNullAsZero = false; 

    /* #################�����޸Ķ��̶߳�ִ����ʾ��#########Begin########## */

    /**
     * �Զ��庯����Ϣ
     */
    private FormulaCustomFunctionCache m_customfunctionCache = new FormulaCustomFunctionCache();

    /**
     * JEP��ʽ������
     */
    private JEPExpressionParser m_jepParser = null;
    
    /**
     * ÿһ���̶߳���ʹ�õĹ�ʽ����visitor
     */
    private EvaluatorVisitor m_evaluator = new EvaluatorVisitor();
    
    /**
     * ������������ʱ�ı����Ϣ��
     */
    private Map m_parserInfoPool = null;
    
    //����ڵ���
    private Map m_expressionCache = null;
    
    //Ϊ�����Ч�ʣ�����ʽ�в��������ݿ��ѯ����ʱ��������һ�κ���ת��
    private boolean m_doPostConvert = true;
    
    //ͳһ��һ��������,���Ч��
    IFormulaAnalyser analyser = new FormulaAnalyser(this,getConvertor()); //���ת����ʽ
    
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
     * ȡ��ExpressionMap
     * @return
     */
    protected Map getExpressionCache()
    {
        if(m_expressionCache == null)
            m_expressionCache = new LRUMap<String, JEPExpression>(100);
        return m_expressionCache;
    }
    
    /**
     * ��JEPExpression���뵽����
     * @param exphashcode hash��
     * @param expression  �Ѿ�������ϵ�JEP���ʽ(�ڵ���)
     */
    public void addToExpressionCache(String formula,JEPExpression expression)
    {
        getExpressionCache().put(formula,expression);
    }
    
    /**
     * ȡ�ù�ʽ
     * @param exphashcode  hash��
     * @return
     */
    public JEPExpression getExpressionFromCache(String formula)
    {
        return (JEPExpression)getExpressionCache().get(formula);
    }
    
    
    /**
     * �Ƿ���Ҫ�����ñ任
     * @return
     */
    public boolean isNeedDoPostConvert()
    {
        return m_doPostConvert;
    }
    
    /**
     * �����Ƿ���Ҫ�����ñ任
     * @param postConvert
     */
    public void setNeedDoPostConvert(boolean postConvert)
    {
        m_doPostConvert = postConvert;
    }
    
    /**
     * �õ������Զ��庯��
     * @return Returns the m_customfunctionmap.
     */
    public FormulaCustomFunctionCache getCustomfunctionCache()
    {
        return m_customfunctionCache;
    }
    
    /**
     * ����JEPParser�����õ�JEP������
     * @return Returns the m_jepParser.
     */
    public JEPExpressionParser getJepParser()
    {
        return m_jepParser;
    }

    /* #################�����޸Ķ��̶߳�ִ����ʾ��########End########### */

    /**
     * ���ݿ��ѯ�Ļ�������
     */
    protected int m_cacheType = INT_MIX_CACHE;
    public final static int INT_BD_CACHE = 0;
    public final static int INT_FOREDB_CACHE = 1;
    public final static int INT_MIX_CACHE = 2;

    /**
     * ���û�������,����V5��ϳ����ϰ�BDCACHE,���Դ˴��Ѿ�����Ҫ������
     * @param cacheType
     * @deprecated
     */
    public void setCacheType(int cacheType)
    {
        m_cacheType = cacheType;
    }

    /**
     * �õ���������,����V5��ϳ����ϰ�BDCACHE,���Դ˴��Ѿ�����Ҫ������
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
     * ���캯������һЩ��ʽ�������ĳ�ʼ������
     */
    public FormulaParseFather()
    {
        //������������еĸ�����Ϣ�����³�ʼ��
        initEnvironment();
    }
    
    /**
     * ���캯������һЩ��ʽ�������ĳ�ʼ������
     * add by cch on 2007-05-22
     * @param ownModuleID ����ģ���ʶ���������ֲ�ͬģ��Ĺ�ʽ��������<br>
     * ���ֵ�Ŀ����Ϊ������Բ�ͬģ��Ľ���������ͬ�ĳ�ʼ���������Զ��庯����
     */
    public FormulaParseFather(String ownModuleID)
    {
        //������������еĸ�����Ϣ�����³�ʼ��
    	m_ownModuleID = ownModuleID;
        initEnvironment();
    }
    
    /**
     * @return ����ģ���ʶ
     */
    public String getOwnModuleID() {
		return m_ownModuleID;
	}

	/**
     * ���������������
     * 
     * @return �������
     */
    public FormulaError getErrorVO()
    {
        if (null == m_error)
            m_error = new FormulaError();
        return m_error;
    }

    /**
     * �õ�������Ϣ
     * 
     * @return ����������ַ���
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
     * ������Ҫ����Ĺ�ʽ�����˹�ʽ������ʵ��֮��һ����ô˷���, �˷����������õ�����ʽ
     * 
     * @param expr ��ʽ�ַ���
     * @return ���ù�ʽ��ͬʱ����������������������ֵ��ʾ�����õĹ�ʽ�Ƿ���ȷ
     * @see public boolean setExpressArray(String[] newExpress)
     */
    public boolean setExpress(String expr)
    {
        return setExpressArray(new String[] { expr });
    }


    /**
     * ������Ҫ����Ĺ�ʽ�����˹�ʽ������ʵ��֮��һ����ô˷���
     * @param newExpress ��ʽ�ַ�������
     * @return ���ù�ʽ��ͬʱ����������������������ֵ��ʾ�����õĹ�ʽ�Ƿ���ȷ
     * @see public boolean setExpress(String expr)
     */
    public boolean setExpressArray(String[] newExpress)
    {
    	
    	valueNum = 1;
        //��ʽ���ô���
//        FormulaCallInfo.getInstance().addParsenum();
        FormulaParseLogger.debug("��ʼ������ʽ:" + Arrays.asList(newExpress));
        m_bParseError = false;
        getErrorVO().clearError();
        if (newExpress==null||newExpress.length==0)
            return true;
        //����ԭʼ��Ϣ
//        m_expressCount = newExpress.length;
        m_formulas = newExpress;
        
        //�鿴��ǰ�߳��Ƿ��е�ǰ��ʽִ����
	    registerParserToCurrentThread();

        //���д���
        formulavos = analyser.split(newExpress); //��ֽ��
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
        	FormulaParseLogger.error("��ʽ��������:\n" + getErrorVO().getError());
            getErrorVO().clearError();
            m_bParseError = true;
            FormulaThread.release();
            return false;
        }
        FormulaParseLogger.debug("������ʽ����!");
        FormulaThread.release();
        return true;
    }

    /**
     * ��鹫ʽ�Ƿ�Ϸ������ⲿ����
     * 
     * @param formula ��Ҫ���Ĺ�ʽ
     * @return �Ƿ�Ϸ�
     * @see checkExpressArray(String[] formulas)
     */
    public boolean checkExpress(String formula)
    {
        //����Ƿ��д���
        return checkExpressArray(new String[] { formula });
    }

    /**
     * ��鹫ʽ�Ƿ�Ϸ������ⲿ����
     * 
     * @param formulas ��Ҫ���Ĺ�ʽ�ַ�������
     * @return �Ƿ�Ϸ�
     * @see checkExpress(String formula)
     */
    public boolean checkExpressArray(String[] formulas)
    {
    	getErrorVO().clearError();
        //��ʽ���ô���
//        FormulaCallInfo.getInstance().addChecknum();
        
        FormulaParseLogger.debug("��ʼ��鹫ʽ:" + Arrays.asList(formulas));
        if (formulas==null||formulas.length==0) //�����ʽΪ�գ���Ϊ��ȷ
            return true;
        //�鿴��ǰ�߳��Ƿ��е�ǰ��ʽִ����
	    registerParserToCurrentThread();

        //���д���
        analyser.split(formulas); //��ֽ��
        String errorinfo = getError();
        if (errorinfo != null && errorinfo.length() != 0)
        {
        	FormulaParseLogger.error("��ʽ����ȷ�����ִ���\n" + errorinfo);
            getErrorVO().clearError();
            FormulaThread.release();
            return false;
        }
        FormulaParseLogger.debug("��ʽ���������ȷ!");
        FormulaThread.release();
        return true;
    }

    /**
     * ��鹫ʽ
     * 
     * @return
     */
    public boolean check()
    {
        //����Ƿ��д���
        if (getErrorVO().getError() != null)
            return false;
        else
            return true;
    }
    /**
     * mlr add
     * ȡ�ù�ʽ��������һ�������깫ʽ���ʽ�����ú���Ӧ�������ɵ��ô˷����õ���ʽ��ֵ
     * <br><font color=red><b>��ʽ����Զ���ʽ����</b></font>
     * 
     * @return ��ά���飬��һά��Ӧ��ʽ����ĸ������ڶ�ά��Ӧ�����ĳ���<p>
     * �����й�ʽ:<br>
     * "a->b+c;d->4*a" <br>
     * ����b=[1,2,3,4],c=[2,2,2,2]<br>
     * ��ô����ֵΪһ��2*4�Ķ�ά���飬ÿһ��Ԫ��ΪInteger��<br>
     * ��һάΪa = [3,4, 5, 6]<br>
     * �ڶ�άΪc = [4,8,12,16]
     * @see public String[][] getValueSArray()
     */
    public Object[][] getValueOArray1()
    {
        //��ʽ���ô���
//        FormulaCallInfo.getInstance().addComputenum();
        
        //�жϹ�ʽ����Ϊ��
        if(m_formulas == null || m_formulas.length == 0)
        {
        	FormulaParseLogger.error("�ڹ�ʽ��ֵǰ��Ҫ�����ù�ʽ���ʽ!");
            return null;
        }
        //��ʽ����ֵ�����Ϊ�գ����ع�ʽ����
        m_expressCount = formulavos.size();
        Object[][] result = new Object[m_expressCount][];
        
        FormulaParseLogger.debug("��ʼȡ��ʽ��ֵ...");
        //����Ƿ��д���
        if (m_bParseError)
        {
            reStoreFormulaSet(); //��ԭ��ʽ��Ϣ
            for (int i = 0; i < m_expressCount; i++)
            {
                result[i] = new Object[] {m_extendedFormulas[i]};
            }
            return result;
        }
        
        //�鿴��ǰ�߳��Ƿ��е�ǰ��ʽִ����
	    registerParserToCurrentThread();
        
        //���㹫ʽ��ֵ��ͬʱ���²�����
        for (int i=0;i<formulavos.size();)
        {
            JEPExpression exp = (JEPExpression) formulavos.get(i);
            Object resobj = (exp==null)?null:exp.getResult1();
            if (resobj == null)
            {
            	FormulaParseLogger.debug("��ʽ["+(exp==null?"":exp.getExpression())+"]"+"ִ�н��Ϊnull");
                //�����������еĴ���
                if(exp!=null && exp.getErrorInfo()!=null){
                	//2007.11.12,append the error info of expression  to Formula error 
                	getErrorVO().appendError("\n"+exp.getErrorInfo());
                	FormulaParseLogger.error(exp.getErrorInfo());
                }
                result[i++] = null;
                continue;
            }
            else if (resobj instanceof List || resobj instanceof Vector) //�����List����ʽ
            {
            	if(FormulaParseLogger.isLogOn())
            		FormulaParseLogger.debug("��ʽ["+exp.getExpression()+"]"+"ִ�н��Ϊ:"+resobj);
                List rowvalues = (List) resobj;
                convertListToObjectArray(rowvalues, result, i, exp);
                updateVariableMap(exp.getLeftName(), rowvalues); //���²�����
                i++;
            } 
			else
			{
			    result[i] = new Object[1];
			    result[i][0] = resobj;
			    updateVariableMap(exp.getLeftName(), resobj);
			    i++;
			    if(FormulaParseLogger.isLogOn())
			    	FormulaParseLogger.debug("��ʽ["+exp.getExpression()+"]"+"ִ�н��Ϊ:"+resobj);
			}
			
        }
        //�ԷǷ���ʽ���ص�NULLֵ���д���
        result = dealResultValue(result);
        if (getError() != null)
        	FormulaParseLogger.error(getError());
        reStoreFormulaSet(); //��ԭ��ʽ��Ϣ
        FormulaThread.release();
        return result;
    }

    /**
     * ȡ�ù�ʽ��������һ�������깫ʽ���ʽ�����ú���Ӧ�������ɵ��ô˷����õ���ʽ��ֵ
     * <br><font color=red><b>��ʽ����Զ���ʽ����</b></font>
     * 
     * @return ��ά���飬��һά��Ӧ��ʽ����ĸ������ڶ�ά��Ӧ�����ĳ���<p>
     * �����й�ʽ:<br>
     * "a->b+c;d->4*a" <br>
     * ����b=[1,2,3,4],c=[2,2,2,2]<br>
     * ��ô����ֵΪһ��2*4�Ķ�ά���飬ÿһ��Ԫ��ΪInteger��<br>
     * ��һάΪa = [3,4, 5, 6]<br>
     * �ڶ�άΪc = [4,8,12,16]
     * @see public String[][] getValueSArray()
     */
    public Object[][] getValueOArray()
    {
        //��ʽ���ô���
//        FormulaCallInfo.getInstance().addComputenum();
        
        //�жϹ�ʽ����Ϊ��
        if(m_formulas == null || m_formulas.length == 0)
        {
        	FormulaParseLogger.error("�ڹ�ʽ��ֵǰ��Ҫ�����ù�ʽ���ʽ!");
            return null;
        }
        //��ʽ����ֵ�����Ϊ�գ����ع�ʽ����
        m_expressCount = formulavos.size();
        Object[][] result = new Object[m_expressCount][];
        
        FormulaParseLogger.debug("��ʼȡ��ʽ��ֵ...");
        //����Ƿ��д���
        if (m_bParseError)
        {
            reStoreFormulaSet(); //��ԭ��ʽ��Ϣ
            for (int i = 0; i < m_expressCount; i++)
            {
                result[i] = new Object[] {m_extendedFormulas[i]};
            }
            return result;
        }
        
        //�鿴��ǰ�߳��Ƿ��е�ǰ��ʽִ����
	    registerParserToCurrentThread();
        
        //���㹫ʽ��ֵ��ͬʱ���²�����
        for (int i=0;i<formulavos.size();)
        {
            JEPExpression exp = (JEPExpression) formulavos.get(i);
            Object resobj = (exp==null)?null:exp.getResult();
            if (resobj == null)
            {
            	FormulaParseLogger.debug("��ʽ["+(exp==null?"":exp.getExpression())+"]"+"ִ�н��Ϊnull");
                //�����������еĴ���
                if(exp!=null && exp.getErrorInfo()!=null){
                	//2007.11.12,append the error info of expression  to Formula error 
                	getErrorVO().appendError("\n"+exp.getErrorInfo());
                	FormulaParseLogger.error(exp.getErrorInfo());
                }
                result[i++] = null;
                continue;
            }
            else if (resobj instanceof List || resobj instanceof Vector) //�����List����ʽ
            {
            	if(FormulaParseLogger.isLogOn())
            		FormulaParseLogger.debug("��ʽ["+exp.getExpression()+"]"+"ִ�н��Ϊ:"+resobj);
                List rowvalues = (List) resobj;
                convertListToObjectArray(rowvalues, result, i, exp);
                updateVariableMap(exp.getLeftName(), rowvalues); //���²�����
                i++;
            } 
			else
			{
			    result[i] = new Object[1];
			    result[i][0] = resobj;
			    updateVariableMap(exp.getLeftName(), resobj);
			    i++;
			    if(FormulaParseLogger.isLogOn())
			    	FormulaParseLogger.debug("��ʽ["+exp.getExpression()+"]"+"ִ�н��Ϊ:"+resobj);
			}
			
        }
        //�ԷǷ���ʽ���ص�NULLֵ���д���
        result = dealResultValue(result);
        if (getError() != null)
        	FormulaParseLogger.error(getError());
        reStoreFormulaSet(); //��ԭ��ʽ��Ϣ
        FormulaThread.release();
        return result;
    }
    
    /**
     * ȡ�ö����ʽ��ֵ,��getValueOArray()��ͬ���ǣ��˷��������ؽ����ת��ΪString����<br>
     * V30�������������Ƽ�ʹ��
     * @return
     * @see public Object[][] getValueOArray()
     */
    public String[][] getValueSArray()
    {
        //���㹫ʽ��ֵ
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
     * ȡ�ö����ʽ��ֵ,��getValueOArray()��ͬ���ǣ��˷��������ؽ����ת��ΪString����<br>
     * V30�������������Ƽ�ʹ��
     * @return
     * @see public Object[][] getValueOArray()
     */
    public String[][] getValueSArray1()
    {
        //���㹫ʽ��ֵ
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
     * �Է���ֵ���д���
     * 
     * @param result
     */
    protected Object[][] dealResultValue(Object[][] result)
    {
        if (result == null)
            return null;
        boolean bAllNull = true;
        
        //�����ֵ
        for (int i = 0; i < result.length; i++) {
			if(result[i]!=null && result[i].length>valueNum)
				valueNum = result[i].length;
		}
        
        for (int i = 0; i < result.length; i++)
        {
            //��ֵ�Ĵ���
            if (result[i] == null || result[i].length == 0)
            {
                result[i] = new Object[valueNum];
            }
            //�Գ����ĳ��Ƚ��е���
            else if (result[i].length == 1 && result[i].length < valueNum)
            {
                Object saveobj = result[i][0];
                result[i] = new Object[valueNum];
                for (int j = 0; j < valueNum; j++)
                {
                    result[i][j] = saveobj;
                }
                bAllNull = false; //�зǿ�ֵ
            }
            else
            {
                bAllNull = false;
            }

            //�Բ�������ֵ���ͽ�������
            JEPExpression exp = (JEPExpression) formulavos.get(i);
            if (result[i] != null && result[i].length > 0)
            {
                for (int j = 0; j < result[i].length; j++)
                {
                    Object obj = result[i][j];
                    if (FormulaUtils.isParamNull(obj)) //����Ϊnull�Ļָ�
                    {
                            obj = null;
                    }
                    else if(obj instanceof NullZeroNumber)
                    {
                        obj = new Double(0);
                    }
                    else if (obj instanceof BooleanObject)
                        obj = ((BooleanObject) obj).toBoolean();

                    //���ȴ���--ȥ���������doubleתUFDouble��0�ᶪʧ��
                    if (obj instanceof Double)
                    {
                        //����Double����
                        obj = getScaleManager().getScaledValue(
                                exp.getLeftName(), (Double) obj);
                    }
                    else if (obj instanceof UFDouble)
                    {
                        //����UFdouble����
                        obj = getScaleManager().getScaledValue(
                                exp.getLeftName(), (UFDouble) obj);
                    }
                    result[i][j] = obj;
                }

            }
        }
        //NULLֵ����
        if(bAllNull) result = null;
        return result;
    }

    /**
     * ��ʼ����ʽִ����
     */
    protected void initEnvironment()
    {
    	//init variable
    	m_jepParser = new JEPExpressionParser(getOwnModuleID());
        //��������
        getScaleManager().setDefaultRoudingUp(BigDecimal.ROUND_HALF_UP);
    }

    /**
     * ��listתΪ����
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
        //�õ���ʽ����Ĳ������ȣ����ڽ���ֵ��ֵ����
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
     * ���²�����
     * 
     * @param rowvalues
     */
    protected synchronized void updateVariableMap(String varname, Object rowvalues)
    {
        m_jepParser.addVariable(varname, rowvalues);
    }

    /**
     * �˷������ڷ��ص�����ʽ�Ҳ���������������µ����������Զ���ʽ����<br>
     * �����ʽ�Ƕ������ֻ���ص�һ��<br>
     * �������Ϊ���飬Ҳֻ���ض�Ӧ�ڵ�һ��������������<br>
     * 
     * @return ��ʽ�����������巵�صĶ�������ȡ���ڹ�ʽ<br>
     * �����UFDouble����������ô�����ص�����UFDouble,<br>
     * ����������ʽ���򷵻ص���Boolean��
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
     * �˷������ڷ��ص�����ʽ�Ҳ���������������µ�������,���ַ�����ʽ����<br>
     * �����ʽ�Ƕ������ֻ���ص�һ��<br>
     * �������Ϊ���飬Ҳֻ���ض�Ӧ�ڵ�һ��������������<br>
     * 
     * @return ��ʽ������,����ת��Ϊ�ַ���
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
     * �˷������ڷ��ص�����ʽ��������,�Զ���ʽ����<br>
     * �����ʽ�Ƕ������ֻ���ص�һ��<br>
     * 
     * @return ��ʽ�����������巵�صĶ�������ȡ���ڹ�ʽ<br>
     * �����UFDouble����������ô�����ص�����UFDouble,<br>
     * ����������ʽ���򷵻ص���Boolean��
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
     * �˷������ڷ��ص�����ʽ��������,���ַ�����ʽ����<br>
     * �����ʽ�Ƕ������ֻ���ص�һ��<br>
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
     * ���ò�����,V3�Ͻӿڣ����Ƽ�ʹ�ã���Ҫ����
     * ������:<br><br>
     * <font color=red>public void setDataSArray(Map keyListMap)</font> ���ߣ�<p>
     * <font color=red>public void addVariable(String name, Object value)</font> ����.
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
     * ���ò����� ,��Objects��ʽ�������(��ΪUFDouble,Integer,String�ȵ�)<br>
     * ������ʾ���ʹ��(����fΪ��ʽ������ʵ��)��<p><code>
     *  List v1 = new ArrayList();<br>
        v1.add("1001AA10000000000DZY"); //row value<br>
        v1.add("1001AA10000000000DZY"); //row value<br>
        v1.add("1001AA10000000000DZY"); //row value<br>
        Map map = new HashMap();<br>
        map.put("pk_deptdoc", v1);<br>
        f.setDataSArray(map);<br></code>
     * @param keyListMap ����������hashmap
     */
    public void setDataSArray(Map keyListMap)
    {
        m_inputArgType = OBJECTTYPE;
        addVariablesFromMap(keyListMap);
    }

    /**
     * ���ⲿ���ܲ���
     * 
     * @param keyListMap
     */
    private void addVariablesFromMap(Map keyListMap)
    {
        //���²�����
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
                //�������Ĳ������Ͳ�֧��
                berror = true;
            }

            //�������Ĳ�������Ϊ0
            if (values == null || values.size() == 0 || berror)
            {
                //���±�����
                updateVariableMap(colName, null);
            }
            else
            {
                valueNum = values.size();
                //����������飬UFDOUBLEת��ΪDouble
                convertDataToKnownType(values);
                //���±�����
                updateVariableMap(colName, values);
            }

        }
    }

    /**
     * Ϊ��ʽ���ӱ���,�������ӱ����ķ�������������Ϊ�򵥶���<br>
     * Ҳ����Ϊ�������List<p>
     * ʹ�÷�����<br><code>
     *  List deptcode = new ArrayList();<br>
        deptcode.add("dept003");<br>
        f.addVariable("deptcode",deptcode);<br></code>
     * @param name ������
     * @param value ����ֵ ֧�ָ��ֻ������ͼ����飬List��
     * 
     */
    public void addVariable(String name, Object value)
    {
        m_inputArgType = OBJECTTYPE;
        updateVariableMap(name, convertDataToKnownType(value));
    }

    /**
     * Ϊ��ʽע���Զ��庯��
     * 
     * @param name ������
     * @param value ������ʵ����ʵ��,ʵ��PostfixMathCommandI�ӿڵ���
     */
    public void addFunction(String name, PostfixMathCommandI value)
    {
    	if(!nc.vo.jcom.lang.StringUtil.isEmpty(name))
    		m_jepParser.addFunction(name.toUpperCase(), value);
    }

    /**
     * ������Ķ�����һ����ת������������ֵ�Ĵ����Լ�����NC30�ϵĴ�ֵ��ʽ
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
                return new BooleanObject((Boolean) obj); //����Boolean�ʹ���
            }
            else if (obj instanceof String && m_inputArgType == STRINGTYPE) //�������ϵķ�ʽ������
            {
                String str = obj.toString();
                return StringUtil.getValueFromString(str);
            }
            return obj;
        }
        catch (ClassCastException e)
        {
            getErrorVO().appendError(FormulaError.ERR_ARGTYPEDIFFER);
            FormulaParseLogger.error("����������ʹ���!",e);
        }
        catch (Exception e)
        {
        	FormulaParseLogger.error("�Դ�������������ת��ʱ���ִ���!",e);
        }
        return null;
    }

	private Object dealObjectOfList(List obj)
	{
		List inlist = (List) obj;
		//�������Ĳ���ΪArrayList����Ϊ��
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
     * �������List��һ����ת��
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
     * �õ�������Ϣ
     * 
     * @return ������������VarryVO
     */
    public VarryVO getVarry()
    {
        VarryVO[] varvos = getVarryArray();
        if (varvos != null)
            return varvos[0];
        return null;
    }

    /**
     * �õ�ָ����ʽ��ı�����Ϣ
     * 
     * @return ������������VarryVO
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
        if (npos == -1) //û�����ù���ʽ���߹�ʽ��û�ҵ�
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
     * �õ����й�ʽ�����еı�����Ϣ
     * 
     * @return VarryVO[]
     */
    public VarryVO[] getVarryArray()
    {
    	FormulaParseLogger.debug("��ʼ�ӹ�ʽ��ȡ�ñ���...");
        VarryVO[] resvos = new VarryVO[m_expressCount];
        if (m_formulas == null || formulavos == null || formulavos.size()==0)
            return null;
        for (int i = 0; i < m_expressCount; i++)
        {
            if (formulavos.get(i) != null)
            {
                resvos[i] = ((JEPExpression) formulavos.get(i)).getVarryVO();
//                FormulaParseLogger.debug("��ʽ����" + i + Arrays.asList(resvos[i].getVarry()));
            }
        }
        FormulaParseLogger.debug("���ع�ʽ�еı���!");
        return resvos;
    }

    /**
     * ����ʽ������ע���Զ��庯����Ϣ�����ⲿ������ķ�����ע�ắ��
     * 
     * @param className ����
     * @param methodName ������
     * @param returnType ����ֵ����
     * @param argTypes ������������
     */
    public void setSelfMethod(String className, String methodName,
            Class returnType, Class[] argTypes)
    {
       setSelfMethod(className, methodName, returnType, argTypes,true);
    }

    /**
     * ע�ắ����JEP
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
     * �����Զ��庯����Ϣ�� �������ڣ�(2001-7-9 15:01:50)
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
            //���ص�JEP
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
            //���ص�JEP
            addFunctionToJepParser(methodName, className,isNullAsZero);
        }
        catch (Exception e)
        {
        	FormulaParseLogger.error(e.getMessage(),e);
            getErrorVO().appendError(FormulaError.ERR_CLASSNOTFOUND);
        }

    }
    

    /**
     * �����Զ��庯����Ϣ�� �������ڣ�(2001-7-9 15:01:50)
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
            //���ص�JEP
            addFunctionToJepParser(method, className,true);
        }
        catch (Exception e)
        {
            FormulaParseLogger.error(e.getMessage(),e);
            getErrorVO().appendError(FormulaError.ERR_CLASSNOTFOUND);
        }
    }

    /**
     * �ӵ��Զ��庯��������
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
     * ���÷��ؽ���ľ���
     * 
     * @param scale
     */
    public void setScale(int scale)
    {
        getScaleManager().setDefaultScale(scale);
    }

    /**
     * ���÷��ؽ���ľ���
     * 
     * @param scale
     */
    public void setScale(int scale, int roundingup)
    {
        getScaleManager().setDefaultScale(scale);
        getScaleManager().setDefaultRoudingUp(roundingup);
    }

    /**
     * ���ñ����ķ��ؾ���
     * 
     * @param varname
     * @param scale
     */
    public void setScale(String varname, int scale)
    {
        getScaleManager().addVarScale(varname, scale);
    }

    /**
     * ���ñ����ķ��ؾ���
     * 
     * @param varname
     * @param scale
     */
    public void setScale(String varname, int scale, int roundingup)
    {
        getScaleManager().addVarScale(varname, scale, roundingup);
    }

    /**
     * �˴����뷽��˵���� ���ܣ����ò����� �������ڣ�(2001-4-25 8:51:15)
     * 
     * @param varryData
     *            java.util.Hashtable
     */
    public void setDataS(java.util.Hashtable varryData)
    {
        setDataSArray(new java.util.Hashtable[] { varryData });
    }

    /**
     * ���ù�ʽ����,V3�Ͻӿڣ���������ʹ��<br>
     * ��ʹ��:<br>
     * public void addVariable(String name, Object value) ����<br>
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
                        varryData.put(key, canShu); //���ò�����
                    }
                    catch (Exception ex)
                    {
                    	FormulaParseLogger.error(ex.getMessage(),ex);
                        getErrorVO().appendError("��ʾ����ֵ���󣡲���:" + key);
                    }
                }
            }
        }
        setDataS(varryData);
    }

    /**
     * NULL�Ƿ�0�á� �������ڣ�(2001-7-9 15:13:42)
     * 
     * @return boolean
     */
    public boolean isNullAsZero()
    {
        return m_bNullAsZero;
    }

    /**
     * ����NULL�Ƿ���Ե�0�á� �������ڣ�(2001-7-9 15:13:42)
     * 
     * @param newNullAsZero
     *            boolean
     */
    public void setNullAsZero(boolean newNullAsZero)
    {
        m_bNullAsZero = newNullAsZero;
    }

    /**
     * ���ַ������߼�",ʹ֮��Ϊ���ַ���
     * V3�Ϸ�������������ʹ��,����NC30�ӿ�
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
     * ���ַ������߼�",ʹ֮��Ϊ���ַ���
     * V3�Ϸ�������������ʹ��,����NC30�ӿ�
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
     * ����ʽ������ԭ��
     * ������ԵĻ���Ӧ��ͳһ������Щ��Ϣ
     *
     */
    public void buildReStorePoint()
    {
    	FormulaParseLogger.debug("����ʽ������������ԭ��...");
        if(m_parserInfoPool == null)
            m_parserInfoPool = new HashMap();
        //������Ҫ��������Ϣ
        m_parserInfoPool.put("nullaszero",new Boolean(isNullAsZero()));
        m_parserInfoPool.put("cachetype",new Integer(getCacheType()));
        m_parserInfoPool.put("nodb",new Boolean(isNeedDoPostConvert()));
    }
    
    /**
     * ��ԭ��ʽ��Ϣ
     *
     */
    public void reStoreFormulaSet()
    {
    	FormulaParseLogger.debug("��ԭ��ʽ��Ϣ...");
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
	 * ���ձ����ڴ�
	 * @since V56
	 */
	public void garbage() {
		//���ձ����ڴ�
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
     * �õ������Ա���,��̨Ĭ��Ϊ��������,����ǰ̨����
     * @return
     */
    public String getLangCode()
    {
    	return "simpchn";
    }
    
    /**
     * V3�Ϸ�������������ʹ��
     * @return
     * @deprecated
     */
    public String getStarts()
    {
        return StringUtil.START_ST;
    }

    /**
     * V3�Ϸ�������������ʹ��
     * @return
     * @deprecated
     */
    public String getEnds()
    {
        return StringUtil.END_ST;
    }
    
    /**
     * �滻��ʽ��ı������õ��滻��Ĺ�ʽ�ַ���
     * @param replacedFormula - ���滻�Ĺ�ʽ�ַ���
     * @param varnames - map<ԭ������,�滻������>
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