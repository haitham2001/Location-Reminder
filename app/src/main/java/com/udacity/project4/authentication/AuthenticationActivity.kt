package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    companion object{
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private lateinit var authBinding: ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authBinding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(authBinding.root)

        authBinding.loginBtnId.setOnClickListener{
            launchSignInFlow()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_RESULT_CODE)
        {
            val response = IdpResponse.fromResultIntent(data)

            if(resultCode == Activity.RESULT_OK)
            {
                //Go to Reminders Activity
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
                Toast.makeText(applicationContext, "Signed in successfully",Toast.LENGTH_SHORT).show()
                finish()
            }
            else
                Toast.makeText(applicationContext, "Signing in failed  ${response?.error?.errorCode}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchSignInFlow() {
        // Types of mails you can sign in with
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(),
            SIGN_IN_RESULT_CODE
        )
    }
}
