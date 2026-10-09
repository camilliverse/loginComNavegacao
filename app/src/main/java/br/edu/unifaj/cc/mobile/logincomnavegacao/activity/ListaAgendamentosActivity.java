package br.edu.unifaj.cc.mobile.logincomnavegacao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.edu.unifaj.cc.mobile.logincomnavegacao.R;
import br.edu.unifaj.cc.mobile.logincomnavegacao.adapter.AgendamentoAdapter;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.ApiRepository;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.RepositoryCallback;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.entity.Agendamento;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.AutoFitGridLayoutManager;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.PrefsManager;

public class ListaAgendamentosActivity extends BaseActivity {

    private RecyclerView recyclerAgendamentos;
    private TextView txtSemAgendamentos;
    private Button btnVoltar;
    private PrefsManager prefsManager;
    private final ApiRepository api = ApiRepository.getInstance();
    private AgendamentoAdapter adapter;
    private List<Agendamento> agendamentos;
    private String cpfDoador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_agendamentos);

        prefsManager = new PrefsManager(this);

        if (!prefsManager.isLoggedIn()) {
            irParaLogin();
            return;
        }

        cpfDoador = prefsManager.getCpfLogado();
        if (cpfDoador == null) {
            irParaLogin();
            return;
        }

        recyclerAgendamentos = findViewById(R.id.recyclerAgendamentos);
        txtSemAgendamentos = findViewById(R.id.txtSemAgendamentos);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Colunas automáticas conforme a largura da tela.
        recyclerAgendamentos.setLayoutManager(new AutoFitGridLayoutManager(this));

        carregarAgendamentos();

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarAgendamentos() {
        api.listarAgendamentos(cpfDoador, new RepositoryCallback<List<Agendamento>>() {
            @Override
            public void onSuccess(List<Agendamento> lista) {
                agendamentos = lista;
                Map<String, Integer> quantidades = new HashMap<>();
                for (Agendamento a : agendamentos) {
                    if (a.getQuantidadeBolsas() != null && a.getQuantidadeBolsas() > 0) {
                        quantidades.put(a.getId(), a.getQuantidadeBolsas());
                    }
                }

                adapter = new AgendamentoAdapter(agendamentos, quantidades,
                        (agendamento, position) -> confirmarCancelamento(agendamento));
                recyclerAgendamentos.setAdapter(adapter);

                boolean vazio = agendamentos.isEmpty();
                txtSemAgendamentos.setVisibility(vazio ? View.VISIBLE : View.GONE);
                recyclerAgendamentos.setVisibility(vazio ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(ListaAgendamentosActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmarCancelamento(Agendamento agendamento) {
        new AlertDialog.Builder(this)
            .setTitle("Cancelar Agendamento")
            .setMessage("Tem certeza que deseja cancelar este agendamento?")
            .setPositiveButton("Sim", (dialog, which) -> cancelar(agendamento))
            .setNegativeButton("Não", null)
            .show();
    }

    private void cancelar(Agendamento agendamento) {
        api.cancelarAgendamento(cpfDoador, Long.parseLong(agendamento.getId()), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(ListaAgendamentosActivity.this, "Agendamento cancelado", Toast.LENGTH_SHORT).show();
                carregarAgendamentos();
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(ListaAgendamentosActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irParaLogin() {
        Intent intent = new Intent(ListaAgendamentosActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
