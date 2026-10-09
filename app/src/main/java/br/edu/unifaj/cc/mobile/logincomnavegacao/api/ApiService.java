package br.edu.unifaj.cc.mobile.logincomnavegacao.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Endpoints do backend Spring Boot. O contrato aqui é o mesmo consumido pela
 * interface WEB (ver o README do ppi-backend).
 */
public interface ApiService {

    @POST("api/doadores")
    Call<DoadorResponse> cadastrar(@Body CadastroRequest body);

    @POST("api/doadores/login")
    Call<DoadorResponse> login(@Body LoginRequest body);

    @GET("api/doadores/{cpf}")
    Call<PerfilResponse> getPerfil(@Path("cpf") String cpf);

    @PUT("api/doadores/{cpf}")
    Call<DoadorResponse> atualizarPerfil(@Path("cpf") String cpf, @Body AtualizarPerfilRequest body);

    @GET("api/hemocentros")
    Call<List<HemocentroResponse>> listarHemocentros();

    @GET("api/agendamentos")
    Call<List<AgendamentoResponse>> listarAgendamentos(@Query("cpf") String cpf);

    @GET("api/agendamentos/para-registro")
    Call<List<AgendamentoResponse>> listarParaRegistro(@Query("cpf") String cpf);

    @POST("api/agendamentos")
    Call<AgendamentoResponse> criarAgendamento(@Query("cpf") String cpf, @Body AgendarRequest body);

    @POST("api/agendamentos/{id}/cancelar")
    Call<Void> cancelarAgendamento(@Path("id") long id, @Query("cpf") String cpf);

    @GET("api/bolsas-sangue")
    Call<List<BolsaSangueResponse>> listarBolsas(@Query("cpf") String cpf);

    @POST("api/bolsas-sangue/agendamentos/{id}/coletas")
    Call<BolsaSangueResponse> registrarColeta(@Path("id") long agendamentoId, @Query("cpf") String cpf,
                                              @Body RegistrarColetaRequest body);
}
