package uni.aimar.anaitapp.supabase.logIn;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import uni.aimar.anaitapp.MainActivity;
import uni.aimar.anaitapp.R;


public class LogInSupa extends AppCompatActivity {
    private SupabaseClient supabaseClient;
    private EditText editTextEmail;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        supabaseClient = new SupabaseClient();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        TextView textViewRegister = findViewById(R.id.textViewRegister);

        buttonLogin.setOnClickListener(v -> attemptLogin());

        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LogInSupa.this, uni.aimar.anaitapp.supabase.logIn.RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void attemptLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        supabaseClient.loginUser(email, password, new SupabaseClient.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    Toast.makeText(LogInSupa.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LogInSupa.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() ->
                        Toast.makeText(LogInSupa.this, error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}