2017 Redesign
============


* Use gradle for dependency management

In order to prevent malicious Publishers from poisoning archive  content (deleting/overwriting files),
subscribers  need to be able to access any previously published version of the archive.


Two Networks: Low and High Bandwidth
----------

Uses the Raft consensus algorithm to maintain a global archive state structure across all nodes.

Every node is easily able to keep-in sync with the Global Archive State Data, 
as GASD updates are low bandwidth (estimated usually <2KB, unless including hashes).
Negotiating the actual downloads of these files is more costly.

The whole system therfore has 2 distributed byte-stores;
the first one (The Global Archive State) is a data structure about the second.

Network Architecture Revisited
------

Each network consists of a single Publisher and many Subscribers.
Only the publisher can modify the contents of the archive;
any node can distribute pieces of the archive to other nodes. All nodes hold a full copy of the
archive.

Although the Publisher is the only one who can directly modify the archive,
Subscribers can request to make changes (request to add, delete or modify part of the archive).
These changes must be explicitly allowed by the Publisher.


Publisher Elections
--------

Subscribers can vote at any time to call an election for a new Publisher; 
this is useful in the case that a Publisher goes absent, or becomes compromised somehow.
The vote must have a certain percentage (as yet undecided) in agreement, in order for the election to be held.
This percentage value may discount Subscribers that have not been seen for a while.

During an election, the incumbent Publisher still remains the Publisher.

TODO: mechanism to prevent a compromised Publisher from damaging archive contents, reducing SPOF-ness



Very-High-Level TODO
===

* Find a good transfer library for Java
    - (download resuming)
    - can receive all manner of HTTP error codes without crashing the whole application
* restructure repo tree to fit traditional gradle
* Use a subset of the Git protocol (provided by Eclipse's JGit) to calculate difference between files
* perhaps use a swarm-like network , or some subset of the BitTorrent protocol
* Use the Kademlia DHT (provided by Joshua Kissoon's [java implementation](https://github.com/JoshuaKissoon/Kademlia) ) to persist network details, eg a list of peers


Technologies Used
================

* Raft
* Gradle
* [JGit](http://www.eclipse.org/jgit/)

Influences/Maybe used
-----
* bsdiff
* rsync's transfer protocol
* [Akka](https://en.wikipedia.org/wiki/Akka_%28toolkit%29)
* [bup](https://github.com/bup/bup)
* [uTP](https://github.com/bittorrent/libutp)
* BitTorrent Java implementations:
    - [TTorrent](https://github.com/mpetazzoni/ttorrent)
    - [born again snark](https://github.com/akerigan/born-again-snark)
    - [bitlet](https://github.com/Toilal/bitlet), or [more officially](https://github.com/bitletorg/bitlet)


Glossary
=======

Archive
: A single directory (with zero or more subfiles and subdirectories) being shared using Distribackup.

Archive Network
: The collection of nodes that an archive is being shared between. 
NOT the internet, the local-area network or all computers everywhere using Distribackup.

Global Archive State Data
:Desribes the 'latest' edition' of the archive's state. 
Usually the same as the version that the Publisher has, unless the Publisher is newly elected, 
or its local archive has been somehow damaged.

Node
: Describes either a Publisher or a Subscriber.

Publisher

Subscriber
: A node which contains a full copy of the archive. 
Listens for changes of the Global Archive State, 
and requests the blocks necessary to bring its local archive copy up-to-date

