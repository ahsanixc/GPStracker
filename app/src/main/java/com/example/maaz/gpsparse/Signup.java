package com.example.maaz.gpsparse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.text.ParseException;

/**
 * A login screen that offers login via email/password.
 */


public class Signup extends AppCompatActivity
{

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView,mConfirmPasswordView, mPhoneNumber;

    View focusView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signup);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPhoneNumber = (EditText) findViewById(R.id.numb);
        mPasswordView = (EditText) findViewById(R.id.password);
        mConfirmPasswordView = (EditText) findViewById(R.id.confirmpassword);
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptSignup();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup()
    {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPhoneNumber.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String number = mPhoneNumber.getText().toString();
        String confirmpassword = mConfirmPasswordView.getText().toString();

        Log.d("Log" , email+"  "+password+"  "+number);

        boolean cancel = false;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        else if (!isEmailValid(email))
        {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if(!password.matches(confirmpassword)) {
            Log.d("Password",password+"Confirm password"+confirmpassword);
            mConfirmPasswordView.setError("Password does not match");
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }

        if (TextUtils.isEmpty(number))
        {
            mPhoneNumber.setError(getString(R.string.error_field_required));
            focusView = mPhoneNumber;
            cancel = true;
        }

        else
        {
            ParseUser user = new ParseUser();
            user.setUsername(email);
            user.setPassword(password);
            user.put("PhoneNum", number);
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(com.parse.ParseException e) {
                    if (e == null)
                    {
                        // Show a simple Toast message upon successful registration
                        Toast.makeText(getApplicationContext(),
                                "Successfully Signed up, please log in.",
                                Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Signup.this, Login.class);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),
                                "Sign up Error", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            });

        }
    }

    private boolean isEmailValid(String email)
    {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }


}

