### jlox

Yet another implementation of the Lox language from the [Crafting
Interpreters][ci-book] book, with tiny differences.

[![Build Status](https://travis-ci.org/yufengwng/jlox.svg?branch=master)](
https://travis-ci.org/yufengwng/jlox)

\# getting started

Pull down the test suite from [the book][book-test-suite]. This only needs to
be done once:

```bash
$ make test_suite
```

Build the interpreter:

```bash
$ make
```

Run the tests:

```bash
$ make test
```

[ci-book]: http://www.craftinginterpreters.com
[book-test-suite]: https://github.com/munificent/craftinginterpreters/tree/master/test
