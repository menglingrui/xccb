package nc.vo.xccb.pub;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import nc.bs.framework.common.NCLocator;
import nc.bs.logging.Logger;
import nc.bs.pf.pub.PfDataCache;
import nc.bs.pub.pf.PfUtilTools;
import nc.itf.uap.pf.IWorkflowDefine;
import nc.itf.uap.pf.IWorkflowMachine;
import nc.ui.pub.beans.UIDialog;
import nc.ui.pub.pf.DispatchDialog;
import nc.ui.pub.pf.WorkFlowCheckDlg;
import nc.ui.pub.pf.dispatch.WFStartDispatchDialog;
import nc.ui.pub.pf.dispatch.WFWorkitemAcceptDlg;
import nc.vo.pf.change.PfUtilBaseTools;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.billtype2.Billtype2VO;
import nc.vo.pub.billtype2.ExtendedClassEnum;
import nc.vo.pub.pf.CurrencyInfo;
import nc.vo.pub.pf.IPFClientBizProcess;
import nc.vo.pub.pf.PFClientBizRetObj;
import nc.vo.pub.pf.PfClientBizProcessContext;
import nc.vo.pub.pf.PfUtilWorkFlowVO;
import nc.vo.pub.pf.workflow.IPFActionName;
import nc.vo.wfengine.definition.IApproveflowConst;
import nc.vo.wfengine.pub.WFTask;
/**
 * 验收单 变更审批流检查
 * @author mlr
 */
public class PfUtilWorkFlow {
	/** 当前审批节点的审批结果 */
	private static int m_iCheckResult = IApproveflowConst.CHECK_RESULT_PASS;
	/** fgj2001-11-27 判断当前动作是否执行成功 */
	private static boolean m_isSuccess = true;
	/**
	 * 审批变量如果审批则true反之false;
	 */
	private static boolean m_checkFlag = true;
	/**
	 * 变更审批流检查 入口类
	 * @author mlr
	 * @param parent
	 * @param actionName
	 * @param billType
	 * @param currentDate
	 * @param billvo
	 * @param userObj
	 * @param strBeforeUIClass
	 * @param checkVo
	 * @param eParam
	 * @return
	 * @throws BusinessException
	 */
	public  static Object checkWorkFlow(Container parent, String actionName, String billType,
			String currentDate, AggregatedValueObject billvo, Object userObj, String strBeforeUIClass,
			AggregatedValueObject checkVo, HashMap eParam) throws BusinessException{
		// 2.查看扩展参数，是否需要审批流相关的交互处理
		PfUtilWorkFlowVO workflowVo = null;
		Object paramNoApprove = eParam == null ? null : eParam.get(PfUtilBaseTools.PARAM_NOAPPROVE);
		if (paramNoApprove == null && (isSaveAction(actionName) || isApproveAction(actionName))) {
			workflowVo = actionAboutApproveflow(parent, actionName, billType, currentDate, billvo, eParam);
			if (!m_isSuccess)
				return null;
		} else if (isStartAction(actionName) || isSignalAction(actionName)) {
			//3.工作流相关的交互处理
			workflowVo = actionAboutWorkflow(parent, actionName, billType, currentDate, billvo, eParam);
			if (!m_isSuccess)
				return null;
		}
		if (workflowVo == null) {
			//检查不到工作项，则后台无需再次检查
			if (eParam == null)
				eParam = new HashMap<String, String>();
			eParam.put(PfUtilBaseTools.PARAM_NOTE_CHECKED, PfUtilBaseTools.PARAM_NOTE_CHECKED);
		}
		return paramNoApprove;	
	}
	/**
	 * 单据启动工作流时,需要的指派信息
	 * <li>包括选择后继活动参与者、选择后继分支转移
	 */
	private static PfUtilWorkFlowVO checkOnStart(Container parent, String actionName,
			String billType, String currentDate, AggregatedValueObject billVo, Stack dlgResult,
			HashMap hmPfExParams) throws BusinessException {
		PfUtilWorkFlowVO wfVo = NCLocator.getInstance().lookup(IWorkflowMachine.class)
				.checkWorkitemOnSave(actionName, billType, currentDate, billVo, hmPfExParams);

		if (wfVo != null) {
			// 得到可指派的信息
			Vector assignInfos = wfVo.getTaskInfo().getAssignableInfos();
			Vector tSelectInfos = wfVo.getTaskInfo().getTransitionSelectableInfos();
			if (assignInfos.size() > 0 || tSelectInfos.size() > 0) {
				// 显示指派对话框并收集实际指派信息
				WFStartDispatchDialog wfdd = new WFStartDispatchDialog(parent, wfVo, billVo);
				int iClose = wfdd.showModal();
				if (iClose == UIDialog.ID_CANCEL)
					dlgResult.push(new Integer(iClose));
			}
		}
		return wfVo;
	}
	/**
	 * 工作流相关的交互处理
	 * @throws BusinessException 
	 */
	private static PfUtilWorkFlowVO actionAboutWorkflow(Container parent, String actionName,
			String billType, String currentDate, AggregatedValueObject billvo, HashMap eParam)
			throws BusinessException {
		PfUtilWorkFlowVO workflowVo = null;

		if (isStartAction(actionName)) {
			Logger.debug("*启动动作=" + actionName + "，检查工作流");
			Stack dlgResult = new Stack();
			workflowVo = checkOnStart(parent, actionName, billType, currentDate, billvo, dlgResult,
					eParam);
			if (dlgResult.size() > 0) {
				m_isSuccess = false;
				Logger.debug("*用户指派时点击了取消，则停止启动工作流");
			}
		} else if (isSignalAction(actionName)) {
			Logger.debug("*执行动作=" + actionName + "，检查工作流");
			// 检查该单据是否处于工作流中
			workflowVo = checkWorkitemWhenSignal(parent, actionName, billType, currentDate, billvo,
					eParam);
			if (workflowVo != null) {
				if (workflowVo.getIsCheckPass()) {
					// XXX::驳回也作为审批通过的一种,需要继续判断 lj+
					WFTask currTask = workflowVo.getTaskInfo().getTask();
					if (currTask.getTaskType() == WFTask.TYPE_BACKWARD) {
						if (currTask.isBackToFirstActivity())
							m_iCheckResult = IApproveflowConst.CHECK_RESULT_REJECT_FIRST;
						else
							m_iCheckResult = IApproveflowConst.CHECK_RESULT_REJECT_LAST;
					} else
						m_iCheckResult = IApproveflowConst.CHECK_RESULT_PASS;
				} else
					m_iCheckResult = IApproveflowConst.CHECK_RESULT_NOPASS;
			} else if (!m_checkFlag) {
				m_isSuccess = false;
				Logger.debug("*用户驱动工作流时点击了取消，则停止执行工作流");
			}
		}
		return workflowVo;
	}
	/**
	 * 检查当前单据是否处于工作流程中，并进行交互
	 */
	private static PfUtilWorkFlowVO checkWorkitemWhenSignal(Container parent, String actionName,
			String billType, String currentDate, AggregatedValueObject billVo, HashMap hmPfExParams)
			throws BusinessException {
		PfUtilWorkFlowVO wfVo = null;
		WFWorkitemAcceptDlg clientWorkFlow = null;
		try {
			wfVo = NCLocator.getInstance().lookup(IWorkflowMachine.class).checkWorkFlow(actionName,
					billType, currentDate, billVo, hmPfExParams);
			if (wfVo == null) {
				m_checkFlag = true;
				return wfVo;
			} else {
				String billId = wfVo.getTaskInfo().getTask().getBillID();
				CurrencyInfo ci = wfVo.getHmCurrency().get(billId);
				boolean bHasMoney = ci != null && ci.isShowMoney();
				clientWorkFlow = new WFWorkitemAcceptDlg(parent, wfVo, bHasMoney, billVo);

				if (clientWorkFlow.showModal() == UIDialog.ID_OK) {
					// 返回处理后的工作项
					m_checkFlag = true;
					wfVo = clientWorkFlow.getWorkFlow();
				} else {
					// 用户取消
					m_checkFlag = false;
					wfVo = null;
				}
			}
		} finally {
			if (clientWorkFlow != null) {
				nc.ui.pub.beans.UIComponentUtil.removeAllComponentRefrence(clientWorkFlow);
			}
		}
		return wfVo;
	}
	/**
	 * 判断某单据动作编码是否为"执行工作流"动作
	 * 
	 * @param actionName 动作编码
	 * @return
	 */
	private static boolean isSignalAction(String actionName) {
		int leng = IPFActionName.SIGNAL.length();
		return actionName.length() >= leng
				&& actionName.toUpperCase().substring(0, leng).equals(IPFActionName.SIGNAL);
	}
	/**
	 * 判断某单据动作编码是否为"启动工作流"动作
	 * 
	 * @param actionName 动作编码
	 * @return
	 */
	private static boolean isStartAction(String actionName) {
		String strUpperName = actionName.toUpperCase();
		return strUpperName.endsWith(IPFActionName.START);
	}

	/**
	 * 审批流相关的交互处理
	 * @throws BusinessException 
	 */
	private static PfUtilWorkFlowVO actionAboutApproveflow(Container parent, String actionName,
			String billType, String currentDate, AggregatedValueObject billvo, HashMap eParam)
			throws BusinessException {
		PfUtilWorkFlowVO workflowVo = null;

		if (isSaveAction(actionName)) {
			Logger.debug("*提交动作=" + actionName + "，检查审批流");
			// 如果为提交动作，可能需要收集提交人的指派信息，这里统一动作名称 lj@2005-4-8
			Stack dlgResult = new Stack();
			workflowVo = checkOnSave(parent, IPFActionName.SAVE, billType, currentDate, billvo,
					dlgResult, eParam);
			if (dlgResult.size() > 0) {
				m_isSuccess = false;
				Logger.debug("*用户指派时点击了取消，则停止送审");
			}
		} else if (isApproveAction(actionName)) {
			Logger.debug("*审批动作=" + actionName + "，检查审批流");
			// 检查该单据是否处于审批流中，并收集审批人的审批信息
			workflowVo = checkWorkitemWhenApprove(parent, actionName, billType, currentDate, billvo,
					eParam);
			if (workflowVo != null) {
				if ("Y".equals(workflowVo.getApproveresult())) {
					m_iCheckResult = IApproveflowConst.CHECK_RESULT_PASS;
				} else if("R".equals(workflowVo.getApproveresult())) {
					// XXX::驳回也作为审批通过的一种,需要继续判断 lj+
					WFTask currTask = workflowVo.getTaskInfo().getTask();
					if (currTask != null && currTask.getTaskType() == WFTask.TYPE_BACKWARD) {
						if (currTask.isBackToFirstActivity())
							m_iCheckResult = IApproveflowConst.CHECK_RESULT_REJECT_FIRST;
						else
							m_iCheckResult = IApproveflowConst.CHECK_RESULT_REJECT_LAST;
					}
				} else
					m_iCheckResult = IApproveflowConst.CHECK_RESULT_NOPASS;
			} else if (!m_checkFlag) {
				m_isSuccess = false;
				Logger.debug("*用户审批时点击了取消，则停止审批");
			}
		}
		return workflowVo;
	}
	/**
	 * 检查当前单据是否处于审批流程中，并进行交互
	 */
	private static PfUtilWorkFlowVO checkWorkitemWhenApprove(Container parent, String actionName,
			String billType, String currentDate, AggregatedValueObject billVo, HashMap hmPfExParams)
			throws BusinessException {
		PfUtilWorkFlowVO wfVo = null;
		WorkFlowCheckDlg clientWorkFlow = null;
		try {
			if(hmPfExParams != null && hmPfExParams.get(PfUtilBaseTools.PARAM_BATCH) != null) {
				//检查单据是否定义了审批流，如果没有定义，则不弹出
				IWorkflowDefine wfDef = NCLocator.getInstance().lookup(IWorkflowDefine.class);
				if(!wfDef.hasFlowDefinition(actionName, billType, billVo, hmPfExParams)){
					m_checkFlag = true;
					return wfVo;
				}
				wfVo = new PfUtilWorkFlowVO();
				clientWorkFlow = new WorkFlowCheckDlg(parent, wfVo, false);
			}else {
				wfVo = NCLocator.getInstance().lookup(IWorkflowMachine.class).checkWorkFlow(actionName,
						billType, currentDate, billVo, hmPfExParams);
				if (wfVo == null) {
					m_checkFlag = true;
					return wfVo;
				} else {
					PFClientBizRetObj retObj = executeBusinessPlugin(parent, billVo, wfVo, false);
					boolean isShowPass = true;
					String hintMessage = null;
					if(retObj != null){
						isShowPass = retObj.isShowPass();
						hintMessage = retObj.getHintMessage();
					}
					
					String billId = wfVo.getTaskInfo().getTask().getBillID();
					CurrencyInfo ci = wfVo.getHmCurrency().get(billId);
					boolean bHasMoney = ci != null && ci.isShowMoney();
//					clientWorkFlow = new WorkFlowCheckDlg(parent, wfVo, bHasMoney);
//					clientWorkFlow.setShowPass(isShowPass);
//					clientWorkFlow.setHintMessage(hintMessage);
//					clientWorkFlow.setCheckNote(hintMessage);
				}
			}

//			if (clientWorkFlow.showModal() == UIDialog.ID_OK) { // 如果用户审批
//				// 返回审批的工作项
//				m_checkFlag = true;
//				wfVo = clientWorkFlow.getWorkFlow();
//			} else { // 用户不审批
//				m_checkFlag = false;
//				wfVo = null;
//			}
		} finally {
			if (clientWorkFlow != null) {
				nc.ui.pub.beans.UIComponentUtil.removeAllComponentRefrence(clientWorkFlow);
			}
		}
		return wfVo;
	}
	private static PFClientBizRetObj executeBusinessPlugin(Container parent, AggregatedValueObject billVo, PfUtilWorkFlowVO wfVo, boolean isMakeBill) {
		if(wfVo != null && wfVo.getApplicationArgs() != null){
			ArrayList<Billtype2VO> bt2VOs = PfDataCache.getBillType2Info(wfVo.getBillType(),
					ExtendedClassEnum.PROC_CLIENT.getIntValue());

			//实例化
			for (Iterator iterator = bt2VOs.iterator(); iterator.hasNext();) {
				Billtype2VO bt2VO = (Billtype2VO) iterator.next();
				try {
					Object obj = PfUtilTools.findBizImplOfBilltype(wfVo.getBillType(), bt2VO.getClassname());
					PfClientBizProcessContext context = new PfClientBizProcessContext();
					context.setBillvo(billVo);
					context.setArgsList(wfVo.getApplicationArgs());
					context.setMakeBill(isMakeBill);
					return ((IPFClientBizProcess)obj).execute(parent, context);
				} catch (Exception e) {
					Logger.error("无法实例化前台业务插件类billType=" + wfVo.getBillType() + ",className=" + bt2VO.getClassname(),
							e);
				}
			}
		}
		return null;
		
	}
	/**
	 * 提交单据时,需要的指派信息
	 * <li>只有"SAVE","EDIT"动作才调用
	 */
	private static PfUtilWorkFlowVO checkOnSave(Container parent, String actionName, String billType,
			String currentDate, AggregatedValueObject billVo, Stack dlgResult, HashMap hmPfExParams)
			throws BusinessException {
		PfUtilWorkFlowVO wfVo = new PfUtilWorkFlowVO();
		//是批处理，不用取指派信息，直接返回
		if(hmPfExParams != null && hmPfExParams.get(PfUtilBaseTools.PARAM_BATCH) != null)
			return wfVo;
		
		wfVo = NCLocator.getInstance().lookup(IWorkflowMachine.class)
				.checkWorkitemOnSave(actionName, billType, currentDate, billVo, hmPfExParams);
		
		//在审批处理框显示之前，调用业务处理
		PFClientBizRetObj retObj = executeBusinessPlugin(parent, billVo, wfVo, true);
		if(retObj != null && retObj.isStopFlow()){
			m_isSuccess = false;
			return null;
		}
		if (wfVo != null) {
			// 得到可指派的输入数据
			Vector assignInfos = wfVo.getTaskInfo().getAssignableInfos();
			if (assignInfos != null && assignInfos.size() > 0) {
				// 显示指派对话框并收集实际指派信息
				DispatchDialog dd = new DispatchDialog(parent);
				dd.initByWorkflowVO(wfVo);
				int iClose = dd.showModal();
				if (iClose == UIDialog.ID_CANCEL)
					dlgResult.push(new Integer(iClose));
			}
		}
		return wfVo;
	}

	/**
	 * 判断某单据动作编码是否为"审批"动作
	 * <li>即以"APPROVE"开头
	 * @param actionName
	 * @return
	 */
	public static boolean isApproveAction(String actionName) {
		return actionName.length() >= 7
				&& actionName.toUpperCase().substring(0, 7).equals(IPFActionName.APPROVE);
	}
	/**
	 * 判断某单据动作编码是否为"提交"或"编辑"动作
	 * <li>即以"SAVE"或"EDIT"结尾
	 * @param actionName 动作编码
	 * @return
	 */
	public static boolean isSaveAction(String actionName) {
		String strUpperName = actionName.toUpperCase();
		return strUpperName.endsWith(IPFActionName.SAVE) || strUpperName.endsWith(IPFActionName.EDIT);
	}
}
