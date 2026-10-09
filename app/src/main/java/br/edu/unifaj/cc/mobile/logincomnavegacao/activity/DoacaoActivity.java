package br.edu.unifaj.cc.mobile.logincomnavegacao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.edu.unifaj.cc.mobile.logincomnavegacao.R;
import br.edu.unifaj.cc.mobile.logincomnavegacao.adapter.AgendamentoParaRegistroAdapter;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.ApiRepository;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.RepositoryCallback;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.entity.Agendamento;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.AutoFitGridLayoutManager;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.PrefsManager;

/**
 * Tela do agente de saúde que escolhe qual agendamento registrar.
 *
 * O doador é quem agenda; aqui o agente só escolhe um agendamento já existente
 * e confirma a coleta. Nada é digitado nesta tela: data, hora e local já estão
 * fixados no agendamento e são levados para o formulário.
 */
public class DoacaoActivity extends BaseActivity {

    private RecyclerView recyclerAgendamentos;
    private View estadoVazio;
    private Button btnAgendamentos;
    private Button btnVoltar;
    private PrefsManager prefsManager;
    private final ApiRepository api = ApiRepository.getInstance();
    private String cpfDoador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doacao);

        prefsManager = new PrefsManager(this);

        if (!prefsManager.isLoggedIn()) {
            irParaLogin();
            return;
        }

        cpfDoador = prefsManager.getCpfLogado();

        recyclerAgendamentos = findViewById(R.id.recyclerAgendamentos);
        estadoVazio = findViewById(R.id.estadoVazio);
        btnAgendamentos = findViewById(R.id.btnAgendamentos);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Colunas automáticas conforme a largura da tela.
        recyclerAgendamentos.setLayoutManager(new AutoFitGridLayoutManager(this));

        btnAgendamentos.setOnClickListener(v ->
                startActivity(new Intent(this, ListaAgendamentosActivity.class)));
        btnVoltar.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Relê ao voltar da tela de registro, para a lista já vir atualizada.
        if (prefsManager != null && prefsManager.isLoggedIn()) {
            carregarAgendamentos();
        }
    }

    private void carregarAgendamentos() {
        if (cpfDoador == null) {
            irParaLogin();
            return;
        }

        api.listarParaRegistro(cpfDoador, new RepositoryCallback<List<Agendamento>>() {
            @Override
            public void onSuccess(List<Agendamento> elegiveis) {
                recyclerAgendamentos.setAdapter(new AgendamentoParaRegistroAdapter(elegiveis,
                        agendamento -> {
                            Intent intent = new Intent(DoacaoActivity.this, RegistrarDoacaoActivity.class);
                            intent.putExtra(RegistrarDoacaoActivity.EXTRA_AGENDAMENTO_ID, agendamento.getId());
                            startActivity(intent);
                        }));

                boolean vazio = elegiveis.isEmpty();
                recyclerAgendamentos.setVisibility(vazio ? View.GONE : View.VISIBLE);
                estadoVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(DoacaoActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irParaLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
