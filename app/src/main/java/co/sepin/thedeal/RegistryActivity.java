package co.sepin.thedeal;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import co.sepin.thedeal.application.ModeClass;


public class RegistryActivity extends ModeClass {

    private Vibrator vib;
    private Animation animShake;
    private CountryCodePicker countryNumberCCP;
    private TextInputLayout telephoneTIL;
    private EditText telephoneET;
    private CircularProgressButton registryBtn;
    private TextView smsInformationTV;
    private Bitmap bitmapDone, bitmapClose;
    private ConstraintLayout mainLayout;
    private boolean isError = false, isStartAnimation = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registry);
        checkIsAppLogin();
        hideKeyboardAndFocus(findViewById(R.id.registryCL), RegistryActivity.this);

        mainLayout = (ConstraintLayout) findViewById(R.id.registryCL);
        countryNumberCCP = (CountryCodePicker) findViewById(R.id.registry_country_numberCCP);
        telephoneET = (EditText) findViewById(R.id.registry_telephoneET);
        telephoneTIL = (TextInputLayout) findViewById(R.id.registry_telephoneTIL);
        registryBtn = (CircularProgressButton) findViewById(R.id.registryCPB);
        smsInformationTV = (TextView) findViewById(R.id.registry_SMS_informationTV);

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        animShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);

        bitmapDone = BitmapFactory.decodeResource(getResources(), R.drawable.done_white_48);
        bitmapClose = BitmapFactory.decodeResource(getResources(), R.drawable.close_white_48);

        setRegistryButtonFont();
        setLogoutSnackBarAnimation();
        setCountryCodePickerListener();
        moveFocusFromET();
    }


    @Override
    protected void onResume() {
        super.onResume();
        telephoneET.setEnabled(true);
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (isStartAnimation)
            registryBtn.revertAnimation();
    }


    private void checkIsAppLogin() {

        SharedPreferences prefsLogin = getSharedPreferences("Login", MODE_PRIVATE);
        boolean login = prefsLogin.getBoolean("login", false);

        if (login) {

            Intent intentMain = new Intent(RegistryActivity.this, MainActivity.class);
            startActivity(intentMain);
            finish();
        }
    }


    private void setRegistryButtonFont() {

        Typeface typeface = ResourcesCompat.getFont(this, R.font.nunito_bold);
        registryBtn.setTypeface(typeface);
    }


    private void setLogoutSnackBarAnimation() {

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {

            String logout = (String) bundle.get("Logout");

            if (logout != null && logout.equals("true")) {

                updateRegistryButton("logout");
                bundle.clear();
            }
        }
    }


    private void setCountryCodePickerListener() {

        countryNumberCCP.registerCarrierNumberEditText(telephoneET);
        countryNumberCCP.setPhoneNumberValidityChangeListener(new CountryCodePicker.PhoneNumberValidityChangeListener() {

            @Override
            public void onValidityChanged(boolean isValidNumber) {

                if (isValidNumber && telephoneTIL.isErrorEnabled()) {

                    telephoneTIL.setErrorEnabled(false);
                    telephoneTIL.clearAnimation();
                    isError = true;
                }
                if (!isValidNumber && isError) {

                    telephoneTIL.setErrorEnabled(true);
                    telephoneTIL.setError(getString(R.string.error_number2));
                    isError = false;
                }
            }
        });
    }


    private void moveFocusFromET() {

        telephoneET.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    mainLayout.requestFocus();
                    hideSoftKeyboard(RegistryActivity.this, v);
                    return true;
                }
                return false;
            }
        });
    }


    private void sendVerify(final String phoneNumber) {

        registryBtn.startAnimation();
        isStartAnimation = true; // zmienna wskazująca czy animacja została uruchomiona

        if (phoneNumber != null) {

            telephoneET.setEnabled(false);

            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseInvitations = firebaseDatabase.getReference("Invitations");
            databaseInvitations.orderByChild("tel_num").equalTo(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() != null)
                        updateRegistryButton("done");
                    else
                        updateRegistryButton("cancel");
                }

                @Override
                public void onCancelled(DatabaseError error) {

                    updateRegistryButton("restart");
                    Toast.makeText(RegistryActivity.this, getString(R.string.db_error) + " " + error.toException(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void updateRegistryButton(String behaviour) {

        switch (behaviour) {

            case "done":

                animateSMSInfo("HIDE");
                registryBtn.startAnimation();
                registryBtn.doneLoadingAnimation(getResources().getColor(R.color.dark_green), bitmapDone);
                isStartAnimation = true; // zmienna wskazująca czy animacja została uruchomiona
                telephoneET.setEnabled(false);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

                        Intent intent = new Intent(RegistryActivity.this, LoginActivity.class);
                        String phoneNumber = countryNumberCCP.getSelectedCountryCodeWithPlus().trim() + telephoneET.getText().toString().trim();
                        intent.putExtra("PhoneNumber", phoneNumber);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }, 2000);

                Snackbar snack = Snackbar.make(mainLayout, getString(R.string.rejestration), Snackbar.LENGTH_SHORT);
                View snackbarView = snack.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.dark_green));
                snack.show();
                break;

            case "cancel":

                animateSMSInfo("HIDE");
                registryBtn.doneLoadingAnimation(Color.RED, bitmapClose);

                Handler handler2 = new Handler();        //
                handler2.postDelayed(new Runnable() {    //
                    public void run() {             //
                        //
                        registryBtn.revertAnimation();  // restart animacji przycisku zarejestruj
                        telephoneET.setEnabled(true);
                        animateSMSInfo("SHOW");
                    }
                }, 2000);

                Snackbar snack2 = Snackbar.make(mainLayout, getString(R.string.rejestration_failure), Snackbar.LENGTH_SHORT);
                View snackbarView2 = snack2.getView();
                snackbarView2.setBackgroundColor(Color.RED);
                snack2.show();
                break;

            case "restart":

                registryBtn.revertAnimation();
                registryBtn.startAnimation(animShake);
                telephoneET.setEnabled(true);
                Snackbar.make(mainLayout, getString(R.string.not_Internet_connection), Snackbar.LENGTH_LONG).show();
                /*View view = snack.getView();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                params.gravity = Gravity.TOP;
                view.setLayoutParams(params); */
                break;

            case "logout":

                animateSMSInfo("HIDE");

                Handler handler3 = new Handler();
                handler3.postDelayed(new Runnable() {
                    public void run() {
                        animateSMSInfo("SHOW");
                    }
                }, 2000);

                Snackbar snack3 = Snackbar.make(mainLayout, getString(R.string.logout_successfull), Snackbar.LENGTH_SHORT);
                //View snackbarView3 = snack3.getView();
                //snackbarView3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                snack3.show();
                break;

            case "limit":

                registryBtn.revertAnimation();
                registryBtn.startAnimation(animShake);
                telephoneET.setEnabled(true);
                Snackbar.make(mainLayout, getString(R.string.limit), Snackbar.LENGTH_SHORT).show();
                break;

            default:
                registryBtn.revertAnimation();
                registryBtn.startAnimation(animShake);
                telephoneET.setEnabled(true);
                Snackbar.make(mainLayout, getString(R.string.incorrect_number_format), Snackbar.LENGTH_SHORT).show();
        }
    }


    private void animateSMSInfo(String action) {

        if (action.equals("SHOW")) {

            YoYo.with(Techniques.FadeInUp)
                    .duration(400)
                    .onStart(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            smsInformationTV.setVisibility(View.VISIBLE);
                        }
                    })
                    .playOn(smsInformationTV);
        }
        else {

            YoYo.with(Techniques.FadeOutDown)
                    .duration(400)
                    .onEnd(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            smsInformationTV.setVisibility(View.INVISIBLE);
                        }
                    })
                    .playOn(smsInformationTV);
        }
    }


    public void onClickRegistry(View view) {

        mainLayout.requestFocus();

        String countryNumber = countryNumberCCP.getSelectedCountryCodeWithPlus().trim();
        String telephoneNumber = telephoneET.getText().toString().trim();

        if (telephoneNumber.isEmpty() || !countryNumberCCP.isValidFullNumber()) {

            telephoneTIL.setAnimation(animShake);
            telephoneTIL.startAnimation(animShake);

            if (checkVibrationMode())
                vib.vibrate(120);

            telephoneTIL.setErrorEnabled(true);
            telephoneTIL.setError(getString(R.string.error_number2));
            return;
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        String phoneNumber = countryNumber + telephoneNumber;
        sendVerify(phoneNumber);
    }

/*
    public void tmpOnClick(View view) {

        registryBtn.startAnimation();
        registryBtn.doneLoadingAnimation(getResources().getColor(R.color.dark_green), bitmapDone);
        isStartAnimation = true; // zmienna wskazująca czy animacja została uruchomiona

        //countryNumberET.setEnabled(false);
        telephoneET.setEnabled(false);

        Snackbar snack = Snackbar.make(view, getString(R.string.rejestration), Snackbar.LENGTH_LONG);
        View snackbarView = snack.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.dark_green));
        snack.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                Intent intent = new Intent(RegistryActivity.this, LoginActivity.class);
                intent.putExtra("PhoneNumber", "+48501234567");
                startActivity(intent);
                startActivity(intent);
            }
        }, 2000);
    }*/
}