package br.edu.unifaj.cc.mobile.logincomnavegacao.api;

public class BolsaSangueResponse {
    public long id;
    public String codigo;
    public String tipoCompleto;
    public String dataColeta;
    public String dataValidade;
    public int volumeMl;
    public int volumeTotalMl;
    public int quantidade;
    public String status;
    public String statusDescricao;
    public HemocentroResponse hemocentroOrigem;
    public Long agendamentoId;
}
