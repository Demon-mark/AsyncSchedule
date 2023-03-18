package top.amosen.asyncSchedule.callback;

import top.amosen.asyncSchedule.result.AWorkerResult;

import java.util.Map;

/**
 * @author Amosen
 * @Date 2023-03-16 13:54
 */
public class DefaultCallbackAdapter<P, R> implements ACallback<P, R>{
    @Override
    public void onBegin(P param) {

    }

    @Override
    public void onResult(R result) {

    }

    @Override
    public boolean onError(Throwable throwable, R result) {
        return false;
    }

    @Override
    public void onComplete(P param, R result, Throwable throwable) {

    }

    @Override
    public void onFail(Throwable throwable, Map<String, AWorkerResult> results) {

    }
}
