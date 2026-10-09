package br.edu.unifaj.cc.mobile.logincomnavegacao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import br.edu.unifaj.cc.mobile.logincomnavegacao.R;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.ApiRepository;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.Perfil;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.RepositoryCallback;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.user.Doador;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.PrefsManager;

/**
 * Tela de perfil, no estilo de um cartazinho de doador.
 *
 * Os dados e o nível vêm da API (PerfilResponse). Para alterar alguma coisa, a
 * tela delega para {@link EditarPerfilActivity}; como ela salva e finishes, o
 * onResume recarrega o cartão com os valores novos.
 */
public class PerfilActivity extends BaseActivity {

    private TextView txtIniciais;
    private TextView txtNome;
    private TextView txtEmail;
    private TextView txtTipoSanguineo;
    private TextView txtNomeDado;
    private TextView txtEmailDado;
    private TextView txtCpf;
    private TextView txtNivelProgresso;
    private LinearProgressIndicator barNivel;
    private PrefsManager prefsManager;
    private final ApiRepository api = ApiRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        prefsManager = new PrefsManager(this);

        txtIniciais = findViewById(R.id.txtIniciais);
        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        txtTipoSanguineo = findViewById(R.id.txtTipoSanguineo);
        txtNomeDado = findViewById(R.id.txtNomeDado);
        txtEmailDado = findViewById(R.id.txtEmailDado);
        txtCpf = findViewById(R.id.txtCpf);
        txtNivelProgresso = findViewById(R.id.txtNivelProgresso);
        barNivel = findViewById(R.id.barNivel);

        findViewById(R.id.btnEditarPerfil)
                .setOnClickListener(v -> startActivity(new Intent(this, EditarPerfilActivity.class)));
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarPerfil();
    }

    private void carregarPerfil() {
        String cpf = prefsManager.getCpfLogado();
        if (cpf == null) {
            Toast.makeText(this, R.string.tela_editar_perfil_erro_sem_dados, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        api.carregarPerfil(cpf, new RepositoryCallback<Perfil>() {
            @Override
            public void onSuccess(Perfil perfil) {
                if (isFinishing() || perfil.getDoador() == null) {
                    return;
                }
                prefsManager.salvarDoador(perfil.getDoador());
                mostrarCartao(perfil.getDoador());
                mostrarNivel(perfil);
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(PerfilActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarCartao(Doador doador) {
        txtIniciais.setText(iniciais(doador.getNome()));

        String nome = doador.getNome();
        txtNome.setText(nome);
        txtNomeDado.setText(nome);

        String email = doador.getEmail();
        txtEmail.setText(email);
        txtEmailDado.setText(email);

        String tipo = doador.getTipoCompleto();
        txtTipoSanguineo.setText(tipo != null && !tipo.isEmpty()
                ? tipo
                : getString(R.string.tela_perfil_tipo_nao_informado));

        String cpf = doador.getCpf();
        txtCpf.setText(cpf != null && !cpf.isEmpty()
                ? cpf
                : getString(R.string.tela_perfil_cpf_nao_informado));
    }

    /**
     * Enche a barra "Nível de doador" com o percentual calculado pelo backend.
     * Sem animação: roda a cada onResume e uma barra animando reiniciaria sozinha.
     */
    private void mostrarNivel(Perfil perfil) {
        barNivel.setProgressCompat(perfil.getPercentual(), false);

        if (perfil.isNivelMaximo()) {
            txtNivelProgresso.setText(R.string.tela_perfil_nivel_maximo);
        } else {
            txtNivelProgresso.setText(getResources().getQuantityString(
                    R.plurals.tela_perfil_nivel_progresso,
                    perfil.getRegistros(), perfil.getRegistros(), perfil.getLimite()));
        }
    }

    /**
     * Pega a inicial do primeiro e do último nome para o avatar.
     * "Maria Souza Costa" vira "MC"; sem nome, cai na letra padrão do layout.
     */
    private String iniciais(String nome) {
        if (nome == null) {
            return getString(R.string.tela_perfil_inicial_padrao);
        }

        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 0 || partes[0].isEmpty()) {
            return getString(R.string.tela_perfil_inicial_padrao);
        }

        StringBuilder resultado = new StringBuilder();
        resultado.append(Character.toUpperCase(partes[0].charAt(0)));
        if (partes.length > 1) {
            resultado.append(Character.toUpperCase(partes[partes.length - 1].charAt(0)));
        }
        return resultado.toString();
    }
}
