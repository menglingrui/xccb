package nc.impl.uap.sf;

import java.util.Enumeration;
import java.util.Hashtable;

import nc.bs.framework.common.InvocationInfoProxy;
import nc.bs.framework.common.NCLocator;
import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
import nc.bs.sm.identityverify.StaticPasswordIAMode;
import nc.bs.sm.login.AccountXMLUtil;
import nc.bs.sm.login.FindUserUtil;
import nc.bs.sm.login.LoginAppBean;
import nc.bs.uap.lock.PKLock;
import nc.bs.uap.sf.excp.SMVerifyException;
import nc.bs.uap.sf.excp.SystemFrameworkException;
import nc.bs.uap.sf.facility.SFServiceFacility;
import nc.itf.uap.bd.corp.ICorpQry;
import nc.itf.uap.cil.ICilService;
import nc.itf.uap.sf.ISMVerifyService;
import nc.vo.bd.CorpVO;
import nc.vo.pub.BusinessException;
import nc.vo.sm.UserVO;
import nc.vo.sm.log.OperatelogVO;
import nc.vo.sm.login.Constant;
import nc.vo.sm.login.LoginFailureInfo;
import nc.vo.sm.login.LoginSessBean;
import nc.vo.sm.login.NCEnv;

/**
 * 此处插入类型说明。 创建日期：(2003-5-15 13:15:20)
 * 
 * @author：李充蒲
 */
public class SMVerifyImpl implements ISMVerifyService {
	/**
	 * SMVerifyBO 构造子注解。
	 */
	public SMVerifyImpl() {
		super();
	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-5-15 14:48:50)
	 * 
	 * @return int
	 * @param moduleCode
	 *            java.lang.String
	 * @param dsName
	 *            java.lang.String
	 * @param userId
	 *            java.lang.String
	 * @exception java.rmi.RemoteException
	 *                异常说明。
	 */
	public int checkModuleLience(String moduleCode, String dsName, String userId)
			throws SMVerifyException {
		try {
			//lyf2013-01-14 
			if(ModulecodeControl.isMySelfNode(moduleCode, dsName, userId)){
				return 0;
			}
			//lyf 2013-01-14
			if (NCEnv.isToControlProductLicense()) {
				/**
				 * 写回0,表示可以打开 写回1,表示产品使用达到最大授权数 写回2，表示登录帐套为demo版，已经过了使用期限
				 */
				if (getLoginAppBean().isLegitimateProduct(
						LoginAppBean.getProductCode(moduleCode))
						&& getLoginAppBean().canOpenModule(
								LoginAppBean.getProductCode(moduleCode),
								userId)) {
					// 增加一个使用该产品的用户：
					try {
						// nc.ui.pub.services0.ServiceProvider0BO_Client.addProductUser(LoginAppBean.getProductLineCode(moduleCode),
						// userId, dsName);
						// new
						// nc.bs.pub.services0.ServiceProvider0BO().addProductUser(LoginAppBean.getProductLineCode(moduleCode),
						// userId, dsName);
						SFServiceFacility
								.getServiceProviderService()
								.addProductUser(
										LoginAppBean
												.getProductCode(moduleCode),
										userId, dsName);
					} catch (Exception e) {
						Logger.error("Error",e);
						// out.writeObject(new Integer(1));
						return 1;
					}
					return 0;
					// out.writeObject(new Integer(0));
				} else {
					return 1;
					// out.writeObject(new Integer(1));
				}
			} else {
				return 0;
				// out.writeObject(new Integer(0));
			}
		} catch (Exception e) {
			Logger.error("Error",e);
			throw new SMVerifyException(e.getMessage());
		}
	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-5-15 14:08:52)
	 * 
	 * @return int
	 * @param dsName
	 *            java.lang.String
	 * @param sid
	 *            java.lang.String
	 * @exception java.rmi.RemoteException
	 *                异常说明。
	 */
	public int connectTest(String dsName, String sid) throws SMVerifyException {
		try {
			Object obj = getLoginAppBean().getLoginUser(dsName).get(sid);
			if (obj != null) {
				LoginSessBean lsb = (LoginSessBean) obj;
				if (lsb.isLogin()) {
					lsb.setVoteTime(System.currentTimeMillis());
					return Constant.CONNECTTEST_EXIST;
				} else {
					// 删除用户的loginsessBean
					getLoginAppBean().getLoginUser(dsName).remove(sid);
					return Constant.CONNECTTEST_EXIST_GOTOUT;
				}
			} else {
				return Constant.CONNECTTEST_NOEXIST;
			}
		} catch (Exception e) {
			Logger.error("Error",e);
			throw new SMVerifyException(e.getMessage());
		}
	}

	/**
	 * 得到loginAppBean的唯一实例。 创建日期：(2003-5-15 13:27:00)
	 * 
	 * @return nc.bs.sm.login.LoginAppBean
	 */
	private LoginAppBean getLoginAppBean() {
		return LoginAppBean.getInstance();
	}

	/**
	 * 此处插入方法说明。 创建日期：(2003-7-16 14:44:48)
	 * 
	 * @return nc.vo.sm.login.LoginSessBean
	 * @param sid
	 *            java.lang.String
	 */
	public LoginSessBean getLoginSessBean(String sid, String dsName)
			throws SMVerifyException {
		LoginSessBean lsb = (LoginSessBean) getLoginAppBean().getLoginUser(
				dsName).get(sid);
		return lsb;
	}

	/**
	 * 恢复登录信息。 创建日期：(2003-5-29 11:17:07)
	 * 
	 * @param lsb
	 *            nc.vo.sm.login.LoginSessBean
	 * @exception java.rmi.RemoteException
	 *                异常说明。
	 */
	public boolean resumeLoginSessBean(LoginSessBean lsb)
			throws SMVerifyException {
		try {
			String sid = lsb.getSID();
			String dsName = lsb.getDataSourceName();
			Object obj = getLoginAppBean().getLoginUser(dsName).get(sid);

			if (obj == null) {
				Hashtable ht = getLoginAppBean().getLoginUser(dsName);
				Enumeration enumKey = ht.keys();
				while (enumKey.hasMoreElements()) {
					String s = (String) enumKey.nextElement();
					LoginSessBean l = (LoginSessBean) ht.get(s);
					if (l.isLogin() && l.getUserId().equals(lsb.getUserId())) {
						// 用户已存在
						return false;
					}
				}

				lsb.setVoteTime(System.currentTimeMillis());
				getLoginAppBean().getLoginUser(dsName).put(sid, lsb);
				SFServiceFacility.getServiceProviderService().addLoginUser(lsb);
				Logger.debug("客户端恢复登录信息：sid ='" + sid + "' dsName='"
						+ dsName + "' userName ='" + lsb.getUserName() + "'");
				return true;
			} else {
				return true;
			}
		} catch (Exception e) {
			Logger.error("Error",e);
			throw new SMVerifyException(e.getMessage());
		}

	}
	
	public void stopUser(String dsName, String userId, String sid,String sysflag) throws SMVerifyException{
		InvocationInfoProxy.getInstance().setSysid(Byte.valueOf(sysflag));
		stopUser(dsName, userId, sid);		
	}

	/**
	 * 用于在系统监视器中终止某个用户。 创建日期：(2003-5-29 13:42:06)
	 * 
	 * @param dsName
	 *            java.lang.String
	 * @param userId
	 *            java.lang.String
	 * @param sid
	 *            java.lang.String
	 * @exception java.rmi.RemoteException
	 *                异常说明。
	 */
	public void stopUser(String dsName, String userId, String sid)
			throws SMVerifyException {
		try {
			String s = "stop user : dsName ='" + dsName
					+ "' userId='" + userId + "' sid='" + sid + "'";
			Logger.debug(s);
			if (sid != null && dsName != null) {
				LoginSessBean lsb = (LoginSessBean) getLoginAppBean()
						.getLoginUser(dsName).get(sid);
				if (lsb != null) {
					// 将登录标志置为false
					lsb.setIsLogin(false);
					// 解除业务锁
					PKLock.getInstance().releaseLocks(userId, dsName);
					// 删除用户注册的信息
					SFServiceFacility.getServiceProviderService()
							.removeLoginUser(dsName, userId);

					//
					// if (Env.isDebug()) {
					Logger.debug("=======================");
					Logger.debug("User stop success: " + userId);
					Logger.debug("=======================");
					// }
				} else {
					Logger.debug("在终止用户时，没有找到该用户的登录信息");
				}
			}
		} catch (Exception e) {
			Logger.error("Error",e);
			throw new SMVerifyException(e.getMessage());
		}
	}

	/**
	 * 该方法响应前台更新登录公司的请求。
	 * 
	 * 创建日期：(2004-2-16 14:51:24)
	 * 
	 * @param newCorp
	 *            nc.vo.bd.CorpVO
	 * @param dsName
	 *            java.lang.String
	 * @param userId
	 *            java.lang.String
	 * @param sid
	 *            java.lang.String
	 * @exception java.rmi.RemoteException
	 *                异常说明。
	 */
	public void updateLoginCorpInfo(nc.vo.bd.CorpVO newCorp, String dsName,
			String userId, String sid) throws SMVerifyException {
		try {
			if (sid != null && dsName != null) {
				LoginSessBean lsb = (LoginSessBean) getLoginAppBean()
						.getLoginUser(dsName).get(sid);
				if (lsb != null) {
					// 更新用户的loginsessBean中公司的信息
					lsb.setCorpCode(newCorp.getUnitcode());
					lsb.setCorpName(newCorp.getUnitname());
					lsb.setPk_corp(newCorp.getPk_corp());
					//
					if ("0001".equals(newCorp.getPk_corp())) {
						lsb.setUserType(LoginSessBean.ACCOUNT_ADM);
					} else {
						lsb.setUserType(LoginSessBean.USER);
					}
					// 解除该用户的业务锁
					PKLock.getInstance().releaseLocks(userId, null);
					// 删除该用户注册的信息并重新注册
					SFServiceFacility.getServiceProviderService()
							.removeLoginUser(dsName, userId);
					SFServiceFacility.getServiceProviderService().addLoginUser(
							lsb);
					// new
					// nc.bs.pub.services0.ServiceProvider0BO().removeLoginUser(dsName,
					// userId);
					// new
					// nc.bs.pub.services0.ServiceProvider0BO().addLoginUser(lsb);
					//
				} else {
					// System.out.println("服务器上没有用户的相关纪录");
				}
			}
		} catch (Exception e) {
			Logger.error("Error",e);
			throw new SMVerifyException(
					"nc.bs.sm.login.SMVerifyBO.updateLoginCorpInfo(nc.vo.bd.CorpVO newCorp, String dsName, String userId, String sid) Exception :"
							+ e.getMessage());
		}

	}

	/**
	 * 客户端调用注销用户信息。 创建日期：(2003-5-15 13:21:25)
	 * 
	 * @param dsName
	 *            java.lang.String
	 * @param userId
	 *            java.lang.String
	 * @param sid
	 *            java.lang.String
	 * @exception java.rmi.RemoteException
	 *                异常说明。
	 */
	public void userLogout(String dsName, String userId, String sid)
			throws SMVerifyException {
		try {
			String msg = nc.bs.ml.NCLangResOnserver.getInstance().getStrByID(
					"smcomm",
					"UPP1005-000006",
					null,
					new String[] { dsName + "' userId='" + userId + "' sid='"
							+ sid + "'" });// /*@res
			Logger.debug(msg);
			// 用于客户端注销登录信息
			if (sid != null && dsName != null) {
				LoginSessBean lsb = (LoginSessBean) getLoginAppBean()
						.getLoginUser(dsName).get(sid);
				if (lsb != null) {
					OperatelogVO log = (OperatelogVO) lsb.get("_login_log_");// 该值在LoginAppBean::onLoginSuccess方法中设置
					if (log != null) {
						// String logoutTime = new
						// ServiceProviderBO().getServerTime().toString();
						String logoutTime = SFServiceFacility
								.getServiceProviderService().getServerTime()
								.toString();
						log.setLogoutTime(logoutTime);
						SFServiceFacility.getOperateLogService().update(log);
					}
					// 删除用户的loginsessBean
					getLoginAppBean().getLoginUser(dsName).remove(sid);
					// 解除业务锁
					PKLock.getInstance().releaseLocks(userId, null);
					// 删除用户注册的信息
					SFServiceFacility.getServiceProviderService()
							.removeLoginUser(dsName, userId);
					//
					String outMsg = nc.bs.ml.NCLangResOnserver.getInstance()
							.getStrByID(
									"smcomm",
									"UPP1005-000007",
									null,
									new String[] { dsName + "' userId='"
											+ userId + "' sid='" + sid + "'" });// /*@res
					Logger.debug(outMsg);
					Logger.debug("=======================");
					Logger.debug("User removed: " + userId);
					Logger.debug("=======================");
				} else {
					// System.out.println("服务器上没有用户的相关纪录");
				}
			}
		} catch (Exception e) {
			Logger.error("Error",e);
			throw new SMVerifyException(
					"nc.bs.sm.login.SMVerifyBO.userLogout(String dsName, String userId, String sid) Exception :"
							+ e.getMessage());
		}
	}

	public boolean isUsedGLBook() throws SMVerifyException {
		ICilService iILicenseService =  NCLocator.getInstance().lookup(ICilService.class);
		return iILicenseService.isUsedGLBook();

	}
	
	/**
	 * 登录nc,返回登录认证的结果
	 * @param lsb
	 * @return
	 * @throws BusinessException
	 */
    public Object[] login(LoginSessBean lsb) throws SMVerifyException {
        try {
            lsb.setUserIp(InvocationInfoProxy.getInstance().getClientHost());//(request.getRemoteAddr());
            lsb.setRemoteHost(InvocationInfoProxy.getInstance().getClientHost());//lsb.getUserIp()
            lsb.setServerName(InvocationInfoProxy.getInstance().getServerHost());//request.getServerName()
            lsb.setServerPort(InvocationInfoProxy.getInstance().getServerPort());//request.getServerPort()            

            Object[] objs = new Object[2];
            int result = getLoginAppBean().login(lsb);
            objs[0] = new Integer(result); 
            if (result == LoginFailureInfo.LOGIN_SUCCESS) {
                objs[1] = lsb;
            } 
            return objs;
       } catch (SystemFrameworkException e) {
            Logger.error("Error",e);
            throw new  SMVerifyException(e.getMessage());
       }
    }

    /* （非 Javadoc）
     * @see nc.itf.uap.sf.ISMVerifyService#verifyLoginInfo(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public int verifyLoginInfo(String langCode, String accountCode, String pkCorp, String workdate, String userCode, String pwd) throws SystemFrameworkException {
		LoginSessBean lsb = new LoginSessBean();
		lsb.setAccountId(accountCode);
		String dsName = AccountXMLUtil.findDsNameByAccountCode(accountCode);
		if(dsName == null)
		    throw new SMVerifyException("账套编码无效:"+accountCode);
		
        InvocationInfoProxy.getInstance().setUserDataSource(dsName);
        InvocationInfoProxy.getInstance().setDefaultDataSource(dsName);		
        lsb.setDataSourceName(dsName);
//		lsb.setCorpCode(corpCode);
        if(pkCorp == null || "null".equals(pkCorp) || pkCorp.equals(""))
            pkCorp = "0001";
        lsb.setPk_corp(pkCorp);
		if(pkCorp.equals("0001")){
            lsb.setCorpCode("0001");
		}else if(!dsName.equals("")){
		    ICorpQry corpQry = (ICorpQry)NCLocator.getInstance().lookup(ICorpQry.class.getName());
//		    CorpVO cond = new CorpVO();
//		    cond.setUnitcode(corpCode);
		    CorpVO corp;
            try {
//                corps = corp.queryCorpVOByVO(cond, new Boolean(true));
                corp = corpQry.findCorpVOByPK(pkCorp);
                if(corp != null){
    		        String corpCode =  corp.getUnitcode();
//    		        lsb.setPk_corp(pkCorp);
                    lsb.setCorpCode(corpCode);
    		    }else{
    		        throw new SMVerifyException("公司主键无效:pkCorp="+pkCorp);
    		    }
            } catch (BusinessException e) {
                Logger.error("Error",e);
                throw new SMVerifyException(e.getMessage());
            }
		}
		lsb.setWorkDate(workdate);
		lsb.setLanguage(langCode);
		lsb.setUserCode(userCode);
		lsb.setPassword(pwd);

        return LoginAppBean.getInstance().verify(lsb, true);
    }

    /* 
     * 静态密码验证用户合法性 
     * modified 2006-05-16 返回用户的VO，验证错误的详细信息以例外的形式抛出
     */
    public UserVO verifyUser(String accountCode, String userCode, String pwd) throws BusinessException {
        int result = LoginFailureInfo.UNKNOWN_ERROR;
        UserVO user = FindUserUtil.findUser(accountCode, userCode);
        if(user == null){
        	throw new SystemFrameworkException(
					LoginFailureInfo.RESULTSTRING[LoginFailureInfo.NAME_WRONG]);
        }else{
            LoginSessBean lsb = new LoginSessBean();
            lsb.setAccountId(accountCode);
            lsb.setUserCode(userCode);
            lsb.setPassword(pwd);
            result = new StaticPasswordIAMode().verify(lsb, user);
        }
        //如果验证不成功，将不成功的信息以例外抛出
        if (result != LoginFailureInfo.LOGIN_LEGALIDENTITY) throw new SystemFrameworkException(
				LoginFailureInfo.RESULTSTRING[result]); 
        return user;
    }

	public String getWorkDir() throws BusinessException {
		return RuntimeEnv.getInstance().getCanonicalNCHome();
	}

	public Object[] otherSysLogin(LoginSessBean lsb) throws BusinessException {
		return otherSysLoginIsAlwaysStaticPWD(lsb, true);
	}

	public Object[] otherSysLoginIsAlwaysStaticPWD(LoginSessBean lsb, boolean alwaysStaticPWD) throws BusinessException {
        try {
            Object[] objs = new Object[2];
            int result = getLoginAppBean().login(lsb,alwaysStaticPWD);
            objs[0] = new Integer(result); 
            if (result == LoginFailureInfo.LOGIN_SUCCESS) {
                objs[1] = lsb;
            } 
            return objs;
       } catch (SystemFrameworkException e) {
            Logger.error("Error",e);
            throw new  SMVerifyException(e.getMessage());
       }
	}



}