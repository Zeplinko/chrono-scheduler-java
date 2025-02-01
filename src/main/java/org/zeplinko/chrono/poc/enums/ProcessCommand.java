package org.zeplinko.chrono.poc.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

@Getter
public enum ProcessCommand {
    SETUP_ENVIRONMENT("setup-environment"),
    PROCESS_BATCH_TRIGGER("process-batch-trigger"),
    PROCESS_SINGLE_TRIGGER("process-single-trigger"),
    PROCESS_TRANSACTIONAL_BATCH_TRIGGER("process-transactional-batch-trigger"),
    PROCESS_TRANSACTIONAL_SINGLE_TRIGGER("process-transactional-single-trigger"),
    ;

    private final String commandName;

    ProcessCommand(String commandName) {
        this.commandName = commandName;
    }

    private static final HashMap<String, ProcessCommand> map = new HashMap<>();

    static {
        Arrays.stream(ProcessCommand.values())
                .forEach(command -> map.put(command.getCommandName(), command));
    }

    public static Optional<ProcessCommand> fromCommandName(String commandName) {
        return Optional.ofNullable(map.get(commandName));
    }
}
