package com.ada.banco.dummy;

import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.model.Conta;

import java.math.BigDecimal;

// Stub

public class ContaGatewayDummyImpl implements ContaGateway {
    @Override
    public Conta buscarPorCpf(String cpf) {
        if(cpf.equals("123456789")) {
            return new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123456789");
        }

        return null;
    }

    @Override
    public Conta salvar(Conta conta) {
        return new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123456789");
    }
}
