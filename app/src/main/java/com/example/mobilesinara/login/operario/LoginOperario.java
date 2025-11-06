package com.example.mobilesinara.login.operario;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Models.OperarioLoginRequestDTO;
import com.example.mobilesinara.R;
import com.example.mobilesinara.TelaOpcoes;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.example.mobilesinara.api.OperarioService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

import retrofit2.Callback;
import retrofit2.Call;
import retrofit2.Response;

public class LoginOperario extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_operario);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton btVoltar = findViewById(R.id.bt_voltar);
        TextInputLayout textInputLayout = findViewById(R.id.textInputLayout5);
        TextInputEditText editTextSenha = findViewById(R.id.text_senha);
        TextInputEditText editTextCpf = findViewById(R.id.text_pesquisa);
        TextInputEditText editTextEmail = findViewById(R.id.text_email);
        TextInputEditText editTextCodEmpresa = findViewById(R.id.text_cod_empresa);
        Button login = findViewById(R.id.bt_fazer_login);

        btVoltar.setOnClickListener(view -> {
            startActivity(new Intent(LoginOperario.this, TelaOpcoes.class));
            overridePendingTransition(0, 0);
        });

        final boolean[] senhaVisivel = {false};
        textInputLayout.setEndIconOnClickListener(v -> {
            senhaVisivel[0] = !senhaVisivel[0];

            if (senhaVisivel[0]) {
                editTextSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                textInputLayout.setEndIconDrawable(ContextCompat.getDrawable(LoginOperario.this, R.drawable.olho_fechado));
            } else {
                editTextSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                textInputLayout.setEndIconDrawable(ContextCompat.getDrawable(LoginOperario.this, R.drawable.olho_aberto));
            }

            if (editTextSenha.getText() != null) {
                editTextSenha.setSelection(editTextSenha.getText().length());
            }
        });

        login.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            String cpf = editTextCpf.getText().toString().trim();
            String senha = editTextSenha.getText().toString().trim();
            String codigoEmpresa = editTextCodEmpresa.getText().toString().trim();

            if (email.isEmpty() || cpf.isEmpty() || senha.isEmpty() || codigoEmpresa.isEmpty()) {
                Toast.makeText(LoginOperario.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            OperarioLoginRequestDTO request = new OperarioLoginRequestDTO(email, cpf, senha, codigoEmpresa);
            OperarioService service = ApiClientAdapter.getRetrofitInstance().create(OperarioService.class);

            service.loginOperario(request).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                        buscarIdOperario(cpf);
                    } else {
                        Toast.makeText(LoginOperario.this, "Dados inválidos. Tente novamente.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Toast.makeText(LoginOperario.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void buscarIdOperario(String cpf) {
        IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
        Call<Integer> call = iOperario.getIdPorCpf(cpf);

        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int idUser = response.body();
                    Log.d("LoginOperario", "ID do operário retornado: " + idUser);

                    Intent intent = new Intent(LoginOperario.this, LoginOperarioAlterarSenha.class);
                    intent.putExtra("idUser", idUser);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                } else {
                    Toast.makeText(LoginOperario.this, "Falha ao recuperar ID do operário", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Toast.makeText(LoginOperario.this, "Erro ao buscar ID: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LoginOperario", "Erro Retrofit: ", t);
            }
        });
    }
}