package com.example.ubertutorandroidstudentapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ubertutorandroidstudentapp.Common.Common;
import com.example.ubertutorandroidstudentapp.Models.StudentModel;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171; //Any number
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    @BindView( R.id.progress_bar )
    ProgressBar progressBar;

    FirebaseDatabase database;
    DatabaseReference studentInfoRef;

    @Override
    protected void onStart() {
        super.onStart( );
        delaySplashScreen();
    }

    private void delaySplashScreen() {
        progressBar.setVisibility( View.VISIBLE );

        Completable.timer( 3, TimeUnit.SECONDS,
                AndroidSchedulers.mainThread() )
                .subscribe( () -> 
                        //After splash, ask for login if not logged in
                        firebaseAuth.addAuthStateListener( listener )
                );
    }

    @Override
    protected void onStop() {
        if ( firebaseAuth != null && listener != null )
            firebaseAuth.removeAuthStateListener( listener );
        super.onStop( );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_splash_screen );
        
        init();
    }

    private void init() {
        ButterKnife.bind( this );

        database = FirebaseDatabase.getInstance();
        studentInfoRef = database.getReference( Common.STUDENT_INFO_REFERENCE);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        firebaseAuth = FirebaseAuth.getInstance();
        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if(user != null)
            {
//                //Update
//                FirebaseInstanceId.getInstance()
//                        .getInstanceId()
//                        .addOnFailureListener( e -> Toast.makeText( SplashScreenActivity.this, e.getMessage(),
//                                Toast.LENGTH_SHORT).show() ).addOnSuccessListener(
//                        instanceIdResult -> {
//                            Log.d("TOKEN", instanceIdResult.getToken());
//                            UserUtils.updateToken( SplashScreenActivity.this,
//                                    instanceIdResult.getToken());
//                        } );
                checkUserFromFirebase();
            }
            else
                showLoginLayout();
        };
    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new  AuthMethodPickerLayout
                .Builder( R.layout.layout_sign_in )
                .setPhoneButtonId( R.id.btn_phone_sign_in )
                .setGoogleButtonId( R.id.btn_google_sign_in )
                .build();

        startActivityForResult( AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout( authMethodPickerLayout )
                .setIsSmartLockEnabled( false )
                .setTheme( R.style.LoginTheme )
                .setAvailableProviders( providers )
                .build(), LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode , int resultCode , @Nullable Intent data) {
        super.onActivityResult( requestCode , resultCode , data );
        if(requestCode == LOGIN_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent( data );
            if(resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else {
                Toast.makeText( this, "[ERROR]: " + response.getError().getMessage(),
                        Toast.LENGTH_SHORT ).show();
            }
        }
    }

    private void checkUserFromFirebase() {
        studentInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent( new ValueEventListener( ) {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
//                        Toast.makeText( SplashScreenActivity.this, "User already registered",
//                                Toast.LENGTH_SHORT).show();
                            StudentModel studentModel = dataSnapshot.getValue(StudentModel.class);
                            goToHomeActivity(studentModel);
                        }
                        else {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText( SplashScreenActivity.this, ""+ databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } );
    }

    private void showRegisterLayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder( this, R.style.DialogTheme );
        View itemView = LayoutInflater.from( this ).inflate( R.layout.layout_register, null );

        TextInputEditText edt_first_name = (TextInputEditText)itemView.findViewById( R.id.edt_first_name );
        TextInputEditText edt_last_name = (TextInputEditText)itemView.findViewById( R.id.edt_last_name );
        TextInputEditText edt_phone_number = (TextInputEditText)itemView.findViewById( R.id.edt_phone_number );
        TextInputEditText edt_student_number = (TextInputEditText)itemView.findViewById( R.id.edt_student_number );

        Button btn_continue = (Button)itemView.findViewById( R.id.btn_register );

        //Set data
        if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null &&
                !TextUtils.isEmpty( FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() ) )
            edt_phone_number.setText( FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() );

        //Set View
        builder.setView( itemView );
        AlertDialog dialog = builder.create();
        dialog.show();

        btn_continue.setOnClickListener( v -> {
            if(TextUtils.isEmpty( edt_first_name.getText().toString()) )
            {
                Toast.makeText( this, "Please enter your first name", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            else if(TextUtils.isEmpty( edt_last_name.getText().toString()) )
            {
                Toast.makeText( this, "Please enter your last name", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            else if(TextUtils.isEmpty( edt_student_number.getText().toString()) )
            {
                Toast.makeText( this, "Please enter your student number", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            else if(TextUtils.isEmpty( edt_phone_number.getText().toString()) )
            {
                Toast.makeText( this, "Please enter your phone number", Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            else {
                StudentModel model = new StudentModel();
                model.setFirstName( edt_first_name.getText().toString() );
                model.setLastName( edt_last_name.getText().toString() );
                model.setStudentNumber( edt_student_number.getText().toString() );
                model.setPhoneNumber( edt_phone_number.getText().toString() );


                studentInfoRef.child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                        .setValue( model )
                        .addOnFailureListener( e -> {
                            dialog.dismiss();
                            Toast.makeText( SplashScreenActivity.this,
                                    e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnSuccessListener( aVoid -> {
                            Toast.makeText( this, "Registered Successfully!",
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            goToHomeActivity( model );
                        } );

            }
        } );

    }

    private void goToHomeActivity(StudentModel studentModel) {
        Common.currentUser = studentModel; //Init value
        startActivity( new Intent( SplashScreenActivity.this, StudentHomeActivity.class ) );
        finish();
    }
}