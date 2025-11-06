package com.example.mobilesinara.login.operario;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mobilesinara.HomeOperario;
import com.example.mobilesinara.R;

public class LoginOperarioAlterarSenha extends AppCompatActivity {

    private int idUser = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_operario_alterar_senha);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton btVoltar = findViewById(R.id.bt_voltar);
        Button btSim = findViewById(R.id.bt_sim);
        Button btNao = findViewById(R.id.bt_nao);

        Bundle info = getIntent().getExtras();
        if (info != null && info.containsKey("idUser")) {
            idUser = info.getInt("idUser");
        } else {
            Toast.makeText(this, "Erro: usuário não identificado!", Toast.LENGTH_SHORT).show();
            android.util.Log.e("LoginOperarioAlterarSenha", "⚠Nenhum idUser recebido pelo Intent!");
        }

        btVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginOperarioAlterarSenha.this, LoginOperario.class);
                intent.putExtra("idUser", idUser);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
            }
        });

        btSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginOperarioAlterarSenha.this, LoginOperarioAlterarSenha2.class);
                intent.putExtra("idUser", idUser);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        btNao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idUser == -1) {
                    Toast.makeText(LoginOperarioAlterarSenha.this, "Erro: id do usuário inválido!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(LoginOperarioAlterarSenha.this, HomeOperario.class);
                intent.putExtra("idUser", idUser);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}
