package br.edu.unifaj.cc.mobile.logincomnavegacao.api;

/** Corpo do PUT /api/doadores/{cpf}. CPF e tipo sanguíneo são imutáveis. */
public class AtualizarPerfilRequest {
    public final String nome;
    public final String email;
    public final String senhaAtual;
    public final String novaSenha;

    public AtualizarPerfilRequest(String nome, String email, String senhaAtual, String novaSenha) {
        this.nome = nome;
        this.email = email;
        this.senhaAtual = senhaAtual;
        this.novaSenha = novaSenha;
    }
}
