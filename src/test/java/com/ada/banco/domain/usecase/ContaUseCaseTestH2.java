package com.ada.banco.domain.usecase;

import com.ada.banco.domain.exception.ContaJaExisteException;
import com.ada.banco.domain.exception.ContaNaoExisteException;
import com.ada.banco.domain.exception.ContaSaldoInsuficienteException;
import com.ada.banco.domain.exception.ContasDiferentesException;
import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.gateway.EmailGateway;
import com.ada.banco.domain.model.Conta;
import com.ada.banco.infra.gateway.bd.ContaGatewayDatabase;
import com.ada.banco.infra.gateway.bd.ContaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ContaUseCaseTestH2 {

    private ContaUseCase contaUseCase;
    @Autowired
    private EmailGateway emailGateway;


    private ContaGateway contaGateway;

    private Conta contaTeste;
    @Autowired
    private ContaRepository contaRepository;

    @BeforeEach
    public void setUp() {
        contaGateway = new ContaGatewayDatabase(contaRepository);
        contaUseCase = new ContaUseCase(contaGateway, emailGateway);
        contaRepository.deleteAll();
        contaTeste = new Conta(1L,2L, 3L, BigDecimal.valueOf(2.49), "Luiz", "000.000.000-00");
    }

    @Test
    @DisplayName("Criar conta")
    public void deveCriarContaCorretamente() throws Exception {
        contaUseCase.criar(contaTeste);
        Conta contaSalva = contaGateway.buscarPorId(contaTeste.getId());
        Assertions.assertEquals(contaTeste, contaSalva);
    }

    @Test
    public void deveLancarExceptionCasoContaJaExista() {
        contaGateway.salvar(contaTeste);
        Throwable throwable =
                Assertions.assertThrows(ContaJaExisteException.class,
                        () -> contaUseCase.criar(contaTeste));
        Assertions.assertEquals("A conta ID: "+ contaTeste.getId() + " ja existe", throwable.getMessage());
    }

    @Test
    @DisplayName("Listar todas as contas")
    public void deveListarContas() throws Exception {
        Conta conta1 = new Conta(3L,2L, 3L, BigDecimal.valueOf(2.32), "Luiz", "000.000.000-00");
        contaUseCase.criar(conta1);
        Conta conta2 = new Conta(4L,2L, 3L, BigDecimal.valueOf(4.76), "Luiz1", "000.000.000-00");
        contaUseCase.criar(conta2);
        Conta conta3 = new Conta(1L,2L, 3L, BigDecimal.valueOf(50000.02), "Luiz2", "000.000.000-00");
        contaUseCase.criar(conta3);

        List<Conta> contasCriadas = new ArrayList<>();
        contasCriadas.add(conta1);
        contasCriadas.add(conta2);
        contasCriadas.add(conta3);

        List<Conta> contasSalvas = contaUseCase.listar();

        Assertions.assertTrue(contasSalvas.containsAll(contasCriadas));
        Assertions.assertTrue(contasCriadas.containsAll(contasSalvas));
    }

    @Test
    @DisplayName("Listar todas as contas do mesmo cpf")
    public void deveListarContasPorCpf() throws Exception {
        Conta conta1 = new Conta(3L,2L, 3L, BigDecimal.valueOf(2.32), "Luiz", "000.000.000-01");
        contaUseCase.criar(conta1);
        Conta conta2 = new Conta(4L,2L, 3L, BigDecimal.valueOf(4.76), "Luiz1", "000.000.000-01");
        contaUseCase.criar(conta2);
        Conta conta3 = new Conta(2L,2L, 3L, BigDecimal.valueOf(50000.02), "Luiz2", "000.000.000-01");
        contaUseCase.criar(conta3);

        contaUseCase.criar(contaTeste); //cria conta com CPF 000.000.000.00

        List<Conta> contasCriadas = new ArrayList<>();
        contasCriadas.add(conta1);
        contasCriadas.add(conta2);
        contasCriadas.add(conta3);

        List<Conta> contasSalvasFiltradas = contaUseCase.listarPorCpf("000.000.000-01");

        Assertions.assertTrue(contasSalvasFiltradas.containsAll(contasCriadas));
        Assertions.assertTrue(contasCriadas.containsAll(contasSalvasFiltradas));
    }

    @Test
    @DisplayName("Transferencia entra contas")
    public void deveTrasferirCorretamente() throws Exception {
        Conta conta2 = new Conta(2L,2L, 3L,BigDecimal.valueOf(2.94), "Luiz", "000.000.000-00");

        contaUseCase.criar(contaTeste);
        contaUseCase.criar(conta2);

        BigDecimal valorAntigoConta1 = contaTeste.getSaldo();
        BigDecimal valorAntigoConta2 = conta2.getSaldo();

        BigDecimal valorATransferir = new BigDecimal("1.50");

        Long numeroTrasacao = contaUseCase.transferir(contaTeste.getId(), conta2.getId(), valorATransferir);

        Conta contaAtualizada1 = contaGateway.buscarPorId(contaTeste.getId());
        Conta contaAtualizada2 = contaGateway.buscarPorId(conta2.getId());

        Assertions.assertAll("Verifica o saldo das contas",
                () -> Assertions.assertEquals(valorAntigoConta1.subtract(valorATransferir), contaAtualizada1.getSaldo()),
                () -> Assertions.assertEquals(valorAntigoConta2.add(valorATransferir), contaAtualizada2.getSaldo())
        );
        System.out.println("Transacao "+numeroTrasacao+": R$"+ valorATransferir+" transferidos da conta ID" +
                contaTeste.getId() + " para a conta ID" +conta2.getId());
    }

    @Test
    @DisplayName("Atualizar uma conta")
    public void deveAtualizarUmaContaCorretamente() throws Exception {
        Conta contaAtualizada = contaUseCase.criar(contaTeste);
        contaAtualizada.setTitular("Luiz Atualizado");

        contaUseCase.atualizar(contaTeste.getId(), contaAtualizada);

        Conta contaSalva = contaGateway.buscarPorId(contaTeste.getId());

        Assertions.assertEquals("Luiz Atualizado", contaSalva.getTitular());

    }

    @Test
    @DisplayName("Não localizou conta")
    public void deveLancarExceptionCasoNaoLocalizeAConta() {
        Throwable throwable =  Assertions.assertThrows(ContaNaoExisteException.class,
                () -> contaUseCase.depositar(1L, BigDecimal.valueOf(100)));

        Assertions.assertEquals("A conta com ID: 1 não existe", throwable.getMessage());
    }

    @Test
    @DisplayName("Não possui saldo para saque")
    public void deveLancarExceptionCasoContaNaoPossuaSaldoSuficiente() throws Exception {
        contaUseCase.criar(contaTeste);
        contaUseCase.depositar(contaTeste.getId(), BigDecimal.valueOf(2000));

        BigDecimal saque = BigDecimal.valueOf(2002.50);

        Throwable throwable =  Assertions.assertThrows(ContaSaldoInsuficienteException.class,
                () -> contaUseCase.sacar(contaTeste.getId(), saque));

        Assertions.assertEquals("A conta com ID: " + contaTeste.getId() + " não possui o saldo para saque", throwable.getMessage());
    }

    @Test
    @DisplayName("Não localizou cpf na lista")
    public void deveLancarExceptionCasoContaNaoLocalizaCpf() throws Exception {
        contaUseCase.criar(contaTeste);
        String cpf = "123";
        Throwable throwable =  Assertions.assertThrows(ContaNaoExisteException.class,
                () -> contaUseCase.listarPorCpf(cpf));

        Assertions.assertEquals("A conta com CPF: " + cpf + " não existe", throwable.getMessage());
    }

    @Test
    @DisplayName("Não atualiza conta com id diferente")
    public void deveLancarExceptionCasoAtualizaContaErrada() throws Exception {
        contaUseCase.criar(contaTeste);

        Conta contaAtualizada = contaGateway.buscarPorId(contaTeste.getId());

        contaAtualizada.setTitular("Luiz Atualizado");
        contaAtualizada.setId(2L);


        Throwable throwable =  Assertions.assertThrows(Exception.class,
                () -> contaUseCase.atualizar(contaTeste.getId(), contaAtualizada));

        Assertions.assertEquals("As contas são diferentes", throwable.getMessage());
    }




}