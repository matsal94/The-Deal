package co.sepin.thedeal;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Telephony;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import co.sepin.thedeal.application.ModeClass;
import co.sepin.thedeal.database.DealType;
import co.sepin.thedeal.interfaces.SmsListener;
import co.sepin.thedeal.receiver.SmsReceiver;

import static co.sepin.thedeal.application.UpdateData.firebaseAuth;
import static co.sepin.thedeal.application.UpdateData.firebaseUser;
import static co.sepin.thedeal.application.UpdateData.getContactsFromFirebase;
import static co.sepin.thedeal.application.UpdateData.updateEventsFromFirebase;
import static co.sepin.thedeal.application.UpdateData.updateProfilInfoFromFirebase;
import static co.sepin.thedeal.application.UpdateData.updateUserStatus;


public class LoginActivity extends ModeClass {

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    public static final String OTP_REGEX = "[0-9]{1,6}";
    private Animation animHide, animShow, animShake, animBounce;
    private SmsReceiver smsReceiver;
    private Runnable finalizer;
    private Handler timeoutHandler = new Handler();
    private Vibrator vib;
    private EditText codeET;
    private Switch loginSw;
    private Button sendAgainBtn;
    private ProgressBar progressBar;
    private TextInputLayout codeTIL;
    private Bitmap bitmapDone, bitmapClose;
    private CircularProgressButton loginCPB;
    private ConstraintLayout constraintLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private String mVerificationId, phoneNumber = null;
    private boolean layoutLogin2 = false, isRegister = false, isStartAnimation = false; // zmienna wskazująca czy animacja została uruchomiona


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        animHide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide_button);
        animShow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_button);
        animShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        animBounce = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);

        bitmapDone = BitmapFactory.decodeResource(getResources(), R.drawable.done_white_48);
        bitmapClose = BitmapFactory.decodeResource(getResources(), R.drawable.close_white_48);

        loginSw = (Switch) findViewById(R.id.login_switch);
        codeET = (EditText) findViewById(R.id.login_codeET);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sendAgainBtn = (Button) findViewById(R.id.login_send_againBtn);
        progressBar = (ProgressBar) findViewById(R.id.loginPB);
        constraintLayout = (ConstraintLayout) findViewById(R.id.login_constraintLayout);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.nunito);
        loginSw.setTypeface(typeface);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null)
            phoneNumber = (String) bundle.get("PhoneNumber");

        setVeryficationCallback();
        getPermissionToReadSMS();
    }


    @Override
    protected void onStart() {
        super.onStart();
        registerSMS();
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterSMS();
    }


    @Override
    public void onBackPressed() {

        if (layoutLogin2) {
            finish();
        }
        super.onBackPressed();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {

            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_DENIED)
                switchLayout();

            sendVerify(phoneNumber);

        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    public void getPermissionToReadSMS() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, READ_SMS_PERMISSIONS_REQUEST);
        else sendVerify(phoneNumber);
    }


    private void setVeryficationCallback() {

        firebaseAuth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                //Log.d(TAG, "onVerificationCompleted:" + credential);

                String code = credential.getSmsCode();
                //System.out.println("Credential: " + credential);
                System.out.println("Code: " + code);
                //verifyVerificationCode(code);
                //signInWithPhoneAuthCredential(credential);
            }
/*
            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }*/

            @Override
            public void onVerificationFailed(FirebaseException e) {

                if (e instanceof FirebaseNetworkException)
                    updateLoginButton("restart");
                else if (e instanceof FirebaseAuthInvalidCredentialsException)
                    updateLoginButton("incorrect number format");
                else if (e instanceof FirebaseTooManyRequestsException)
                    updateLoginButton("limit");
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                ///Log.d(TAG, "onCodeSent:" + verificationId);
                System.out.println("verificationId: " + verificationId);
                System.out.println("token: " + token);
                mVerificationId = verificationId;
                mResendToken = token;
                //verifyVerificationCode("123456");

                // Save verification ID and resending token so we can use them later
                ///mVerificationId = verificationId;
                ///mResendToken = token;

                // ...
            }
        };
    }


    private void sendVerify(String phoneNumber) {

        System.out.println("sendVerify: " + phoneNumber);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                30,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallback

        if (!layoutLogin2) {

            finalizer = new Runnable() {

                public void run() {

                    sendAgainBtn.setAnimation(animBounce);
                    sendAgainBtn.startAnimation(animBounce);
                    sendAgainBtn.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            };
            timeoutHandler.postDelayed(finalizer, 30 * 1000); //po upływie 30 sekund pojawi się przycisk z ponownym wysłaniem smsa
        }
    }


    private void registerAndReceiverSMS() {

/*        final Runnable finalizer = new Runnable() {

            public void run() {

                sendAgainBtn.setAnimation(animBounce);
                sendAgainBtn.startAnimation(animBounce);
                sendAgainBtn.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        };
        timeoutHandler.postDelayed(finalizer, 30 * 1000); //po upływie 30 sekund pojawi się przycisk z ponownym wysłaniem smsa*/

        SmsReceiver.bindListener(new SmsListener() {

            @Override
            public void messageReceived(String otp) {

                timeoutHandler.removeCallbacks(finalizer); // anulowanie handlera
                progressBar.setVisibility(View.INVISIBLE);
                codeET.setText(otp);

                userVerification(otp);
            }
        });

        smsReceiver = new SmsReceiver();
        registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
    }


    private void userVerification(String code) {

        if (layoutLogin2) {

            loginCPB.startAnimation();
            isStartAnimation = true;
        }

        if (phoneNumber != null || code != null) {

            codeET.setEnabled(false);
            verifyVerificationCode(code);
        }
    }


    private void verifyVerificationCode(String code) {

        System.out.println("Login verifyVerificationCode");

        if (mVerificationId != null) {

            System.out.println("Login verifyVerificationCode mVerificationId != null");

            //creating the credential
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            //signing the user
            signInWithPhoneAuthCredential(credential);
        } else
            System.out.println("Login verifyVerificationCode mVerificationId == null");
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        System.out.println("Login signInWithPhoneAuthCredential onComplete");

                        if (task.isSuccessful()) {

                            System.out.println("Login signInWithPhoneAuthCredential onComplete task.isSuccessful()");
                            firebaseUser = task.getResult().getUser();

                            if (Objects.requireNonNull(task.getResult()).getAdditionalUserInfo().isNewUser()) {

                                System.out.println("Login: nowy user");
                                addNewUserToFirebase();
                            } else {

                                System.out.println("Login: stary user");
                                System.out.println("Login stary user firebaseAuth.getCurrentUser(): " + firebaseAuth.getCurrentUser().getPhoneNumber());
                                System.out.println("Login stary user firebaseUser.getPhoneNumber(): " + firebaseUser.getPhoneNumber());
                            }

                            loginSw.setVisibility(View.INVISIBLE);
                            updateLoginButton("done");
                            getDictionary(); // pobranie słownika do lokalnej bazy
                        }
                    }
                })
                .addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        if (e instanceof FirebaseNetworkException) {

                            System.out.println("Login: sprawdź połaczenie z Internetem");
                            updateLoginButton("restart");
                        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {

                            System.out.println("Login: błędny kod weryfikacyjny");
                            updateLoginButton("cancel");
                        } else
                            System.out.println("Login addOnFailureListener: " + e);
                    }
                });
    }


    private void addNewUserToFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseUsers = database.getReference("Users").push();
        databaseUsers.child("tel_num").setValue(firebaseUser.getPhoneNumber());
        databaseUsers.child("uid").setValue(firebaseUser.getUid());

        DatabaseReference databaseProfiles = database.getReference("Profiles").child(Objects.requireNonNull(firebaseUser.getPhoneNumber()));
        databaseProfiles.child("uid").setValue(firebaseUser.getUid());
        databaseProfiles.child("avatar_uri").setValue("default");
        databaseProfiles.child("tel_num").setValue(firebaseUser.getPhoneNumber());
        databaseProfiles.child("about_me").setValue("");
        databaseProfiles.child("email").setValue("");
        databaseProfiles.push();
    }


    private void switchLayout() {

        setContentView(R.layout.activity_login2);
        hideKeyboardAndFocus(findViewById(R.id.login2_constraintLayout), LoginActivity.this);

        if (timeoutHandler != null)
            timeoutHandler.removeCallbacks(finalizer); // anulowanie handlera

        layoutLogin2 = true;
        codeET = (EditText) findViewById(R.id.login2_codeET);
        codeTIL = (TextInputLayout) findViewById(R.id.login2_codeLayout);
        loginCPB = (CircularProgressButton) findViewById(R.id.login2_CPBtn);
        constraintLayout = (ConstraintLayout) findViewById(R.id.login2_constraintLayout);

        constraintLayout.requestFocus();
        codeET.setEnabled(true);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.nunito_bold);
        loginCPB.setTypeface(typeface);

        moveFocusFromET();
    }


    private void getDictionary() {

        //Delete.table(DealType.class);
        DealType dealType = new DealType();

        //ModelAdapter<DealType> adapter = FlowManager.getModelAdapter(DealType.class);

        dealType.setId(1);
        dealType.setDescription("PRZETARG");
        dealType.insert();
        //adapter.insert(dealType);

        dealType.setId(2);
        dealType.setDescription("SPOTKANIE");
        dealType.insert();
        //adapter.insert(dealType);

        dealType.setId(3);
        dealType.setDescription("OFERTA");
        dealType.insert();
        //adapter.insert(dealType);

        dealType.setId(4);
        dealType.setDescription("WSPÓŁPRACA");
        dealType.insert();
        //adapter.insert(dealType);

        dealType.setId(5);
        dealType.setDescription("KONTAKT");
        dealType.insert();
        //adapter.insert(dealType);


/*
        Api api = RetrofitUtils.getInstance().create(Api.class);
        Call<List<DealType>> call = api.getDealType();

        call.enqueue(new Callback<List<DealType>>() {
            @Override
            public void onResponse(Call<List<DealType>> call, Response<List<DealType>> response) {

                if (response.code() == 200) {

                    for (DealType dt : response.body())
                        dt.save();

                    Toast.makeText(getApplicationContext(), "Pobrano słownik z serwera i zapisano do bazy", Toast.LENGTH_SHORT).show();
                }
                else {

                    Toast.makeText(getApplicationContext(), "Nie udało się pobrać słownika", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<DealType>> call, Throwable t) {

                Toast.makeText(getApplicationContext(), "Nie udało się pobrać słownika", Toast.LENGTH_SHORT).show();
            }
        });*/
    }


    private void updateLoginButton(String behaviour) {

        switch (behaviour) {

            case "done":

                System.out.println("Login done");
                updateApplicationData();

                if (layoutLogin2) {

                    loginCPB.doneLoadingAnimation(getResources().getColor(R.color.dark_green), bitmapDone);
                    loginCPB.setFocusable(true);
                } else
                    progressBar.setVisibility(View.INVISIBLE);

                codeET.setEnabled(false);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                }, 2000);

                Snackbar snack = Snackbar.make(constraintLayout, getString(R.string.veryfication), Snackbar.LENGTH_SHORT);
                View snackbarView = snack.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.dark_green));
                snack.show();
                break;

            case "cancel":

                if (layoutLogin2) {

                    loginCPB.doneLoadingAnimation(Color.RED, bitmapClose);

                    Handler handler1 = new Handler();        //
                    handler1.postDelayed(new Runnable() {    //
                        public void run() {                 //
                            loginCPB.revertAnimation();     // restart animacji przycisku zaloguj
                            if (layoutLogin2) codeET.setEnabled(true);
                        }                                   //
                    }, 2000);                     //
                }

                Snackbar snack1 = Snackbar.make(constraintLayout, getString(R.string.veryfication_failure), Snackbar.LENGTH_SHORT);
                View snackbarView1 = snack1.getView();
                snackbarView1.setBackgroundColor(Color.RED);
                snack1.show();
                break;

            case "restart":

                if (layoutLogin2) {

                    if (isStartAnimation) {
                        loginCPB.revertAnimation();
                        isStartAnimation = false;
                    }
                    loginCPB.startAnimation(animShake);
                    codeET.setEnabled(true);
                }
                Snackbar.make(constraintLayout, getString(R.string.not_Internet_connection), Snackbar.LENGTH_LONG).show();
                break;

            case "limit":

                if (layoutLogin2) {

                    if (isStartAnimation) {
                        loginCPB.revertAnimation();
                        isStartAnimation = false;
                    }
                    loginCPB.startAnimation(animShake);
                    codeET.setEnabled(true);
                }
                Snackbar.make(constraintLayout, getString(R.string.limit), Snackbar.LENGTH_LONG).show();
                break;

            default:

                if (layoutLogin2) {

                    if (isStartAnimation) {
                        loginCPB.revertAnimation();
                        isStartAnimation = false;
                    }
                    loginCPB.startAnimation(animShake);
                    codeET.setEnabled(true);
                }
                Snackbar.make(constraintLayout, getString(R.string.incorrect_number_format), Snackbar.LENGTH_LONG).show();
        }
    }


    private void updateApplicationData() {

        new Thread(new Runnable() {
            public void run() {
                getContactsFromFirebase();
            }
        }).start();


        new Thread(new Runnable() {
            public void run() {

                updateProfilInfoFromFirebase();
                updateEventsFromFirebase();
            }
        }).start();
    }


    private void moveFocusFromET() {

        codeET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    constraintLayout.requestFocus();
                    hideSoftKeyboard(LoginActivity.this, v);
                    return true;
                }
                return false;
            }
        });
    }


    private void registerSMS() {

        if (!isRegister && !layoutLogin2) {

            registerAndReceiverSMS(); // nasłuchiwanie na sms z haslem
            isRegister = true;
        }
    }


    private void unregisterSMS() {

        if (isRegister) {

            try {

                smsReceiver.clearAbortBroadcast(); //anulowanie nasluchiwania
                unregisterReceiver(smsReceiver);
                isRegister = false;
            } catch (IllegalArgumentException ex) {
                Toast.makeText(this, "Login unregisterSMS: " + ex, Toast.LENGTH_LONG).show();
            }
        }
    }


    public void onClickLogin(View view) {

        constraintLayout.requestFocus();

        String codeText = codeET.getText().toString().trim();

        if (codeText.isEmpty() || (codeText.length() != 6)) {

            codeTIL.setAnimation(animShake);
            codeTIL.startAnimation(animShake);

            if (checkVibrationMode())
                vib.vibrate(120);

            codeTIL.setErrorEnabled(true);
            codeTIL.setError(getString(R.string.errorCode));
            return;
        }

        codeTIL.clearAnimation();
        codeTIL.setErrorEnabled(false);
        userVerification(codeText);
    }


    public void onClickSendAgain(View view) {

        sendAgainBtn.setAnimation(animHide);
        sendAgainBtn.startAnimation(animHide);
        sendAgainBtn.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        if (phoneNumber != null)
            sendVerify(phoneNumber);
        else
            Toast.makeText(getApplicationContext(), "Kod weryfikacyjny nie został wysłany", Toast.LENGTH_LONG).show();
    }


    public void onClickSwitch(View view) {

        unregisterSMS();
        switchLayout();
    }
}

/*
    public void tmpOnClick(View view) {

        updateApplicationData();

        if (layoutLogin2) {

            loginCPB.startAnimation();
            loginCPB.doneLoadingAnimation(getResources().getColor(R.color.dark_green), bitmapDone);
        } else
            progressBar.setVisibility(View.INVISIBLE);

        getDictionary();

        Snackbar snack = Snackbar.make(constraintLayout, getString(R.string.veryfication), Snackbar.LENGTH_LONG);
        View snackbarView = snack.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.dark_green));
        snack.show();

        codeET.setEnabled(false);

        new Handler().postDelayed(new Runnable() {
            public void run() {

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                //intent.putExtra("PhoneNumber", phoneNumber);
                startActivity(intent);
                finishAffinity();
                //finish();

            }
        }, 2000);
    }
}*/


/*
    private void sendRequest(final Password password) {

        Api api = RetrofitUtils.getInstance().create(Api.class);
        Call<Password> call = api.setPassword(password);

        call.enqueue(new Callback<Password>() {
            @Override
            public void onResponse(Call<Password> call, Response<Password> response) {
                try {
                    Toast.makeText(getApplicationContext(), "Hasło: " + response.body().getPassword() + " dodane", Toast.LENGTH_LONG).show();
                }
                catch (NullPointerException n) {
                    Toast.makeText(getApplicationContext(), "Hasło nie zostało dodane. " + response.message() + " " + response.code(), Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<Password> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    } */

/*
    private void receiveRequest(final Password password) {

        Api api = RetrofitUtils.getInstance().create(Api.class);
        Call<List<Password>> call = api.getPassword();

        call.enqueue(new Callback<List<Password>>() {
            @Override
            public void onResponse(Call<List<Password>> call, Response<List<Password>> response) {
                Toast.makeText(getApplicationContext(), "Pass: " + response.code(), Toast.LENGTH_LONG).show();

                List<Password> password = response.body();
            }

            @Override
            public void onFailure(Call<List<Password>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }*/

//    public void onClick(View view) {
//
//        Password password = new Password(codeTV.getText().toString());
//        sendRequest(password);
//        List lista;
//        lista = new Select().from(OneTimePassword.class).queryList();
//        for (int i=0; i<lista.size(); i++)
//            hasloTV.setText(lista.get(i).toString());
//    }
