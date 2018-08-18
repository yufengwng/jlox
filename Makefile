SRC_DIR := src
BUILD_DIR := build

SOURCES := $(wildcard $(SRC_DIR)/org/yufengwng/lox/*.java)
CLASSES := $(addprefix $(BUILD_DIR)/, $(SOURCES:$(SRC_DIR)/%.java=%.class))

EXE := jlox
MAIN := org.yufengwng.lox.Lox

JAVAC_OPTS := -Werror


default: jlox

jlox: compile jar exe

compile: $(CLASSES)

$(BUILD_DIR)/%.class: $(SRC_DIR)/%.java
	@ mkdir -p $(BUILD_DIR)
	javac -d $(BUILD_DIR) $(JAVAC_OPTS) $<

jar:
	@ cd $(BUILD_DIR) && jar cfe $(EXE).jar $(MAIN) *
	@ printf 'Built jar: $(BUILD_DIR)/$(EXE).jar\n'

exe:
	@ printf '#!/usr/bin/env bash\njava -jar $(BUILD_DIR)/$(EXE).jar "$$@"\n' > $(EXE)
	@ chmod +x $(EXE)
	@ printf 'Generated executable script: $(EXE)\n'

clean:
	rm -rf $(BUILD_DIR) $(EXE)


.PHONY: compile clean default exe jar jlox
