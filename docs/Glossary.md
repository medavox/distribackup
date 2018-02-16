Glossary of Terms
=======

Archive
: A single directory (with zero or more subfiles and subdirectories) being shared using Distribackup.

Archive Network
: The collection of nodes that an archive is being shared between. 
NOT the internet, the local-area network or all computers everywhere using Distribackup.

Edition
:Analogous to a commit in git. Represents a version of the repository.

Global Archive State Data
:Describes the 'latest edition' of the archive's state. 
Usually the same as the version that the Publisher has, unless the Publisher is newly elected, 
or its local archive has been somehow damaged.

HashChunk


Node
: Describes either a Publisher or a Subscriber.

Publisher

Subscriber
: A node which contains a full copy of the archive. 
Listens for changes of the Global Archive State, 
and requests the blocks necessary to bring its local archive copy up-to-date

