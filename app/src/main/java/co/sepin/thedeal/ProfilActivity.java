package co.sepin.thedeal;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

import co.sepin.thedeal.application.DrawerClass;
import co.sepin.thedeal.database.Contact;
import co.sepin.thedeal.database.Deal;
import co.sepin.thedeal.database.DealType;
import co.sepin.thedeal.database.Event;
import co.sepin.thedeal.database.User;
import co.sepin.thedeal.database.User_Table;
import co.sepin.thedeal.interfaces.InternetChangeListener;
import co.sepin.thedeal.other.ViewDialog;
import co.sepin.thedeal.receiver.InternetChangeReceiver;
import de.hdodenhof.circleimageview.CircleImageView;

import static co.sepin.thedeal.application.UpdateData.firebaseAuth;
import static co.sepin.thedeal.application.UpdateData.firebaseUser;
import static co.sepin.thedeal.application.UpdateData.updateUserStatus;


public class ProfilActivity extends DrawerClass {

    private static final int CAMERA_REQUEST = 0;
    private static final int GALLERY_REQUEST = 1;
    private InternetChangeReceiver internetChangeReceiver;
    private CircleImageView avatarCIV;
    private ImageButton addPhotoIB, deletePhotoIB, editIB;
    private Button logoutBtn;
    private Snackbar snack;
    private MaterialEditText aboutMeMET, emailMET;
    private FloatingActionButton doneFab, cancelFab;
    private ConstraintLayout constraintLayout;
    private String aboutMe, email, imagePath = null;
    private boolean isEdit = false, isRegister = false, isInternet = false, isUpload = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Animation animShow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_button);

        constraintLayout = (ConstraintLayout) findViewById(R.id.profilCL);
        ImageView strokeIV = (ImageView) findViewById(R.id.profil_strokeIV);
        avatarCIV = (CircleImageView) findViewById(R.id.profil_avatarCIV);
        addPhotoIB = (ImageButton) findViewById(R.id.profil_add_photoIB);
        deletePhotoIB = (ImageButton) findViewById(R.id.profil_delete_photoIB);
        editIB = (ImageButton) findViewById(R.id.profil_editIB);
        logoutBtn = (Button) findViewById(R.id.profil_logoutBtn);
        doneFab = (FloatingActionButton) findViewById(R.id.profil_doneFab);
        cancelFab = (FloatingActionButton) findViewById(R.id.profil_cancelFab);
        MaterialEditText telephoneMET = (MaterialEditText) findViewById(R.id.profil_telephoneMET);
        aboutMeMET = (MaterialEditText) findViewById(R.id.profil_about_meMET);
        emailMET = (MaterialEditText) findViewById(R.id.profil_emailMET);

        strokeIV.setAnimation(animShow);
        avatarCIV.setAnimation(animShow);
        aboutMeMET.setAnimation(animShow);
        telephoneMET.setAnimation(animShow);
        emailMET.setAnimation(animShow);

        strokeIV.startAnimation(animShow);
        avatarCIV.startAnimation(animShow);
        aboutMeMET.startAnimation(animShow);
        telephoneMET.startAnimation(animShow);
        emailMET.startAnimation(animShow);

        reloadAvatar();
        formatNumber(telephoneMET);
        getProfilInfoFromDb();
        hideKeyboardAndFocus(constraintLayout);
        moveFocusFromET();
    }


    @Override
    public int getContentView() {
        return R.layout.activity_profil;
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (!isRegister)
            registerBroadcast();
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (isRegister)
            unregisterBroadcast();
    }


    @Override
    public void onBackPressed() {

        if (isEdit) {

            aboutMeMET.setText(aboutMe);
            emailMET.setText(email);
            cancelEdit();
        } else
            super.onBackPressed();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {

            case GALLERY_REQUEST:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    addPhotoFromGallery();
                else
                    Toast.makeText(this, getString(R.string.permission_storage_denied), Toast.LENGTH_LONG).show();

                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;

            case CAMERA_REQUEST:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    addPhotoFromCamera();
                else
                    Toast.makeText(this, getString(R.string.permission_camera_denied), Toast.LENGTH_LONG).show();

                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GALLERY_REQUEST:

                if (resultCode == RESULT_OK && data != null && data.getData() != null) {

                    Uri filePath = data.getData();

                    if (isInternet)
                        createBitmap(filePath, null);
                    else
                        Toast.makeText(this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, getString(R.string.no_select_photo), Toast.LENGTH_SHORT).show();

                break;

            case CAMERA_REQUEST:

                if (resultCode == RESULT_OK) {

                    String root = Environment.getExternalStorageDirectory().toString();
                    File temp = new File(root + File.separator + "TheDeal/images/avatars/temp.jpg");

                    if (isInternet)
                        createBitmap(null, temp.getPath());
                    else
                        Toast.makeText(this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();

                    /*for (File temp : f.listFiles()) {
                        if (temp.getName().equals("temp.jpg")) {
                            f = temp;
                            createBitmap(null, f.getPath());
                            break;
                        }
                    }*/

                    if (!temp.exists()) {
                        Toast.makeText(this, getString(R.string.error_take_photo), Toast.LENGTH_SHORT).show();
                        return;
                    } else
                        temp.delete();
                } else
                    Toast.makeText(this, getString(R.string.no_take_photo), Toast.LENGTH_SHORT).show();

                break;
        }
    }


    private void createBitmap(Uri filePathGalery, String filePathCamera) {

        Bitmap bitmap = null;

        if (filePathGalery != null) {

            try {

                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), filePathGalery);


                String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
                Cursor cur = managedQuery(filePathGalery, orientationColumn, null, null, null);
                int orientation = -1;
                if (cur != null && cur.moveToFirst()) {
                    orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
                }

                if (orientation != 0) {

                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();

                    Matrix matrix = new Matrix();
                    matrix.preRotate(orientation);

                    // Rotating Bitmap & convert to ARGB_8888, required by tess
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {

                bitmap = BitmapFactory.decodeFile(String.valueOf(filePathCamera));

                int rotate = 0;
                try {
                    ExifInterface exif = new ExifInterface(String.valueOf(filePathCamera));
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotate = 270;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotate = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotate = 90;
                            break;
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Brak dostępu do pamięci wewnętrznej telefonu", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        String root = Environment.getExternalStorageDirectory().toString();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "Image_" + timeStamp + ".jpg";
        imagePath = root + File.separator + "TheDeal/images/avatars/" + imageName;


        File file = new File(imagePath);
        if (file.exists()) file.delete();

        try {

            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            Uri uri = Uri.fromFile(file);
            uploadImageToFirebase(uri, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        if (bitmap != null) {

            UserImage userImage = new UserImage();
            userImage.userId = 1;
            userImage.imagePath = imagePath;
            userImage.insert();
        }

        Glide.with(this)
                .load(bitmap)
                .into(avatarCIV);

        Uri uri = Uri.fromFile(file);
        uploadImageToFirebase(uri);
        */
        //animatePhotoButtons("hide");
        //loadAvatar(); // metoda w DrawerClass
    }


    private void reloadAvatar() {

        String avatarUri = getAvatarUriFromDB();

        Glide.with(this)
                .load(avatarUri)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                        //Delete.table(UserImage.class); // usuwanie wyniku z tabeli
                        loadAvatar();
                        animatePhotoButtons("show");

                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        animatePhotoButtons("hide");
                        return false;
                    }
                })
                .error(Glide.with(this).load(R.drawable.user_profil))
                .into(avatarCIV);
    }


    private void getProfilInfoFromDb() {

        FlowCursor cursor = new Select().from(User.class).query();

        if (cursor.getCount() > 0)
            while (cursor.moveToNext()) {

                aboutMe = cursor.getString(cursor.getColumnIndex("aboutMe"));
                email = cursor.getString(cursor.getColumnIndex("email"));

                aboutMeMET.setText(aboutMe);
                emailMET.setText(email);
            }

        cursor.close();
    }


    private void formatNumber(MaterialEditText telephoneMET) {

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getNetworkCountryIso();//getSimCountryIso();
        String formattedNumber = PhoneNumberUtils.formatNumber(firebaseUser.getPhoneNumber(), countryCode);
        telephoneMET.setText(formattedNumber);
    }


    private void cancelEdit() {

        isEdit = false;
        aboutMeMET.setEnabled(false);
        aboutMeMET.setHideUnderline(true);
        aboutMeMET.setTextColor(getResources().getColor(R.color.colorDisabled));
        emailMET.setEnabled(false);
        emailMET.setHideUnderline(true);
        emailMET.setTextColor(getResources().getColor(R.color.colorDisabled));
        constraintLayout.requestFocus();

        animatePhotoButtons("show edit");
        animatePhotoButtons("show logout");
        animatePhotoButtons("hide done");
        animatePhotoButtons("hide cancel");
    }


    private void addPhotoFromCamera() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        else {

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + File.separator + "TheDeal/images/avatars");
            if (!myDir.exists())
                myDir.mkdirs();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(root + File.separator + "TheDeal/images/avatars", "temp.jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }


    private void addPhotoFromGallery() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GALLERY_REQUEST);
        else {

            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + File.separator + "TheDeal/images/avatars");
            if (!myDir.exists())
                myDir.mkdirs();

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(intent, GALLERY_REQUEST);
        }
    }


    private void deleteImage() {

        FirebaseStorage storage = FirebaseStorage.getInstance();

        try {

            StorageReference storageImages = storage.getReference("avatars")
                    .child(Objects.requireNonNull(Objects.requireNonNull(firebaseUser).getPhoneNumber()));
            storageImages.delete().addOnSuccessListener(new OnSuccessListener<Void>() {

                @Override
                public void onSuccess(Void aVoid) {

                    deleteUriFromFirebase();

                    try {

                        User user = new User();
                        user.avatarUri = "default";
                        user.update();
                        //SQLite.update(User_Table.avatarUri).set(User_Table.avatarUri.).from(User.class).where(User_Table.userId.eq(firebaseUser.getUid())).querySingle().set;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    File file = new File(imagePath); // usuwanie pliku
                    if (file.exists()) file.delete();

                    //Delete.table(User.class); // usuwanie tabeli

                    Glide.with(ProfilActivity.this)
                            .load(R.drawable.user_profil)
                            .into(avatarCIV);

                    animatePhotoButtons("show");
                    loadAvatar(); // metoda w DrawerClass

                    Toast.makeText(ProfilActivity.this, "Pomyślnie usunięto", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfilActivity.this, "Usuwanie nie powiodło się", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {

            e.printStackTrace();
            Toast.makeText(ProfilActivity.this, "Wystąpił błąd podczas usuwania", Toast.LENGTH_SHORT).show();
        }
    }


    private void deleteUriFromFirebase() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseProfiles = database.getReference("Profiles").child(Objects.requireNonNull(firebaseUser.getPhoneNumber()));
        databaseProfiles.child("avatar_uri").setValue("default");
    }


    private void uploadImageToFirebase(Uri filePath, final Bitmap bitmap) {

        if (filePath != null) {

            final ViewDialog loadingDialog = new ViewDialog(this, getString(R.string.sending_image));
            loadingDialog.showDialog();
            isUpload = true;

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageImages = storage.getReference("avatars").child(Objects.requireNonNull((firebaseUser).getPhoneNumber()));

            storageImages.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    storageImages.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            updateImagePathToFirebase(uri);
                            setNewImage(uri);
                        }
                    });

                    loadingDialog.hideDialog();
                    isUpload = false;

                    Toast.makeText(ProfilActivity.this, "Pomyślnie załadowano zdjęcie", Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            loadingDialog.hideDialog();
                            isUpload = false;

                            Toast.makeText(ProfilActivity.this, "Błąd podczas wgrywania zdjęcia" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            long progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            loadingDialog.uploadProgress((int) progress);
                        }
                    });
        }
    }


    private void updateImagePathToFirebase(Uri uri) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseProfiles = database.getReference("Profiles").child(Objects.requireNonNull(firebaseUser.getPhoneNumber()));
        databaseProfiles.child("avatar_uri").setValue(uri.toString());
        System.out.println("Profil add");
    }


    private void setNewImage(Uri uri) {

        User user = new User();
        user.userId = firebaseUser.getUid();
        user.avatarUri = uri.toString();
        user.update();

        Glide.with(ProfilActivity.this)
                .load(uri)
                .into(avatarCIV);

        animatePhotoButtons("hide");
        loadAvatar(); // metoda w DrawerClass
    }


    private void saveUserInfoToDB() {

        try {

            User user = new User();
            user.phone = firebaseUser.getPhoneNumber();
            user.aboutMe = aboutMe;
            user.email = email;
            user.save();

            FlowCursor cursor = new Select().from(User.class).query();

            if (cursor.getCount() > 0)
                while (cursor.moveToNext()) {

                    System.out.println("Profil aboutMe: " + cursor.getString(cursor.getColumnIndex("aboutMe")));
                    System.out.println("Profil email: " + cursor.getString(cursor.getColumnIndex("email")));

                    aboutMeMET.setText(aboutMe);
                    emailMET.setText(email);
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void viewAvatar() {

        String avatarUri = getAvatarUriFromDB();

        if (!avatarUri.equals("default")) {

            Fresco.initialize(this);
            new ImageViewer.Builder(this, Collections.singletonList(avatarUri))
                    .hideStatusBar(true)
                    .allowZooming(true)
                    .allowSwipeToDismiss(true)
                    .show();
        }
/*
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(avatarUri), "image/*");
        startActivity(intent);*/
    }


    private String getAvatarUriFromDB() {

        String avatarUri = "";
        try {
            avatarUri = SQLite.select(User_Table.avatarUri).from(User.class).querySingle().getAvatarUri();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return avatarUri;
    }


    public void onClickAvatar(View view) {
        viewAvatar();
    }


    public void onClickAddPhoto(View view) {

        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle(getString(R.string.upload_picture_option));

        myAlertDialog.setPositiveButton(getString(R.string.gallery),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        addPhotoFromGallery();
                    }
                });

        myAlertDialog.setNegativeButton(getString(R.string.camera), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                addPhotoFromCamera();
            }
        });

        myAlertDialog.show();
    }


    public void onClickDeletePhoto(View view) {

        if (isInternet)
            deleteImage();
        else
            Toast.makeText(this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
    }


    public void onClickEdit(View view) {

        isEdit = true;
        aboutMeMET.setEnabled(true);
        aboutMeMET.setHideUnderline(false);
        aboutMeMET.setTextColor(getResources().getColor(R.color.textColorSecondary));
        emailMET.setEnabled(true);
        emailMET.setHideUnderline(false);
        emailMET.setTextColor(getResources().getColor(R.color.textColorSecondary));

        animatePhotoButtons("hide edit");
        animatePhotoButtons("hide logout");
        animatePhotoButtons("show done");
        animatePhotoButtons("show cancel");
    }


    public void onClickDone(View view) {

        if (isInternet) {

            aboutMe = String.valueOf(aboutMeMET.getText()).trim();
            email = String.valueOf(emailMET.getText()).trim();

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseProfiles = database.getReference("Profiles").child(Objects.requireNonNull(firebaseUser.getPhoneNumber()));

            databaseProfiles.child("about_me").setValue(aboutMe);
            databaseProfiles.child("email").setValue(email);

            databaseProfiles.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Toast.makeText(ProfilActivity.this, "Zaktualizowano", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ProfilActivity.this, "Błąd podczas aktualizacji", Toast.LENGTH_SHORT).show();
                }
            });

            saveUserInfoToDB();
            cancelEdit();
        } else {

            Toast.makeText(this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
        }
    }


    public void onClickCancel(View view) {

        aboutMeMET.setText(aboutMe);
        emailMET.setText(email);

        cancelEdit();
    }


    public void onClickLogout(View view) {

        if (isInternet) {

            try {

                imagePath = getAvatarUriFromDB();
                File file = new File(imagePath); // usuwanie zdjęcia z telefonu
                if (file.exists()) file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Delete.tables(Contact.class, Deal.class, DealType.class, Event.class, User.class); // czyszczenie tabel na koniec
            updateUserStatus("Offline");

            if (firebaseAuth != null)
                firebaseAuth.signOut();

            SharedPreferences.Editor editor = getSharedPreferences("Login", MODE_PRIVATE).edit();
            editor.putBoolean("login", false);
            editor.apply();

            Intent intent = new Intent(ProfilActivity.this, RegistryActivity.class);
            intent.putExtra("Logout", "true");
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in_activity, R.anim.fade_out_activity);

            finishAffinity();
        }
        else
            Toast.makeText(this, getString(R.string.no_Internet_connection), Toast.LENGTH_SHORT).show();
    }


    private void registerBroadcast() {

        InternetChangeReceiver.isNews = false;
        InternetChangeReceiver.isProfil = true;
        InternetChangeReceiver.isSelectContacts = false;
        InternetChangeReceiver.isWeather = false;


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");

        internetChangeReceiver = new InternetChangeReceiver();
        registerReceiver(internetChangeReceiver, intentFilter);

        isRegister = true;

        InternetChangeReceiver.bindProfilListener(new InternetChangeListener() {

            @Override
            public void internetConnected() {
                isInternet = true;

                if (isUpload && snack != null) {

                    snack.dismiss();
                    snack = null;
                }
            }

            @Override
            public void noInternetConnected() {
                isInternet = false;

                if (isUpload) {

                    snack = Snackbar.make(findViewById(android.R.id.content), getString(R.string.stoppingUpload), Snackbar.LENGTH_INDEFINITE);
                    View snackbarView = snack.getView();
                    snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    snack.show();
                }
            }
        });
    }


    private void unregisterBroadcast() {

        InternetChangeReceiver.isProfil = false;
        InternetChangeReceiver.bindProfilListener(null);
        unregisterReceiver(internetChangeReceiver);
        isRegister = false;
    }


    private void animatePhotoButtons(String behavior) {

        switch (behavior) {
            case "show":
                YoYo.with(Techniques.FadeOutRight)
                        .duration(400)
                        .onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                deletePhotoIB.setVisibility(View.INVISIBLE);
                            }
                        })
                        .playOn(deletePhotoIB);

                YoYo.with(Techniques.FadeInLeft)
                        .duration(400)
                        .onStart(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                addPhotoIB.setVisibility(View.VISIBLE);
                            }
                        })
                        .playOn(addPhotoIB);
                break;

            case "hide":
                YoYo.with(Techniques.FadeInRight)
                        .duration(400)
                        .onStart(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                deletePhotoIB.setVisibility(View.VISIBLE);
                            }
                        })
                        .playOn(deletePhotoIB);

                YoYo.with(Techniques.FadeOutLeft)
                        .duration(400)
                        .onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                addPhotoIB.setVisibility(View.INVISIBLE);
                            }
                        })
                        .playOn(addPhotoIB);
                break;

            case "show edit":
                YoYo.with(Techniques.FadeInRight)
                        .duration(400)
                        .onStart(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                editIB.setVisibility(View.VISIBLE);
                            }
                        })
                        .playOn(editIB);
                break;

            case "hide edit":
                YoYo.with(Techniques.FadeOutRight)
                        .duration(400)
                        .onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                editIB.setVisibility(View.INVISIBLE);
                            }
                        })
                        .playOn(editIB);
                break;

            case "show logout":
                YoYo.with(Techniques.DropOut)
                        .duration(500)
                        .onStart(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                logoutBtn.setVisibility(View.VISIBLE);
                            }
                        })
                        .playOn(logoutBtn);
                break;

            case "hide logout":
                YoYo.with(Techniques.FadeOutDown)
                        .duration(400)
                        .onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                logoutBtn.setVisibility(View.INVISIBLE);
                            }
                        })
                        .playOn(logoutBtn);
                break;

            case "show done":
                YoYo.with(Techniques.FadeInLeft)
                        .duration(500)
                        .onStart(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                doneFab.setVisibility(View.VISIBLE);
                            }
                        })
                        .playOn(doneFab);
                break;

            case "hide done":
                YoYo.with(Techniques.FadeOutLeft)
                        .duration(400)
                        .onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                doneFab.setVisibility(View.INVISIBLE);
                            }
                        })
                        .playOn(doneFab);
                break;

            case "show cancel":
                YoYo.with(Techniques.FadeInRight)
                        .duration(500)
                        .onStart(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                cancelFab.setVisibility(View.VISIBLE);
                            }
                        })
                        .playOn(cancelFab);
                break;

            case "hide cancel":
                YoYo.with(Techniques.FadeOutRight)
                        .duration(400)
                        .onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                cancelFab.setVisibility(View.INVISIBLE);
                            }
                        })
                        .playOn(cancelFab);
                break;
        }
    }


    private void moveFocusFromET() {

        aboutMeMET.setHorizontallyScrolling(false); // multiline ET
        aboutMeMET.setMaxLines(Integer.MAX_VALUE); // multiline ET
        aboutMeMET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    constraintLayout.requestFocus();
                    hideSoftKeyboard(v);
                    return true;
                }
                return false;
            }
        });

        emailMET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    constraintLayout.requestFocus();
                    hideSoftKeyboard(v);
                    return true;
                }
                return false;
            }
        });
    }


    public void hideKeyboardAndFocus(final View view) {

        if (!(view instanceof EditText))
            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    hideSoftKeyboard(view);
                    v.requestFocus();
                    return false;
                }
            });

        if (this.getCurrentFocus() != null)
            if (view instanceof ViewGroup)
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                    View innerView = ((ViewGroup) view).getChildAt(i);
                    hideKeyboardAndFocus(innerView);
                }
    }


    public void hideSoftKeyboard(View view) {

        InputMethodManager inputMethodManager = (InputMethodManager) ProfilActivity.this.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}