package br.edu.unifaj.cc.mobile.logincomnavegacao.activity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import br.edu.unifaj.cc.mobile.logincomnavegacao.R;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.ApiRepository;
import br.edu.unifaj.cc.mobile.logincomnavegacao.data.RepositoryCallback;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.enums.TipoSanguineo;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.enums.FatorRh;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.user.Doador;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.ValidacaoUtils;

public class CadastroActivity extends BaseActivity {

    private EditText editNome;
    private EditText editEmail;
    private EditText editSenha;
    private EditText editCpf;
    private Spinner spinnerTipoSanguineo;
    private Button btnCadastrar;
    private Button btnVoltar;
    private final ApiRepository api = ApiRepository.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        editCpf = findViewById(R.id.editCpf);
        spinnerTipoSanguineo = findViewById(R.id.spinnerTipoSanguineo);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnVoltar = findViewById(R.id.btnVoltar);

        String[] tiposSanguineos = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        // Usa os layouts do projeto, cujas cores vêm do tema: nada de branco fixo.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, tiposSanguineos);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerTipoSanguineo.setAdapter(adapter);

        btnCadastrar.setOnClickListener(v -> cadastrarDoador());

        btnVoltar.setOnClickListener(v -> finish());
    }

    private void cadastrarDoador() {
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();
        String cpf = editCpf.getText().toString().trim();
        Object selecionado = spinnerTipoSanguineo.getSelectedItem();
        String tipoCompleto = selecionado != null ? selecionado.toString() : "";

        // Valida campos obrigatórios
        String validacao = ValidacaoUtils.validarCamposObrigatorios(nome, email, senha, cpf, tipoCompleto);
        if (validacao != null) {
            Toast.makeText(this, validacao, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validações específicas
        if (!ValidacaoUtils.validarEmail(email)) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidacaoUtils.validarSenha(senha)) {
            Toast.makeText(this, "Senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ValidacaoUtils.validarCpf(cpf)) {
            Toast.makeText(this, "CPF inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Garante que o rótulo do spinner é um tipo sanguíneo válido antes de enviar.
        TipoSanguineo tipoSanguineo = TipoSanguineo.fromValorCompleto(tipoCompleto);
        FatorRh fatorRh = FatorRh.fromValor(fatorRestante(tipoCompleto, tipoSanguineo));

        if (tipoSanguineo == null || fatorRh == null) {
            Toast.makeText(this, "Tipo sanguíneo inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCadastrar.setEnabled(false);
        api.cadastrar(nome, email, senha, cpf, tipoCompleto, new RepositoryCallback<Doador>() {
            @Override
            public void onSuccess(Doador doador) {
                Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String mensagem) {
                btnCadastrar.setEnabled(true);
                Toast.makeText(CadastroActivity.this, mensagem, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Devolve o fator Rh que sobrou do rotulo depois do tipo.
     *
     * "AB-" vira "-" e "O+" vira "+". Sem tipo reconhecido, devolve o rotulo
     * inteiro: quem valida e o {@link FatorRh#fromValor(String)}, que rejeita.
     */
    private static String fatorRestante(String tipoCompleto, TipoSanguineo tipo) {
        if (tipoCompleto == null) {
            return null;
        }
        if (tipo == null) {
            return tipoCompleto.trim();
        }
        String rotulo = tipoCompleto.trim();
        return rotulo.length() > tipo.getValor().length()
                ? rotulo.substring(tipo.getValor().length())
                : "";
    }
}
