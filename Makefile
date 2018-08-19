SRC_DIR := src
TEST_DIR := spec
BUILD_DIR := build

SOURCES := $(wildcard $(SRC_DIR)/org/yufengwng/lox/*.java)
CLASSES := $(addprefix $(BUILD_DIR)/, $(SOURCES:$(SRC_DIR)/%.java=%.class))

MAIN := org.yufengwng.lox.Lox
JAVAC_OPTS := -Werror


default: jlox

jlox: compile
	@ cd $(BUILD_DIR) && jar cfe jlox.jar $(MAIN) *
	@ printf 'Built jar: $(BUILD_DIR)/jlox.jar\n'

compile: $(CLASSES)

$(BUILD_DIR)/%.class: $(SRC_DIR)/%.java
	@ mkdir -p $(BUILD_DIR)
	javac -d $(BUILD_DIR) $(JAVAC_OPTS) $<

test: jlox
ifdef FILTER
	@ python3 util/test.py $(FILTER)
else
	@ python3 util/test.py
endif

test_load:
	git clone https://github.com/munificent/craftinginterpreters ci
	mv ci/test $(TEST_DIR)
	rm -rf ci

test_clean:
	rm -rf $(TEST_DIR)

clean:
	rm -rf $(BUILD_DIR)


.PHONY: compile clean default jlox test test_clean test_load
