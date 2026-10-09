package br.edu.unifaj.cc.mobile.logincomnavegacao.api;

/** Corpo do POST /api/doadores/login. */
public class LoginRequest {
    public final String email;
    public final String senha;

    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }
}
