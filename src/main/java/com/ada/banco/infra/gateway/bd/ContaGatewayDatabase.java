package com.ada.banco.infra.gateway.bd;

import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.model.Conta;
import org.springframework.stereotype.Component;

@Component
public class ContaGatewayDatabase implements ContaGateway {
    ContaRepository contaRepository;

    public ContaGatewayDatabase(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Override
    public Conta buscarPorCpf(String cpf) {
        return contaRepository.findByCpf(cpf);
    }

    @Override
    public Conta salvar(Conta conta) {
        return contaRepository.save(conta);
    }
}
