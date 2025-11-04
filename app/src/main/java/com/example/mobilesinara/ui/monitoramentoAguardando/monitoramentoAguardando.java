package com.example.mobilesinara.ui.monitoramentoAguardando;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.mobilesinara.Interface.Mongo.IFormularioPadrao;
import com.example.mobilesinara.Interface.Mongo.IFormularioPersonalizado;
import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Models.Operario;
import com.example.mobilesinara.R;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.example.mobilesinara.databinding.FragmentMonitoramentoAguardandoBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class monitoramentoAguardando extends Fragment {

    private FragmentMonitoramentoAguardandoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MonitoramentoAguardandoViewModel MonitoramentoAguardandoViewModel =
                new ViewModelProvider(this).get(MonitoramentoAguardandoViewModel.class);

        binding = FragmentMonitoramentoAguardandoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Bundle args = getArguments();
        if (args == null || !args.containsKey("idUser") || !args.containsKey("idFormulario") || !args.containsKey("tipo")) {
            Toast.makeText(getContext(), "Erro: usuário, formulario ou tipo não identificado", Toast.LENGTH_SHORT).show();
            return root;
        }
        int idUser = args.getInt("idUser");
        String idFormulario = args.getString("idFormulario");
        String tipo = args.getString("tipo");
        IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
        Call<Operario> callOperario = iOperario.getOperarioPorId(idUser);
        callOperario.enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if(response.isSuccessful() && response.body() != null){
                    if(tipo.equals("padrão")){
                        IFormularioPadrao iFormularioPadrao = ApiClientAdapter.getRetrofitInstance().create(IFormularioPadrao.class);
                    }
                    else if(tipo.equals("personalizado")){
                        IFormularioPersonalizado iFormularioPersonalizado = ApiClientAdapter.getRetrofitInstance().create(IFormularioPersonalizado.class);
                    }
                    else{
                        Toast.makeText(requireContext(), "Tipo não identificado", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                Toast.makeText(requireContext(), "Usuario não foi encontrado", Toast.LENGTH_SHORT).show();
            }
        });


        Button bt_enviar_form = root.findViewById(R.id.button9);
        bt_enviar_form.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.formularioRespondido);
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