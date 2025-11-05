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
import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Models.FormularioPadrao;
import com.example.mobilesinara.Models.FormularioPersonalizado;
import com.example.mobilesinara.Models.campos;
import com.example.mobilesinara.Models.Operario;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.example.mobilesinara.databinding.FragmentMonitoramentoAguardandoBinding;

import java.util.LinkedHashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class monitoramentoAguardando extends Fragment {

    private FragmentMonitoramentoAguardandoBinding binding;
    private static final String TAG = "MonitoramentoAguardando";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView: Iniciando fragmento...");

        new ViewModelProvider(this).get(MonitoramentoAguardandoViewModel.class);
        binding = FragmentMonitoramentoAguardandoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        if (args == null || !args.containsKey("idUser") || !args.containsKey("idFormulario") || !args.containsKey("tipo")) {
            Log.e(TAG, "onCreateView: Argumentos ausentes! args=" + args);
            Toast.makeText(getContext(), "Erro: usuário, formulário ou tipo não identificado", Toast.LENGTH_SHORT).show();
            return root;
        }

        int idUser = args.getInt("idUser");
        String idFormulario = args.getString("idFormulario");
        String tipo = args.getString("tipo");

        Log.d(TAG, "onCreateView: idUser=" + idUser + ", idFormulario=" + idFormulario + ", tipo=" + tipo);

        LinearLayout layoutOpcoes = root.findViewById(R.id.layoutOpcoes);

        IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
        Call<Operario> callOperario = iOperario.getOperarioPorId(idUser);

        callOperario.enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (tipo.equalsIgnoreCase("padrao") || tipo.equalsIgnoreCase("padrão")) {
                        carregarFormularioPadrao(layoutOpcoes, idFormulario);
                    } else if (tipo.equalsIgnoreCase("personalizado")) {
                        carregarFormularioPersonalizado(layoutOpcoes, idFormulario);
                    } else {
                        Toast.makeText(requireContext(), "Tipo não identificado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar Operário", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                Toast.makeText(requireContext(), "Usuário não foi encontrado", Toast.LENGTH_SHORT).show();
            }
        });

        Button bt_enviar_form = root.findViewById(R.id.button9);
        bt_enviar_form.setOnClickListener(v -> {
            boolean camposValidos = validarCampos(layoutOpcoes);
            if (!camposValidos) {
                Toast.makeText(requireContext(), "Preencha todos os campos obrigatórios antes de enviar", Toast.LENGTH_SHORT).show();
                return;
            }

            Navigation.findNavController(v).navigate(R.id.formularioRespondido);
        });

        return root;
    }

    private boolean validarCampos(LinearLayout layoutOpcoes) {
        for (int i = 0; i < layoutOpcoes.getChildCount(); i++) {
            View card = layoutOpcoes.getChildAt(i);

            // Verifica campo de texto
            View textInput = card.findViewById(R.id.text_desc);
            if (textInput != null && textInput instanceof com.google.android.material.textfield.TextInputEditText) {
                String texto = ((com.google.android.material.textfield.TextInputEditText) textInput).getText().toString().trim();
                TextView txtPergunta = card.findViewById(R.id.txtPergunta);

                if (txtPergunta.getText().toString().contains("*") && texto.isEmpty()) {
                    return false;
                }
            }

            // Verifica grupo de opções
            View opcoesContainer = card.findViewById(R.id.opcoesContainer);
            if (opcoesContainer != null && opcoesContainer instanceof LinearLayout) {
                LinearLayout container = (LinearLayout) opcoesContainer;
                for (int j = 0; j < container.getChildCount(); j++) {
                    View innerView = container.getChildAt(j);
                    if (innerView instanceof RadioGroup) {
                        RadioGroup group = (RadioGroup) innerView;
                        TextView txtPergunta = card.findViewById(R.id.txtPergunta);

                        if (txtPergunta.getText().toString().contains("*") && group.getCheckedRadioButtonId() == -1) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void carregarFormularioPadrao(LinearLayout layoutOpcoes, String idFormulario) {
        IFormularioPadrao iFormularioPadrao = ApiClientAdapter.getRetrofitInstance().create(IFormularioPadrao.class);
        iFormularioPadrao.getFormularioPadrao(idFormulario).enqueue(new Callback<FormularioPadrao>() {
            @Override
            public void onResponse(Call<FormularioPadrao> call, Response<FormularioPadrao> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FormularioPadrao f = response.body();
                    Map<String, Object> campos = new LinkedHashMap<>();
                    campos.put("Cloro Residual", f.getCloroResidual());
                    campos.put("Cor da Água Bruta", f.getCorAguaBruta());
                    campos.put("Cor da Água Tratada", f.getCorAguaTratada());
                    campos.put("Fluoreto", f.getFluoreto());
                    campos.put("Nitrato", f.getNitrato());
                    campos.put("pH Água Bruta", f.getPhAguaBruta());
                    campos.put("pH Água Tratada", f.getPhAguaTratada());
                    campos.put("Turbidez Água Bruta", f.getTurbinezAguaBruta());
                    campos.put("Turbidez Água Tratada", f.getTurbidezAguaTratada());
                    campos.put("Qualidade", f.getQualidade());

                    LayoutInflater inflater = getLayoutInflater();
                    for (Map.Entry<String, Object> entry : campos.entrySet()) {
                        View card = inflater.inflate(R.layout.item_pergunta_texto, layoutOpcoes, false);
                        TextView txtPergunta = card.findViewById(R.id.txtPergunta);
                        txtPergunta.setText(entry.getKey());
                        layoutOpcoes.addView(card);
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar formulário padrão", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FormularioPadrao> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha de conexão com o servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarFormularioPersonalizado(LinearLayout layoutOpcoes, String idFormulario) {
        IFormularioPersonalizado iFormularioPersonalizado = ApiClientAdapter.getRetrofitInstance().create(IFormularioPersonalizado.class);
        iFormularioPersonalizado.getFormularioPersonalizado(idFormulario).enqueue(new Callback<FormularioPersonalizado>() {
            @Override
            public void onResponse(Call<FormularioPersonalizado> call, Response<FormularioPersonalizado> response) {
                if (response.isSuccessful() && response.body() != null) {
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
                            opcoesContainer.setVisibility(View.VISIBLE);
                            RadioGroup group = new RadioGroup(requireContext());
                            group.setOrientation(RadioGroup.VERTICAL);

                            for (String opcao : campo.getOpcoes()) {
                                RadioButton rb = new RadioButton(requireContext());
                                rb.setText(opcao);
                                group.addView(rb);
                            }

                            opcoesContainer.addView(group);
                            layoutOpcoes.addView(card);
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar formulário personalizado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FormularioPersonalizado> call, Throwable t) {
                Toast.makeText(requireContext(), "Falha de conexão com o servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
