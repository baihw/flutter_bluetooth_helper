package com.wee0.flutter.bluetooth_helper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class MyFuture<T> implements Future<T> {

    private final Thread _thread;

    private boolean _isCancelled = false;
    private boolean _isDone = false;
    private T _data;
    private CountDownLatch _latch = new CountDownLatch(1);

    MyFuture() {
        this._thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (null != _data) {
                        _isDone = true;
                        return;
                    }
                    if (_thread.isInterrupted()) {
                        _isCancelled = true;
                        _isDone = true;
                        return;
                    }
                }
            }
        }, "MyFutureThread-" + this);
        this._thread.start();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this._thread.interrupt();
        return this._isCancelled;
    }

    @Override
    public boolean isCancelled() {
        return this._isCancelled;
    }

    @Override
    public boolean isDone() {
        return this._isDone;
    }

    @Override
    public T get() throws ExecutionException, InterruptedException {
        this._latch.await();
        return this._data;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        this._latch.await(timeout, unit);
        return this._data;
    }

    /**
     * 设置数据，结束任务
     *
     * @param data 数据
     */
    public void done(T data) {
        this._data = data;
        this._latch.countDown();
    }
}
