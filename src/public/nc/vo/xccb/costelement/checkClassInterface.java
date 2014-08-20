package nc.vo.xccb.costelement;

import java.io.Serializable;

import nc.vo.trade.pub.IBDGetCheckClass2;

public class checkClassInterface implements IBDGetCheckClass2,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8251436265601954259L;

	public String getCheckClass() {
		return "nc.bs.xccb.costelement.CostElementBO";
	}
	public String getUICheckClass() {
		return "nc.ui.xccb.costelement.ClientCheckCHK";
	}
}
