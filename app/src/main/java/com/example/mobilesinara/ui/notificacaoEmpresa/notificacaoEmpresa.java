package com.example.mobilesinara.ui.notificacaoEmpresa;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilesinara.Interface.Mongo.INotificacao;
import com.example.mobilesinara.Interface.SQL.IEmpresa;
import com.example.mobilesinara.Models.Empresa;
import com.example.mobilesinara.Models.Notificacao;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.example.mobilesinara.databinding.FragmentNotificacaoEmpresaBinding;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class notificacaoEmpresa extends Fragment {

    private FragmentNotificacaoEmpresaBinding binding;
    private static final String TAG = "NotificacaoEmpresa";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        NotificacaoEmpresaViewModel notificacaoEmpresaViewModel =
                new ViewModelProvider(this).get(NotificacaoEmpresaViewModel.class);

        binding = FragmentNotificacaoEmpresaBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        String cnpj = null;

        if (args != null && args.containsKey("cnpj")) {
            cnpj = args.getString("cnpj");
            Log.d(TAG, "CNPJ recebido via argumentos: " + cnpj);
        } else {
            SharedPreferences prefs = requireContext().getSharedPreferences("sinara_prefs", Context.MODE_PRIVATE);
            cnpj = prefs.getString("cnpj", null);
            Log.d(TAG, "CNPJ recuperado do SharedPreferences: " + cnpj);
        }

        if (cnpj == null || cnpj.isEmpty()) {
            Log.e(TAG, "CNPJ é null ou vazio! Não é possível chamar a API.");
            Toast.makeText(getContext(), "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
            return root;
        }

        RecyclerView recyclerView = root.findViewById(R.id.recyclerNotification);
        ImageView imgEmpresa = root.findViewById(R.id.imgEmpresa);

        IEmpresa iEmpresa = ApiClientAdapter.getRetrofitInstance().create(IEmpresa.class);
        Log.d(TAG, "Chamando API getEmpresaPorCnpj com CNPJ: " + cnpj);
        Call<Empresa> callEmpresaPorCnpj = iEmpresa.getEmpresaPorCnpj(cnpj);

        callEmpresaPorCnpj.enqueue(new Callback<Empresa>() {
            @Override
            public void onResponse(Call<Empresa> call, Response<Empresa> response) {
                Log.d(TAG, "Resposta empresa - código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Empresa empresa = response.body();
                    Log.d(TAG, "Empresa retornada: " + new Gson().toJson(empresa));

                    int empresaId = empresa.getId();
                    String urlEmpresa = empresa.getImagemUrl();

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

                    Log.d(TAG, "Chamando getNotificacaoPorEmpresa com id: " + empresaId);
                    INotificacao iNotificacao = ApiClientAdapter.getRetrofitInstance().create(INotificacao.class);
                    Call<List<Notificacao>> callNotificacao = iNotificacao.getNotificacaoPorEmpresa(empresaId);

                    callNotificacao.enqueue(new Callback<List<Notificacao>>() {
                        @Override
                        public void onResponse(Call<List<Notificacao>> call, Response<List<Notificacao>> response) {
                            Log.d(TAG, "Resposta notificações - código: " + response.code());

                            if (response.isSuccessful() && response.body() != null) {
                                List<Notificacao> lista = response.body();
                                Log.d(TAG, "Quantidade de notificações: " + lista.size());

                                NotificacaoAdapter notificacaoAdapter = new NotificacaoAdapter(lista);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                recyclerView.setAdapter(notificacaoAdapter);
                            } else {
                                Log.e(TAG, "Falha ao carregar notificações - resposta nula ou código não 200");
                                Toast.makeText(getContext(), "Nenhuma notificação encontrada", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Notificacao>> call, Throwable t) {
                            Log.e(TAG, "Erro ao buscar notificações: " + t.getMessage(), t);
                            Toast.makeText(getContext(), "Erro ao carregar notificações", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Log.e(TAG, "Empresa não encontrada ou resposta inválida");
                    Toast.makeText(getContext(), "Empresa não encontrada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Empresa> call, Throwable t) {
                Log.e(TAG, "Erro ao buscar empresa por CNPJ: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Erro ao carregar empresa", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
