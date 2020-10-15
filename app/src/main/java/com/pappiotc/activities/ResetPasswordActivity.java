package com.pappiotc.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.pappiotc.R;
import com.pappiotc.controller.SalesTrackerController;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ResetPasswordActivity extends AppCompatActivity implements Validator.ValidationListener {

    @InjectView(R.id.reset_password_send_button)
    Button resetButton;

    @InjectView(R.id.reset_password_email_edittext)
    @NotEmpty
    @Email
    EditText emailEditText;

    private Validator validator;
    private SalesTrackerController salesTrackerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        // initialize
        ButterKnife.inject(this);
        salesTrackerController = new SalesTrackerController(ResetPasswordActivity.this);
        validator = new Validator(this);
        validator.setValidationListener(this);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validate();
            }
        });
    }

    /**
     * onValidationSucceeded: Do the logic of login after validating user data
     */
    @Override
    public void onValidationSucceeded() {
        salesTrackerController.callResetPasswordService(emailEditText.getText().toString());
        finish();
    }

    /**
     * onValidationFailed: Display error message that results from the validation process
     *
     * @param errors
     */
    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        // Show errors
        for (int i = 0; i < errors.size(); i++) {
            ValidationError validationError = errors.get(i);
            String message = validationError.getCollatedErrorMessage(this);
            if (validationError.getView() instanceof EditText) {
                validationError.getView().requestFocus();
                ((EditText) validationError.getView()).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
