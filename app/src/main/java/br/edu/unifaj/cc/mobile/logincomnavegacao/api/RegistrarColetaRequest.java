package br.edu.unifaj.cc.mobile.logincomnavegacao.api;

/** Corpo do POST /api/bolsas-sangue/agendamentos/{id}/coletas?cpf=. */
public class RegistrarColetaRequest {
    public final int volumeMl;
    public final int quantidade;

    public RegistrarColetaRequest(int volumeMl, int quantidade) {
        this.volumeMl = volumeMl;
        this.quantidade = quantidade;
    }
}
