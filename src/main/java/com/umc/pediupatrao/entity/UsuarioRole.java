package com.umc.pediupatrao.entity;

public enum UsuarioRole {
    USER("USER"),
    MANAGER("MANAGER"),
    ADMIN("ADMIN");

    private String name;

    private UsuarioRole(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public boolean compare(UsuarioRole role) {
        return this.name.equalsIgnoreCase(role.getName());
    }

}
