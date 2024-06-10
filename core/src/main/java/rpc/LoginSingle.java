/**
 * @file LoginSingle.java
 * @brief RPC单机登录
 * @author taotao.hu@dbappsecurity.com.cn
 */

package rpc;

public class LoginSingle {
    /**
     * 单机登录，同步接口
     * @param endPoint 登录的主机地址
     * @return 登录成功则返回到此主机的通道Hash，登录失败返回空字符串
     */
    public native String login(String endPoint);

    /**
     * 单机登录，异步接口
     * @param endPoint
     * @param owner 回调函数所属对象
     * @param loginSuccessCb 登录成功回调函数标识， 回调函数的原型是 void cb_login_success(String channelHash, Object ctx)
     * @param loginFailsCb 登录失败回调函数标识， 回调函数的原型是 void cb_login_fails(long rt, Object ctx)
     * @param loginDisconnectCb 连接断开回调函数标识， 回调函数的原型是 void cb_login_success(long rt, String channelHash, Object ctx)
     * @return rpc返回值需要用ErrorCode解析
     */
    public native long login_async(String endPoint, Object owner, String loginSuccessCb, String loginFailsCb, String loginDisconnectCb, Object ctx);
}