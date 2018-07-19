package com.arturofilio.androiddrinkshop.Retrofit;

import com.arturofilio.androiddrinkshop.Model.CheckUserResponse;
import com.arturofilio.androiddrinkshop.Model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IDrinkShopApi {

    @FormUrlEncoded
    @POST("checkuser.php")
    Call<CheckUserResponse> checkUserExists(@Field("phone") String phone);


    @FormUrlEncoded
    @POST("register.php")
    Call<User> registerNewUser(@Field("phone") String phone,
                               @Field("name") String name,
                               @Field("address") String birthdate,
                               @Field("address") String address);
}
