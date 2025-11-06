package com.example.mobilesinara.ui.configuration;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.mobilesinara.R;
import com.example.mobilesinara.databinding.FragmentConfigurationBinding;
import com.example.mobilesinara.login.operario.LoginOperarioAlterarSenha2;
import com.example.mobilesinara.login.operario.LoginOperarioCadastroRosto;

import java.util.Map;

public class Configuration extends Fragment {

    private ActivityResultLauncher<String[]> requestPermissions;
    private FragmentConfigurationBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        new ViewModelProvider(this).get(ConfigurationViewModel.class);

        binding = FragmentConfigurationBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageView bt_voltar = root.findViewById(R.id.imageView3);
        Button bt_cadastrar_rosto = root.findViewById(R.id.button11);
        Button bt_alterar_senha = root.findViewById(R.id.alterar_senha);
        Button bt_camera_permission = root.findViewById(R.id.button14);
        Button bt_sms_permission = root.findViewById(R.id.button15);

        // Inicializa permissões
        configurarPermissoes();

        // Checa permissões
        verificarPermissaoCamera(getContext(), bt_camera_permission);
        verificarPermissaoSMS(getContext(), bt_sms_permission);

        // Recebe argumentos
        Bundle args = getArguments();
        if (args == null || !args.containsKey("idUser")) {
            Toast.makeText(getContext(), "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
            return root;
        }

        int idUser = args.getInt("idUser");

        // Botão para cadastrar rosto
        bt_cadastrar_rosto.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginOperarioCadastroRosto.class);
            intent.putExtra("idUser", idUser);
            startActivity(intent);
        });

        // Botão voltar
        bt_voltar.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_home_operario)
        );

        // Botão para alterar senha
        bt_alterar_senha.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginOperarioAlterarSenha2.class);
            intent.putExtra("idUser", idUser);
            startActivity(intent);
        });

        return root;
    }

    private void configurarPermissoes() {
        requestPermissions = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        Log.d("Permissions", entry.getKey() + " = " + entry.getValue());
                    }
                    // Atualiza estado visual dos botões
                    verificarPermissaoCamera(getContext(), binding.button14);
                    verificarPermissaoSMS(getContext(), binding.button15);
                }
        );
    }

    private void verificarPermissaoCamera(Context context, Button bt) {
        boolean granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        if (!granted) {
            bt.setText("ATIVAR");
            bt.setBackgroundColor(Color.parseColor("#FF8669"));
            bt.setTextColor(Color.WHITE);
            bt.setOnClickListener(v -> requestPermissions.launch(new String[]{Manifest.permission.CAMERA}));
        } else {
            bt.setText("OK");
            bt.setBackgroundColor(Color.parseColor("#7FD170"));
            bt.setTextColor(Color.parseColor("#409346"));
            bt.setOnClickListener(v -> abrirConfiguracoesApp());
        }
    }

    private void verificarPermissaoSMS(Context context, Button bt) {
        boolean granted = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;

        if (!granted) {
            bt.setText("ATIVAR");
            bt.setBackgroundColor(Color.parseColor("#FF8669"));
            bt.setTextColor(Color.WHITE);
            bt.setOnClickListener(v -> requestPermissions.launch(new String[]{Manifest.permission.SEND_SMS}));
        } else {
            bt.setText("OK");
            bt.setBackgroundColor(Color.parseColor("#7FD170"));
            bt.setTextColor(Color.parseColor("#409346"));
            bt.setOnClickListener(v -> abrirConfiguracoesApp());
        }
    }

    private void abrirConfiguracoesApp() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Desativar permissão")
                .setMessage("Para desativar esta permissão, vá até as configurações do aplicativo.")
                .setPositiveButton("Abrir configurações", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", requireActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
