package com.se1853_jv.labverse.data.service.firebase;

public interface UploadCallback {
    void onSuccess(String downloadUrl);

    void onFailure(Exception e);
}
