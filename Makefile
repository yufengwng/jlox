SRC_DIR := src
TEST_DIR := spec
BUILD_DIR := build

SOURCES := $(wildcard $(SRC_DIR)/org/yufengwng/lox/*.java)
CLASSES := $(addprefix $(BUILD_DIR)/, $(SOURCES:$(SRC_DIR)/%.java=%.class))

NAME := jlox
MAIN := org.yufengwng.lox.Lox
JAVAC_OPTS := -Werror

# Build the interpreter by default.
default: jlox

# Compile classes and build the jar.
jlox: $(BUILD_DIR)/$(NAME).jar

$(BUILD_DIR)/$(NAME).jar: $(CLASSES)
	@ cd $(BUILD_DIR) && jar cfe $(NAME).jar $(MAIN) *
	@ printf 'Built jar: $(BUILD_DIR)/$(NAME).jar\n'

compile: $(CLASSES)

$(BUILD_DIR)/%.class: $(SRC_DIR)/%.java
	@ mkdir -p $(BUILD_DIR)
	javac -cp $(SRC_DIR) -d $(BUILD_DIR) $(JAVAC_OPTS) $<

# Run interpreter against test suite with optional filters.
test: jlox
ifdef FILTERS
	@ python3 test.py $(FILTERS)
else
	@ python3 test.py
endif

# Download test suite from the book and put it in the right place.
test_suite:
	mkdir -p $(TEST_DIR)
	git clone https://github.com/munificent/craftinginterpreters ci
	cp -r ci/test/* $(TEST_DIR)
	rm -rf ci

# Remove the test suite.
test_clean:
	rm -rf $(TEST_DIR)

# Remove build artifacts.
clean:
	rm -rf $(BUILD_DIR)

# Hey make, these targets are not actual files.
.PHONY: compile clean default jlox test test_clean test_suite
