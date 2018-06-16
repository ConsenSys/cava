# Module cava

In the spirit of [Google Guava](https://github.com/google/guava/), Cava is a set of libraries and other tools to aid development of blockchain and other decentralized software in Java and other JVM languages.

# Package net.consensys.cava.bytes

Classes and utilities for working with byte arrays.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-bytes` (`cava-bytes.jar`).

# Package net.consensys.cava.concurrent

Classes and utilities for working with concurrency.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-concurrent` (`cava-concurrent.jar`).

# Package net.consensys.cava.config

A general-purpose library for managing configuration data.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-config` (`cava-config.jar`).

# Package net.consensys.cava.concurrent.coroutines.experimental

Extensions for mapping [AsyncResult][net.consensys.cava.concurrent.AsyncResult] and [AsyncCompletion][net.consensys.cava.concurrent.AsyncCompletion] objects to and from Kotlin coroutines.

# Package net.consensys.cava.crypto

Classes and utilities for working with cryptography.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-crypto` (`cava-crypto.jar`).

# Package net.consensys.cava.crypto.sodium

Classes and utilities for working with the sodium native library.

Classes and utilities in this package provide an interface to the native Sodium crypto library (https://www.libsodium.org/), which must be installed on the same system as the JVM. It will be searched for in common library locations, or its it can be loaded explicitly using [net.consensys.cava.crypto.sodium.Sodium.loadLibrary].

Classes in this package also depend upon the JNR-FFI library being available on the classpath, along with its dependencies. See https://github.com/jnr/jnr-ffi. JNR-FFI can be included using the gradle dependency `com.github.jnr:jnr-ffi`.

# Package net.consensys.cava.eth.domain

Classes and utilities for working with Ethereum domain objects.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-eth-domain` (`cava-eth-domain.jar`).

# Package net.consensys.cava.io

Classes and utilities for handling file and network IO.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-io` (`cava-io.jar`).

# Package net.consensys.cava.io.file

General utilities for working with files and the filesystem.

# Package net.consensys.cava.junit

Utilities for better junit testing.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-junit` (`cava-junit.jar`).

# Package net.consensys.cava.kv

Classes and utilities for working with key/value stores.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-kv` (`cava-kv.jar`).

# Package net.consensys.cava.net

Classes and utilities for working with networking.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-net` (`cava-net.jar`).

# Package net.consensys.cava.net.tls

Utilities for doing fingerprint based TLS certificate checking.

# Package net.consensys.cava.rlp

Recursive Length Prefix (RLP) encoding and decoding.

An implementation of the Ethereum Recursive Length Prefix (RLP) algorithm, as described at https://github.com/ethereum/wiki/wiki/RLP.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-rlp` (`cava-rlp.jar`).

# Package net.consensys.cava.toml

A parser for Tom's Obvious, Minimal Language (TOML).

A parser and semantic checker for Tom's Obvious, Minimal Language (TOML), as described at https://github.com/toml-lang/toml/.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-toml` (cava-toml.jar).

# Package net.consensys.cava.trie

Merkle Trie implementations.

Implementations of the Ethereum Patricia Trie, as described at https://github.com/ethereum/wiki/wiki/Patricia-Tree.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-merkle-trie` (`cava-merkle-trie.jar`).

# Package net.consensys.cava.trie.experimental

Merkle Trie implementations using Kotlin coroutines.

# Package net.consensys.cava.units

Classes and utilities for working with 256 bit integers and Ethereum units.

These classes are included in the standard Cava distribution, or separately when using the gradle dependency `net.consensys.cava:cava-units` (`cava-units.jar`).

# Package net.consensys.cava.units.bigints

Classes and utilities for working with 256 bit integers.

# Package net.consensys.cava.units.ethereum

Classes and utilities for working with Ethereum units.
