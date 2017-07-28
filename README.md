Weather!
========

This is a [scala play][] app for displaying historical weather data.

### Getting Started

To get started, run `bin/setup`

### Running the app

You can run the app with:

    sbt run

In addition, you can run a compiler to watch for changes with by starting sbt
with `sbt`, then running

    ~compile

The `~` will instruct sbt to watch for any file changs and re-compile for each
change.

### Handling dependencies

Dependencies go in `build.sbt`. If you change them, you'll need to restart your
`run` and `~compile` processes by running `reload` followed by `update` in the
sbt repl.

[scala play]: https://www.playframework.com/
