package ru.diploma.accesscontrol.automaton.classical;

import ru.diploma.accesscontrol.automaton.Automaton;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ClassicalAutomaton<S, X, Y> implements Automaton<S, X, Y> {

    private final Set<X> inputAlphabet;
    private final Set<Y> outputAlphabet;

    private final Set<S> states;
    private final S initialState;

    private final Map<S, Map<X, S>> transitions;
    private final Map<S, Map<X, Y>> outputs;

    private S currentState;

    public ClassicalAutomaton(
            Set<X> inputAlphabet,
            Set<Y> outputAlphabet,

            Set<S> states,
            S initialState,

            Map<S, Map<X, S>> transitions,
            Map<S, Map<X, Y>> outputs
    ) {
        this.inputAlphabet = Set.copyOf(
                Objects.requireNonNull(inputAlphabet, "Input alphabet must not be null")
        );
        this.outputAlphabet = Set.copyOf(
                Objects.requireNonNull(outputAlphabet, "Output alphabet must not be null")
        );

        this.states = Set.copyOf(
                Objects.requireNonNull(states, "States must not be null")
        );
        this.initialState = Objects.requireNonNull(initialState, "Initial state must not be null");

        this.transitions = copyTable(
                Objects.requireNonNull(transitions, "Transitions must not be null")
        );
        this.outputs = copyTable(
                Objects.requireNonNull(outputs, "Outputs must not be null")
        );

        validateDefinition();

        this.currentState = this.initialState;
    }

    @Override
    public S getCurrentState() {
        return currentState;
    }

    @Override
    public Y processInput(X input) {
        Objects.requireNonNull(input, "Input must not be null");

        if (!inputAlphabet.contains(input)) {
            throw new IllegalArgumentException(
                    "Input symbol does not belong to the input alphabet: " + input
            );
        }

        Map<X, S> stateTransitions = transitions.get(currentState);
        Map<X, Y> stateOutputs = outputs.get(currentState);

        if (stateTransitions == null || !stateTransitions.containsKey(input)) {
            throw new IllegalStateException(
                    "Transition is not defined for state " + currentState
                            + " and input " + input
            );
        }

        if (stateOutputs == null || !stateOutputs.containsKey(input)) {
            throw new IllegalStateException(
                    "Output is not defined for state " + currentState
                            + " and input " + input
            );
        }

        S nextState = stateTransitions.get(input);
        Y output = stateOutputs.get(input);

        currentState = nextState;

        return output;
    }

    @Override
    public void reset() {
        currentState = initialState;
    }

    private void validateDefinition() {
        validateBasicProperties();

        validateTable(
                transitions,
                states,
                "Transition table contains an unknown state: ",
                "Transition table contains an unknown input symbol: ",
                "Transition leads to an unknown state: "
        );

        validateTable(
                outputs,
                outputAlphabet,
                "Output table contains an unknown state: ",
                "Output table contains an unknown input symbol: ",
                "Output table contains an unknown output symbol: "
        );

        validateCompleteness();
    }

    private void validateBasicProperties() {
        if (states.isEmpty()) {
            throw new IllegalArgumentException("States must not be empty");
        }

        if (inputAlphabet.isEmpty()) {
            throw new IllegalArgumentException("Input alphabet must not be empty");
        }

        if (outputAlphabet.isEmpty()) {
            throw new IllegalArgumentException("Output alphabet must not be empty");
        }

        if (!states.contains(initialState)) {
            throw new IllegalArgumentException("Initial state must belong to the set of states");
        }
    }

    private <V> void validateTable(
            Map<S, Map<X, V>> table,
            Set<V> allowedValues,
            String invalidStateMessage,
            String invalidInputMessage,
            String invalidValueMessage
    ) {
        for (Map.Entry<S, Map<X, V>> stateEntry : table.entrySet()) {
            S state = stateEntry.getKey();

            if (!states.contains(state)) {
                throw new IllegalArgumentException(invalidStateMessage + state);
            }

            for (Map.Entry<X, V> entry : stateEntry.getValue().entrySet()) {
                X input = entry.getKey();
                V value = entry.getValue();

                if (!inputAlphabet.contains(input)) {
                    throw new IllegalArgumentException(invalidInputMessage + input);
                }

                if (!allowedValues.contains(value)) {
                    throw new IllegalArgumentException(invalidValueMessage + value);
                }
            }
        }
    }

    private void validateCompleteness() {
        for (S state : states) {
            Map<X, S> stateTransitions = transitions.get(state);
            Map<X, Y> stateOutputs = outputs.get(state);

            for (X input : inputAlphabet) {
                if (stateTransitions == null || !stateTransitions.containsKey(input)) {
                    throw new IllegalArgumentException(
                            "Transition is not defined for state " + state
                                    + " and input " + input
                    );
                }

                if (stateOutputs == null || !stateOutputs.containsKey(input)) {
                    throw new IllegalArgumentException(
                            "Output is not defined for state " + state
                                    + " and input " + input
                    );
                }
            }
        }
    }

    private static <S, X, V> Map<S, Map<X, V>> copyTable(
            Map<S, Map<X, V>> table
    ) {
        Map<S, Map<X, V>> copy = new HashMap<>();

        table.forEach((state, row) ->
                copy.put(
                        Objects.requireNonNull(state, "Table state must not be null"),
                        Map.copyOf(
                                Objects.requireNonNull(row, "Table row must not be null")
                        )
                )
        );

        return Map.copyOf(copy);
    }
}
