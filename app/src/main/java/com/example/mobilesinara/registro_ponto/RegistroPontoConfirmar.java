package com.example.mobilesinara.registro_ponto;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobilesinara.Interface.SQL.IRegistroPonto;
import com.example.mobilesinara.Models.RegistroPontoRequest;
import com.bumptech.glide.Glide;
import com.example.mobilesinara.Interface.SQL.IEmpresa;
import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Models.Empresa;
import com.example.mobilesinara.Models.Operario;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroPontoConfirmar extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private int usuarioId;

    public RegistroPontoConfirmar() {}

    public static RegistroPontoConfirmar newInstance(String param1, String param2) {
        RegistroPontoConfirmar fragment = new RegistroPontoConfirmar();
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
        SharedPreferences prefs = requireContext().getSharedPreferences("sinara_prefs", getContext().MODE_PRIVATE);
        usuarioId = prefs.getInt("idUser", -1);

        if (usuarioId == -1) {
            Toast.makeText(getContext(), "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro_ponto_confirmar, container, false);

        TextView textViewHora = view.findViewById(R.id.textView21);
        ImageView imgUser = view.findViewById(R.id.iconUser);
        ImageView imgEmpresa = view.findViewById(R.id.iconEmpresa);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Atualiza hora a cada segundo
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String horaAtual = sdf.format(new Date());
                textViewHora.setText(horaAtual);
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        Button btRegistroPonto = view.findViewById(R.id.bt_confirmar);
        Button btCancelar = view.findViewById(R.id.bt_cancelar);

        btCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_registroPontoConfirmar_to_registroPonto);
            }
        });

        // Clique para registrar ponto
        btRegistroPonto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
                iOperario.getOperarioPorId(usuarioId).enqueue(new Callback<Operario>() {
                    @Override
                    public void onResponse(Call<Operario> call, Response<Operario> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Operario operario = response.body();
                            int idEmpresa = operario.getIdEmpresa();

                            IRegistroPonto iRegistroPonto = ApiClientAdapter.getRetrofitInstance().create(IRegistroPonto.class);

                            // Pega quantidade de registros do dia para decidir entrada/saída
                            iRegistroPonto.getQuantidadeRegistroPonto(usuarioId).enqueue(new Callback<Integer>() {
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        int quantidade = response.body();

                                        RegistroPontoRequest registro;
                                        if (quantidade % 2 == 0) {
                                            // Registrar entrada
                                            registro = new RegistroPontoRequest(
                                                    LocalDateTime.now().toString(), // horarioEntrada
                                                    null,                           // horarioSaida
                                                    usuarioId,
                                                    idEmpresa
                                            );
                                        } else {
                                            // Registrar saída
                                            registro = new RegistroPontoRequest(
                                                    null,                           // horarioEntrada
                                                    LocalDateTime.now().toString(), // horarioSaida
                                                    usuarioId,
                                                    idEmpresa
                                            );
                                        }

                                        iRegistroPonto.inserirRegistroPonto(registro).enqueue(new Callback<String>() {
                                            @Override
                                            public void onResponse(Call<String> call, Response<String> response) {
                                                if (response.isSuccessful()) {
                                                    String tipo = (quantidade % 2 == 0) ? "Entrada" : "Saída";
                                                    Toast.makeText(getContext(), tipo + " registrada com sucesso!", Toast.LENGTH_SHORT).show();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putBoolean("atualizarStatus", true);
                                                    Navigation.findNavController(view).navigate(R.id.action_registroPontoConfirmar_to_registroPontoSucesso, bundle);
                                                } else {
                                                    Toast.makeText(getContext(), "Erro ao registrar ponto: " + response.code(), Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<String> call, Throwable t) {
                                                Toast.makeText(getContext(), "Falha na comunicação: " + t.getMessage(), Toast.LENGTH_LONG).show();
                                                Log.e("RetrofitError", "Erro ao registrar ponto", t);
                                            }
                                        });

                                    } else {
                                        Toast.makeText(getContext(), "Erro ao verificar registros do dia", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    Toast.makeText(getContext(), "Falha ao buscar registros do dia: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(getContext(), "Erro ao buscar empresa do operário", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Operario> call, Throwable t) {
                        Toast.makeText(getContext(), "Erro ao buscar operário", Toast.LENGTH_SHORT).show();
                        Log.e("RetrofitError", "Erro ao buscar operário", t);
                    }
                });
            }
        });

        // Carrega imagens do usuário e empresa
        IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
        iOperario.getOperarioPorId(usuarioId).enqueue(new Callback<Operario>() {
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

                    // Carrega imagem da empresa
                    int idEmpresa = operario.getIdEmpresa();
                    IEmpresa iEmpresa = ApiClientAdapter.getRetrofitInstance().create(IEmpresa.class);
                    iEmpresa.getEmpresaPorId(idEmpresa).enqueue(new Callback<Empresa>() {
                        @Override
                        public void onResponse(Call<Empresa> call, Response<Empresa> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String urlEmpresa = response.body().getImagemUrl();
                                Glide.with(requireContext())
                                        .load((urlEmpresa == null || urlEmpresa.isEmpty()) ? R.drawable.profile_pic_default : urlEmpresa)
                                        .circleCrop()
                                        .placeholder(R.drawable.profile_pic_default)
                                        .error(R.drawable.profile_pic_default)
                                        .into(imgEmpresa);
                            }
                        }

                        @Override
                        public void onFailure(Call<Empresa> call, Throwable t) {}
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

        return view;
    }
}