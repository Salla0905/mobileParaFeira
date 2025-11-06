package com.example.mobilesinara.registro_ponto;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mobilesinara.Interface.SQL.IEmpresa;
import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Models.Empresa;
import com.example.mobilesinara.Models.Operario;
import com.example.mobilesinara.Models.SenhaRequest;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalDateTime;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroPontoSenha extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private int idUser = -1;

    public RegistroPontoSenha() {}

    public static RegistroPontoSenha newInstance(String param1, String param2) {
        RegistroPontoSenha fragment = new RegistroPontoSenha();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences prefs = requireContext().getSharedPreferences("sinara_prefs", getContext().MODE_PRIVATE);
        idUser = prefs.getInt("idUser", -1);

        if (idUser == -1) {
            Toast.makeText(getContext(), "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
        }

        View view = inflater.inflate(R.layout.fragment_registro_ponto_senha, container, false);

        TextView textViewHora = view.findViewById(R.id.textView21);

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                LocalDateTime agora = LocalDateTime.now();
                String horaAtual = String.format(Locale.getDefault(), "%02d:%02d", agora.getHour(), agora.getMinute());
                textViewHora.setText(horaAtual);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        TextInputEditText editTextSenha = view.findViewById(R.id.text_senha);
        TextView textoRegistroRosto = view.findViewById(R.id.txt_registro_rosto);
        Button btRegistroPonto = view.findViewById(R.id.bt_registro_ponto);
        ImageView imgUser = view.findViewById(R.id.iconUser);
        ImageView imgEmpresa = view.findViewById(R.id.iconEmpresa);
        ImageView voltar = view.findViewById(R.id.voltar);
        IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
        iOperario.getOperarioPorId(idUser).enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Operario operario = response.body();
                    String urlOperario = operario.getImagemUrl();
                    if (urlOperario == null || urlOperario.isEmpty()) {
                        Glide.with(requireContext()).load(R.drawable.profile_pic_default).into(imgUser);
                    } else {
                        Glide.with(requireContext()).load(urlOperario).circleCrop().into(imgUser);
                    }

                    // Empresa
                    int idEmpresa = operario.getIdEmpresa();
                    IEmpresa iEmpresa = ApiClientAdapter.getRetrofitInstance().create(IEmpresa.class);
                    iEmpresa.getEmpresaPorId(idEmpresa).enqueue(new Callback<Empresa>() {
                        @Override
                        public void onResponse(Call<Empresa> call, Response<Empresa> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String urlEmpresa = response.body().getImagemUrl();

                                if (urlEmpresa == null || urlEmpresa.isEmpty()) {
                                    Glide.with(requireContext())
                                            .load(R.drawable.profile_pic_default)
                                            .circleCrop()
                                            .placeholder(R.drawable.profile_pic_default)
                                            .error(R.drawable.profile_pic_default)
                                            .into(imgEmpresa);
                                } else {
                                    Glide.with(requireContext())
                                            .load(urlEmpresa)
                                            .circleCrop()
                                            .placeholder(R.drawable.profile_pic_default)
                                            .error(R.drawable.profile_pic_default)
                                            .into(imgEmpresa);
                                }
                            }
                        }
                        @Override
                        public void onFailure(Call<Empresa> call, Throwable t) { }
                    });
                } else {
                    Log.e("API", "Erro de resposta: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                Log.e("RetrofitError", "Erro: " + t.getMessage(), t);
            }
        });

        btRegistroPonto.setOnClickListener(v -> {
            String senhaDigitada = editTextSenha.getText().toString();

            if (senhaDigitada.isEmpty()) {
                Toast.makeText(getContext(), "Digite a senha", Toast.LENGTH_SHORT).show();
                return;
            }

            if (idUser == -1) {
                Toast.makeText(getContext(), "Usuário inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            SenhaRequest request = new SenhaRequest(idUser, senhaDigitada);

            Call<Boolean> call = iOperario.verificarSenha(idUser, senhaDigitada);
            call.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    Log.d("Retrofit", "Response code: " + response.code());
                    Log.d("Retrofit", "Response body: " + response.body());

                    if (response.isSuccessful() && Boolean.TRUE.equals(response.body())) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("idUser", idUser);

                        Navigation.findNavController(v)
                                .navigate(R.id.action_registroPontoSenha_to_registroPontoConfirmar, bundle);
                    } else {
                        Toast.makeText(getContext(), "Senha incorreta!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Toast.makeText(getContext(), "Erro de conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        textoRegistroRosto.setOnClickListener(view1 ->
                Navigation.findNavController(view1).navigate(R.id.action_registroPontoSenha_to_registroPonto)
        );

        voltar.setOnClickListener(view12 ->
                Navigation.findNavController(view12).navigate(R.id.action_registroPontoSenha_to_registroPonto)
        );

        return view;
    }
}