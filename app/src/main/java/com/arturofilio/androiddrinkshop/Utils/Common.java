package com.arturofilio.androiddrinkshop.Utils;

import com.arturofilio.androiddrinkshop.Retrofit.IDrinkShopApi;
import com.arturofilio.androiddrinkshop.Retrofit.RetrofitClient;

public class Common {
    //In Emulator, localhost = 127.0.0.1
    private static final String BASE_URL = "http://localhost/drinkshop/";

    public static IDrinkShopApi getApi(){
        return RetrofitClient.getClient(BASE_URL).create(IDrinkShopApi.class);

    }

}
