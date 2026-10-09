package br.edu.unifaj.cc.mobile.logincomnavegacao.api;

/** Corpo do POST /api/doadores (cadastro). */
public class CadastroRequest {
    public final String nome;
    public final String email;
    public final String senha;
    public final String cpf;
    public final String tipoSanguineo;

    public CadastroRequest(String nome, String email, String senha, String cpf, String tipoSanguineo) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.cpf = cpf;
        this.tipoSanguineo = tipoSanguineo;
    }
}
