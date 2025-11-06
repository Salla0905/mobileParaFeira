package com.example.mobilesinara.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mobilesinara.Interface.Mongo.IFormularioPadrao;
import com.example.mobilesinara.Interface.Mongo.IFormularioPersonalizado;
import com.example.mobilesinara.Interface.SQL.IEmpresa;
import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Models.Empresa;
import com.example.mobilesinara.Models.FormularioItem;
import com.example.mobilesinara.Models.FormularioPadrao;
import com.example.mobilesinara.Models.FormularioPersonalizado;
import com.example.mobilesinara.Models.Operario;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.example.mobilesinara.databinding.FragmentDashboardBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private RecyclerView recyclerView;
    private EditText txtPesquisa;

    private final List<FormularioItem> listaUnificadaAll = new ArrayList<>();
    private FormUnificadoAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = (binding = FragmentDashboardBinding.inflate(inflater, container, false)).getRoot();

        SharedPreferences prefs = requireContext().getSharedPreferences("sinara_prefs", Context.MODE_PRIVATE);
        int idUser = prefs.getInt("idUser", -1);

        if (idUser == -1) {
            Toast.makeText(getContext(), "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
            Log.e("ProfileFragment", "ID do usuário não encontrado nas SharedPreferences");
            return root;
        }

        Log.d("ProfileFragment", "Usuário logado: " + idUser);

        txtPesquisa = root.findViewById(R.id.text_pesquisa);
        recyclerView = root.findViewById(R.id.recyclerForms);
        ImageView imgUser = root.findViewById(R.id.imgUser);
        ImageView imgEmpresa = root.findViewById(R.id.imgEmpresa);
        TextInputLayout textInputLayout = root.findViewById(R.id.textInputLayout3);
        TextInputEditText editText = root.findViewById(R.id.text_pesquisa);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new FormUnificadoAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        carregarDadosUsuario(imgUser, imgEmpresa, idUser);

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                textInputLayout.setHint("");
            } else if (editText.getText() == null || editText.getText().toString().isEmpty()) {
                textInputLayout.setHint("Pesquisar formulário");
            }
        });

        txtPesquisa.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarLista(s == null ? "" : s.toString());
            }
        });

        return root;
    }

    private void carregarDadosUsuario(ImageView imgUser, ImageView imgEmpresa, int idUser) {
        IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
        Call<Operario> callOperario = iOperario.getOperarioPorId(idUser);

        callOperario.enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Operario operario = response.body();

                    // Carrega imagem do usuário
                    if (isAdded()) {
                        Glide.with(requireContext())
                                .load(operario.getImagemUrl())
                                .circleCrop()
                                .into(imgUser);
                    }

                    int idEmpresa = operario.getIdEmpresa();

                    IEmpresa iEmpresa = ApiClientAdapter.getRetrofitInstance().create(IEmpresa.class);
                    iEmpresa.getEmpresaPorId(idEmpresa).enqueue(new Callback<Empresa>() {
                        @Override
                        public void onResponse(Call<Empresa> call, Response<Empresa> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful() && response.body() != null) {
                                String urlEmpresa = response.body().getImagemUrl();
                                // Carrega imagem da empresa (ou padrão)
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
                            } else {
                                Log.e("API", "Erro empresa: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Empresa> call, Throwable t) {
                            Log.e("RetrofitError", "Erro empresa: " + t.getMessage(), t);
                        }
                    });

                    IFormularioPadrao iFormularioPadrao = ApiClientAdapter.getRetrofitInstance().create(IFormularioPadrao.class);
                    iFormularioPadrao.getFormularioPadraoPorEmpresa(idEmpresa).enqueue(new Callback<List<FormularioPadrao>>() {
                        @Override
                        public void onResponse(Call<List<FormularioPadrao>> call, Response<List<FormularioPadrao>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault());
                                for (FormularioPadrao f : response.body()) {
                                    String titulo;
                                    if (f.getQualidade() != null && !f.getQualidade().trim().isEmpty()) {
                                        titulo = "Padrão — " + f.getQualidade();
                                    } else if (f.getDataPreenchimento() != null) {
                                        titulo = "Padrão — " + sdf.format(f.getDataPreenchimento());
                                    } else {
                                        titulo = "Formulário Padrão";
                                    }
                                    String status = f.getQualidade() != null ? f.getQualidade() : "Sem status";
                                    listaUnificadaAll.add(new FormularioItem("padrao", titulo, status, f.getDataPreenchimento()));
                                }
                                adapter.updateList(new ArrayList<>(listaUnificadaAll));
                            } else {
                                Log.e("API", "Erro padrao: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<List<FormularioPadrao>> call, Throwable t) {
                            Log.e("RetrofitError", "Erro padrao: " + t.getMessage(), t);
                        }
                    });

                    IFormularioPersonalizado iFormularioPersonalizado = ApiClientAdapter.getRetrofitInstance().create(IFormularioPersonalizado.class);
                    iFormularioPersonalizado.getFormularioPersonalizadoPorEmpresa(idEmpresa).enqueue(new Callback<List<FormularioPersonalizado>>() {
                        @Override
                        public void onResponse(Call<List<FormularioPersonalizado>> call, Response<List<FormularioPersonalizado>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                for (FormularioPersonalizado f : response.body()) {
                                    String titulo = f.getTitulo() != null ? f.getTitulo() : "Formulário Personalizado";
                                    listaUnificadaAll.add(new FormularioItem("personalizado", titulo, "Aguardando resposta", null));
                                }
                                adapter.updateList(new ArrayList<>(listaUnificadaAll));
                            } else {
                                Log.e("API", "Erro personalizado: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<List<FormularioPersonalizado>> call, Throwable t) {
                            Log.e("RetrofitError", "Erro personalizado: " + t.getMessage(), t);
                        }
                    });
                } else {
                    Log.e("API", "Erro operario: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                Log.e("RetrofitError", "Erro operario: " + t.getMessage(), t);
            }
        });
    }

    private void filtrarLista(String termo) {
        String t = termo == null ? "" : termo.trim().toLowerCase(Locale.ROOT);
        if (t.isEmpty()) {
            adapter.updateList(new ArrayList<>(listaUnificadaAll));
            return;
        }
        List<FormularioItem> filtrada = new ArrayList<>();
        for (FormularioItem item : listaUnificadaAll) {
            String titulo = item.getTitulo() != null ? item.getTitulo().toLowerCase(Locale.ROOT) : "";
            String status = item.getStatus() != null ? item.getStatus().toLowerCase(Locale.ROOT) : "";
            if (titulo.contains(t) || status.contains(t) || item.getTipo().toLowerCase(Locale.ROOT).contains(t)) {
                filtrada.add(item);
            }
        }
        adapter.updateList(filtrada);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}