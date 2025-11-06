package com.example.mobilesinara.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mobilesinara.R;
import com.example.mobilesinara.Interface.Mongo.IFormularioPersonalizado;
import com.example.mobilesinara.Interface.Mongo.IRespostaFormularioPersonalizado;
import com.example.mobilesinara.Interface.SQL.IEmpresa;
import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Interface.SQL.IRegistroPonto;
import com.example.mobilesinara.Models.Empresa;
import com.example.mobilesinara.Models.Operario;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.example.mobilesinara.databinding.FragmentHomeBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Retrofit retrofit;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        if (args == null || !args.containsKey("idUser")) {
            Toast.makeText(getContext(), "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
            return root;
        }

        int idUser = args.getInt("idUser");

        Button botaoPonto = root.findViewById(R.id.button2);
        botaoPonto.setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.action_navigation_home_to_registroPonto);
            getActivity().overridePendingTransition(0, 0);
        });

        Button btStatus = root.findViewById(R.id.button7);
        TextView formsPendentes = root.findViewById(R.id.formsPendentes);
        TextView formsRespondidos = root.findViewById(R.id.formsRespondidos);
        ImageView iconUser = root.findViewById(R.id.imgUser);
        ImageView iconEmpresa = root.findViewById(R.id.imgEmpresa);

        Button btSinaraAi = root.findViewById(R.id.button);
        btSinaraAi.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.ChatBot));

        Button btForms = root.findViewById(R.id.button3);
        btForms.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_forms_operario));

        ImageView btConfiguration = root.findViewById(R.id.imageView13);
        btConfiguration.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_configuration));

        chamarApi(formsPendentes, formsRespondidos, iconUser, iconEmpresa, btStatus, idUser);

        return root;
    }

    private void chamarApi(TextView formsPendentes, TextView formsRespondidos,
                           ImageView iconUser, ImageView iconEmpresa,
                           Button btStatus, int idUser) {

        retrofit = new Retrofit.Builder()
                .baseUrl("https://ms-sinara-sql-oox0.onrender.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
        IRegistroPonto iRegistroPonto = ApiClientAdapter.getRetrofitInstance().create(IRegistroPonto.class);
        IRespostaFormularioPersonalizado iRespostaFormularioPersonalizado = ApiClientAdapter.getRetrofitInstance().create(IRespostaFormularioPersonalizado.class);
        IFormularioPersonalizado iFormularioPersonalizado = ApiClientAdapter.getRetrofitInstance().create(IFormularioPersonalizado.class);
        IEmpresa iEmpresa = ApiClientAdapter.getRetrofitInstance().create(IEmpresa.class);

        iRegistroPonto.getStatusOperario(idUser).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    btStatus.setText(response.body() ? "Online" : "Offline");
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("STATUS", "Erro ao buscar status: " + t.getMessage());
            }
        });

        iRespostaFormularioPersonalizado.getQuantidadeRespostasPorUsuario(idUser)
                .enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            formsRespondidos.setText(String.valueOf(response.body()));
                            Log.d("FORM_RESPONDIDOS", "Respondidos: " + response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Log.e("FORM_RESPONDIDOS", "Erro: " + t.getMessage());
                    }
                });

        iFormularioPersonalizado.getQtdFormulariosPendentes(idUser)
                .enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            formsPendentes.setText(String.valueOf(response.body()));
                            Log.d("FORM_PENDENTES", "Pendentes: " + response.body());
                        }
                    }
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Log.e("FORM_PENDENTES", "Erro: " + t.getMessage());
                    }
                });

        iOperario.getOperarioPorId(idUser).enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Operario operario = response.body();

                    String urlOperario = operario.getImagemUrl();
                    if (isAdded() && getContext() != null) {
                        if (urlOperario == null || urlOperario.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(R.drawable.profile_pic_default)
                                    .circleCrop()
                                    .into(iconUser);
                        } else {
                            String urlCompletaOperario = urlOperario.startsWith("http")
                                    ? urlOperario
                                    : "https://ms-sinara-sql-oox0.onrender.com" + urlOperario;

                            Glide.with(requireContext())
                                    .load(urlCompletaOperario)
                                    .circleCrop()
                                    .placeholder(R.drawable.profile_pic_default)
                                    .error(R.drawable.profile_pic_default)
                                    .into(iconUser);
                        }
                    }

                    int idEmpresa = operario.getIdEmpresa();
                    Log.d("EMPRESA_ID", "ID Empresa: " + idEmpresa);

                    iEmpresa.getEmpresaPorId(idEmpresa).enqueue(new Callback<Empresa>() {
                        @Override
                        public void onResponse(Call<Empresa> call, Response<Empresa> response) {
                            if (!isAdded() || getContext() == null) return;

                            if (response.isSuccessful() && response.body() != null) {
                                String urlEmpresa = response.body().getImagemUrl();
                                Log.d("URL_EMPRESA", "URL recebida: " + urlEmpresa);

                                String urlCompletaEmpresa = (urlEmpresa != null && urlEmpresa.startsWith("http"))
                                        ? urlEmpresa
                                        : "https://ms-sinara-sql-oox0.onrender.com" + (urlEmpresa != null ? urlEmpresa : "");

                                Glide.with(requireContext())
                                        .load(urlCompletaEmpresa)
                                        .circleCrop()
                                        .placeholder(R.drawable.profile_pic_default)
                                        .error(R.drawable.profile_pic_default)
                                        .into(iconEmpresa);
                            } else {
                                Log.e("EMPRESA", "Erro resposta: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Empresa> call, Throwable t) {
                            Log.e("EMPRESA", "Erro: " + t.getMessage());
                        }
                    });

                } else {
                    Log.e("OPERARIO", "Erro resposta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                Log.e("OPERARIO", "Erro Retrofit: " + t.getMessage(), t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
