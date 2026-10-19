package ru.diploma.accesscontrol.automaton;

public interface Automaton<S, X, Y> {
    S getCurrentState();

    Y processInput(X inputSymbol);

    void reset();
}
