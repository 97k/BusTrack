package com.example.aditya.bustrack;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import android.graphics.drawable.AnimationDrawable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private AnimationDrawable animationDrawable;

    @BindView(R.id.text_input_layout_email)
    TextInputLayout emailWrapper;
    @BindView(R.id.text_input_layout_password)
    TextInputLayout passwordWrapper;
    @BindView(R.id.btnLogin)
    Button login;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.registerbtn)
    Button registerbtn;
    @BindView(R.id.chooser_spinner)
    Spinner userType;

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    public static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private int bus_num = 0;
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

        // init coordinatorLayout
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_login);
        // initializing animation drawable by getting background from constraint layout
        animationDrawable = (AnimationDrawable) coordinatorLayout.getBackground();

        // setting enter fade animation duration to 5 seconds
        animationDrawable.setEnterFadeDuration(5000);

        // setting exit fade animation duration to 2 seconds
        animationDrawable.setExitFadeDuration(2000);


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
                    } else {
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
                if (userType.getSelectedItem().toString().equals("Driver")) {
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putBoolean(getString(R.string.isDriver), true);
                    editor.commit();
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful())
                            Toast.makeText(LoginActivity.this, "Sign in error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyBoard();
                emailInput = (TextInputEditText) emailWrapper.getEditText();
                passwordInput = (TextInputEditText) passwordWrapper.getEditText();
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                String user = userType.getSelectedItem().toString();
                if (user.equals("Driver")) {
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putBoolean(getString(R.string.isDriver), true);
                    editor.commit();



                } else {
                    SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                    editor.remove(getString(R.string.isDriver));
                    editor.commit();
                }

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Sign up error!", Toast.LENGTH_SHORT).show();
                        } else {
                            String user_id = mAuth.getCurrentUser().getUid();

                            Log.i(LOG_TAG, "User is : " + user);
                            Boolean isDriver = getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.isDriver), false);
                            if (isDriver) {
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
    @Override
    protected void onResume() {
        super.onResume();
        if (animationDrawable != null && !animationDrawable.isRunning()) {
            // start the animation
            animationDrawable.start();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (animationDrawable != null && animationDrawable.isRunning()) {
            // stop the animation
            animationDrawable.stop();
        }
    }
}

