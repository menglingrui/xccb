package nc.ui.pf.changedir;

import nc.ui.pf.change.VOConversionUI;
import nc.vo.pf.change.UserDefineFunction;

/**
 * ��� ����->��̽�������յ�,���﹤�����յ�
 * 
 * @author dp
 * 
 */
public class CHGQYOTATODRILLVO extends VOConversionUI {
	/**
	 * CHG20TO21 ������ע�⡣
	 */
	public CHGQYOTATODRILLVO() {
		super();
	}
	/**
	* ��ú������ȫ¼�����ơ�
	* @return java.lang.String[]
	*/
	public String getAfterClassName() {
		return null;
	}
	/**
	* �����һ���������ȫ¼�����ơ�
	* @return java.lang.String[]
	*/
	public String getOtherClassName() {
		return null;
	}
	/**
	 * ����ֶζ�Ӧ��
	 * @return java.lang.String[]
	 */
	public String[] getField() {
		return new String[] {
				// "H_pk_corp->H_pk_corp",//��˾
				 "H_pk_invbasdoc->H_pk_invbasdoc",//�����������id
				 "H_pk_invmandoc->H_pk_invmandoc",//����id
				 "H_num->H_quantity"//����
				 
			};
	}
	/**
	* ��ù�ʽ��
	* @return java.lang.String[]
	*/
	public String[] getFormulas() {
	  return null;
	}
	/**
	* �����û��Զ��庯����
	*/
	public UserDefineFunction[] getUserDefineFunction() {
		return null;
	}
}
