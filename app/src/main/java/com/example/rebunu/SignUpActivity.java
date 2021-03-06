package com.example.rebunu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Sign up screen
 * @author Zihao Huang
 */

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Database db = new Database();

        Button button_return;
        Button button_finish;
        RadioButton button_rider;
        RadioButton button_driver;
        EditText editText_username;
        EditText editText_email;
        EditText editText_phone;
        EditText editText_password;
        EditText editText_confirmPassword;

        button_return = findViewById(R.id.signup_button_return);
        button_finish = findViewById(R.id.signup_button_finish);
        button_rider = findViewById(R.id.signup_radiobutton_rider);
        button_driver = findViewById(R.id.signup_radiobutton_driver);
        editText_username = findViewById(R.id.signup_edittext_name);
        editText_email = findViewById(R.id.signup_edittext_email);
        editText_phone = findViewById(R.id.signup_edittext_phone);
        editText_password = findViewById(R.id.signup_edittext_password);
        editText_confirmPassword = findViewById(R.id.signup_edittext_confirmpassword);

        button_return.setOnClickListener(v -> finish());

        button_finish.setOnClickListener(v -> {
            // do something for registration
            Boolean check = true;
            if (editText_username.getText().toString().isEmpty()) {
                editText_username.setError(getResources().getString(R.string.username_empty));
                check = false;
            }
            if (editText_email.getText().toString().isEmpty()) {
                editText_email.setError(getResources().getString(R.string.email_empty));
                check = false;
            }
            if(! editText_email.getText().toString().matches("^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")){
                editText_email.setError(getResources().getString(R.string.invalid_email_address));
                check = false;
            }
            if (editText_phone.getText().toString().isEmpty()) {
                editText_phone.setError(getResources().getString(R.string.phone_empty));
                check = false;
            }
            if (editText_phone.getText().toString().length()!= 10) {
                editText_phone.setError(getResources().getString(R.string.invalid_phone_length));
                check = false;
            }
            if(! editText_phone.getText().toString().matches("^[0-9]+$")){
                editText_phone.setError(getResources().getString(R.string.invalid_email_address));
                check = false;
            }
            if (editText_password.getText().toString().isEmpty()) {
                editText_password.setError(getResources().getString(R.string.password_empty));
                check = false;
            }
            if (editText_confirmPassword.getText().toString().isEmpty()) {
                editText_confirmPassword.setError(getResources().getString(R.string.confirmPassword_empty));
                check = false;
            }
            if (! editText_confirmPassword.getText().toString().equals(editText_password.getText().toString())){
                editText_confirmPassword.setError(getResources().getString(R.string.two_password_not_match));
                editText_password.setError(getResources().getString(R.string.two_password_not_match));
                check = false;
            }
            if((!button_rider.isChecked()) && (!button_driver.isChecked())){
                button_driver.setError(getResources().getString(R.string.not_select_role));
                button_rider.setError(getResources().getString(R.string.not_select_role));
                check = false;
            }

            if(check){

                DocumentReference emailAuth = db.auth.document(editText_email.getText().toString());
                emailAuth.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            editText_email.setError(getResources().getString(R.string.email_address_exists));

                        } else {
                            DocumentReference phoneAuth = db.auth.document(editText_phone.getText().toString());
                            phoneAuth.get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DocumentSnapshot document1 = task1.getResult();
                                    if (document1.exists()) {

                                        editText_phone.setError(getResources().getString(R.string.phone_exists));

                                    } else {
                                        HashMap<String, Object> info = new HashMap<>();
                                        ArrayList<Integer> rating = new ArrayList<>();
                                        info.put("name",editText_username.getText().toString());
                                        info.put("phone", editText_phone.getText().toString());
                                        info.put("password", Utility.md5Hashing(editText_password.getText().toString()));
                                        info.put("email",editText_email.getText().toString());
                                        info.put("balance", 0);
                                        if(button_driver.isChecked()){
                                            info.put("role", true);
                                            rating.add(0);
                                            rating.add(0);
                                            info.put("rating", rating);
                                        }else{
                                            info.put("role", false);
                                            info.put("rating", rating);
                                        }
                                        String userId = db.register(info);
                                        Boolean role = button_driver.isChecked();
                                        Toast.makeText(getApplicationContext(),"Successfully Registered!! \nYour register ID is: "+ userId, Toast.LENGTH_SHORT).show();

                                        Intent logInIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                                        logInIntent.putExtra("userId", userId);
                                        logInIntent.putExtra("role", role);
                                        startActivity(logInIntent);
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Oops, a little problem occurred, please try again...", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            });
                        }
                    } else { return; }
                });
            }else return;
        });
    }
}