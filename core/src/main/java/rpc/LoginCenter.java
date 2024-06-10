/**
* @file LoginSingle.java
*
* @brief RPC登录到云中心
*
* @author  taotao.hu@dbappsecurity.com.cn
*/

package rpc;

public class LoginCenter
{
	/**
	 * 异步登录到云中心
	 * @param endPoint 云中心地址
	 * @param owner 回调函数所属对象
	 * @param loginSuccessCb 登录成功回调函数标识， 回调函数的原型是 void cb_login_success(String channelHash, Object ctx)
	 * @param loginFailsCb 登录失败回调函数标识， 回调函数的原型是 void cb_login_fails(long rt, Object ctx)
	 * @param channelDisconnectCb 连接断开回调函数标识， 回调函数的原型是 void cb_channel_disconnect(long rt, String channelHash, Object ctx)
	 * @param machineStatusCb 机器状态改变（上下线）回调函数标识， 回调函数的原型是 void cb_machine_status(String machineId, String machineIP, String channelHash, boolean status_online, Object ctx)
	 * @return rpc返回值需要用ErrorCode解析
	 */
	public native long login_center_async(String endPoint, Object owner, String loginSuccessCb, String loginFailsCb, String channelDisconnectCb, String machineStatusCb, Object ctx);
	
	
	/**
	 * 异步登录到云中心
	 * @param endPoint 云中心地址
	 * @param owner 回调函数所属对象
	 * @param loginSuccessCb 登录成功回调函数标识， 回调函数的原型是 void cb_login_success(String channelHash, Object ctx)
	 * @param loginFailsCb 登录失败回调函数标识， 回调函数的原型是 void cb_login_fails(long rt, Object ctx)
	 * @param channelDisconnectCb 连接断开回调函数标识， 回调函数的原型是 void cb_channel_disconnect(long rt, String channelHash, Object ctx)
	 * @param machineStatusCb 机器状态改变（上下线）回调函数标识， 回调函数的原型是 void cb_machine_status(String machineId, String machineIP, String channelHash, boolean status_online, Object ctx)
	 * @param postLogCb 接收日志回调函数标识， 回调函数的原型是 String cb_post_log(String machineId, String api, String post_data, Object ctx)
	 * @return rpc返回值需要用ErrorCode解析
	 */
	public native long login_center2_async(String endPoint, Object owner, String loginSuccessCb, String loginFailsCb, String channelDisconnectCb, String machineStatusCb, String postLogCb, Object ctx);
}