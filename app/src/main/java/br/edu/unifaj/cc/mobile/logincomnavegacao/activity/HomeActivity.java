package br.edu.unifaj.cc.mobile.logincomnavegacao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import br.edu.unifaj.cc.mobile.logincomnavegacao.R;
import br.edu.unifaj.cc.mobile.logincomnavegacao.adapter.MenuHomeAdapter;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.ApiRepository;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.Perfil;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.RepositoryCallback;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.user.Doador;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.AutoFitGridLayoutManager;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.PrefsManager;

/**
 * Tela principal.
 *
 * O menu é uma RecyclerView com colunas calculadas pela largura da tela,
 * então o mesmo código serve celular e tablet.
 */
public class HomeActivity extends BaseActivity {

    private static final int POSICAO_REGISTRAR = 0;
    private static final int POSICAO_AGENDAR = 1;
    private static final int POSICAO_HISTORICO = 2;
    private static final int POSICAO_AGENDAMENTOS = 3;

    private TextView txtNomeUsuario;
    private TextView txtTipoSanguineo;
    private View cardPerfil;
    private Button btnSair;
    private PrefsManager prefsManager;
    private final ApiRepository api = ApiRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefsManager = new PrefsManager(this);

        if (!prefsManager.isLoggedIn()) {
            irParaLogin();
            return;
        }

        txtNomeUsuario = findViewById(R.id.txtNomeUsuario);
        txtTipoSanguineo = findViewById(R.id.txtTipoSanguineo);
        cardPerfil = findViewById(R.id.cardPerfil);
        btnSair = findViewById(R.id.btnSair);

        // Mostra o cache na hora e atualiza com o perfil do backend em seguida.
        mostrarDoador(prefsManager.getDoador());
        carregarPerfil();

        cardPerfil.setOnClickListener(v -> startActivity(new Intent(this, PerfilActivity.class)));

        RecyclerView recyclerMenu = findViewById(R.id.recyclerMenu);
        recyclerMenu.setLayoutManager(new AutoFitGridLayoutManager(this, 260));
        recyclerMenu.setAdapter(new MenuHomeAdapter(menuItens(), posicao -> abrirTela(posicao)));

        btnSair.setOnClickListener(v -> {
            prefsManager.logout();
            Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show();
            irParaLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPerfil();
    }

    private void carregarPerfil() {
        String cpf = prefsManager.getCpfLogado();
        if (cpf == null) {
            return;
        }
        api.carregarPerfil(cpf, new RepositoryCallback<Perfil>() {
            @Override
            public void onSuccess(Perfil perfil) {
                if (isFinishing() || perfil.getDoador() == null) {
                    return;
                }
                prefsManager.salvarDoador(perfil.getDoador());
                mostrarDoador(perfil.getDoador());
            }

            @Override
            public void onError(String mensagem) {
                // Home continua com o cache; erro de rede não bloqueia a navegação.
            }
        });
    }

    private void mostrarDoador(Doador doador) {
        if (doador == null) {
            return;
        }
        txtNomeUsuario.setText(doador.getNome());
        String tipoCompleto = doador.getTipoCompleto();
        if (tipoCompleto != null) {
            txtTipoSanguineo.setText(tipoCompleto);
        }
    }

    private MenuHomeAdapter.Item[] menuItens() {
        return new MenuHomeAdapter.Item[]{
                new MenuHomeAdapter.Item(R.drawable.ic_drop,
                        R.string.tela_home_registrar_doacao,
                        R.string.tela_home_registrar_doacao_descricao),
                new MenuHomeAdapter.Item(R.drawable.ic_calendar,
                        R.string.tela_home_agendar_doacao,
                        R.string.tela_home_agendar_doacao_descricao),
                new MenuHomeAdapter.Item(R.drawable.ic_history,
                        R.string.tela_home_ver_historico,
                        R.string.tela_home_ver_historico_descricao),
                new MenuHomeAdapter.Item(R.drawable.ic_bag,
                        R.string.tela_home_meus_agendamentos,
                        R.string.tela_home_meus_agendamentos_descricao)
        };
    }

    private void abrirTela(int posicao) {
        Class<?> destino;
        if (posicao == POSICAO_REGISTRAR) {
            destino = DoacaoActivity.class;
        } else if (posicao == POSICAO_AGENDAR) {
            destino = AgendamentoActivity.class;
        } else if (posicao == POSICAO_HISTORICO) {
            destino = HistoricoActivity.class;
        } else if (posicao == POSICAO_AGENDAMENTOS) {
            destino = ListaAgendamentosActivity.class;
        } else {
            return;
        }

        startActivity(new Intent(this, destino));
    }

    private void irParaLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
