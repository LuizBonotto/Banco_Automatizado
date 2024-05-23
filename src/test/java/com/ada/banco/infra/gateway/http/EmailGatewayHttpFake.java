package com.ada.banco.infra.gateway.http;

import com.ada.banco.domain.gateway.EmailGateway;

public class EmailGatewayHttpFake implements EmailGateway {
    @Override
    public void send(String cpf) {
        System.out.println("Email para o CPF: "+ cpf +" foi enviado!");
    }
}
