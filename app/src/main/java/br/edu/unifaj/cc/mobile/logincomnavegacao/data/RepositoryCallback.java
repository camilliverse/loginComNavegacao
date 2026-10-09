package br.edu.unifaj.cc.mobile.logincomnavegacao.data;

/**
 * Callback das chamadas de rede. Os métodos rodam na thread principal, então
 * podem atualizar a UI direto.
 */
public interface RepositoryCallback<T> {
    void onSuccess(T data);
    void onError(String mensagem);
}
