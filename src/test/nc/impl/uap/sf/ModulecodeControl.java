package nc.impl.uap.sf;

public class ModulecodeControl {
	
	/**
	 * 
	 * @author lyf
	 * @˵�� 
	 * @param moduleCode
	 *  ���ܽڵ��
	 * @param dsName
	 * @param userId
	 * @return
	 * @ʱ�� 2013-1-14����03:44:30
	 *
	 */
	public static  boolean isMySelfNode(String moduleCode, String dsName, String userId){
//		if("3618601005".equalsIgnoreCase(moduleCode) 
//				||"3618601006".equalsIgnoreCase(moduleCode) 
//				||"3618601045".equalsIgnoreCase(moduleCode)){
//			return true;
//		}
		if(moduleCode.startsWith("2002AA") || moduleCode.startsWith("DT")){
			return true;
		}
		return false;
	}

}
