package com.arturofilio.androiddrinkshop;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.arturofilio.androiddrinkshop.Model.CheckUserResponse;
import com.arturofilio.androiddrinkshop.Model.User;
import com.arturofilio.androiddrinkshop.Retrofit.IDrinkShopApi;
import com.arturofilio.androiddrinkshop.Utils.Common;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.szagurskii.patternedtextwatcher.PatternedTextWatcher;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1000;

    private Context mContext = MainActivity.this;

    Button btn_continue;

    IDrinkShopApi mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mService = Common.getApi();

        btn_continue = (Button)findViewById(R.id.btn_continue);
        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoginPage(LoginType.PHONE);
            }
        });
    }

    private void startLoginPage(LoginType loginType) {
        Intent intent = new Intent(this, AccountKitActivity.class);

        AccountKitConfiguration.AccountKitConfigurationBuilder builder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(loginType,
                        AccountKitActivity.ResponseType.TOKEN);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,builder.build());
        startActivityForResult(intent,REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE) {
            
            AccountKitLoginResult result = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

            if(result.getError() != null)
            {
                Toast.makeText(this, ""+result.getError().getErrorType().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
            else if (result.wasCancelled())
            {
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if(result.getAccessToken() != null)
                {
                    final AlertDialog alertDialog = new SpotsDialog.Builder()
                            .setContext(mContext)
                            .setTheme(R.style.AppTheme)
                            .build();

                    //Get User phone and Check if it already exists on the server
                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(final Account account) {
                            mService.checkUserExists(account.getPhoneNumber().toString())
                                    .enqueue(new Callback<CheckUserResponse>() {
                                        @Override
                                        public void onResponse(Call<CheckUserResponse> call, Response<CheckUserResponse> response) {
                                            CheckUserResponse userResponse = response.body();
                                            if(userResponse.isExists())
                                            {
                                                //If user already exits, just start new Activity
                                                alertDialog.dismiss();
                                            }
                                            else
                                            {
                                                //Else, need register
                                                alertDialog.dismiss();

                                                showRegisterDialog(account.getPhoneNumber().toString());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<CheckUserResponse> call, Throwable t) {
                                            
                                        }
                                    });
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Log.d("ERROR", accountKitError.getErrorType().getMessage());
                        }
                    });
                }
            }
        }
    }

    private void showRegisterDialog(final String phone) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("REGISTER");

        LayoutInflater inflater = this.getLayoutInflater();
        View register_layout = inflater.inflate(R.layout.register_layout,null);

        final MaterialEditText edit_name = (MaterialEditText)register_layout.findViewById(R.id.edit_name);
        final MaterialEditText edit_address = (MaterialEditText)register_layout.findViewById(R.id.edit_address);
        final MaterialEditText edit_birthdate = (MaterialEditText)register_layout.findViewById(R.id.edit_birthdate);

        Button btn_register = (Button)register_layout.findViewById(R.id.btn_register);

        edit_birthdate.addTextChangedListener(new PatternedTextWatcher("####-##-##"));

        //Event
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Close dialog
                alertDialog.create().dismiss();

                if(TextUtils.isEmpty(edit_name.getText().toString()))
                {
                    Toast.makeText(mContext, "Please enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edit_birthdate.getText().toString()))
                {
                    Toast.makeText(mContext, "Please enter your birthdate", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edit_address.getText().toString()))
                {
                    Toast.makeText(mContext, "Please enter your address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check here for the spots dialog
                final AlertDialog waitingDialog = new SpotsDialog.Builder()
                        .setContext(mContext)
                        .setTheme(R.style.AppTheme)
                        .build();

                mService.registerNewUser(phone,
                        edit_name.getText().toString(),
                        edit_address.getText().toString(),
                        edit_birthdate.getText().toString())
                        .enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(Call<User> call, Response<User> response) {
                                waitingDialog.dismiss();

                                User user = response.body();
                                if(TextUtils.isEmpty(user.getError_msg()))
                                {
                                    Toast.makeText(mContext, "User registration was succesful", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<User> call, Throwable t) {
                                waitingDialog.dismiss();
                            }
                        });
            }

        });

        alertDialog.setView(register_layout);
        alertDialog.show();
    }

    private void printKeyHash() {
        try {
           PackageInfo info = getPackageManager().getPackageInfo("com.arturofilio.androiddrinkshop",
                   PackageManager.GET_SIGNATURES);
           for(Signature signature:info.signatures) {
               MessageDigest md = MessageDigest.getInstance("SHA");
               md.update(signature.toByteArray());
               Log.d("KEYHASH", Base64.encodeToString(md.digest(),Base64.DEFAULT));
           }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
