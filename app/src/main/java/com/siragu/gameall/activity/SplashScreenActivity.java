package com.siragu.gameall.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.onesignal.OneSignal;
import com.siragu.gameall.network.ApiError;
import com.siragu.gameall.network.ApiUtils;
import com.siragu.gameall.network.DrService;
import com.siragu.gameall.network.ErrorUtils;
import com.siragu.gameall.network.request.UserUpdateRequest;
import com.siragu.gameall.network.response.UserResponse;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;

import com.siragu.gameall.R;
import com.siragu.gameall.util.Constants;
import com.siragu.gameall.util.Helper;
import com.siragu.gameall.util.SharedPreferenceUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This activity is used as the starting Activity to display animation
 */
public class SplashScreenActivity extends AppCompatActivity {
    private static final int RC_GOOGLE_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager facebookCallbackManager;
    private LoginButton facebookLoginButton;
    private FirebaseAuth mAuth;

    private SharedPreferenceUtil sharedPreferenceUtil;

    private LinearLayout authOptionsContainer;
    private SignInButton google_sign_in_button;
    private ProgressBar authProgress;

    private String post_id_deep_linked;
    private View titleContainer;


    @Override
    public void onStart() {
        super.onStart();
        // FirebaseDynamicLinks init
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
            @Override
            public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                // Get deep link from result (may be null if no link is found)
                if (pendingDynamicLinkData != null) {
                    Uri deepLink = pendingDynamicLinkData.getLink();
                    if (deepLink.getBooleanQueryParameter("post", false)) {
                        post_id_deep_linked = deepLink.getQueryParameter("post");
                    }
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("DYNAMICLINK", "getDynamicLink:onFailure", e);
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      // FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_splash_screen);

        Constants.Shoppker = false;

        sharedPreferenceUtil = new SharedPreferenceUtil(this);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        initUi();
        if (user != null) {
            refreshToken(user);
        } else {
            setupAuth();
            OneSignal.provideUserConsent(true);
            OneSignal.startInit(this)
                    .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                    .init();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (user == null) {
                    showAuthOptions();
                }
            }
        }, 2000);


        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup_a);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                switch(checkedId)
                {
                    case R.id.radioButton:
                        // TODO Something
                        Constants.Shoppker = true ;
                        break;
                    case R.id.radioButton2:
                        // TODO Something
                        Constants.Shoppker = false;
                        break;

                }

            }
        });


    }

    private void refreshToken(FirebaseUser user) {
        user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    Log.d("Authorization", "Bearer " + idToken);
                    sharedPreferenceUtil.setStringPreference(Constants.KEY_API_KEY, "Bearer " + idToken);
                    //openActivity(isPaid ? post_id_deep_linked != null ? DetailHomeItemActivity.newIntent(SplashScreenActivity.this, post_id_deep_linked) : new Intent(SplashScreenActivity.this, MainActivity.class) : new Intent(SplashScreenActivity.this, StripePaymentActivity.class));
                    openActivity(post_id_deep_linked != null ? DetailHomeItemActivity.newIntent(SplashScreenActivity.this, post_id_deep_linked) : new Intent(SplashScreenActivity.this, MainActivity.class));
                } else {
                    Log.e(SplashScreenActivity.class.getName(), task.getException().getMessage());
                    // Handle error -> task.getException();
                    Toast.makeText(SplashScreenActivity.this, "Unable to connect, kindly retry.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupAuth() {
        mAuth = FirebaseAuth.getInstance();

          facebookCallbackManager = CallbackManager.Factory.create();
     facebookLoginButton.setPermissions(Arrays.asList("email", "public_profile"));
        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
                authProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(FacebookException exception) {
                Log.e("FacebookLogin", exception.toString());
                authProgress.setVisibility(View.INVISIBLE);
            }
        });
        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authProgress.setVisibility(View.VISIBLE);
                google_sign_in_button.setClickable(false);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().requestIdToken(getString(R.string.web_client_id)).build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        google_sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_GOOGLE_SIGN_IN);
                authProgress.setVisibility(View.VISIBLE);
                facebookLoginButton.setClickable(false);
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.d("FacebookLogin", "handleFacebookAccessToken:" + accessToken);
        Toast.makeText(SplashScreenActivity.this, R.string.profile_fetch, Toast.LENGTH_SHORT).show();
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    getToken(user);
                } else {
                    authProgress.setVisibility(View.INVISIBLE);
                    if (task.getException() != null && task.getException().getMessage() != null) {
                        Toast.makeText(SplashScreenActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                    // If sign in fails, display a message to the user.
                    Log.w("FacebookLogin", "signInWithCredential:failure", task.getException());
                }
            }
        });
    }

    private void initUi() {
        TextView title = findViewById(R.id.title);
        title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Montserrat_Bold.ttf"));
        titleContainer = findViewById(R.id.titleContainer);
        authOptionsContainer = findViewById(R.id.authOptionsContainer);
       facebookLoginButton = findViewById(R.id.login_button);
        google_sign_in_button = findViewById(R.id.google_sign_in_button);
        authProgress = findViewById(R.id.authProgress);

        TextView gSignInButtonText = (TextView) google_sign_in_button.getChildAt(0);
        if (gSignInButtonText != null)
            gSignInButtonText.setText("Google");
    }

    private void showAuthOptions() {
        Animation slide_up = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slide_up.setFillAfter(true);
        authOptionsContainer.setVisibility(View.VISIBLE);
        authOptionsContainer.startAnimation(slide_up);
        titleContainer.animate().translationY(-0.5f * authOptionsContainer.getHeight()).setDuration(600).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                authProgress.setVisibility(View.INVISIBLE);
                // Google Sign In failed, update UI appropriately
                Log.w("GoogleSignIn", "Google sign in failed", e);
                // ...
            }
        }
//        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("GoogleSignIn", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Toast.makeText(SplashScreenActivity.this, R.string.profile_fetch, Toast.LENGTH_SHORT).show();
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    getToken(user);
                } else {
                    authProgress.setVisibility(View.INVISIBLE);
                    if (task.getException() != null && task.getException().getMessage() != null) {
                        Toast.makeText(SplashScreenActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                    // If sign in fails, display a message to the user.
                    Log.w("GoogleSignIn", "signInWithCredential:failure", task.getException());
                }
            }
        });
    }

    private void getToken(FirebaseUser user) {
        user.getIdToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult getTokenResult) {
                String idToken = getTokenResult.getToken();
                sharedPreferenceUtil.setStringPreference(Constants.KEY_API_KEY, "Bearer " + idToken);
                Log.d("Authorization", "Bearer " + idToken);
                getUser(idToken);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                authProgress.setVisibility(View.INVISIBLE);
                Toast.makeText(SplashScreenActivity.this, "Token retrieve error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getUser(String token) {
        DrService service = ApiUtils.getClient().create(DrService.class);
        service.createUpdateUser("Bearer " + token,
                new UserUpdateRequest("m",
                        OneSignal.getPermissionSubscriptionState().getSubscriptionStatus().getUserId(),
                        true,
                        true,
                        true, 0), 0).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                Log.d("createUpdateUser", response.toString());
                if (response.isSuccessful()) {
                    Helper.setLoggedInUser(sharedPreferenceUtil, response.body());
                    openActivity(post_id_deep_linked != null ? DetailHomeItemActivity.newIntent(SplashScreenActivity.this, post_id_deep_linked) : new Intent(SplashScreenActivity.this, MainActivity.class));
                } else {
                    ApiError apiError = ErrorUtils.parseError(response);
                    Toast.makeText(SplashScreenActivity.this, apiError.status() == 417 ? "BLOCKED by admin." : apiError.status() == 400 ? "Something went wrong" : (TextUtils.isEmpty(apiError.message()) ? "Login failed" : apiError.message()), Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
//                    LoginManager.getInstance().logOut();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                authProgress.setVisibility(View.INVISIBLE);
                Toast.makeText(SplashScreenActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
             //   LoginManager.getInstance().logOut();
            }
        });
    }

    private void openActivity(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}