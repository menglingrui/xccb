package nc.vo.xccb.pub;

import java.util.List;
import nc.bs.dao.BaseDAO;
import nc.bs.dap.out.IAccountProcMsgInBulk;
import nc.ui.xew.costaccount.AggCostAccountVO;
import nc.ui.xew.costaccount.CostAccountVO;
import nc.ui.xew.costaccount.CostAccoutBVO;
import nc.vo.dap.voucher.MsgAggregatedStruct;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
/**
 * 会计平台业务重算类
 * @author mlr
 */
public class AccountProcBO implements IAccountProcMsgInBulk,nc.bs.dap.out.IAccountProcMsg{
    BaseDAO dao=new BaseDAO();
	public MsgAggregatedStruct[] queryDataByProcIds(String billTypeOrProc,
			String[] procMsg) throws BusinessException {
		MsgAggregatedStruct[] msgs = new MsgAggregatedStruct[procMsg.length];
		if (billTypeOrProc != null && procMsg != null && procMsg.length > 0) {
			for (int i = 0; i < procMsg.length; i++) {
				String pk_h = procMsg[i];
				List list = (List) dao.retrieveByClause(CostAccountVO.class,
						" pk_billtype='" + billTypeOrProc
								+ "' and pk_costaccount='" + pk_h + "' and isnull(dr,0)=0 ");
				CostAccountVO hvo = (CostAccountVO) list.get(0);

				List listb = (List) dao.retrieveByClause(CostAccoutBVO.class,
						" pk_costaccount='" + pk_h + "' and isnull(dr,0)=0 ");

				CostAccoutBVO[] bovs = (CostAccoutBVO[]) listb
						.toArray(new CostAccoutBVO[0]);
				AggCostAccountVO billvo = new AggCostAccountVO();
				billvo.setParentVO(hvo);
				billvo.setChildrenVO(bovs);
				try {
					msgs[i] = new MsgAggregatedStruct(billvo, pk_h);
				} catch (Exception e) {
					e.printStackTrace();
					throw new BusinessException(e);
				}
			}

		}
		return msgs;
	}
	public AggregatedValueObject queryDataByProcId(String billTypeOrProc,
			String procMsg) throws BusinessException {
		String pk_h = procMsg;
		List list = (List) dao.retrieveByClause(CostAccountVO.class,
				" pk_billtype='" + billTypeOrProc
						+ "' and pk_costaccount='" + pk_h + "' and isnull(dr,0)=0 ");
		CostAccountVO hvo = (CostAccountVO) list.get(0);

		List listb = (List) dao.retrieveByClause(CostAccoutBVO.class,
				" pk_costaccount='" + pk_h + "' and isnull(dr,0)=0 ");

		CostAccoutBVO[] bovs = (CostAccoutBVO[]) listb
				.toArray(new CostAccoutBVO[0]);
		AggCostAccountVO billvo = new AggCostAccountVO();
		billvo.setParentVO(hvo);
		billvo.setChildrenVO(bovs);

		return billvo;
	}

}
