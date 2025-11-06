package com.example.mobilesinara.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilesinara.Models.Notificacao;
import com.example.mobilesinara.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private List<Notificacao> lista;
    public NotificationAdapter(List<Notificacao> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public NotificationAdapter.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacao, parent, false);
        return new NotificationAdapter.NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdapter.NotificationViewHolder holder, int position) {
        Notificacao notificacao = lista.get(position);
        holder.titulo.setText(notificacao.getTipo());
        holder.descricao.setText(notificacao.getMensagem());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        String dataFormatada = formatter.format(notificacao.getData());
        holder.dataEHora.setText(dataFormatada);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder{
        TextView titulo;
        TextView descricao;
        TextView dataEHora;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titulo = itemView.findViewById(R.id.textView16);
            descricao = itemView.findViewById(R.id.txtPergunta);
            dataEHora = itemView.findViewById(R.id.textView18);
        }
    }
}
