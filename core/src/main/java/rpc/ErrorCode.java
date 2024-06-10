/**
* @file ErrorCode.java
*
* @brief RPC接口错误码解析
*
* @details  RPC接口错误码解析
*
* @version 1.0
*
* @author  taotao.hu@dbappsecurity.com.cn
*/

package rpc;

public class ErrorCode
{
	/**
	 * 函数功能：判断RPC的调用返回值是否是一个成功的值
	 */
	public static native boolean isSuccess(long rt);

	/**
	 * 函数功能：判断RPC的调用返回值是否是一个失败的值
	 */
	public static native boolean isFail(long rt);
	
	/**
	 * 函数功能：若RPC的调用返回值是一个失败的值，调用此函数可以获取失败原因
	 */
	public static native String desc(long rt);
}