package com.ada.banco.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class Conta {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private Long agencia;
    private Long digito;
    private BigDecimal saldo;

    // Usuario / Titular
    private String titular;
    private String cpf;

    public Conta() {
    }

    public Conta(Long id, Long agencia, Long digito, BigDecimal saldo, String titular, String cpf) {
        this.id = id;
        this.agencia = agencia;
        this.digito = digito;
        this.saldo = saldo;
        this.titular = titular;
        this.cpf = cpf;
    }

    public Conta(Long agencia, Long digito, BigDecimal saldo, String titular, String cpf) {
        this.agencia = agencia;
        this.digito = digito;
        this.saldo = saldo;
        this.titular = titular;
        this.cpf = cpf;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAgencia() {
        return agencia;
    }

    public void setAgencia(Long agencia) {
        this.agencia = agencia;
    }

    public Long getDigito() {
        return digito;
    }

    public void setDigito(Long digito) {
        this.digito = digito;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
