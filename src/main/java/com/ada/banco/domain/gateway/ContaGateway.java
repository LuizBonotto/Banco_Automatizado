package com.ada.banco.domain.gateway;

import com.ada.banco.domain.model.Conta;

import java.util.List;

public interface ContaGateway {
    Conta buscarPorCpf(String cpf);
    Conta salvar(Conta conta);
    Conta buscarPorId(Long id);
    Conta atualizar(Conta conta);
    List<Conta> listar();
    List<Conta> listarPorCpf(String cpf);
}
