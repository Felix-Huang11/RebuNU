package com.example.rebunu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

/**
 * @author Zijian Xi, Zihao Huang
 */
public class UserInformationActivity extends AppCompatActivity {
    String userId;
    private String password = null;
    private Boolean role;
    private String phone;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);
        userId = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("userId")).toString();

        TextView textview_username_large;
        EditText edittext_username_small;
        EditText edittext_userEmail;
        EditText edittext_userPhone;
        TextView textview_userRole;
        TextView textview_userBalance;
        Button userInformation_button_return;
        Button userInformation_button_apply;

        textview_username_large = findViewById(R.id.userInformation_textview_username_large);
        edittext_username_small = findViewById(R.id.userInformation_edittext_username_small);
        edittext_userEmail = findViewById(R.id.userInformation_edittext_userEmail);
        edittext_userPhone = findViewById(R.id.userInformation_edittext_userPhone);
        textview_userRole = findViewById(R.id.userInformation_textview_userRole);
        textview_userBalance = findViewById(R.id.userInforamtion_textview_userBalance);

        userInformation_button_return = findViewById(R.id.userInformation_button_return);
        userInformation_button_apply = findViewById(R.id.userInformation_button_apply);

        userInformation_button_return.setOnClickListener(v -> finish());

        // retrieve from database ...
        Database db = new Database();
        db.profiles.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = Objects.requireNonNull(task.getResult());
                if (document.exists()) {
                    textview_username_large.setText((String) document.get("name"));
                    edittext_username_small.setText((String) document.get("name"));
                    edittext_userEmail.setText((String) document.get("email"));
                    email = (String) document.get("email");
                    edittext_userPhone.setText((String) document.get("phone"));
                    phone = (String) document.get("phone");
                    textview_userBalance.setText(((Long) document.get("balance")).toString());
                    if((boolean)document.get("role")){
                        textview_userRole.setText("DRIVER");
                    }else {
                        textview_userRole.setText("RIDER");
                    }

                    db.auth.document(phone).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            DocumentSnapshot document2 = Objects.requireNonNull(task2.getResult());
                            if (document2.exists()) {
                                password = (String) document2.get("password");
                                role = (Boolean) document2.get("role");
                            } else {
                                Toast.makeText(getApplicationContext(),"Not found!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Oops, little problem occured, please try again...", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(),"Not found!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(getApplicationContext(), "Oops, little problem occured, please try again...", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        userInformation_button_apply.setOnClickListener(v -> {
            Boolean check = true;
            if (edittext_username_small.getText().toString().isEmpty()) {
                edittext_username_small.setError(getResources().getString(R.string.username_empty));
                check = false;
            }
            if (edittext_userEmail.getText().toString().isEmpty()) {
                edittext_userEmail.setError(getResources().getString(R.string.email_empty));
                check = false;
            }
            if(! edittext_userEmail.getText().toString().matches("^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")){
                edittext_userEmail.setError(getResources().getString(R.string.invalid_email_address));
                check = false;
            }
            if (edittext_userPhone.getText().toString().isEmpty()) {
                edittext_userPhone.setError(getResources().getString(R.string.phone_empty));
                check = false;
            }
            if (edittext_userPhone.getText().toString().length()!= 10) {
                edittext_userPhone.setError(getResources().getString(R.string.invalid_phone_length));
                check = false;
            }
            if(! edittext_userPhone.getText().toString().matches("^[0-9]+$")){
                edittext_userPhone.setError(getResources().getString(R.string.invalid_email_address));
                check = false;
            }

            if(check){

                DocumentReference emailAuth = db.auth.document(edittext_userEmail.getText().toString());
                emailAuth.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()&& (document.get("profileId").toString().compareTo(userId)) != 0) {

                            edittext_userEmail.setError(getResources().getString(R.string.email_address_exists));

                        } else {
                            DocumentReference phoneAuth = db.auth.document(edittext_userPhone.getText().toString());
                            phoneAuth.get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    DocumentSnapshot document1 = task1.getResult();
                                    if (document1.exists() && (document1.get("profileId").toString().compareTo(userId)) != 0) {

                                        edittext_userPhone.setError(getResources().getString(R.string.phone_exists));

                                    } else {
                                        final DocumentReference proDocRef = db.profiles.document(userId);

                                        proDocRef
                                                .update("email",edittext_userEmail.getText().toString(),"phone",edittext_userPhone.getText().toString(),"name",edittext_username_small.getText().toString())
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("", "DocumentSnapshot successfully updated!");
                                                })
                                                .addOnFailureListener(e -> Log.w("TAG", "Error updating document", e));
                                        db.deleteAuth(phone,email);
                                        db.addAuth(edittext_userPhone.getText().toString(),edittext_userEmail.getText().toString(), password,userId,role);

                                        finish();

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

