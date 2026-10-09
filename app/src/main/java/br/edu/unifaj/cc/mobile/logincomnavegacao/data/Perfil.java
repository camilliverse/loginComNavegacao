package br.edu.unifaj.cc.mobile.logincomnavegacao.data;

import br.edu.unifaj.cc.mobile.logincomnavegacao.model.user.Doador;

/** Perfil do doador + nível, do jeito que o backend devolve (PerfilResponse). */
public class Perfil {
    private final Doador doador;
    private final int registros;
    private final int limite;
    private final int percentual;
    private final boolean nivelMaximo;

    public Perfil(Doador doador, int registros, int limite, int percentual, boolean nivelMaximo) {
        this.doador = doador;
        this.registros = registros;
        this.limite = limite;
        this.percentual = percentual;
        this.nivelMaximo = nivelMaximo;
    }

    public Doador getDoador() {
        return doador;
    }

    public int getRegistros() {
        return registros;
    }

    public int getLimite() {
        return limite;
    }

    public int getPercentual() {
        return percentual;
    }

    public boolean isNivelMaximo() {
        return nivelMaximo;
    }
}
