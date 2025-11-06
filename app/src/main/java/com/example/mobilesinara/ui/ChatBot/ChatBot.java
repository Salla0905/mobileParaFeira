package com.example.mobilesinara.ui.ChatBot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.mobilesinara.HomeOperario;
import com.example.mobilesinara.Interface.IChatBotAPI;
import com.example.mobilesinara.Interface.SQL.IEmpresa;
import com.example.mobilesinara.Interface.SQL.IOperario;
import com.example.mobilesinara.Models.ChatResponse;
import com.example.mobilesinara.Models.Empresa;
import com.example.mobilesinara.Models.Operario;
import com.example.mobilesinara.R;
import com.example.mobilesinara.TelaOpcoes;
import com.example.mobilesinara.adapter.ApiClientAdapter;
import com.example.mobilesinara.adapter.RetrofitClient;
import com.example.mobilesinara.databinding.FragmentChatBotBinding;
import com.example.mobilesinara.login.adm.LoginADM;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBot extends Fragment {

    private FragmentChatBotBinding binding;
    private static final String TAG = "ChatBot";
    private Call<ChatResponse> chatCall;
    private Call<Operario> operarioCall;
    private Call<Empresa> empresaCall;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        new ViewModelProvider(this).get(ChatBotViewModel.class);
        binding = FragmentChatBotBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences prefs = requireContext().getSharedPreferences("sinara_prefs", Context.MODE_PRIVATE);
        int idUser = prefs.getInt("idUser", -1);

        if (idUser == -1) {
            if (isAdded()) {
                Toast.makeText(getContext(), "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
            }
            Log.e(TAG, "ID do usuário não encontrado nas SharedPreferences");
            return root;
        }

        EditText input = root.findViewById(R.id.text_pesquisa);
        ImageView sendButton = root.findViewById(R.id.imageView14);
        ImageView imgUser = root.findViewById(R.id.imgUser);
        ImageView btVoltar = root.findViewById(R.id.bt_voltar);
        ImageView imgEmpresa = root.findViewById(R.id.imgEmpresa);
        LinearLayout layoutMessages = root.findViewById(R.id.layoutMessages);
        ScrollView scrollView = root.findViewById(R.id.scrollViewChat);

        btVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.navigation_home_operario);
            }
        });

        sendButton.setOnClickListener(v -> {
            String message = input.getText().toString().trim();
            if (!message.isEmpty()) {
                addUserMessage(message, layoutMessages, inflater);
                input.setText("");
                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                sendMessageToBot(message, layoutMessages, inflater, scrollView);
            }
        });

        IOperario iOperario = ApiClientAdapter.getRetrofitInstance().create(IOperario.class);
        operarioCall = iOperario.getOperarioPorId(idUser);
        operarioCall.enqueue(new Callback<Operario>() {
            @Override
            public void onResponse(Call<Operario> call, Response<Operario> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Operario operario = response.body();
                    String urlOperario = operario.getImagemUrl();

                    if (urlOperario == null || urlOperario.isEmpty()) {
                        Glide.with(requireContext()).load(R.drawable.profile_pic_default).into(imgUser);
                    } else {
                        Glide.with(requireContext()).load(urlOperario).circleCrop().into(imgUser);
                    }

                    int idEmpresa = operario.getIdEmpresa();
                    IEmpresa iEmpresa = ApiClientAdapter.getRetrofitInstance().create(IEmpresa.class);
                    empresaCall = iEmpresa.getEmpresaPorId(idEmpresa);
                    empresaCall.enqueue(new Callback<Empresa>() {
                        @Override
                        public void onResponse(Call<Empresa> call, Response<Empresa> response) {
                            if (!isAdded()) return;

                            if (response.isSuccessful() && response.body() != null) {
                                String urlEmpresa = response.body().getImagemUrl();

                                Glide.with(requireContext())
                                        .load((urlEmpresa == null || urlEmpresa.isEmpty())
                                                ? R.drawable.profile_pic_default
                                                : urlEmpresa)
                                        .circleCrop()
                                        .placeholder(R.drawable.profile_pic_default)
                                        .error(R.drawable.profile_pic_default)
                                        .into(imgEmpresa);
                            }
                        }

                        @Override
                        public void onFailure(Call<Empresa> call, Throwable t) {
                            Log.e(TAG, "Erro ao carregar imagem da empresa", t);
                        }
                    });
                } else {
                    Log.e(TAG, "Erro ao obter operário: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Operario> call, Throwable t) {
                Log.e(TAG, "Falha na API de operário: " + t.getMessage(), t);
            }
        });

        return root;
    }

    private void addUserMessage(String message, LinearLayout layout, LayoutInflater inflater) {
        View messageView = inflater.inflate(R.layout.item_chat_user, layout, false);
        ((TextView) messageView.findViewById(R.id.textViewUser)).setText(message);
        layout.addView(messageView);
    }

    private void addBotMessage(String message, LinearLayout layout, LayoutInflater inflater) {
        if (!isAdded()) return;
        View messageView = inflater.inflate(R.layout.item_chat_bot, layout, false);
        ((TextView) messageView.findViewById(R.id.textViewBot)).setText(message);
        layout.addView(messageView);
    }

    private void sendMessageToBot(String userMessage, LinearLayout layout, LayoutInflater inflater, ScrollView scrollView) {
        IChatBotAPI api = RetrofitClient.getInstance().create(IChatBotAPI.class);
        chatCall = api.sendMessage(userMessage, null, "auto");

        chatCall.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getAnswer();
                    addBotMessage(reply, layout, inflater);
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                } else {
                    addBotMessage("Erro na resposta do servidor (" + response.code() + ")", layout, inflater);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                if (!isAdded()) return;
                addBotMessage("Falha na conexão: " + t.getMessage(), layout, inflater);
                Context context = getContext();
                if (context != null) {
                    Toast.makeText(context, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatCall != null && !chatCall.isCanceled()) chatCall.cancel();
        if (operarioCall != null && !operarioCall.isCanceled()) operarioCall.cancel();
        if (empresaCall != null && !empresaCall.isCanceled()) empresaCall.cancel();
        binding = null;
    }
}
