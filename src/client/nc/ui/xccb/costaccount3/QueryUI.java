package nc.ui.xccb.costaccount3;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nc.bs.dao.BaseDAO;
import nc.bs.dao.DAOException;
import nc.bs.logging.Logger;
import nc.itf.zmpub.pub.ISonVO;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.ui.pub.beans.MessageDialog;
import nc.ui.pub.beans.UIButton;
import nc.ui.pub.beans.UIPanel;
import nc.ui.pub.bill.BillEditEvent;
import nc.ui.pub.bill.BillEditListener;
import nc.ui.pub.bill.BillEditListener2;
import nc.ui.pub.bill.BillListPanel;
import nc.ui.trade.business.HYPubBO_Client;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.scm.pu.PuPubVO;
import nc.vo.scm.pub.vosplit.SplitBillVOs;
import nc.vo.xccb.costaccount.AggCostAccountVO;
import nc.vo.xccb.costaccount.CostAccoutBVO;
/**
 * @author zpm
 */
public class QueryUI extends nc.ui.pub.beans.UIDialog implements
		ActionListener, ListSelectionListener, BillEditListener,
		BillEditListener2 {

	/**
	 * 
	 */
	private BaseDAO dao=null;
	public BaseDAO getDao() {
	if (dao == null) {
		dao = new BaseDAO();
	}
	return dao;
}
	private static final long serialVersionUID = 1L;

	private ClientUI myClientUI;

	private JPanel ivjUIDialogContentPane = null;

	protected BillListPanel ivjbillListPanel = null;

	private String m_pkcorp = null;

	private String m_operator = null;

	private String m_billType = null;

	private UIPanel ivjPanlCmd = null;

	private UIButton ivjbtnOk = null;

	private UIButton ivjbtnCancel = null;

	private UIButton btn_addline = null;

	private UIButton btn_deline = null;
	
//	zhf add 增加虚拟托盘的绑定实际托盘功能按钮   绑定
	private UIButton ivjbtnLock = null;

	private Map<String, List<CostAccoutBVO>> map = null;

	private boolean isEdit = true;
	
	private String pk_ware = null;//仓库
	
	private boolean isSign = false;//是否签字通过

	public QueryUI(String m_billType, String m_operator,
			String m_pkcorp, String m_nodeKey, ClientUI myClientUI,
			boolean isEdit) {
		super();
		this.myClientUI = myClientUI;
		this.m_billType = m_billType;
		this.m_operator = m_operator;
		this.m_pkcorp = m_pkcorp;
		this.isEdit = isEdit;
		init();
	}

	private void init() {
		setName("BillSourceUI");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(750, 550);
		setTitle("查看分配信息");
		setContentPane(getUIDialogContentPane());
		// 设置编缉状态
		setEdit();
		//
//		getbtnOk().addActionListener(this);
//		getbtnLock().addActionListener(this);
//		getbtnCancel().addActionListener(this);
//		getAddLine().addActionListener(this);
//		getDeline().addActionListener(this);
		getbillListPanel().addEditListener(this);
		getbillListPanel().addBodyEditListener(this);
		getbillListPanel().getHeadTable().getSelectionModel()
				.addListSelectionListener(this);
		getbillListPanel().getBodyScrollPane("xew_costaccount_b")
				.addEditListener2(this);
		// 加载表头数据
		loadHeadData();
	}

	public void setEdit() {
		getbillListPanel().setEnabled(isEdit);
//		getbtnCancel().setEnabled(true);
//		getbtnCancel().setEnabled(isEdit);
//		getbtnOk().setEnabled(isEdit);
//		getAddLine().setEnabled(isEdit);
//		getDeline().setEnabled(isEdit);
		
//		getbtnLock().setEnabled(isEdit||!isSign);
	}

	public void loadHeadData() {
		try {
			AggCostAccountVO billvo = null;
			if(isEdit){
				billvo = (AggCostAccountVO) myClientUI.getVOFromUI();
			}else{
				billvo = (AggCostAccountVO)myClientUI.getBufferData().getCurrentVO();
			}
			
			if (billvo != null) {
				getbillListPanel().setHeaderValueVO(billvo.getChildrenVO());
				getbillListPanel().getHeadBillModel().execLoadFormula();
				//setBodys(billvo.getChildrenVO());
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}
   /**
    * 设置子表数据
    * @param childrenVO
    */
	private void setBodys(CircularlyAccessibleValueObject[] bvos) {
		if(bvos==null || bvos.length==0)
			return;
	    if(bvos[0] instanceof ISonVO){
	    	ISonVO vo=(ISonVO) bvos[0];
	    	if(vo.getSonVOS()!=null&&vo.getSonVOS().size()>0)
	    	getbillListPanel().setBodyValueVO((CircularlyAccessibleValueObject[]) vo.getSonVOS().toArray(new SuperVO[0]));
	    }
		
	}

	// 增行，孙表默认值
	public void setBodyDefaultValue(int row) {}
	

	public Map<String, List<CostAccoutBVO>> getBufferData() {
		if (map == null) {
			map = cloneBufferData();
		}
		return map;
	}

	public Map<String, List<CostAccoutBVO>> cloneBufferData()  {
		AggCostAccountVO billvo=	(AggCostAccountVO) myClientUI.getBufferData().getCurrentVO();
		if(billvo==null){
			myClientUI.showErrorMessage("请选中查看记录");
			return null;
		}
		String vbillno=PuPubVO.getString_TrimZeroLenAsNull(billvo.getParentVO().getAttributeValue("vbillno"));
		Map<String, List<CostAccoutBVO>> map2 = new HashMap<String, List<CostAccoutBVO>>();
		try {
			
			CostAccoutBVO[] cvos=(CostAccoutBVO[] )HYPubBO_Client.queryByCondition(CostAccoutBVO.class, " vlastbillcode ='"+vbillno+"' and isnull(dr,0)=0");
			if (cvos!=null&&cvos.length>0) {
				CostAccoutBVO[][] bvos = (CostAccoutBVO[][]) SplitBillVOs.getSplitVOs(cvos, new String[]{"vlastbillrowid"});
				for (int i=0;i<bvos.length;i++) {
					CostAccoutBVO[] vos=bvos[i];
					List<CostAccoutBVO> blist = new ArrayList<CostAccoutBVO>();
					if(vos!=null&&vos.length>0){						
						for(int j=0;j<vos.length;j++){
							blist.add(vos[j]);
						}
					}
					map2.put(vos[0].getVlastbillrowid(), cloneBBVO(blist));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return map2;
//		if (map1.size() > 0) {
//			Iterator<String> it = map1.keySet().iterator();
//			while (it.hasNext()) {
//				String key = it.next();
//				List<CostAccoutBVO> list = new ArrayList<CostAccoutBVO>();
//				Map<String,CostAccoutBVO> bmap=map1.get(key);
//				if(bmap!=null&&bmap.size()>0){
//					for(String bkey:bmap.keySet()){
//						list.add(bmap.get(bkey));
//					}
//				}
//				map2.put(key, cloneBBVO(list));
//			}
//		}
//		return map2;
		
		
	}

	public List<CostAccoutBVO> cloneBBVO(List<CostAccoutBVO> list) {
		List<CostAccoutBVO> list1 = new ArrayList<CostAccoutBVO>();
		if (list != null && list.size() > 0) {
			for (CostAccoutBVO b : list) {
				list1.add((CostAccoutBVO) b.clone());
			}
		}
		return list1;
	}

	protected CostAccoutBVO getHeadBVO(int row) {
		CostAccoutBVO vo = (CostAccoutBVO) getbillListPanel().getHeadBillModel()
				.getBodyValueRowVO(row, CostAccoutBVO.class.getName());
		return vo;
	}

	protected CostAccoutBVO getBodyVO(int row) {
		CostAccoutBVO vo = (CostAccoutBVO) getbillListPanel()
				.getBodyBillModel().getBodyValueRowVO(row,
						CostAccoutBVO.class.getName());
		return vo;
	}

	protected int getBodyCurrentRow() {
		int row = getbillListPanel().getBodyTable().getRowCount() - 1;
		return row;
	}

	protected int getHeadCurrentRow() {
		int row = getbillListPanel().getHeadTable().getSelectedRow();
		return row;
	}

	// 增行
	protected void onLineAdd() {
		getbillListPanel().getBodyBillModel().addLine();	
	}



	// 删行
	protected void onLineDel() {
		int[] rows = getbillListPanel().getBodyTable().getSelectedRows();
		getbillListPanel().getBodyBillModel().delLine(rows);
	}

	protected BillListPanel getbillListPanel() {
		if (ivjbillListPanel == null) {
			try {
				ivjbillListPanel = new BillListPanel();
				ivjbillListPanel.setName("billListPanel");
				ivjbillListPanel.loadTemplet(m_billType, null, m_operator,
						m_pkcorp);
				ivjbillListPanel.getHeadTable().setSelectionMode(
						ListSelectionModel.SINGLE_INTERVAL_SELECTION);// 单选
				ivjbillListPanel.getBodyTable().setSelectionMode(
						ListSelectionModel.SINGLE_INTERVAL_SELECTION);// 单选
				ivjbillListPanel.getChildListPanel().setTotalRowShow(true);
				ivjbillListPanel.setEnabled(true);
			} catch (java.lang.Throwable e) {
				Logger.error(e.getMessage(), e);
			}
		}
		return ivjbillListPanel;
	}

	protected JPanel getUIDialogContentPane() {
		if (ivjUIDialogContentPane == null) {
			ivjUIDialogContentPane = new JPanel();
			ivjUIDialogContentPane.setName("UIDialogContentPane");
			ivjUIDialogContentPane.setLayout(new BorderLayout());
			ivjUIDialogContentPane.add(getbillListPanel(), "Center");
			ivjUIDialogContentPane.add(getPanlCmd(), BorderLayout.SOUTH);
		}
		return ivjUIDialogContentPane;
	}

	private UIPanel getPanlCmd() {
		if (ivjPanlCmd == null) {
			ivjPanlCmd = new UIPanel();
			ivjPanlCmd.setName("PanlCmd");
			ivjPanlCmd.setPreferredSize(new Dimension(0, 40));
			ivjPanlCmd.setLayout(new FlowLayout());
//			ivjPanlCmd.add(getAddLine(), getAddLine().getName());
//			ivjPanlCmd.add(getDeline(), getDeline().getName());
//			ivjPanlCmd.add(getbtnOk(), getbtnOk().getName());
//			ivjPanlCmd.add(getbtnCancel(), getbtnCancel().getName());
//			ivjPanlCmd.add(getbtnLock(),getbtnLock().getName());
		}
		return ivjPanlCmd;
	}

//	private UIButton getbtnOk() {
//		if (ivjbtnOk == null) {
//			ivjbtnOk = new UIButton();
//			ivjbtnOk.setName("btnOk");
//			ivjbtnOk.setText("确定");
//		}
//		return ivjbtnOk;
//	}
//
//	private UIButton getAddLine() {
//		if (btn_addline == null) {
//			btn_addline = new UIButton();
//			btn_addline.setName("addline");
//			btn_addline.setText("增行");
//		}
//		return btn_addline;
//	}
//
//	private UIButton getDeline() {
//		if (btn_deline == null) {
//			btn_deline = new UIButton();
//			btn_deline.setName("deline");
//			btn_deline.setText("删行");
//		}
//		return btn_deline;
//	}
//
//	private UIButton getbtnCancel() {
//		if (ivjbtnCancel == null) {
//			ivjbtnCancel = new UIButton();
//			ivjbtnCancel.setName("btnCancel");
//			ivjbtnCancel.setText("取消");
//		}
//		return ivjbtnCancel;
//	}
	
	// 添加绑定按钮
//	private UIButton getbtnLock() {
//		if (ivjbtnLock == null) {
//			ivjbtnLock = new UIButton();
//			ivjbtnLock.setName("ivjbtnLock");
//			ivjbtnLock.setText("绑定");
//		}
//		return ivjbtnLock;
//	}

	public void actionPerformed(ActionEvent e) {
//		if (e.getSource().equals(getbtnOk())) {
//			try {
//				saveCurrentData(getHeadCurrentRow());
//				check();
//				closeOK();
//			} catch (Exception e1) {
//				MessageDialog.showErrorDlg(this, "警告", e1.getMessage());
//			}
//		} else if (e.getSource().equals(getbtnCancel())) {
//			closeCancel();
//		} else if (e.getSource().equals(getAddLine())) {
//			onLineAdd();
//		} else if (e.getSource().equals(getDeline())) {
//			onLineDel();
//		}else if(e.getSource().equals(getbtnLock())){
//		//	onLock();
//		}
	}

	public void saveCurrentData(int row) {
		if (row < 0) {
			return;
		}
		CostAccoutBVO bvo = getHeadBVO(row);
		String key = bvo.getCrowno();
		CostAccoutBVO[] bvos = (CostAccoutBVO[]) getbillListPanel()
				.getBodyBillModel().getBodyValueVOs(
						CostAccoutBVO.class.getName());
		if (bvos != null && bvos.length > 0) {
			getBufferData().put(key, arrayToList(bvos));
		} else {
			getBufferData().remove(key);
		}
	}

	public void check() throws BusinessException {

	}




	// 根据行号找VO
	public CostAccoutBVO getGenBVO(String crowno) {
		CostAccoutBVO bvo = null;
		int row = getbillListPanel().getHeadBillModel().getRowCount();
		for (int i = 0; i < row; i++) {
			Object o = getbillListPanel().getHeadBillModel().getValueAt(i,
					"crowno");
			if (o.equals(crowno)) {
				bvo = getHeadBVO(i);
			}
		}
		return bvo;
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == getbillListPanel().getHeadTable()
				.getSelectionModel()) {
			// 备份数据
			// 重新加载表体数据
		}
	}

	// 取托盘最大容积
	public void calcMaxTray() {
		// 目前通过公式自动查询出来
	}

	public boolean beforeEdit(BillEditEvent e) {
		String key = e.getKey();
		int row = e.getRow();
		return true;
	}

	/**
	 * 
	 * @作者：lyf
	 * @说明：完达山物流项目 获得当前已经使用的托盘ID
	 * @时间：2011-5-4下午02:42:38
	 * @param curRow
	 * @return
	 */
	private String getSubSql(int curRow) {
		StringBuffer sql = new StringBuffer();
		sql.append("('aa'");
		int rowCount = getbillListPanel().getBodyTable().getRowCount();
		for (int i = 0; i < rowCount; i++) {
			if (i == curRow)
				continue;
			String cdt_id = PuPubVO
					.getString_TrimZeroLenAsNull(getbillListPanel()
							.getBodyBillModel().getValueAt(i, "cdt_pk"));// 托盘id
			if (cdt_id == null)
				continue;
			sql.append(",'" + cdt_id + "'");
		}
		sql.append(")");
		return sql.toString();
	}

	public void afterEdit(BillEditEvent e) {
		String key = e.getKey();
		int row = e.getRow();
		saveCurrentData(getHeadCurrentRow());
	}

	public ArrayList<CostAccoutBVO> arrayToList(CostAccoutBVO[] o) {
		if (o == null || o.length == 0)
			return null;
		ArrayList<CostAccoutBVO> list = new ArrayList<CostAccoutBVO>();
		for (CostAccoutBVO s : o) {
			list.add(s);
		}
		return list;
	}

	public void bodyRowChange(BillEditEvent e) {
		if (e.getSource() == getbillListPanel().getParentListPanel().getTable()) {
			// 备份数据
			int oldrow = e.getOldRow();
			if (oldrow >= 0) {
			//	saveCurrentData(oldrow);
			}
			// 清空表体数据
			getbillListPanel().getBodyBillModel().clearBodyData();
			// 重新加载表体数据
			int row = e.getRow();
			CostAccoutBVO newbvo = getHeadBVO(row);
		//	String key2 = newbvo.getCrowno();
            String pk=newbvo.getPrimaryKey();

			ArrayList<CostAccoutBVO> list = (ArrayList<CostAccoutBVO>)getBufferData().get(pk);
			if(list !=null && list.size() > 0){
				getbillListPanel().getBodyBillModel().setBodyDataVO(list.toArray(new CostAccoutBVO[0]));			
				getbillListPanel().getBodyBillModel().execLoadFormula();				
			}			
		}
		
	}

	public boolean isEdit() {
		return isEdit;
	}

	public void setEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}
	
	private String getkey(int row){
		return PuPubVO.getString_TrimZeroLenAsNull(getBodyValue(row, "pk_proaccept_bb"))+","+
		PuPubVO.getString_TrimZeroLenAsNull(getBodyValue(row, "crowno"));
	}
	
	private Object getBodyValue(int row,String fieldname){
		return getbillListPanel().getBodyBillModel().getValueAt(row, fieldname);
	}
	
	

	
	private Object getHeadValue(String fieldname){
		if(PuPubVO.getString_TrimZeroLenAsNull(fieldname)==null)
			return null;
		int row = getbillListPanel().getHeadTable().getSelectedRow();
		if(row < 0)
			return null;
		return getbillListPanel().getHeadBillModel().getValueAt(row, fieldname);
	}	

}
