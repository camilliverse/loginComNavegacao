package br.edu.unifaj.cc.mobile.logincomnavegacao.api;

/**
 * Corpo do POST /api/agendamentos?cpf=.
 * {@code data} em ISO (yyyy-MM-dd) e {@code hora} em HH:mm, como o backend espera.
 */
public class AgendarRequest {
    public final String data;
    public final String hora;
    public final String hemocentroId;

    public AgendarRequest(String data, String hora, String hemocentroId) {
        this.data = data;
        this.hora = hora;
        this.hemocentroId = hemocentroId;
    }
}
