package br.edu.unifaj.cc.mobile.logincomnavegacao.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import br.edu.unifaj.cc.mobile.logincomnavegacao.R;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.ApiRepository;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.RepositoryCallback;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.user.Doador;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.PrefsManager;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.ValidacaoUtils;

/**
 * Activity responsável pela tela de login.
 * Valida email e senha do usuário contra a API.
 */
public class LoginActivity extends BaseActivity {
    private EditText editEmail;
    private EditText editSenha;
    private Button btnEntrar;
    private Button btnCadastrar;
    private PrefsManager prefsManager;
    private final ApiRepository api = ApiRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefsManager = new PrefsManager(this);

        // Verifica se já está logado, se sim, vai para Home
        if (prefsManager.isLoggedIn()) {
            irParaHome();
            return;
        }

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnEntrar.setOnClickListener(v -> validarLogin());

        btnCadastrar.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CadastroActivity.class)));
    }

    /**
     * Valida os dados e autentica na API. Em caso de sucesso guarda a sessão e
     * redireciona para a Home.
     */
    private void validarLogin() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidacaoUtils.validarEmail(email)) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        btnEntrar.setEnabled(false);
        api.login(email, senha, new RepositoryCallback<Doador>() {
            @Override
            public void onSuccess(Doador doador) {
                btnEntrar.setEnabled(true);
                prefsManager.salvarDoador(doador);
                irParaHome();
            }

            @Override
            public void onError(String mensagem) {
                btnEntrar.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irParaHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
