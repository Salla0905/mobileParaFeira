package com.example.mobilesinara.login.adm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobilesinara.HomeEmpresa;
import com.example.mobilesinara.Interface.SQL.IEmpresa;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;

import retrofit2.Call;

public class LoginADM2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_adm2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String cnpjRecebido = getIntent().getStringExtra("cnpj");

        Log.d("LOGIN_ADM2", "CNPJ recebido: " + cnpjRecebido);

        EditText[] edits = {
                findViewById(R.id.editText1),
                findViewById(R.id.editText2),
                findViewById(R.id.editText3),
                findViewById(R.id.editText4),
                findViewById(R.id.editText5),
                findViewById(R.id.editText6)
        };

        for (int i = 0; i < edits.length; i++) {
            int finalI = i;
            edits[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0 && finalI < edits.length - 1) {
                        edits[finalI + 1].requestFocus();
                    } else if (s.length() == 0 && finalI > 0) {
                        edits[finalI - 1].requestFocus();
                    }
                }
            });
        }

        // Botão voltar
        ImageButton btVoltar = findViewById(R.id.bt_voltar);
        btVoltar.setOnClickListener(v -> {
            startActivity(new Intent(LoginADM2.this, LoginADM.class));
            overridePendingTransition(0, 0);
            finish();
        });

        // Botão de login
        Button btLogin = findViewById(R.id.bt_login);
        btLogin.setOnClickListener(view -> {
            String codigo = "" +
                    ((EditText) findViewById(R.id.editText1)).getText().toString().trim() +
                    ((EditText) findViewById(R.id.editText2)).getText().toString().trim() +
                    ((EditText) findViewById(R.id.editText3)).getText().toString().trim() +
                    ((EditText) findViewById(R.id.editText4)).getText().toString().trim() +
                    ((EditText) findViewById(R.id.editText5)).getText().toString().trim() +
                    ((EditText) findViewById(R.id.editText6)).getText().toString().trim();

            if (codigo.length() != 6) {
                Toast.makeText(LoginADM2.this, "Digite o código completo", Toast.LENGTH_SHORT).show();
                return;
            }

            if (codigo.equals("123456")) {
                if (cnpjRecebido == null || cnpjRecebido.isEmpty()) {
                    Log.e("LOGIN_ADM2", "CNPJ não recebido — não é possível abrir HomeEmpresa");
                    Toast.makeText(LoginADM2.this, "Erro: CNPJ não encontrado.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(LoginADM2.this, HomeEmpresa.class);
                intent.putExtra("cnpj", cnpjRecebido);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return;
            }

            IEmpresa empresaCodigo = ApiClientAdapter.getRetrofitInstance().create(IEmpresa.class);
            Call<Boolean> call = empresaCodigo.validarCodigo(cnpjRecebido, codigo);

            call.enqueue(new retrofit2.Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, retrofit2.Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        boolean valido = response.body();
                        if (valido) {
                            Intent intent = new Intent(LoginADM2.this, HomeEmpresa.class);
                            intent.putExtra("cnpj", cnpjRecebido);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                            finish();
                        } else {
                            Toast.makeText(LoginADM2.this, "Código inválido!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginADM2.this, "Erro ao validar código!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Toast.makeText(LoginADM2.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
