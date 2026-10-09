package br.edu.unifaj.cc.mobile.logincomnavegacao.data;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.edu.unifaj.cc.mobile.logincomnavegacao.api.AgendarRequest;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.AgendamentoResponse;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.ApiClient;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.ApiService;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.AtualizarPerfilRequest;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.BolsaSangueResponse;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.CadastroRequest;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.DoadorResponse;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.EnderecoResponse;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.HemocentroResponse;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.LoginRequest;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.PerfilResponse;
import br.edu.unifaj.cc.mobile.logincomnavegacao.api.RegistrarColetaRequest;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.entity.Agendamento;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.entity.BolsaSangue;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.entity.Endereco;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.entity.Hemocentro;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.enums.FatorRh;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.enums.StatusAgendamento;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.enums.StatusBolsa;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.enums.TipoSanguineo;
import br.edu.unifaj.cc.mobile.logincomnavegacao.model.user.Doador;
import br.edu.unifaj.cc.mobile.logincomnavegacao.util.DateUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fachada da API REST do backend. Cada método dispara a chamada em background
 * (Retrofit enfileira) e entrega o resultado já convertido nos modelos do app,
 * na thread principal, através de um {@link RepositoryCallback}.
 */
public class ApiRepository {

    private static ApiRepository instancia;

    private final ApiService api;

    private ApiRepository() {
        this.api = ApiClient.getService();
    }

    public static synchronized ApiRepository getInstance() {
        if (instancia == null) {
            instancia = new ApiRepository();
        }
        return instancia;
    }

    /* ------------------------------------------------------------------ */
    /* Doador                                                             */
    /* ------------------------------------------------------------------ */

    public void login(String email, String senha, RepositoryCallback<Doador> cb) {
        chamar(api.login(new LoginRequest(email, senha)), new RepositoryCallback<DoadorResponse>() {
            @Override public void onSuccess(DoadorResponse data) { cb.onSuccess(toDoador(data)); }
            @Override public void onError(String mensagem) { cb.onError(mensagem); }
        });
    }

    public void cadastrar(String nome, String email, String senha, String cpf, String tipoCompleto,
                          RepositoryCallback<Doador> cb) {
        chamar(api.cadastrar(new CadastroRequest(nome, email, senha, cpf, tipoCompleto)),
                new RepositoryCallback<DoadorResponse>() {
                    @Override public void onSuccess(DoadorResponse data) { cb.onSuccess(toDoador(data)); }
                    @Override public void onError(String mensagem) { cb.onError(mensagem); }
                });
    }

    public void carregarPerfil(String cpf, RepositoryCallback<Perfil> cb) {
        chamar(api.getPerfil(cpf), new RepositoryCallback<PerfilResponse>() {
            @Override
            public void onSuccess(PerfilResponse data) {
                cb.onSuccess(new Perfil(toDoador(data.doador), data.registros, data.limite,
                        data.percentual, data.nivelMaximo));
            }
            @Override public void onError(String mensagem) { cb.onError(mensagem); }
        });
    }

    public void atualizarPerfil(String cpf, String nome, String email, String senhaAtual, String novaSenha,
                                RepositoryCallback<Doador> cb) {
        chamar(api.atualizarPerfil(cpf, new AtualizarPerfilRequest(nome, email, senhaAtual, novaSenha)),
                new RepositoryCallback<DoadorResponse>() {
                    @Override public void onSuccess(DoadorResponse data) { cb.onSuccess(toDoador(data)); }
                    @Override public void onError(String mensagem) { cb.onError(mensagem); }
                });
    }

    /* ------------------------------------------------------------------ */
    /* Hemocentros                                                        */
    /* ------------------------------------------------------------------ */

    public void listarHemocentros(RepositoryCallback<List<Hemocentro>> cb) {
        chamar(api.listarHemocentros(), new RepositoryCallback<List<HemocentroResponse>>() {
            @Override
            public void onSuccess(List<HemocentroResponse> data) {
                List<Hemocentro> saida = new ArrayList<>();
                if (data != null) {
                    for (HemocentroResponse h : data) {
                        saida.add(toHemocentro(h));
                    }
                }
                cb.onSuccess(saida);
            }
            @Override public void onError(String mensagem) { cb.onError(mensagem); }
        });
    }

    /* ------------------------------------------------------------------ */
    /* Agendamentos                                                       */
    /* ------------------------------------------------------------------ */

    public void agendar(String cpf, String dataBr, String hora, String hemocentroId,
                        RepositoryCallback<Agendamento> cb) {
        AgendarRequest body = new AgendarRequest(DateUtils.brParaIso(dataBr), hora, hemocentroId);
        chamar(api.criarAgendamento(cpf, body), new RepositoryCallback<AgendamentoResponse>() {
            @Override public void onSuccess(AgendamentoResponse data) { cb.onSuccess(toAgendamento(data)); }
            @Override public void onError(String mensagem) { cb.onError(mensagem); }
        });
    }

    public void listarAgendamentos(String cpf, RepositoryCallback<List<Agendamento>> cb) {
        chamar(api.listarAgendamentos(cpf), cbAgendamentos(cb));
    }

    public void listarParaRegistro(String cpf, RepositoryCallback<List<Agendamento>> cb) {
        chamar(api.listarParaRegistro(cpf), cbAgendamentos(cb));
    }

    public void cancelarAgendamento(String cpf, long agendamentoId, RepositoryCallback<Void> cb) {
        chamar(api.cancelarAgendamento(agendamentoId, cpf), cb);
    }

    private RepositoryCallback<List<AgendamentoResponse>> cbAgendamentos(
            RepositoryCallback<List<Agendamento>> cb) {
        return new RepositoryCallback<List<AgendamentoResponse>>() {
            @Override
            public void onSuccess(List<AgendamentoResponse> data) {
                List<Agendamento> saida = new ArrayList<>();
                if (data != null) {
                    for (AgendamentoResponse a : data) {
                        saida.add(toAgendamento(a));
                    }
                }
                cb.onSuccess(saida);
            }
            @Override public void onError(String mensagem) { cb.onError(mensagem); }
        };
    }

    /* ------------------------------------------------------------------ */
    /* Bolsas                                                             */
    /* ------------------------------------------------------------------ */

    public void listarBolsas(String cpf, RepositoryCallback<List<BolsaSangue>> cb) {
        chamar(api.listarBolsas(cpf), new RepositoryCallback<List<BolsaSangueResponse>>() {
            @Override
            public void onSuccess(List<BolsaSangueResponse> data) {
                List<BolsaSangue> saida = new ArrayList<>();
                if (data != null) {
                    for (BolsaSangueResponse b : data) {
                        saida.add(toBolsa(b));
                    }
                }
                cb.onSuccess(saida);
            }
            @Override public void onError(String mensagem) { cb.onError(mensagem); }
        });
    }

    public void registrarColeta(String cpf, long agendamentoId, int volumeMl, int quantidade,
                                RepositoryCallback<BolsaSangue> cb) {
        RegistrarColetaRequest body = new RegistrarColetaRequest(volumeMl, quantidade);
        chamar(api.registrarColeta(agendamentoId, cpf, body), new RepositoryCallback<BolsaSangueResponse>() {
            @Override public void onSuccess(BolsaSangueResponse data) { cb.onSuccess(toBolsa(data)); }
            @Override public void onError(String mensagem) { cb.onError(mensagem); }
        });
    }

    /* ------------------------------------------------------------------ */
    /* Conversões e infraestrutura                                        */
    /* ------------------------------------------------------------------ */

    private Doador toDoador(DoadorResponse r) {
        if (r == null) {
            return null;
        }
        Doador doador = new Doador();
        doador.setNome(r.nome);
        doador.setEmail(r.email);
        doador.setCpf(r.cpf);
        doador.setTipoSanguineo(TipoSanguineo.fromValor(r.tipoSanguineo));
        doador.setFatorRh(FatorRh.fromValor(r.fatorRh));
        return doador;
    }

    private Hemocentro toHemocentro(HemocentroResponse r) {
        if (r == null) {
            return null;
        }
        Endereco endereco = new Endereco();
        if (r.endereco != null) {
            EnderecoResponse e = r.endereco;
            endereco = new Endereco(e.logradouro, e.numero, e.bairro, e.cidade, e.estado, e.cep);
            endereco.setComplemento(e.complemento);
        }
        Hemocentro hemocentro = new Hemocentro(r.id, r.nome, endereco, r.telefone);
        hemocentro.setEmail(r.email);
        hemocentro.setHorarioAbertura(r.horarioAbertura);
        hemocentro.setHorarioFechamento(r.horarioFechamento);
        hemocentro.setFuncionamento24Horas(r.funcionamento24Horas);
        return hemocentro;
    }

    private Agendamento toAgendamento(AgendamentoResponse r) {
        if (r == null) {
            return null;
        }
        Agendamento agendamento = new Agendamento();
        agendamento.setId(String.valueOf(r.id));
        agendamento.setData(DateUtils.isoParaBr(r.data));
        agendamento.setHora(DateUtils.horaParaBr(r.hora));
        agendamento.setHemocentro(toHemocentro(r.hemocentro));
        agendamento.setStatus(status(r.status));
        agendamento.setQuantidadeBolsas(r.quantidadeBolsas);
        return agendamento;
    }

    private BolsaSangue toBolsa(BolsaSangueResponse r) {
        if (r == null) {
            return null;
        }
        BolsaSangue bolsa = new BolsaSangue();
        bolsa.setCodigo(r.codigo);
        bolsa.setDataColeta(DateUtils.isoParaBr(r.dataColeta));
        bolsa.setDataValidade(DateUtils.isoParaBr(r.dataValidade));
        bolsa.setVolumeMl(r.volumeMl);
        bolsa.setQuantidade(r.quantidade);
        bolsa.setStatus(statusBolsa(r.status));
        bolsa.setHemocentroOrigem(toHemocentro(r.hemocentroOrigem));
        bolsa.setAgendamentoId(r.agendamentoId == null ? null : String.valueOf(r.agendamentoId));

        String tipoCompleto = r.tipoCompleto;
        if (tipoCompleto != null) {
            TipoSanguineo tipo = TipoSanguineo.fromValorCompleto(tipoCompleto);
            bolsa.setTipoSanguineo(tipo);
            if (tipo != null) {
                String resto = tipoCompleto.trim().substring(tipo.getValor().length());
                bolsa.setFatorRh(FatorRh.fromValor(resto));
            }
        }
        return bolsa;
    }

    private StatusAgendamento status(String nome) {
        if (nome == null) {
            return StatusAgendamento.PENDENTE;
        }
        try {
            return StatusAgendamento.valueOf(nome);
        } catch (IllegalArgumentException e) {
            return StatusAgendamento.PENDENTE;
        }
    }

    private StatusBolsa statusBolsa(String nome) {
        if (nome == null) {
            return StatusBolsa.DISPONIVEL;
        }
        try {
            return StatusBolsa.valueOf(nome);
        } catch (IllegalArgumentException e) {
            return StatusBolsa.DISPONIVEL;
        }
    }

    private <T> void chamar(Call<T> call, RepositoryCallback<T> cb) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> c, @NonNull Response<T> resposta) {
                if (resposta.isSuccessful()) {
                    cb.onSuccess(resposta.body());
                } else {
                    cb.onError(mensagemErro(resposta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> c, @NonNull Throwable t) {
                cb.onError("Não foi possível conectar ao servidor. Verifique se o backend está no ar.");
            }
        });
    }

    private String mensagemErro(Response<?> resposta) {
        try {
            if (resposta.errorBody() != null) {
                String corpo = resposta.errorBody().string();
                JSONObject json = new JSONObject(corpo);
                if (json.has("detail")) {
                    return json.getString("detail");
                }
                if (json.has("message")) {
                    return json.getString("message");
                }
                if (json.has("erro")) {
                    return json.getString("erro");
                }
            }
        } catch (Exception ignored) {
            // corpo não-JSON: cai na mensagem genérica abaixo
        }
        return "Erro ao processar a solicitação (" + resposta.code() + ")";
    }
}
