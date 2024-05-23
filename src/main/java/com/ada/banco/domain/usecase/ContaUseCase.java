package com.ada.banco.domain.usecase;

import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.gateway.EmailGateway;
import com.ada.banco.domain.model.Conta;
import org.springframework.stereotype.Component;

@Component
public class ContaUseCase {
    private ContaGateway contaGateway;
    private EmailGateway emailGateway;

    public ContaUseCase(ContaGateway contaGateway, EmailGateway emailGateway) {
        this.contaGateway = contaGateway;
        this.emailGateway = emailGateway;
    }

    public Conta execute(Conta conta) throws Exception {
        if(contaGateway.buscarPorCpf(conta.getCpf()) != null) {
             throw new Exception("Usuario ja possui uma conta");
        }

        Conta novaConta = contaGateway.salvar(conta);
        emailGateway.send(conta.getCpf());
        return novaConta;
    }

    public Conta execute2(Conta conta, Boolean usuarioExiste) throws Exception {
        if(usuarioExiste) {
            throw new Exception("Usuario ja possui uma conta");
        }

        Conta novaConta = contaGateway.salvar(conta);
        emailGateway.send(conta.getCpf());
        return novaConta;
    }
}
