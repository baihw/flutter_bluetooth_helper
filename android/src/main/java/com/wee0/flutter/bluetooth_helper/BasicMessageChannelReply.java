package com.wee0.flutter.bluetooth_helper;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BasicMessageChannel;

/**
 * BasicMessageChannel.reply实现
 */
final class BasicMessageChannelReply implements IReply {

    final BasicMessageChannel.Reply<Object> reply;

    BasicMessageChannelReply(BasicMessageChannel.Reply<Object> reply) {
        if (null == reply) throw new IllegalArgumentException("reply can not be null!");
        this.reply = reply;
    }

    private void _doReply(final Object data) {
        PlatformHelper.me().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                reply.reply(data);
            }
        });
    }


    @Override
    public void success(Object data) {
        Map<String, Object> _result = new HashMap<>(8);
        _result.put("code", DEF_SUCCESS_CODE);
        _result.put("message", "ok");
        _result.put("data", data);
        _doReply(_result);
    }

    @Override
    public void error(String code, String message, Object details) {
        Map<String, Object> _result = new HashMap<>(8);
        _result.put("code", code);
        _result.put("message", message);
        _result.put("data", details);
        _doReply(_result);
    }

    @Override
    public void error(String code, String message) {
        error(code, message, null);
    }

    @Override
    public void error(String message) {
        error(DEF_ERROR_CODE, message, null);
    }
}
