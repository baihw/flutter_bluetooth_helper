package com.wee0.flutter.bluetooth_helper;

/**
 * 异步操作回复对象
 */
public interface IReply {
    /**
     * 默认成功代码
     */
    String DEF_SUCCESS_CODE = "200";

    /**
     * 默认错误代码
     */
    String DEF_ERROR_CODE = "500";

    /**
     * 成功回复逻辑
     *
     * @param data 数据
     */
    void success(Object data);

    /**
     * 出错回复逻辑
     *
     * @param code    错误代码
     * @param message 错误消息
     * @param details 错误详细信息
     */
    void error(String code, String message, Object details);

    /**
     * 出错回复逻辑
     *
     * @param code    错误代码
     * @param message 错误消息
     */
    void error(String code, String message);

    /**
     * 出错回复逻辑
     *
     * @param message 错误消息
     */
    void error(String message);
}
