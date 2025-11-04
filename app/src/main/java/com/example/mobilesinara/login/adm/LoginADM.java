package com.example.mobilesinara.login.adm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobilesinara.Interface.SQL.IEmpresa;
import com.example.mobilesinara.Models.EmpresaLoginRequestDTO;
import com.example.mobilesinara.Models.EmpresaLoginResponseDTO;
import com.example.mobilesinara.R;
import com.example.mobilesinara.TelaOpcoes;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginADM extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_adm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextInputLayout textInputLayout = findViewById(R.id.textInputLayoutSenha);
        TextInputEditText editTextCnpj = findViewById(R.id.text_cnpj);
        TextInputEditText editTextSenha = findViewById(R.id.text_senha);
        MaterialButton btLogin = findViewById(R.id.bt_fazer_login);
        ImageButton btVoltar = findViewById(R.id.bt_voltar);
        TextView cadastro = findViewById(R.id.texto_cadastro);

        // txt de cadastro
        cadastro.setOnClickListener(view -> {
            String url = "https://sinara-front.onrender.com";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // Botão de voltar
        btVoltar.setOnClickListener(view -> {
            startActivity(new Intent(LoginADM.this, TelaOpcoes.class));
            overridePendingTransition(0, 0);
        });

        // Controle de visibilidade da senha
        final boolean[] senhaVisivel = {false};
        textInputLayout.setEndIconOnClickListener(v -> {
            senhaVisivel[0] = !senhaVisivel[0];

            if (senhaVisivel[0]) {
                editTextSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                textInputLayout.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.olho_fechado));
            } else {
                editTextSenha.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                textInputLayout.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.olho_aberto));
            }

            if (editTextSenha.getText() != null) {
                editTextSenha.setSelection(editTextSenha.getText().length());
            }
        });

        // Botão de login
        btLogin.setOnClickListener(v -> {
            String cnpj = editTextCnpj.getText().toString().trim();
            String senha = editTextSenha.getText().toString().trim();

            if (!cnpj.isEmpty() && !senha.isEmpty()) {
                EmpresaLoginRequestDTO request = new EmpresaLoginRequestDTO(cnpj, senha);
                IEmpresa empresa = ApiClientAdapter.getRetrofitInstance().create(IEmpresa.class);

                Call<EmpresaLoginResponseDTO> call = empresa.loginEmpresa(request);
                call.enqueue(new Callback<EmpresaLoginResponseDTO>() {
                    @Override
                    public void onResponse(Call<EmpresaLoginResponseDTO> call, Response<EmpresaLoginResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            EmpresaLoginResponseDTO dados = response.body();

                            Log.d("LOGIN_ADM", "Login bem-sucedido para CNPJ: " + cnpj);
                            Log.d("LOGIN_ADM", "Email retornado: " + dados.getEmail());

                            getSharedPreferences("empresaPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putLong("idEmpresa", dados.getId() == null ? -1L : dados.getId())
                                    .putString("nomeEmpresa", dados.getNome())
                                    .putString("emailEmpresa", dados.getEmail())
                                    .putString("imagemUrlEmpresa", dados.getImagemUrl())
                                    .putString("cnpjEmpresa", cnpj)
                                    .apply();

                            Toast.makeText(LoginADM.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginADM.this, LoginADM2.class);
                            intent.putExtra("cnpj", cnpj);
                            startActivity(intent);
                            overridePendingTransition(0, 0);
                        } else {
                            Toast.makeText(LoginADM.this, "Credenciais inválidas.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<EmpresaLoginResponseDTO> call, Throwable t) {
                        Toast.makeText(LoginADM.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("LOGIN_ADM", "Erro de rede: " + t.getMessage());
                    }
                });
            } else {
                Toast.makeText(LoginADM.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
