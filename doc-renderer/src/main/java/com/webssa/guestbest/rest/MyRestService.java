package com.webssa.guestbest.rest;

import androidx.annotation.NonNull;

import com.webssa.guestbest.config.model.ConfigModel;
import com.webssa.guestbest.documents.model.DocumentRefRestModel;
import com.webssa.guestbest.documents.model.DocumentRestModel;
import com.webssa.guestbest.rest.helper.HttpClientProvider;
import com.webssa.guestbest.rest.helper.ParserProvider;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MyRestService {

    MyRestService INSTANCE = new Retrofit.Builder()
            .baseUrl("https://my-json-server.typicode.com/")
            .client(HttpClientProvider.getInstance().getMockHttpClient().newBuilder().addInterceptor(new LocalDocumentsProvidingInterceptor()).build())
            .addConverterFactory(MoshiConverterFactory.create(ParserProvider.getRestApiMoshi()))
            .build()
            .create(MyRestService.class);


    @NonNull
    @GET("Stanislav-Perchenko/document-renderer/config")
    Call<ConfigModel> getConfig();

    @NonNull
    @GET("Stanislav-Perchenko/document-renderer/document_index")
    Call<DocumentRefRestModel[]> getDocumentIndex();

    @NonNull
    @GET("Stanislav-Perchenko/document-renderer/documents/{id}")
    Call<DocumentRestModel> getDocumentById(@Path("id") long documentId);

    @NonNull
    @GET("local/documents/{docName}")
    Call<DocumentRestModel> getLocalDocumentByName(@Path("docName") String docName);
}
