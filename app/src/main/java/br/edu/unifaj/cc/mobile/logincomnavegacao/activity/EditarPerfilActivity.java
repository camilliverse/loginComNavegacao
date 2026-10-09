package br.edu.unifaj.cc.mobile.logincomnavegacao.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import br.edu.unifaj.cc.mobile.logincomnavegacao.R;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.ApiRepository;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.RepositoryCallback;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.user.Doador;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.PrefsManager;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.ValidacaoUtils;

/**
 * Edição do perfil: nome, email e senha.
 *
 * CPF e tipo sanguíneo ficam de fora de propósito: são imutáveis no backend. A
 * senha é opcional: se os dois campos ficarem vazios, a atual é mantida. Trocar
 * a senha exige a senha atual, validada pelo próprio servidor.
 */
public class EditarPerfilActivity extends BaseActivity {

    private TextInputEditText editNome;
    private TextInputEditText editEmail;
    private TextInputEditText editSenhaAtual;
    private TextInputEditText editNovaSenha;
    private TextView txtTipoSanguineo;
    private PrefsManager prefsManager;
    private final ApiRepository api = ApiRepository.getInstance();
    private String cpfDoador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        prefsManager = new PrefsManager(this);

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editSenhaAtual = findViewById(R.id.editSenhaAtual);
        editNovaSenha = findViewById(R.id.editNovaSenha);
        txtTipoSanguineo = findViewById(R.id.txtTipoSanguineo);

        cpfDoador = prefsManager.getCpfLogado();
        Doador doador = prefsManager.getDoador();
        if (cpfDoador == null || doador == null) {
            Toast.makeText(this, R.string.tela_editar_perfil_erro_sem_dados, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editNome.setText(doador.getNome());
        editEmail.setText(doador.getEmail());

        String tipo = doador.getTipoCompleto();
        txtTipoSanguineo.setText(tipo != null && !tipo.isEmpty()
                ? tipo
                : getString(R.string.tela_perfil_tipo_nao_informado));

        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvar());
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }

    private void salvar() {
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senhaAtual = editSenhaAtual.getText().toString();
        String novaSenha = editNovaSenha.getText().toString();

        if (nome.isEmpty()) {
            Toast.makeText(this, R.string.tela_editar_perfil_erro_nome, Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, R.string.tela_editar_perfil_erro_email, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidacaoUtils.validarEmail(email)) {
            Toast.makeText(this, R.string.tela_editar_perfil_erro_email_invalido, Toast.LENGTH_SHORT).show();
            return;
        }

        // Senha em branco nos dois campos: mantém a atual (campos vão nulos).
        boolean querTrocarSenha = !senhaAtual.isEmpty() || !novaSenha.isEmpty();
        if (querTrocarSenha) {
            if (senhaAtual.isEmpty() || novaSenha.isEmpty()) {
                Toast.makeText(this, R.string.tela_editar_perfil_erro_senha_parcial, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!ValidacaoUtils.validarSenha(novaSenha)) {
                Toast.makeText(this, R.string.tela_editar_perfil_erro_senha_curta, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String atual = querTrocarSenha ? senhaAtual : null;
        String nova = querTrocarSenha ? novaSenha : null;

        api.atualizarPerfil(cpfDoador, nome, email, atual, nova, new RepositoryCallback<Doador>() {
            @Override
            public void onSuccess(Doador doador) {
                prefsManager.salvarDoador(doador);
                Toast.makeText(EditarPerfilActivity.this, R.string.tela_editar_perfil_salvo, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String mensagem) {
                Toast.makeText(EditarPerfilActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
