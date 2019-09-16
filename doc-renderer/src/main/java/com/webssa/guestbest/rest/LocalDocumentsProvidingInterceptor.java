package com.webssa.guestbest.rest;

import com.webssa.guestbest.MyApplication;
import com.webssa.guestbest.utils.FileUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LocalDocumentsProvidingInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request origRequest = chain.request();
        if ("GET".equals(origRequest.method())) {
            String pathSegs[] = origRequest.url().pathSegments().toArray(new String[0]);
            if ((pathSegs.length == 3) && "local".equals(pathSegs[0]) && "documents".equals(pathSegs[1])) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new IOException();
                }
                Response.Builder bld = new Response.Builder()
                        .request(origRequest)
                        .protocol(Protocol.HTTP_1_1)
                        .addHeader("content-type", "application/json");
                try {
                    String document = FileUtils.loadAsset(MyApplication.getStaticAppContext(), "documents/"+pathSegs[2]);
                    bld.code(200).message("OK").body(ResponseBody.create(MediaType.parse("application/json"), document.getBytes())).build();
                } catch (IOException e) {
                    bld.code(404).message("Not found").body(ResponseBody.create(MediaType.parse("application/json"), new byte[0])).build();
                }
                return bld.build();
            }
        }
        return chain.proceed(origRequest);
    }

}
