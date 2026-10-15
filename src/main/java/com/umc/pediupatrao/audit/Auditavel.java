package com.umc.pediupatrao.audit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.umc.pediupatrao.entity.AcaoAuditoria;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditavel {

    String acao() default "";

    String descricao() default "";

    String entidade() default "";

    AcaoAuditoria tipo() default AcaoAuditoria.OUTRO;

    boolean registrarArgumentos() default false;
}
