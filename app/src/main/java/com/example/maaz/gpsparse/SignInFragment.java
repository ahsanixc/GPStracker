package com.example.maaz.gpsparse;

import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseUser;

public class SignInFragment extends Fragment {

	// UI references.
	private AutoCompleteTextView mEmailView;
	private EditText mPasswordView;
	private Button mEmailSignInButton;

	View focusView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_signin, container, false);

		// Set up the login form.
		mEmailView = (AutoCompleteTextView) rootView.findViewById(R.id.email);
		mEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_ACTION_NEXT) {
					mPasswordView.requestFocus();
					return true;
				}
				return false;
			}
		});

		mPasswordView = (EditText) rootView.findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_ACTION_DONE) {
//					InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//					imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
					return true;
				}
				return false;
			}
		});

		mEmailSignInButton = (Button) rootView.findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(getContext(),"Please wait!",Toast.LENGTH_LONG).show();
				attemptLogin();
			}
		});
		return rootView;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	private void attemptLogin()
	{
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();

		Log.d("Log" , email+"  "+password);

		boolean cancel = false;

		// Check if password not empty
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError("Password Required");
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid password, if the user entered one.
		if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!isEmailValid(email)) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			ParseUser.logInInBackground(email, password, new LogInCallback()
			{
				@Override
				public void done(ParseUser user, com.parse.ParseException e)
				{
					if (user != null)
					{
						Toast.makeText(getContext(),"Successfully Logged in",Toast.LENGTH_LONG).show();
						Intent intent = new Intent(getActivity(), MapsActivitySignin.class);
						getActivity().startActivity(intent);
					}
					else
					{
						Toast.makeText(getContext(),"No such user exist, please signup",Toast.LENGTH_LONG).show();
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
