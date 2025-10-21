package com.se1853_jv.labverse.data.api;

public interface ApiCallback<T> {
    void onSuccess(T data);

    void onError(String error);
}
