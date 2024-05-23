package com.ada.banco.domain.usecase;

import com.ada.banco.domain.exception.ContaJaExisteException;
import com.ada.banco.domain.exception.ContaNaoExisteException;
import com.ada.banco.domain.exception.ContaSaldoInsuficienteException;
import com.ada.banco.domain.exception.ContasDiferentesException;
import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.gateway.EmailGateway;
import com.ada.banco.domain.model.Conta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ContaUseCaseTestMockito {

    @Mock
    private ContaGateway contaGateway;
    @Mock
    private EmailGateway emailGateway;

    @InjectMocks
    private ContaUseCase contaUseCase;

    private Conta contaTeste;

    @BeforeEach
    public void setUp() {
        contaTeste =
                new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Luiz", "000.000.000-00");
    }

    @Test
    public void deveLancarExceptionCasoAContaJaExista() {
        // Mockito -> Mocks
        // Dado
        when(contaGateway.buscarPorId(contaTeste.getId())).thenReturn(contaTeste);

        // When
        Throwable throwable = Assertions.assertThrows(ContaJaExisteException.class, () -> contaUseCase.criar(contaTeste));

        // Then
        Assertions.assertEquals("A conta ID: "+ contaTeste.getId() + " ja existe", throwable.getMessage());

        verify(contaGateway, never()).salvar(contaTeste);
        verify(emailGateway, never()).send(any());

    }

    @Test
    public void deveSalvarAContaComSucesso() throws Exception {
        // Dado

        //when(contaGateway.buscarPorCpf("000.000.000-00")).thenReturn(null);
        when(contaGateway.salvar(contaTeste)).thenReturn(contaTeste);


        // Quando// Entao
        Conta contaSalva = contaUseCase.criar(contaTeste);

        // Then

        verify(contaGateway).salvar(contaTeste);

        verify(contaGateway, times(1)).salvar(contaTeste);
        verify(emailGateway, times(1)).send(any());
        verify(emailGateway, times(1)).send("000.000.000-00");

        Assertions.assertEquals(contaTeste, contaSalva);

    }

    @Test
    @DisplayName("Criar uma Conta")
    public void deveCriarContaComSucesso() throws Exception {

        Conta contaCriada = contaUseCase.criar(contaTeste);

        Assertions.assertEquals(contaTeste, contaCriada);
    }

    @Test
    @DisplayName("Criar conta com saldo Zero")
    public void deveIniciarContaComSemSaldo() {
        Assertions.assertEquals(BigDecimal.ZERO, contaTeste.getSaldo());
    }

    @Test
    @DisplayName("Depositar corretamente o valor")
    public void deveDepositarCorretamente() throws Exception {
        BigDecimal deposito = new BigDecimal("1000.00");

        when(contaGateway.buscarPorId(1L)).thenReturn(contaTeste);

        BigDecimal saldoAntigo = contaTeste.getSaldo();

        contaUseCase.depositar(contaTeste.getId(), deposito);

        Assertions.assertEquals(deposito.add(saldoAntigo), contaTeste.getSaldo());
    }

    @Test
    @DisplayName("Lancar Exception caso conta não exista")
    public void deveLancarExceptionCasoContaNaoExita() {
        BigDecimal deposito = new BigDecimal("1000.00");

        Throwable throwable = Assertions.assertThrows(ContaNaoExisteException.class,
                () -> contaUseCase.depositar(2L, deposito));

        Assertions.assertEquals("A conta com ID: " + 2L + " não existe", throwable.getMessage());

    }

    @Test
    @DisplayName("Sacar Corretamente")
    public void deveSacarCorretamente() throws Exception {
        contaTeste.setSaldo(BigDecimal.valueOf(1001.50));
        BigDecimal saque = new BigDecimal("1000.00");

        when(contaGateway.buscarPorId(1L)).thenReturn(contaTeste);

        BigDecimal saldoAntigo = contaTeste.getSaldo();

        contaUseCase.sacar(contaTeste.getId(), saque);

        Assertions.assertEquals(saldoAntigo.subtract(saque), contaTeste.getSaldo());

    }

    @Test
    @DisplayName("Lancar Exception caso não tenha saldo")
    public void deveLancarExceptionCasoNaoTenhaSaldo() {
        BigDecimal saque = new BigDecimal("1000.00");

        when(contaGateway.buscarPorId(1L)).thenReturn(contaTeste);

        Throwable throwable = Assertions.assertThrows(ContaSaldoInsuficienteException.class,
                () -> contaUseCase.sacar(contaTeste.getId(), saque));

        Assertions.assertEquals("A conta com ID: " + contaTeste.getId() + " não possui o saldo para saque", throwable.getMessage());

    }

    @Test
    @DisplayName("Transferir entre duas contas")
    public void deveTransferirEntreDuasContas() throws Exception {
        BigDecimal traferencia = new BigDecimal("1.50");
        contaTeste.setSaldo(BigDecimal.valueOf(2.00));
        Conta contaAlvo =
                new Conta(2L, 2L, 3L, BigDecimal.ZERO, "Henrique", "000.000.000-01");

        when(contaGateway.buscarPorId(1L)).thenReturn(contaTeste);
        when(contaGateway.buscarPorId(2L)).thenReturn(contaAlvo);

        BigDecimal saldoAntigoContaOrigem = contaTeste.getSaldo();
        BigDecimal saldoAntigoContaAlvo = contaAlvo.getSaldo();

        Long transation = contaUseCase.transferir(contaTeste.getId(), contaAlvo.getId(), traferencia);

        Assertions.assertAll("Transferencia",
                () -> Assertions.assertEquals(saldoAntigoContaOrigem.subtract(traferencia), contaTeste.getSaldo()),
                () -> Assertions.assertEquals(saldoAntigoContaAlvo.add(traferencia), contaAlvo.getSaldo())
        );

    }

    @Test
    @DisplayName("Atualizar uma conta")
    public void deveAtualizarUmaConta() throws Exception {
        Conta contaAtualizada =
                new Conta(contaTeste.getId(), 2L, 3L, BigDecimal.ZERO, "Henrique", "000.000.000-01");
        when(contaGateway.buscarPorId(contaTeste.getId())).thenReturn(contaTeste);
        when(contaGateway.buscarPorId(contaAtualizada.getId())).thenReturn(contaAtualizada);

        when(contaGateway.atualizar(contaAtualizada)).thenReturn(contaAtualizada);

        Conta contaNova = contaUseCase.atualizar(contaTeste.getId(), contaAtualizada);

        Assertions.assertEquals(contaAtualizada, contaNova);

    }

    @Test
    @DisplayName("Não deve atualizar Conta se o id não corresponder")
    public void deveLancarExceptionSeOIdNaoCorrespondeAoAtualizar() {
        Conta contaAtualizada =
                new Conta(2L, 2L, 3L, BigDecimal.ZERO, "Henrique", "000.000.000-01");
        when(contaGateway.buscarPorId(contaTeste.getId())).thenReturn(contaTeste);

        Throwable throwable = Assertions.assertThrows(ContasDiferentesException.class,
                () -> contaUseCase.atualizar(contaTeste.getId(), contaAtualizada));

        Assertions.assertEquals("As contas são diferentes", throwable.getMessage());

    }
}


