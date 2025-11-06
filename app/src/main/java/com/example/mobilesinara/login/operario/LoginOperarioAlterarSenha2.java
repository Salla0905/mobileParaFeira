package com.example.mobilesinara.login.operario;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobilesinara.HomeOperario;
import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Models.Operario;
import com.example.mobilesinara.Models.SenhaRequestDTO;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;

public class LoginOperarioAlterarSenha2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_operario_alterar_senha2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextInputLayout textInputLayout1 = findViewById(R.id.textInputLayout1);
        TextInputEditText editTextSenha1 = findViewById(R.id.mostrar_senha_atual);
        TextInputLayout textInputLayout2 = findViewById(R.id.textInputLayout2);
        TextInputEditText editTextSenha2 = findViewById(R.id.mostrar_senha_nova);
        TextInputLayout textInputLayout3 = findViewById(R.id.textInputLayout3);
        TextInputEditText editTextSenha3 = findViewById(R.id.confirmar_senha);

        ImageButton btVoltar = findViewById(R.id.bt_voltar);
        Button btContinuar = findViewById(R.id.bt_continuar);
        int idUser;
        Bundle info = getIntent().getExtras();
        if (info != null && info.containsKey("idUser")) {
            idUser = info.getInt("idUser");
        } else {
            idUser = -1;
            Toast.makeText(this, "Erro: ID do usuário não recebido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        btContinuar.setOnClickListener(v -> {
            String senhaAtual = editTextSenha1.getText().toString();
            String senhaNova = editTextSenha2.getText().toString();
            String confirmarSenha = editTextSenha3.getText().toString();

            if (senhaAtual.isEmpty() || senhaNova.isEmpty() || confirmarSenha.isEmpty()) {
                Toast.makeText(LoginOperarioAlterarSenha2.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!senhaNova.equals(confirmarSenha)) {
                Toast.makeText(LoginOperarioAlterarSenha2.this, "As senhas não correspondem", Toast.LENGTH_SHORT).show();
                return;
            }

            if (senhaAtual.equals(senhaNova)) {
                Toast.makeText(LoginOperarioAlterarSenha2.this, "A nova senha não pode ser igual à senha atual", Toast.LENGTH_SHORT).show();
                return;
            }

            IOperario apiService = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);

            Call<Boolean> verificarCall = apiService.verificarSenha(idUser, senhaAtual);
            verificarCall.enqueue(new retrofit2.Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, retrofit2.Response<Boolean> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (response.body()) {
                            SenhaRequestDTO request = new SenhaRequestDTO(senhaAtual);
                            request.setNovaSenha(senhaNova);

                            Call<Operario> atualizarCall = apiService.atualizarSenha(idUser, request);
                            atualizarCall.enqueue(new retrofit2.Callback<Operario>() {
                                @Override
                                public void onResponse(Call<Operario> call, retrofit2.Response<Operario> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Toast.makeText(LoginOperarioAlterarSenha2.this, "Senha atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(LoginOperarioAlterarSenha2.this, HomeOperario.class);
                                        intent.putExtra("idUser", idUser);
                                        startActivity(intent);
                                        overridePendingTransition(0, 0);
                                    } else {
                                        Toast.makeText(LoginOperarioAlterarSenha2.this, "Falha ao atualizar a senha", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Operario> call, Throwable t) {
                                    Toast.makeText(LoginOperarioAlterarSenha2.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(LoginOperarioAlterarSenha2.this, "Senha atual incorreta", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginOperarioAlterarSenha2.this, "Erro na verificação da senha", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Toast.makeText(LoginOperarioAlterarSenha2.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


        //botão de voltar
        btVoltar.setOnClickListener(view -> {
            Intent intent = new Intent(LoginOperarioAlterarSenha2.this, LoginOperarioAlterarSenha.class);
            intent.putExtra("idUser", idUser);
            startActivity(intent);
        });


        final boolean[] senhaVisivel1 = {false};

        textInputLayout1.setEndIconOnClickListener(v -> {
            senhaVisivel1[0] = !senhaVisivel1[0];

            if (senhaVisivel1[0]) {
                // Mostra a senha
                editTextSenha1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                textInputLayout1.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.olho_fechado));
            } else {
                // Esconde a senha
                editTextSenha1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                textInputLayout1.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.olho_aberto));
            }

            // Mantém o cursor no fim
            if (editTextSenha1.getText() != null) {
                editTextSenha1.setSelection(editTextSenha1.getText().length());
            }
        });

        final boolean[] senhaVisivel2 = {false};

        textInputLayout2.setEndIconOnClickListener(v -> {
            senhaVisivel2[0] = !senhaVisivel2[0];

            if (senhaVisivel2[0]) {
                // Mostra a senha
                editTextSenha2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                textInputLayout2.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.olho_fechado));
            } else {
                // Esconde a senha
                editTextSenha2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                textInputLayout2.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.olho_aberto));
            }

            // Mantém o cursor no fim
            if (editTextSenha2.getText() != null) {
                editTextSenha2.setSelection(editTextSenha2.getText().length());
            }
        });

        final boolean[] senhaVisivel3 = {false};

        textInputLayout3.setEndIconOnClickListener(v -> {
            senhaVisivel3[0] = !senhaVisivel3[0];

            if (senhaVisivel3[0]) {
                // mostra a senha
                editTextSenha3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                textInputLayout3.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.olho_fechado));
            } else {
                // esconde a senha
                editTextSenha3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                textInputLayout3.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.olho_aberto));
            }

            // mantém o cursor no fim
            if (editTextSenha3.getText() != null) {
                editTextSenha3.setSelection(editTextSenha3.getText().length());
            }
        });
    }
}