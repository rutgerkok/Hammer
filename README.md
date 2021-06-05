# Hammer

[![Build Status](https://img.shields.io/github/workflow/status/rutgerkok/Hammer/dev%20build)](https://github.com/rutgerkok/Hammer/actions?query=workflow%3A%22dev+build%22)

World editor library for Minecraft, written in Java. It is intended to be used by programs that need to interact with Minecraft level files. It has support for both the Java version of Minecraft and the Pocket Edition.

## Compiling
This project uses [Maven](https://maven.apache.org/). Download link and Git clone URLs are in the sidebar.

To compile, run `mvn install`.

## Usage
First, compile a build yourself of Hammer. Then, if you are using Maven, add the following to the `<dependencies>`
section of your `pom.xml` file:

    <dependency>
        <groupId>nl.rutgerkok</groupId>
        <artifactId>hammer</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>

All public methods are documented using JavaDocs. To get started, create an instance of either `AnvilWorld` or
`PocketWorld`. You can then use the various methods on those classes to inspect or modify the level.

## Updating blocks_pc.json
You can extract a new block list from the Minecraft Server as follows:

    java -cp server.jar net.minecraft.data.Main --reports

This will create a file `reports/blocks.json`.
