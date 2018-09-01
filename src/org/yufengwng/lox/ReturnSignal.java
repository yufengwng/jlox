package org.yufengwng.lox;

@SuppressWarnings("serial")
class ReturnSignal extends RuntimeException {
    final Object value;

    ReturnSignal(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
