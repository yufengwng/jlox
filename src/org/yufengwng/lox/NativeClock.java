package org.yufengwng.lox;

import java.time.Instant;
import java.util.List;

class NativeClock implements LoxCallable {
    static final String NAME = "clock";

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) Instant.now().getEpochSecond();
    }

    @Override
    public String toString() {
        return "<native fn>";
    }
}
