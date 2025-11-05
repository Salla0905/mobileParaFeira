package com.example.mobilesinara.ui.monitoramentoAguardando;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.mobilesinara.Interface.Mongo.IFormularioPadrao;
import com.example.mobilesinara.Interface.Mongo.IFormularioPersonalizado;
import com.example.mobilesinara.Interface.Mongo.IRespostaFormularioPersonalizado;
import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Models.FormularioPadrao;
import com.example.mobilesinara.Models.FormularioPersonalizado;
import com.example.mobilesinara.Models.RespostaFormularioPersonalizado;
import com.example.mobilesinara.Models.Respostas;
import com.example.mobilesinara.Models.campos;
import com.example.mobilesinara.Models.Operario;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.example.mobilesinara.databinding.FragmentMonitoramentoAguardandoBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class monitoramentoAguardando extends Fragment {

    private FragmentMonitoramentoAguardandoBinding binding;
    private static final String TAG = "MonitoramentoAguardando";
    private Respostas respostaSelecionada = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: Iniciando fragmento...");

        new ViewModelProvider(this).get(MonitoramentoAguardandoViewModel.class);
        binding = FragmentMonitoramentoAguardandoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        if (args == null || !args.containsKey("idUser") || !args.containsKey("idFormulario") || !args.containsKey("tipo")) {
            Toast.makeText(getContext(), "Erro: parâmetros ausentes", Toast.LENGTH_SHORT).show();
            return root;
        }

        final int[] idEmpresa = new int[1];
        int idUser = args.getInt("idUser");
        String idFormulario = args.getString("idFormulario");
        String tipo = args.getString("tipo");
        TextView descricao = root.findViewById(R.id.textView26);

        LinearLayout layoutOpcoes = root.findViewById(R.id.layoutOpcoes);

        IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
        Call<Operario> callOperario = iOperario.getOperarioPorId(idUser);

        callOperario.enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    idEmpresa[0] = response.body().getIdEmpresa();
                    if (tipo.equalsIgnoreCase("padrao") || tipo.equalsIgnoreCase("padrão")) {
                        carregarFormularioPadrao(layoutOpcoes, idFormulario, descricao);
                    } else if (tipo.equalsIgnoreCase("personalizado")) {
                        carregarFormularioPersonalizado(layoutOpcoes, idFormulario, descricao);
                    } else {
                        Toast.makeText(requireContext(), "Tipo não identificado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar Operário", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha de conexão", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro ao buscar operário", t);
            }
        });

        Button btEnviar = root.findViewById(R.id.button9);
        btEnviar.setOnClickListener(v -> {
            respostaSelecionada = coletarPrimeiraResposta(layoutOpcoes);
            if (respostaSelecionada == null) {
                Toast.makeText(requireContext(), "Preencha pelo menos um campo obrigatório", Toast.LENGTH_SHORT).show();
                return;
            }

            String dataISO = getDataAtualISO();

            List<Respostas> listaRespostas = new ArrayList<>();
            listaRespostas.add(respostaSelecionada);

            RespostaFormularioPersonalizado resposta = new RespostaFormularioPersonalizado(
                    listaRespostas,
                    dataISO,
                    idFormulario,
                    idUser,
                    idEmpresa[0]
            );

            Gson gson = new Gson();
            Log.i("JSON_ENVIADO", gson.toJson(resposta));

            IRespostaFormularioPersonalizado api = ApiClientAdapter.getRetrofitInstance().create(IRespostaFormularioPersonalizado.class);
            Call<ResponseBody> call = api.insertRespostaFormularioPersonalizado(resposta);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Respostas enviadas com sucesso!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(v).navigate(R.id.formularioRespondido);
                    } else {
                        Toast.makeText(requireContext(), "Erro ao enviar: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro HTTP: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(requireContext(), "Falha ao conectar ao servidor", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Falha Retrofit", t);
                }
            });
        });

        return root;
    }

    private String getDataAtualISO() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private Respostas coletarPrimeiraResposta(LinearLayout layoutOpcoes) {
        for (int i = 0; i < layoutOpcoes.getChildCount(); i++) {
            View card = layoutOpcoes.getChildAt(i);
            TextView txtPergunta = card.findViewById(R.id.txtPergunta);
            String label = txtPergunta.getText().toString().replace(" *", "");

            // Campo de texto
            View textInput = card.findViewById(R.id.text_desc);
            if (textInput instanceof TextInputEditText) {
                String texto = ((TextInputEditText) textInput).getText().toString().trim();
                if (!texto.isEmpty()) {
                    return new Respostas(label, "texto", texto);
                }
            }

            // Campo de escolha
            LinearLayout opcoesContainer = card.findViewById(R.id.opcoesContainer);
            if (opcoesContainer != null && opcoesContainer.getChildCount() > 0) {
                for (int j = 0; j < opcoesContainer.getChildCount(); j++) {
                    View inner = opcoesContainer.getChildAt(j);
                    if (inner instanceof RadioGroup) {
                        RadioGroup group = (RadioGroup) inner;
                        int checkedId = group.getCheckedRadioButtonId();
                        if (checkedId != -1) {
                            RadioButton rb = group.findViewById(checkedId);
                            return new Respostas(label, "escolha", rb.getText().toString());
                        }
                    }
                }
            }
        }
        return null;
    }

    private void carregarFormularioPadrao(LinearLayout layoutOpcoes, String idFormulario, TextView desc) {
        IFormularioPadrao api = ApiClientAdapter.getRetrofitInstance().create(IFormularioPadrao.class);
        api.getFormularioPadrao(idFormulario).enqueue(new Callback<FormularioPadrao>() {
            @Override
            public void onResponse(Call<FormularioPadrao> call, Response<FormularioPadrao> response) {
                if (response.isSuccessful() && response.body() != null) {
                    desc.setText("Formulário padrão");
                    FormularioPadrao f = response.body();
                    Map<String, Object> campos = new LinkedHashMap<>();
                    campos.put("Cloro Residual", f.getCloroResidual());
                    campos.put("pH Água Bruta", f.getPhAguaBruta());
                    campos.put("pH Água Tratada", f.getPhAguaTratada());
                    campos.put("Qualidade", f.getQualidade());

                    LayoutInflater inflater = getLayoutInflater();
                    for (String key : campos.keySet()) {
                        View card = inflater.inflate(R.layout.item_pergunta_texto, layoutOpcoes, false);
                        TextView txtPergunta = card.findViewById(R.id.txtPergunta);
                        txtPergunta.setText(key);
                        layoutOpcoes.addView(card);
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar formulário padrão", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FormularioPadrao> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha de conexão", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro no formulário padrão", t);
            }
        });
    }

    private void carregarFormularioPersonalizado(LinearLayout layoutOpcoes, String idFormulario, TextView desc) {
        IFormularioPersonalizado api = ApiClientAdapter.getRetrofitInstance().create(IFormularioPersonalizado.class);
        api.getFormularioPersonalizado(idFormulario).enqueue(new Callback<FormularioPersonalizado>() {
            @Override
            public void onResponse(Call<FormularioPersonalizado> call, Response<FormularioPersonalizado> response) {
                if (response.isSuccessful() && response.body() != null) {
                    desc.setText(response.body().getDescricao());
                    FormularioPersonalizado form = response.body();
                    LayoutInflater inflater = getLayoutInflater();

                    for (campos campo : form.getCampos()) {
                        String label = campo.getLabel();
                        if (campo.getObrigatorio() != null && campo.getObrigatorio()) {
                            label += " *";
                        }

                        if (campo.getTipo().equalsIgnoreCase("Escrita")) {
                            View card = inflater.inflate(R.layout.item_pergunta_texto, layoutOpcoes, false);
                            TextView txtPergunta = card.findViewById(R.id.txtPergunta);
                            txtPergunta.setText(label);
                            layoutOpcoes.addView(card);

                        } else if (campo.getTipo().equalsIgnoreCase("Escolha")) {
                            View card = inflater.inflate(R.layout.item_pergunta_alternativa, layoutOpcoes, false);
                            TextView txtPergunta = card.findViewById(R.id.txtPergunta);
                            txtPergunta.setText(label);

                            LinearLayout opcoesContainer = card.findViewById(R.id.opcoesContainer);
                            RadioGroup group = new RadioGroup(requireContext());
                            group.setOrientation(RadioGroup.VERTICAL);
                            group.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            ));

                            List<String> opcoes = campo.getOpcoes();
                            if (opcoes != null && !opcoes.isEmpty()) {
                                for (String opcao : opcoes) {
                                    RadioButton rb = new RadioButton(requireContext());
                                    rb.setText(opcao);
                                    rb.setTextColor(getResources().getColor(R.color.md_theme_primaryContainer));
                                    rb.setTextSize(16);
                                    group.addView(rb);
                                }

                                // 🔥 Adiciona o grupo de opções antes de adicionar o card no layout
                                opcoesContainer.removeAllViews();
                                opcoesContainer.addView(group);
                                layoutOpcoes.addView(card);

                                Log.d(TAG, "Campo adicionado: " + label + " (" + opcoes.size() + " opções)");

                            } else {
                                Log.w(TAG, "Campo sem opções: " + campo.getLabel());
                                TextView aviso = new TextView(requireContext());
                                aviso.setText("Sem opções disponíveis");
                                aviso.setTextColor(getResources().getColor(R.color.md_theme_error));
                                opcoesContainer.addView(aviso);
                                layoutOpcoes.addView(card);
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar formulário personalizado", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<FormularioPersonalizado> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha ao buscar formulário", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro no formulário personalizado", t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
