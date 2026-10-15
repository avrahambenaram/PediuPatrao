package com.umc.pediupatrao.audit;

public final class FormatadorArgumentos {

    private FormatadorArgumentos() {
    }

    public static String formatar(Object[] argumentos, String[] nomes) {
        if (argumentos == null || argumentos.length == 0) {
            return null;
        }

        StringBuilder resultado = new StringBuilder("[");
        for (int i = 0; i < argumentos.length; i++) {
            if (i > 0) {
                resultado.append(", ");
            }
            String nome = (nomes != null && i < nomes.length) ? nomes[i] : ("arg" + i);
            resultado.append(nome).append('=').append(valorSeguro(nome, argumentos[i]));
        }
        return resultado.append(']').toString();
    }

    private static String valorSeguro(String nome, Object valor) {
        if (valor == null) {
            return "null";
        }
        if (isSensivel(nome)) {
            return "\"***\"";
        }
        if (valor instanceof CharSequence || valor instanceof Number
                || valor instanceof Boolean || valor instanceof Enum) {
            return String.valueOf(valor);
        }
        return valor.getClass().getSimpleName();
    }

    private static boolean isSensivel(String nome) {
        String normalizado = nome.toLowerCase();
        return normalizado.contains("senha")
                || normalizado.contains("password")
                || normalizado.contains("secret")
                || normalizado.contains("token");
    }
}
