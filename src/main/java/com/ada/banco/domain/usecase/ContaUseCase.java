package com.ada.banco.domain.usecase;

import com.ada.banco.domain.exception.ContaJaExisteException;
import com.ada.banco.domain.exception.ContaNaoExisteException;
import com.ada.banco.domain.exception.ContaSaldoInsuficienteException;
import com.ada.banco.domain.exception.ContasDiferentesException;
import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.gateway.EmailGateway;
import com.ada.banco.domain.model.Conta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ContaUseCase {
    @Autowired
    private ContaGateway contaGateway;
    @Autowired
    private EmailGateway emailGateway;

    public ContaUseCase(ContaGateway contaGateway, EmailGateway emailGateway) {
        this.contaGateway = contaGateway;
        this.emailGateway = emailGateway;
    }


    public Conta criar(Conta conta) throws Exception {
        if(contaGateway.buscarPorId(conta.getId()) != null) {
            throw new ContaJaExisteException("A conta ID: "+ conta.getId() + " ja existe");
        }

        emailGateway.send(conta.getCpf());

        contaGateway.salvar(conta);

        return conta;
    }

    public BigDecimal depositar (Long id, BigDecimal valor) throws Exception {
        verificaContaPorId(id);
        Conta conta = contaGateway.buscarPorId(id);
        conta.setSaldo(conta.getSaldo().add(valor));
        contaGateway.salvar(conta);
        return valor;
    }

    public BigDecimal sacar(Long id, BigDecimal saque) throws Exception {
        verificaContaPorId(id);
        Conta conta = contaGateway.buscarPorId(id);
        verificaSaldoParaSaque(conta, saque);
        conta.setSaldo(conta.getSaldo().subtract(saque));
        contaGateway.salvar(conta);
        return saque;
    }

    private void verificaContaPorId(Long id) throws Exception {
        if(contaGateway.buscarPorId(id) == null) {
            throw new ContaNaoExisteException("A conta com ID: " + id + " não existe");
        }
    }

    private void verificaSaldoParaSaque(Conta conta, BigDecimal saque) throws Exception {
        verificaContaPorId(conta.getId());
        if(conta.getSaldo().compareTo(saque) <= 0) {
            throw new ContaSaldoInsuficienteException("A conta com ID: " + conta.getId() + " não possui o saldo para saque");
        }
    }

    public Long transferir(Long idOut, Long idIn, BigDecimal traferencia) throws Exception {
        sacar(idOut, traferencia);
        depositar(idIn, traferencia);
        return gerarCodigoOperacao();
    }

    public Long gerarCodigoOperacao() {
        Long leftLimit = 10000000L;
        Long rightLimit = 19999999L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
    }

    public Conta atualizar (Long id, Conta contaNova) throws Exception {
        verificaContaPorId(id);

        if (id != contaNova.getId()) {
            throw new ContasDiferentesException("As contas são diferentes");
        }
        System.out.println("cheogu");
        contaGateway.atualizar(contaNova);

        return contaGateway.buscarPorId(id);
    }

    public List<Conta> listar() {
        return contaGateway.listar();
    }

    public List<Conta> listarPorCpf(String cpf) throws ContaNaoExisteException {
        List<Conta> contas = contaGateway.listarPorCpf(cpf);
        if(contas.isEmpty()) {
            throw new ContaNaoExisteException("A conta com CPF: " + cpf + " não existe");
        }
        return contas;
    }
}
