package br.edu.unifaj.cc.mobile.logincomnavegacao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.edu.unifaj.cc.mobile.logincomnavegacao.R;
import br.edu.unifaj.cc.mobile.logincomnavegacao.adapter.BolsaSangueAdapter;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.ApiRepository;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.RepositoryCallback;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.entity.BolsaSangue;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.AutoFitGridLayoutManager;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.PrefsManager;

public class HistoricoActivity extends BaseActivity {

    private RecyclerView recyclerBolsas;
    private TextView txtSemAgendamentos;
    private Button btnVoltar;
    private PrefsManager prefsManager;
    private final ApiRepository api = ApiRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        prefsManager = new PrefsManager(this);

        if (!prefsManager.isLoggedIn()) {
            irParaLogin();
            return;
        }

        recyclerBolsas = findViewById(R.id.recyclerDoacoes);
        txtSemAgendamentos = findViewById(R.id.txtSemAgendamentos);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Colunas automáticas conforme a largura da tela.
        recyclerBolsas.setLayoutManager(new AutoFitGridLayoutManager(this));

        carregarBolsas();

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void carregarBolsas() {
        api.listarBolsas(prefsManager.getCpfLogado(), new RepositoryCallback<List<BolsaSangue>>() {
            @Override
            public void onSuccess(List<BolsaSangue> bolsas) {
                recyclerBolsas.setAdapter(new BolsaSangueAdapter(bolsas));

                boolean vazio = bolsas.isEmpty();
                recyclerBolsas.setVisibility(vazio ? View.GONE : View.VISIBLE);
                txtSemAgendamentos.setVisibility(vazio ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(HistoricoActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irParaLogin() {
        Intent intent = new Intent(HistoricoActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
