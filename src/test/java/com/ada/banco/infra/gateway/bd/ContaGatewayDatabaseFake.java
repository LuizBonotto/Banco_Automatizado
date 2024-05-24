package com.ada.banco.infra.gateway.bd;

import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.model.Conta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContaGatewayDatabaseFake implements ContaGateway {

    public static Map<Long, Conta> contaRepositoryFake = new HashMap<>();

    @Override
    public Conta buscarPorCpf(String cpf) {
        return contaRepositoryFake.get(cpf);
    }

    @Override
    public Conta salvar(Conta conta) {
        return contaRepositoryFake.put(conta.getId(), conta);
    }

    @Override
    public Conta buscarPorId(Long id) {
        return contaRepositoryFake.get(id);
    }

    @Override
    public Conta atualizar(Conta conta) {
        return contaRepositoryFake.replace(conta.getId(), conta);
    }

    @Override
    public List<Conta> listar() {
        List<Conta> contas = new ArrayList<>();
        contas.addAll(contaRepositoryFake.values());
        return contas;
    }

    @Override
    public List<Conta> listarPorCpf(String cpf) {
        List<Conta> contas = new ArrayList<>();
        for (Conta conta : contaRepositoryFake.values()) {
            if (conta.getCpf().equals(cpf)) {
                contas.add(conta);
            }
        }
        return contas;
    }
}
