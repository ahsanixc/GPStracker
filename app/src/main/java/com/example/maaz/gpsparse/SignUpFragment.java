package com.example.maaz.gpsparse;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpFragment extends Fragment {

	// UI references.
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView,mConfirmPasswordView, mPhoneNumber;
	private Button SignUpButton;

	View focusView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_signup, container, false);

		// Set up the login form.
		mEmailView = (AutoCompleteTextView) rootView.findViewById(R.id.email);
		mPhoneNumber = (EditText) rootView.findViewById(R.id.numb);
		mPasswordView = (EditText) rootView.findViewById(R.id.password);
		mConfirmPasswordView = (EditText) rootView.findViewById(R.id.confirmpassword);
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

		SignUpButton = (Button) rootView.findViewById(R.id.email_sign_in_button);
		SignUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptSignup();
			}
		});

		return rootView;
	}

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
					Toast.makeText(getContext(),"Successfully Signed up, please log in.", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(getActivity(), Login.class);
					startActivity(intent);
				}
				else
				{
					Toast.makeText(getContext(),"Connectivity Problem! \nPlease Try Later", Toast.LENGTH_LONG).show();
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
