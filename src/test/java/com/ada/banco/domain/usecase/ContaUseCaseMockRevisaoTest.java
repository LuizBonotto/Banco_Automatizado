package com.ada.banco.domain.usecase;

import com.ada.banco.domain.gateway.ContaGateway;
import com.ada.banco.domain.gateway.EmailGateway;
import com.ada.banco.domain.model.Conta;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

// Vamos utilizar o Mockito Core

@ExtendWith(MockitoExtension.class)
public class ContaUseCaseMockRevisaoTest {
    @Mock
    ContaGateway contaGateway;

    @Mock
    EmailGateway emailGateway;

    @InjectMocks
    ContaUseCase contaUseCase;

    @Test
    public void deveLancarExceptionCasoAContaJaExista() {
        // Given
        Conta conta =
                new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "123456789");

        Mockito.when(contaGateway.buscarPorCpf(conta.getCpf())).thenReturn(conta); // Stub no mockito

        // When Then
        Throwable throwable = Assertions.assertThrows(
                Exception.class,
                () -> contaUseCase.execute(conta)
        );

        Assertions.assertEquals("Usuario ja possui uma conta", throwable.getMessage());

        Mockito.verify(contaGateway, Mockito.times(1)).buscarPorCpf(conta.getCpf());
        Mockito.verify(contaGateway, Mockito.never()).salvar(Mockito.any());
        Mockito.verify(emailGateway, Mockito.never()).send(Mockito.any());
    }

    @Test
    public void deveCriarNovaConta() throws Exception {
        // Given
        Conta conta =
                new Conta(1L, 2L, 3L, BigDecimal.ZERO, "Pedro", "222222222");

        Mockito.when(contaGateway.buscarPorCpf(conta.getCpf())).thenReturn(null);
        Mockito.when(contaGateway.salvar(conta)).thenReturn(conta);
        Mockito.doNothing().when(emailGateway).send(conta.getCpf());

        // When
        Conta novaConta = contaUseCase.execute(conta);

        // Then
        Assertions.assertAll(
                () -> Assertions.assertEquals(1L, novaConta.getId()),
                () -> Assertions.assertEquals("Pedro", novaConta.getTitular())
        );

        Mockito.verify(contaGateway, Mockito.times(1)).salvar(Mockito.any());
        Mockito.verify(contaGateway, Mockito.times(1)).buscarPorCpf(Mockito.any());
        Mockito.verify(emailGateway, Mockito.times(1)).send(Mockito.any());
    }
}