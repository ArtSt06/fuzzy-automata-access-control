package ru.diploma.accesscontrol.automaton.classical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassicalAutomatonTest {

    private enum State {
        NORMAL,
        PARTIALLY_RESTRICTED,
        RESTRICTED,
        UNKNOWN
    }

    private enum Input {
        NORMAL_ACTIVITY,
        SUSPICIOUS_ACTIVITY
    }

    private enum Output {
        ALLOW,
        PARTIALLY_RESTRICT,
        RESTRICT
    }

    private static final Set<Input> INPUT_ALPHABET =
            Set.of(
                    Input.NORMAL_ACTIVITY,
                    Input.SUSPICIOUS_ACTIVITY
            );

    private static final Set<Output> OUTPUT_ALPHABET =
            Set.of(
                    Output.ALLOW,
                    Output.PARTIALLY_RESTRICT,
                    Output.RESTRICT
            );

    private static final Set<State> STATES =
            Set.of(
                    State.NORMAL,
                    State.PARTIALLY_RESTRICTED,
                    State.RESTRICTED
            );

    private static Map<State, Map<Input, State>> validTransitions() {
        return Map.of(
                State.NORMAL, Map.of(
                        Input.NORMAL_ACTIVITY, State.NORMAL,
                        Input.SUSPICIOUS_ACTIVITY, State.PARTIALLY_RESTRICTED
                ),
                State.PARTIALLY_RESTRICTED, Map.of(
                        Input.NORMAL_ACTIVITY, State.NORMAL,
                        Input.SUSPICIOUS_ACTIVITY, State.RESTRICTED
                ),
                State.RESTRICTED, Map.of(
                        Input.NORMAL_ACTIVITY, State.PARTIALLY_RESTRICTED,
                        Input.SUSPICIOUS_ACTIVITY, State.RESTRICTED
                )
        );
    }

    private static Map<State, Map<Input, Output>> validOutputs() {
        return Map.of(
                State.NORMAL, Map.of(
                        Input.NORMAL_ACTIVITY, Output.ALLOW,
                        Input.SUSPICIOUS_ACTIVITY, Output.PARTIALLY_RESTRICT
                ),
                State.PARTIALLY_RESTRICTED, Map.of(
                        Input.NORMAL_ACTIVITY, Output.ALLOW,
                        Input.SUSPICIOUS_ACTIVITY, Output.RESTRICT
                ),
                State.RESTRICTED, Map.of(
                        Input.NORMAL_ACTIVITY, Output.PARTIALLY_RESTRICT,
                        Input.SUSPICIOUS_ACTIVITY, Output.RESTRICT
                )
        );
    }

    private static ClassicalAutomaton<State, Input, Output> createAutomaton(
            Set<Input> inputAlphabet,
            Set<Output> outputAlphabet,
            Set<State> states,
            State initialState,
            Map<State, Map<Input, State>> transitions,
            Map<State, Map<Input, Output>> outputs
    ) {
        return new ClassicalAutomaton<>(
                inputAlphabet,
                outputAlphabet,
                states,
                initialState,
                transitions,
                outputs
        );
    }

    @Nested
    class ProcessingTests {

        private ClassicalAutomaton<State, Input, Output> automaton;

        @BeforeEach
        void setUp() {
            automaton = createAutomaton(
                    INPUT_ALPHABET,
                    OUTPUT_ALPHABET,
                    STATES,
                    State.NORMAL,
                    validTransitions(),
                    validOutputs()
            );
        }

        @Test
        void shouldStartInInitialState() {
            assertThat(automaton.getCurrentState()).isEqualTo(State.NORMAL);
        }

        @Test
        void shouldResetToInitialState() {
            automaton.processInput(Input.SUSPICIOUS_ACTIVITY);
            automaton.processInput(Input.SUSPICIOUS_ACTIVITY);

            automaton.reset();

            assertThat(automaton.getCurrentState()).isEqualTo(State.NORMAL);
        }

        @Test
        void shouldProcessSequenceOfInputs() {
            Output firstOutput = automaton.processInput(Input.SUSPICIOUS_ACTIVITY);
            Output secondOutput = automaton.processInput(Input.SUSPICIOUS_ACTIVITY);

            assertThat(firstOutput).isEqualTo(Output.PARTIALLY_RESTRICT);
            assertThat(secondOutput).isEqualTo(Output.RESTRICT);
            assertThat(automaton.getCurrentState()).isEqualTo(State.RESTRICTED);
        }

        @Test
        void shouldReturnOutputAndChangeStateAfterProcessingInput() {
            Output output = automaton.processInput(Input.SUSPICIOUS_ACTIVITY);

            assertThat(output).isEqualTo(Output.PARTIALLY_RESTRICT);
            assertThat(automaton.getCurrentState()).isEqualTo(State.PARTIALLY_RESTRICTED);
        }
    }

    @Nested
    class ValidationTests {

        @Test
        void shouldRejectNullInputWithoutChangingState() {
            ClassicalAutomaton<State, Input, Output> automaton =
                    createAutomaton(
                            INPUT_ALPHABET,
                            OUTPUT_ALPHABET,
                            STATES,
                            State.NORMAL,
                            validTransitions(),
                            validOutputs()
                    );

            assertThrows(
                    NullPointerException.class,
                    () -> automaton.processInput(null)
            );
            assertThat(automaton.getCurrentState()).isEqualTo(State.NORMAL);
        }

        @Test
        void shouldRejectInputOutsideAlphabetWithoutChangingState() {
            ClassicalAutomaton<State, Input, Output> automaton =
                    createAutomaton(
                            Set.of(Input.NORMAL_ACTIVITY),
                            OUTPUT_ALPHABET,
                            STATES,
                            State.NORMAL,
                            Map.of(
                                    State.NORMAL, Map.of(
                                            Input.NORMAL_ACTIVITY, State.NORMAL
                                    ),
                                    State.PARTIALLY_RESTRICTED, Map.of(
                                            Input.NORMAL_ACTIVITY, State.NORMAL
                                    ),
                                    State.RESTRICTED, Map.of(
                                            Input.NORMAL_ACTIVITY, State.PARTIALLY_RESTRICTED
                                    )
                            ),
                            Map.of(
                                    State.NORMAL, Map.of(
                                            Input.NORMAL_ACTIVITY, Output.ALLOW
                                    ),
                                    State.PARTIALLY_RESTRICTED, Map.of(
                                            Input.NORMAL_ACTIVITY, Output.ALLOW
                                    ),
                                    State.RESTRICTED, Map.of(
                                            Input.NORMAL_ACTIVITY, Output.PARTIALLY_RESTRICT
                                    )
                            )
                    );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> automaton.processInput(Input.SUSPICIOUS_ACTIVITY)
            );
            assertThat(automaton.getCurrentState()).isEqualTo(State.NORMAL);
        }

        @Test
        void shouldRejectEmptyStates() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> createAutomaton(
                            INPUT_ALPHABET,
                            OUTPUT_ALPHABET,
                            Set.of(),
                            State.NORMAL,
                            Map.of(),
                            Map.of()
                    )
            );
        }

        @Test
        void shouldRejectInitialStateOutsideStates() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> createAutomaton(
                            INPUT_ALPHABET,
                            OUTPUT_ALPHABET,
                            STATES,
                            State.UNKNOWN,
                            Map.of(),
                            Map.of()
                    )
            );
        }

        @Test
        void shouldRejectIncompleteTransitions() {
            Map<State, Map<Input, State>> transitions =
                    Map.of(
                            State.NORMAL, Map.of(
                                    Input.NORMAL_ACTIVITY, State.NORMAL
                            ),
                            State.PARTIALLY_RESTRICTED, Map.of(
                                    Input.NORMAL_ACTIVITY, State.NORMAL,
                                    Input.SUSPICIOUS_ACTIVITY, State.RESTRICTED
                            ),
                            State.RESTRICTED, Map.of(
                                    Input.NORMAL_ACTIVITY, State.PARTIALLY_RESTRICTED,
                                    Input.SUSPICIOUS_ACTIVITY, State.RESTRICTED
                            )
                    );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> createAutomaton(
                            INPUT_ALPHABET,
                            OUTPUT_ALPHABET,
                            STATES,
                            State.NORMAL,
                            transitions,
                            validOutputs()
                    )
            );
        }

        @Test
        void shouldRejectIncompleteOutputs() {
            Map<State, Map<Input, Output>> outputs =
                    Map.of(
                            State.NORMAL, Map.of(
                                    Input.NORMAL_ACTIVITY, Output.ALLOW,
                                    Input.SUSPICIOUS_ACTIVITY, Output.PARTIALLY_RESTRICT
                            ),
                            State.PARTIALLY_RESTRICTED, Map.of(
                                    Input.NORMAL_ACTIVITY, Output.ALLOW,
                                    Input.SUSPICIOUS_ACTIVITY, Output.RESTRICT
                            ),
                            State.RESTRICTED, Map.of(
                                    Input.NORMAL_ACTIVITY, Output.PARTIALLY_RESTRICT
                            )
                    );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> createAutomaton(
                            INPUT_ALPHABET,
                            OUTPUT_ALPHABET,
                            STATES,
                            State.NORMAL,
                            validTransitions(),
                            outputs
                    )
            );
        }

        @Test
        void shouldRejectTransitionToStateOutsideStates() {
            Map<State, Map<Input, State>> transitions =
                    Map.of(
                            State.NORMAL, Map.of(
                                    Input.NORMAL_ACTIVITY, State.NORMAL,
                                    Input.SUSPICIOUS_ACTIVITY, State.UNKNOWN
                            ),
                            State.PARTIALLY_RESTRICTED, Map.of(
                                    Input.NORMAL_ACTIVITY, State.NORMAL,
                                    Input.SUSPICIOUS_ACTIVITY, State.RESTRICTED
                            ),
                            State.RESTRICTED, Map.of(
                                    Input.NORMAL_ACTIVITY, State.PARTIALLY_RESTRICTED,
                                    Input.SUSPICIOUS_ACTIVITY, State.RESTRICTED
                            )
                    );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> createAutomaton(
                            INPUT_ALPHABET,
                            OUTPUT_ALPHABET,
                            STATES,
                            State.NORMAL,
                            transitions,
                            validOutputs()
                    )
            );
        }
    }

    @Nested
    class ImmutabilityTests {

        @Test
        void shouldNotBeAffectedByChangesToOriginalTransitionTable() {
            Map<State, Map<Input, State>> transitions =
                    mutableValidTransitions();

            ClassicalAutomaton<State, Input, Output> automaton =
                    createAutomaton(
                            INPUT_ALPHABET,
                            OUTPUT_ALPHABET,
                            STATES,
                            State.NORMAL,
                            transitions,
                            validOutputs()
                    );

            transitions.get(State.NORMAL).put(Input.NORMAL_ACTIVITY, State.UNKNOWN);

            automaton.processInput(Input.NORMAL_ACTIVITY);

            assertThat(automaton.getCurrentState()).isEqualTo(State.NORMAL);
        }

        private Map<State, Map<Input, State>> mutableValidTransitions() {
            Map<State, Map<Input, State>> transitions =
                    new HashMap<>();

            transitions.put(
                    State.NORMAL,
                    new HashMap<>(Map.of(
                            Input.NORMAL_ACTIVITY, State.NORMAL,
                            Input.SUSPICIOUS_ACTIVITY, State.PARTIALLY_RESTRICTED
                    ))
            );

            transitions.put(
                    State.PARTIALLY_RESTRICTED,
                    new HashMap<>(Map.of(
                            Input.NORMAL_ACTIVITY, State.NORMAL,
                            Input.SUSPICIOUS_ACTIVITY, State.RESTRICTED
                    ))
            );

            transitions.put(
                    State.RESTRICTED,
                    new HashMap<>(Map.of(
                            Input.NORMAL_ACTIVITY, State.PARTIALLY_RESTRICTED,
                            Input.SUSPICIOUS_ACTIVITY, State.RESTRICTED
                    ))
            );

            return transitions;
        }
    }
}
