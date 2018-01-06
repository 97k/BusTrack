package com.example.aditya.bustrack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.text_input_layout_email)
    TextInputLayout emailWrapper;
    @BindView(R.id.text_input_layout_password)
    TextInputLayout passwordWrapper;
    @BindView(R.id.btnLogin)
    Button login;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.sign_up)
    LinearLayout switchToSignUp;
    @BindView(R.id.registerbtn)
    Button registerbtn;
    @BindView(R.id.chooser_spinner)
    Spinner userType;

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    public static final String LOG_TAG = LoginActivity.class.getSimpleName();

    //Firebase utils
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        // overridePendingTransition(R.anim.slide_in_right, R.anim.stay_in_place);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Boolean ut = getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.isDriver), false);
                    if (ut) {
                        Intent intent = new Intent(LoginActivity.this, DriverMapsActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Intent intent = new Intent(LoginActivity.this, StudentMapsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };

//        switchToSignUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(LoginActivity.this, SignUPActivity.class);
//                startActivity(intent);
//            }
//        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyBoard();
                emailInput = (TextInputEditText) emailWrapper.getEditText();
                passwordInput = (TextInputEditText) passwordWrapper.getEditText();
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();

                /**
                 * Saving detail of the user in a shared preference file i.e he is driver or not
                 * and will check from that shared pref while logging in!
                 */
                if (userType.getSelectedItem().toString().equals("Driver")){
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putBoolean(getString(R.string.isDriver), true);
                    editor.commit();
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful())
                            Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailInput = (TextInputEditText) emailWrapper.getEditText();
                passwordInput = (TextInputEditText) passwordWrapper.getEditText();
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                String user = userType.getSelectedItem().toString();

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        } else {
                            String user_id = mAuth.getCurrentUser().getUid();
                            Log.i(LOG_TAG, "User is : " + user);
                            if (user.equals("Driver")) {
                                DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id);
                                user_db.setValue(true);
                            } else {
                                DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Students").child(user_id);
                                user_db.setValue(true);
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        //LoginActivity.this.overridePendingTransition(0, R.anim.slide_out_right);
    }

    private void hideKeyBoard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            view.clearFocus();
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

