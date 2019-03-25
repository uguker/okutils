package com.uguke.android.okgo;

/**
 * 预处理器（对个别Response进行拦截）
 * @author LeiJue
 */
public interface InterceptHandler {

    /**
     * 筛选处理
     * @param response 请求码
     * @return true为拦截
     */
    boolean onHandle(Response response);
}
