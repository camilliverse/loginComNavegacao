package br.edu.unifaj.cc.mobile.logincomnavegacao.api;

/**
 * Resposta de doador da API ({@code DoadorResponse} do backend).
 * Nunca traz a senha.
 */
public class DoadorResponse {
    public long id;
    public String nome;
    public String email;
    public String cpf;
    public String tipoSanguineo;
    public String fatorRh;
    public String tipoCompleto;
}
