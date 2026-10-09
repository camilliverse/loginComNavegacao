package br.edu.unifaj.cc.mobile.logincomnavegacao.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import br.edu.unifaj.cc.mobile.logincomnavegacao.model.user.Doador;

/**
 * Guarda apenas o estado local da sessão: qual doador está logado e um cache do
 * seu cadastro, para o topo da Home aparecer sem esperar a rede. Os dados de
 * agendamentos, bolsas e perfil agora vêm da API (ver {@code data.ApiRepository}).
 */
public class PrefsManager {

    private static final String PREF_NAME = "PrefDoacaoSangue";
    private static final String KEY_DOADOR = "doador_cache";
    private static final String KEY_LOGGED_CPF = "logged_cpf";

    private final SharedPreferences prefs;
    private final Gson gson;

    public PrefsManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void salvarDoador(Doador doador) {
        prefs.edit()
                .putString(KEY_DOADOR, gson.toJson(doador))
                .putString(KEY_LOGGED_CPF, doador == null ? null : doador.getCpf())
                .apply();
    }

    public Doador getDoador() {
        String json = prefs.getString(KEY_DOADOR, null);
        return json == null ? null : gson.fromJson(json, Doador.class);
    }

    public String getCpfLogado() {
        return prefs.getString(KEY_LOGGED_CPF, null);
    }

    public boolean isLoggedIn() {
        return getCpfLogado() != null;
    }

    public void logout() {
        prefs.edit().remove(KEY_LOGGED_CPF).remove(KEY_DOADOR).apply();
    }
}
