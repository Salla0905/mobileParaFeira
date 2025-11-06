package com.example.mobilesinara.ui.notificacaoEmpresa;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilesinara.Models.Notificacao;
import com.example.mobilesinara.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.NotificacaoViewHolder> {
    private List<Notificacao> lista;

    public NotificacaoAdapter(List<Notificacao> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public NotificacaoAdapter.NotificacaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacao_empresa_ponto, parent, false);
        return new NotificacaoAdapter.NotificacaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacaoAdapter.NotificacaoViewHolder holder, int position) {
        Notificacao notificacao = lista.get(position);
        holder.conteudo.setText(notificacao.getMensagem());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        String dataFormatada = formatter.format(notificacao.getData());
        holder.dataEHora.setText(dataFormatada);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class NotificacaoViewHolder extends RecyclerView.ViewHolder{
        TextView conteudo;
        TextView dataEHora;
        ImageView icon;

        public NotificacaoViewHolder(@NonNull View itemView) {
            super(itemView);
            conteudo = itemView.findViewById(R.id.txtPergunta);
            dataEHora = itemView.findViewById(R.id.textView18);
            icon = itemView.findViewById(R.id.imageView16);
        }
    }
}
