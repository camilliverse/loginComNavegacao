package br.edu.unifaj.cc.mobile.logincomnavegacao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import br.edu.unifaj.cc.mobile.logincomnavegacao.R;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.ApiRepository;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.RepositoryCallback;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.entity.Agendamento;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.entity.BolsaSangue;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.user.Doador;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.PrefsManager;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.ValidacaoUtils;

/**
 * Confirmação da coleta de um agendamento escolhido na {@link DoacaoActivity}.
 *
 * Data e local não são digitados: vêm do agendamento e aparecem travados, para
 * que o registro bata com o que o doador agendou. O agente de saúde informa o
 * volume de cada bolsa e quantas bolsas foram coletadas.
 */
public class RegistrarDoacaoActivity extends BaseActivity {

    public static final String EXTRA_AGENDAMENTO_ID = "extra_agendamento_id";

    private static final String ESTADO_VOLUME = "estado_volume";
    private static final String ESTADO_QUANTIDADE = "estado_quantidade";

    private TextView txtHemocentro;
    private TextView txtEndereco;
    private TextView txtTipoDoador;
    private EditText editData;
    private EditText editLocal;
    private EditText editVolumeMl;
    private EditText editQuantidade;
    private Button btnRegistrar;
    private Button btnVoltar;

    private PrefsManager prefsManager;
    private final ApiRepository api = ApiRepository.getInstance();
    private String agendamentoId;
    private String cpfDoador;
    private Agendamento agendamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_doacao);

        prefsManager = new PrefsManager(this);

        if (!prefsManager.isLoggedIn()) {
            irParaLogin();
            return;
        }

        cpfDoador = prefsManager.getCpfLogado();
        agendamentoId = getIntent().getStringExtra(EXTRA_AGENDAMENTO_ID);
        if (agendamentoId == null) {
            Toast.makeText(this, R.string.msg_agendamento_nao_encontrado, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtHemocentro = findViewById(R.id.txtHemocentro);
        txtEndereco = findViewById(R.id.txtEndereco);
        txtTipoDoador = findViewById(R.id.txtTipoDoador);
        editData = findViewById(R.id.editData);
        editLocal = findViewById(R.id.editLocal);
        editVolumeMl = findViewById(R.id.editVolumeMl);
        editQuantidade = findViewById(R.id.editQuantidade);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnVoltar = findViewById(R.id.btnVoltar);

        btnRegistrar.setEnabled(false);
        restaurarCamposDigitados(savedInstanceState);
        carregarAgendamento();

        btnRegistrar.setOnClickListener(v -> registrarColeta());
        btnVoltar.setOnClickListener(v -> finish());
    }

    /**
     * Busca o agendamento na lista de elegíveis do backend. A fonte da verdade é
     * a API: além de preencher a tela, confirma que o agendamento ainda pode
     * receber coleta (protege contra lista desatualizada e duplo toque).
     */
    private void carregarAgendamento() {
        api.listarParaRegistro(cpfDoador, new RepositoryCallback<List<Agendamento>>() {
            @Override
            public void onSuccess(List<Agendamento> elegiveis) {
                for (Agendamento a : elegiveis) {
                    if (agendamentoId.equals(a.getId())) {
                        agendamento = a;
                        break;
                    }
                }

                if (agendamento == null) {
                    Toast.makeText(RegistrarDoacaoActivity.this,
                            R.string.msg_coleta_nao_elegivel, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                preencherAgendamento();
                btnRegistrar.setEnabled(true);
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(RegistrarDoacaoActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void preencherAgendamento() {
        txtHemocentro.setText(agendamento.getHemocentro().getNome());
        txtEndereco.setText(agendamento.getHemocentro().getEndereco().getEnderecoCompleto());

        // Data e local travados, herdados do agendamento.
        editData.setText(agendamento.getData());
        editLocal.setText(agendamento.getHemocentro().getNome());

        Doador doador = prefsManager.getDoador();
        String tipoCompleto = doador == null ? null : doador.getTipoCompleto();
        txtTipoDoador.setText(tipoCompleto == null
                ? getString(R.string.tela_registro_tipo_padrao)
                : getString(R.string.tela_registro_tipo_doador, tipoCompleto));
    }

    private void restaurarCamposDigitados(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        editVolumeMl.setText(savedInstanceState.getString(ESTADO_VOLUME, ""));
        editQuantidade.setText(savedInstanceState.getString(ESTADO_QUANTIDADE, ""));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ESTADO_VOLUME, editVolumeMl.getText().toString());
        outState.putString(ESTADO_QUANTIDADE, editQuantidade.getText().toString());
    }

    private void registrarColeta() {
        int volumeMl;
        int quantidade;

        try {
            volumeMl = Integer.parseInt(editVolumeMl.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.msg_informe_volume, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidacaoUtils.validarVolumeDoacao(volumeMl)) {
            Toast.makeText(this, R.string.msg_volume_invalido, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            quantidade = Integer.parseInt(editQuantidade.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.msg_informe_quantidade, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidacaoUtils.validarQuantidadeBolsas(quantidade)) {
            Toast.makeText(this, R.string.msg_quantidade_invalida, Toast.LENGTH_SHORT).show();
            return;
        }

        Doador doador = prefsManager.getDoador();
        if (doador == null || doador.getTipoSanguineo() == null || doador.getFatorRh() == null) {
            Toast.makeText(this, R.string.msg_doador_sem_tipo, Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegistrar.setEnabled(false);
        api.registrarColeta(cpfDoador, Long.parseLong(agendamentoId), volumeMl, quantidade,
                new RepositoryCallback<BolsaSangue>() {
                    @Override
                    public void onSuccess(BolsaSangue bolsa) {
                        Toast.makeText(RegistrarDoacaoActivity.this,
                                R.string.msg_coleta_registrada, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String mensagem) {
                        btnRegistrar.setEnabled(true);
                        Toast.makeText(RegistrarDoacaoActivity.this, mensagem, Toast.LENGTH_SHORT).show();
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
