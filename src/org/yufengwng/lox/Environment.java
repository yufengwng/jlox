package org.yufengwng.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    final Environment enclosing;

    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        this.enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object fetch(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        if (enclosing != null) {
            return enclosing.fetch(name);
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    Object fetchAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    Environment ancestor(int distance) {
        Environment current = this;
        for (int i = 0; i < distance; i++) {
            current = current.enclosing;
        }
        return current;
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }
}
