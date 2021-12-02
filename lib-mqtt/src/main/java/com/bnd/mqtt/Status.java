
package com.bnd.mqtt;

/**
 * mqtt 生命周期的连接状态
 */
enum Status {
	/**
	 * Indicates that the operation succeeded
	 */
	OK, 
	
	/**
	 * Indicates that the operation failed
	 */
	ERROR,
	
	/**
	 * Indicates that the operation's result may be returned asynchronously
	 */
	NO_RESULT
}
