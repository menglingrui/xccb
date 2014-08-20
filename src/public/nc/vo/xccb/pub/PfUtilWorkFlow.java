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
 * ���յ� ������������
 * @author mlr
 */
public class PfUtilWorkFlow {
	/** ��ǰ�����ڵ��������� */
	private static int m_iCheckResult = IApproveflowConst.CHECK_RESULT_PASS;
	/** fgj2001-11-27 �жϵ�ǰ�����Ƿ�ִ�гɹ� */
	private static boolean m_isSuccess = true;
	/**
	 * �����������������true��֮false;
	 */
	private static boolean m_checkFlag = true;
	/**
	 * ������������ �����
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
		// 2.�鿴��չ�������Ƿ���Ҫ��������صĽ�������
		PfUtilWorkFlowVO workflowVo = null;
		Object paramNoApprove = eParam == null ? null : eParam.get(PfUtilBaseTools.PARAM_NOAPPROVE);
		if (paramNoApprove == null && (isSaveAction(actionName) || isApproveAction(actionName))) {
			workflowVo = actionAboutApproveflow(parent, actionName, billType, currentDate, billvo, eParam);
			if (!m_isSuccess)
				return null;
		} else if (isStartAction(actionName) || isSignalAction(actionName)) {
			//3.��������صĽ�������
			workflowVo = actionAboutWorkflow(parent, actionName, billType, currentDate, billvo, eParam);
			if (!m_isSuccess)
				return null;
		}
		if (workflowVo == null) {
			//��鲻����������̨�����ٴμ��
			if (eParam == null)
				eParam = new HashMap<String, String>();
			eParam.put(PfUtilBaseTools.PARAM_NOTE_CHECKED, PfUtilBaseTools.PARAM_NOTE_CHECKED);
		}
		return paramNoApprove;	
	}
	/**
	 * ��������������ʱ,��Ҫ��ָ����Ϣ
	 * <li>����ѡ���̻�����ߡ�ѡ���̷�֧ת��
	 */
	private static PfUtilWorkFlowVO checkOnStart(Container parent, String actionName,
			String billType, String currentDate, AggregatedValueObject billVo, Stack dlgResult,
			HashMap hmPfExParams) throws BusinessException {
		PfUtilWorkFlowVO wfVo = NCLocator.getInstance().lookup(IWorkflowMachine.class)
				.checkWorkitemOnSave(actionName, billType, currentDate, billVo, hmPfExParams);

		if (wfVo != null) {
			// �õ���ָ�ɵ���Ϣ
			Vector assignInfos = wfVo.getTaskInfo().getAssignableInfos();
			Vector tSelectInfos = wfVo.getTaskInfo().getTransitionSelectableInfos();
			if (assignInfos.size() > 0 || tSelectInfos.size() > 0) {
				// ��ʾָ�ɶԻ����ռ�ʵ��ָ����Ϣ
				WFStartDispatchDialog wfdd = new WFStartDispatchDialog(parent, wfVo, billVo);
				int iClose = wfdd.showModal();
				if (iClose == UIDialog.ID_CANCEL)
					dlgResult.push(new Integer(iClose));
			}
		}
		return wfVo;
	}
	/**
	 * ��������صĽ�������
	 * @throws BusinessException 
	 */
	private static PfUtilWorkFlowVO actionAboutWorkflow(Container parent, String actionName,
			String billType, String currentDate, AggregatedValueObject billvo, HashMap eParam)
			throws BusinessException {
		PfUtilWorkFlowVO workflowVo = null;

		if (isStartAction(actionName)) {
			Logger.debug("*��������=" + actionName + "����鹤����");
			Stack dlgResult = new Stack();
			workflowVo = checkOnStart(parent, actionName, billType, currentDate, billvo, dlgResult,
					eParam);
			if (dlgResult.size() > 0) {
				m_isSuccess = false;
				Logger.debug("*�û�ָ��ʱ�����ȡ������ֹͣ����������");
			}
		} else if (isSignalAction(actionName)) {
			Logger.debug("*ִ�ж���=" + actionName + "����鹤����");
			// ���õ����Ƿ��ڹ�������
			workflowVo = checkWorkitemWhenSignal(parent, actionName, billType, currentDate, billvo,
					eParam);
			if (workflowVo != null) {
				if (workflowVo.getIsCheckPass()) {
					// XXX::����Ҳ��Ϊ����ͨ����һ��,��Ҫ�����ж� lj+
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
				Logger.debug("*�û�����������ʱ�����ȡ������ִֹͣ�й�����");
			}
		}
		return workflowVo;
	}
	/**
	 * ��鵱ǰ�����Ƿ��ڹ��������У������н���
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
					// ���ش����Ĺ�����
					m_checkFlag = true;
					wfVo = clientWorkFlow.getWorkFlow();
				} else {
					// �û�ȡ��
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
	 * �ж�ĳ���ݶ��������Ƿ�Ϊ"ִ�й�����"����
	 * 
	 * @param actionName ��������
	 * @return
	 */
	private static boolean isSignalAction(String actionName) {
		int leng = IPFActionName.SIGNAL.length();
		return actionName.length() >= leng
				&& actionName.toUpperCase().substring(0, leng).equals(IPFActionName.SIGNAL);
	}
	/**
	 * �ж�ĳ���ݶ��������Ƿ�Ϊ"����������"����
	 * 
	 * @param actionName ��������
	 * @return
	 */
	private static boolean isStartAction(String actionName) {
		String strUpperName = actionName.toUpperCase();
		return strUpperName.endsWith(IPFActionName.START);
	}

	/**
	 * ��������صĽ�������
	 * @throws BusinessException 
	 */
	private static PfUtilWorkFlowVO actionAboutApproveflow(Container parent, String actionName,
			String billType, String currentDate, AggregatedValueObject billvo, HashMap eParam)
			throws BusinessException {
		PfUtilWorkFlowVO workflowVo = null;

		if (isSaveAction(actionName)) {
			Logger.debug("*�ύ����=" + actionName + "�����������");
			// ���Ϊ�ύ������������Ҫ�ռ��ύ�˵�ָ����Ϣ������ͳһ�������� lj@2005-4-8
			Stack dlgResult = new Stack();
			workflowVo = checkOnSave(parent, IPFActionName.SAVE, billType, currentDate, billvo,
					dlgResult, eParam);
			if (dlgResult.size() > 0) {
				m_isSuccess = false;
				Logger.debug("*�û�ָ��ʱ�����ȡ������ֹͣ����");
			}
		} else if (isApproveAction(actionName)) {
			Logger.debug("*��������=" + actionName + "�����������");
			// ���õ����Ƿ����������У����ռ������˵�������Ϣ
			workflowVo = checkWorkitemWhenApprove(parent, actionName, billType, currentDate, billvo,
					eParam);
			if (workflowVo != null) {
				if ("Y".equals(workflowVo.getApproveresult())) {
					m_iCheckResult = IApproveflowConst.CHECK_RESULT_PASS;
				} else if("R".equals(workflowVo.getApproveresult())) {
					// XXX::����Ҳ��Ϊ����ͨ����һ��,��Ҫ�����ж� lj+
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
				Logger.debug("*�û�����ʱ�����ȡ������ֹͣ����");
			}
		}
		return workflowVo;
	}
	/**
	 * ��鵱ǰ�����Ƿ������������У������н���
	 */
	private static PfUtilWorkFlowVO checkWorkitemWhenApprove(Container parent, String actionName,
			String billType, String currentDate, AggregatedValueObject billVo, HashMap hmPfExParams)
			throws BusinessException {
		PfUtilWorkFlowVO wfVo = null;
		WorkFlowCheckDlg clientWorkFlow = null;
		try {
			if(hmPfExParams != null && hmPfExParams.get(PfUtilBaseTools.PARAM_BATCH) != null) {
				//��鵥���Ƿ����������������û�ж��壬�򲻵���
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

//			if (clientWorkFlow.showModal() == UIDialog.ID_OK) { // ����û�����
//				// ���������Ĺ�����
//				m_checkFlag = true;
//				wfVo = clientWorkFlow.getWorkFlow();
//			} else { // �û�������
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

			//ʵ����
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
					Logger.error("�޷�ʵ����ǰ̨ҵ������billType=" + wfVo.getBillType() + ",className=" + bt2VO.getClassname(),
							e);
				}
			}
		}
		return null;
		
	}
	/**
	 * �ύ����ʱ,��Ҫ��ָ����Ϣ
	 * <li>ֻ��"SAVE","EDIT"�����ŵ���
	 */
	private static PfUtilWorkFlowVO checkOnSave(Container parent, String actionName, String billType,
			String currentDate, AggregatedValueObject billVo, Stack dlgResult, HashMap hmPfExParams)
			throws BusinessException {
		PfUtilWorkFlowVO wfVo = new PfUtilWorkFlowVO();
		//������������ȡָ����Ϣ��ֱ�ӷ���
		if(hmPfExParams != null && hmPfExParams.get(PfUtilBaseTools.PARAM_BATCH) != null)
			return wfVo;
		
		wfVo = NCLocator.getInstance().lookup(IWorkflowMachine.class)
				.checkWorkitemOnSave(actionName, billType, currentDate, billVo, hmPfExParams);
		
		//�������������ʾ֮ǰ������ҵ����
		PFClientBizRetObj retObj = executeBusinessPlugin(parent, billVo, wfVo, true);
		if(retObj != null && retObj.isStopFlow()){
			m_isSuccess = false;
			return null;
		}
		if (wfVo != null) {
			// �õ���ָ�ɵ���������
			Vector assignInfos = wfVo.getTaskInfo().getAssignableInfos();
			if (assignInfos != null && assignInfos.size() > 0) {
				// ��ʾָ�ɶԻ����ռ�ʵ��ָ����Ϣ
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
	 * �ж�ĳ���ݶ��������Ƿ�Ϊ"����"����
	 * <li>����"APPROVE"��ͷ
	 * @param actionName
	 * @return
	 */
	public static boolean isApproveAction(String actionName) {
		return actionName.length() >= 7
				&& actionName.toUpperCase().substring(0, 7).equals(IPFActionName.APPROVE);
	}
	/**
	 * �ж�ĳ���ݶ��������Ƿ�Ϊ"�ύ"��"�༭"����
	 * <li>����"SAVE"��"EDIT"��β
	 * @param actionName ��������
	 * @return
	 */
	public static boolean isSaveAction(String actionName) {
		String strUpperName = actionName.toUpperCase();
		return strUpperName.endsWith(IPFActionName.SAVE) || strUpperName.endsWith(IPFActionName.EDIT);
	}
}
