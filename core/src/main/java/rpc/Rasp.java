/**
* @file Rasp.java
*
* @brief RPC接口Java调用代码
*
* @details 此代码由程序生成，禁止手动修改，如在代码中发现BUG请联系作者
*
* @version 1.0
*
* @author  taotao.hu@dbappsecurity.com.cn
*/

package rpc;

import java.io.ByteArrayOutputStream;

public class Rasp
{
	private final int defaultTimeout = 10;

	/**
	 * 函数功能：信息同步,包括心跳包和策略更新
	 * @remark 异步调用，调用结果在回调函数中返回
	 * @param channel_hash 远程通道标识
	 * @param in 原始类型是 RaspPb.String

		以下代码演示了如何构造这个值

		import rpc.RaspPb;

		RaspPb.String.Builder builder = RaspPb.String.newBuilder();

		builder.setStdString(string);

		RaspPb.String msgPb = builder.build();

		byte[] in = msgPb.toByteArray();

	 * @param owner 回调函数所在的类对象
	 * @param call_back 回调函数的字符串标识，回调函数的原型是 void update_rasp_info_call_back(long rt, byte[] out, boolean is_done, Object ctx)
	 * @param ctx 调用者传入的参数，这个值会在回调函数触发时带入，调用者不需要此功能传null即可
	 * @param timeout RPC调用的超时时间
	 * @return 返回错误码，表示了异步调用是否成功
	 */
	public native long call_update_rasp_info_async(String channel_hash, byte[] in, Object owner, String call_back, Object ctx, int timeout);
	public final  long call_update_rasp_info_async(String channel_hash, byte[] in, Object owner, String call_back, Object ctx) { return call_update_rasp_info_async(channel_hash, in, owner, call_back, ctx, defaultTimeout); }
	public final  long call_update_rasp_info_async(String channel_hash, byte[] in, Object owner, String call_back) { return call_update_rasp_info_async(channel_hash, in, owner, call_back, null, defaultTimeout); }

	/**
	 * 函数功能：信息同步,包括心跳包和策略更新
	 * @remark 同步调用，调用完成即可返回结果
	 * @param channel_hash 远程通道标识
	 * @param in 原始类型是 RaspPb.String

		以下代码演示了如何构造这个值

		import rpc.RaspPb;

		RaspPb.String.Builder builder = RaspPb.String.newBuilder();

		builder.setStdString(string);

		RaspPb.String msgPb = builder.build();

		byte[] in = msgPb.toByteArray();

	 * @param owner 回调函数所在的类对象
	 * @param call_back 回调函数的字符串标识，回调函数的原型是 void update_rasp_info_call_back(long rt, byte[] out, boolean is_done, Object ctx)
	 * @param ctx 调用者传入的参数，这个值会在回调函数触发时带入，调用者不需要此功能传null即可
	 * @param timeout RPC调用的超时时间
	 * @return 返回错误码，表示了异步调用是否成功
	 */
	public native long call_update_rasp_info(String channel_hash, byte[] in, ByteArrayOutputStream out, int timeout);

	public long call_update_rasp_info(String channel_hash, byte[] in, ByteArrayOutputStream out)
	{
		 return call_update_rasp_info(channel_hash, in, out, defaultTimeout);
	}

	/**
	 * 函数功能：告警上报,进行日志记录
	 * @remark 异步调用，调用结果在回调函数中返回
	 * @param channel_hash 远程通道标识
	 * @param in 原始类型是 RaspPb.String

		以下代码演示了如何构造这个值

		import rpc.RaspPb;

		RaspPb.String.Builder builder = RaspPb.String.newBuilder();

		builder.setStdString(string);

		RaspPb.String msgPb = builder.build();

		byte[] in = msgPb.toByteArray();

	 * @param owner 回调函数所在的类对象
	 * @param call_back 回调函数的字符串标识，回调函数的原型是 void upload_rasp_log_call_back(long rt, byte[] out, boolean is_done, Object ctx)
	 * @param ctx 调用者传入的参数，这个值会在回调函数触发时带入，调用者不需要此功能传null即可
	 * @param timeout RPC调用的超时时间
	 * @return 返回错误码，表示了异步调用是否成功
	 */
	public native long call_upload_rasp_log_async(String channel_hash, byte[] in, Object owner, String call_back, Object ctx, int timeout);
	public final  long call_upload_rasp_log_async(String channel_hash, byte[] in, Object owner, String call_back, Object ctx) { return call_upload_rasp_log_async(channel_hash, in, owner, call_back, ctx, defaultTimeout); }
	public final  long call_upload_rasp_log_async(String channel_hash, byte[] in, Object owner, String call_back) { return call_upload_rasp_log_async(channel_hash, in, owner, call_back, null, defaultTimeout); }

	/**
	 * 函数功能：告警上报,进行日志记录
	 * @remark 同步调用，调用完成即可返回结果
	 * @param channel_hash 远程通道标识
	 * @param in 原始类型是 RaspPb.String

		以下代码演示了如何构造这个值

		import rpc.RaspPb;

		RaspPb.String.Builder builder = RaspPb.String.newBuilder();

		builder.setStdString(string);

		RaspPb.String msgPb = builder.build();

		byte[] in = msgPb.toByteArray();

	 * @param owner 回调函数所在的类对象
	 * @param call_back 回调函数的字符串标识，回调函数的原型是 void upload_rasp_log_call_back(long rt, byte[] out, boolean is_done, Object ctx)
	 * @param ctx 调用者传入的参数，这个值会在回调函数触发时带入，调用者不需要此功能传null即可
	 * @param timeout RPC调用的超时时间
	 * @return 返回错误码，表示了异步调用是否成功
	 */
	public native long call_upload_rasp_log(String channel_hash, byte[] in, ByteArrayOutputStream out, int timeout);

	public long call_upload_rasp_log(String channel_hash, byte[] in, ByteArrayOutputStream out)
	{
		 return call_upload_rasp_log(channel_hash, in, out, defaultTimeout);
	}
}