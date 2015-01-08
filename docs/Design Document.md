#Design Document for Distribox

##Features and General Design Intentions

Remember this is NOT a Source Code Management solution

main use case is large, rarely-changing binary files such as images and videos
optimised for networks with low common uptime

I've been working all along under the (secret) assumption that my network will be resistant to attack
and takedown requests, ie there's no central "hub" (as defined below) without which the network fails.

However making an easy photo/video sharing ad backup solution doesn't require this robustness.

How do we reconcile these two viewpoints satisfiably -- ie without compromising goals of either?

You could have selection of a new central hub if the incumbent one "fails" - becomes unavailable, or notifies spokes of withdrawal, etc...

We are not currently catering to the security crowd - our primary goal is not to worry about malicious parties (like torrent poisoning) -- for now.
It's to provide an easy-to-understand service for lay users to share and backup files without investing in corporate storage or hardware (which requires a tech education)
However if things end up going down that route in future, then so be it -- Perfect Forward Secrecy for all! etc


#One-Way Sync: the Central Publisher
Consider adopting 1-way synchronisation with a star topology, with one node in the middle pushing updates to interested subscribed mirrors
(who share update pieces amongst themselves to speed things up & reduce publisher's load)

* updates to mirrors, which cannot modify the files - "spokes in a wheel"
* what about if we share photos with a phone, which we then lose? what happens to folder ownership?
* using top-down star topology doesn't rule out distributed chunk trading amongst receiving peers

In our case, each folder will be its own spoke, representing a collection of data (photos from one 
day out) belonging to one participant.
"Pushing" this spoke's updates is sharing this data collection with viewers.

Downside is, the system isn't as decentralised; we rely on a central mirror

##ALERT: Rsync is complex (and possibly unnecessary)
if we are only supporting 1-way sync, then (ignoring user intervention for now) we always know the 
diff between remote copies and the new copies in the spoke, as the remote files are the same as the spoke's file pre-change
We don't need to solve rsync's diff between remote files problem, we can just use a normal diff algo

##One-Way At A Time Sync
publisher authority (ie which mirror can update the network) can be controlled with an arbitrary token, eg RSA key

* allowing us to move the publishing mirror dynamically
* which moves us back into the use-case area of 1 person, many machines like dropbox!
* something like ssh private key? possibly a password? should be sth. intangible, portable
* this has the bonus of being conceptually easy for lay users to understand

when there's a new publisher,

* each node can choose to join the new version of this swarm, or stick with the old, static one
* Subscribers sticking with the old Publisher can serve common files (while there are files in common) to other subscribers in both swarms 

#Bt-Sync
Bit-torrent sync is now our main competitor --
we're more or less building an opensource clone -- except I don't plan to support two-way syncing.

Without file differencing, their protocol is inefficient for updates to large files

With this star topology, we are enabling the possibility of a decentralising publishing platform - self-hosted web pages with built-in scalability

##Handling Incoming Messages

* a queue of enums which each represent incoming events 
* a single event handling thread deals with each event in order
* each event enum will need info about it attached, like WHICH peer announced its exit
* connection operators receiving bytes (which they then decode into messages) will add to the queue


each Peer has its own UUID, generated during first run and stored on-disk
------

* Will not require an always-on machine - will reliably and efficiently (but not necessarily quickly) sync mirrors.
* Not intended for real-time sync
* The system will work best with infrequent updates among sometimes-on machines that form a network with common uptime
	(ie each mirror is on at the same time as at least one other machine, to pass updates)
    - a Raspberry Pi could bolster this and speed up full network sync, but should not be necessary.

As a bare minimum interface (all of which will be optional, and will default to sane values) users will be given the following choices:-

* a folder they wish to keep synced
    - Creating a new network with this folder, or adding it to an existing network
        * Joining an existing network will require some kind of network identity key as a minimum. See Aims and Objectives.
* a maximum size limit of data to sync (optionally no limit)

Gathering user input may be implemented using some kind of rudimentary GUI, such as a wizard.

#Scenarios (Use Cases)

* Setting up a new backup network
* Adding new mirrors to an existing network
* Propagating updates from one mirror to the whole network
    - Deletion of existing files
    - Changes to an existing file
    - Addition of new files/folders
    - Adding new data (en masse) to network
* Provision for dependencies -- ie you can't delete the film without deleting the subtitles as well?
* Prioritise updates to and from low uptime mirrors - ie granny's laptop she uses once a week
* Organise p2p traffic based on measured speeds of mirrors

#Structure of Functional Components

##Socket Connections Manager
* Manage open connections
* Handles all low-level network housekeeping

##Rsync Diff Chunker

* Calculates rsync diff sums against each other known online mirror (provided by Peer Manager)
 * tightly coupled with Peer Manager, as different diffchunks may need to be sent out to each mirror, as each mirror may itself be unsynced to every other (worst case). 

##Peer Manager

In the star trek metaphor: the comms officer between this and other ships, AKA external comms/foreign minister

manages list of:

* Seen mirrors, their MAC addresses and current IP address (and possibly another unique identifier)
* Last seen and average common uptime
* Also sends heartbeats to all known online mirrors

##Filesystem Watcher

* Uses inotify on linux & android
* Triggers events which start the sync process


##DHT Manager

* Talks to and provides a portion of the DHT
* Interfaces between other components and the distributed data
 * maintains a local copy of the table? probably has to
	* Freshness rating?

##Functional Components (as described in FYPP):

* Mirror discovery - given that mirrors have dynamic IP addresses and <100% uptime?
    - use DHTs eg Kademlia
        - is DHT mirror discovery too difficult to implement in the given time?
* Identifying/authenticating mirrors - preventing man-in the middle attacks
    - use SSH authentication model
* Implementing p2p file transfer - a la bit torrent
    - downloading a file from multiple peers at once by splitting into file pieces
        - will be heavily related to rsync-derived algorithm 
* Sending file changes only
    - look at rsync algorithm, remote differential compression, diff, bsdiff, chromium's Courgette[^courgette]
    - Process edge-case file updates efficiently - eg renaming a file, swapping file piece order
* Detecting changes rapidly
    - using Inotify? (linux systems only)
* merging conflicting versions - just do what DropBox does and rename conflicts /create copies
* Working out which files and versions are most up to date -  propagating most up-to-date file
* Obtaining an open outgoing port - UPnP?

For each update between a sender-receiver pair:

Collect multiple rapid-fire updates into one network transaction (minimise network overhead due to tiny updates)

* Diffchecking phase
* Diffchunk sending phase
* Reassemble new version from diffchunks + original
* Sync check

	Upon startup:
	mirror discovers others using DHT as overlay network

	Upon a file change:
	Inotify alerts of file change
	
	if a file has been deleted:
		if file is confirmed to exist on other mirrors
		and is identical on other mirrors,
			then a deletion announcement is sent through overlay network to other mirrors

	if a file is added:
		rsync is used to find which mirrors don't have the file
		file is sent via p2p implementation to any mirrors which don't have the file (or a piece of it)
		
	if a file is updated:
		rsync diffs these changes against copies on other mirrors
		differing chunks are sent via p2p implementation

Intelligent handling of renamed files will depend on inotify (and the java binding library), and whether it can detect file moves.


We're using computing time to reduce bandwidth usage
It may be useful to store state (or diffs) of filesystem -- the old state of the files, to compare against changed files, and filter for small changes and mind-changes

Next design phase: go through each component and specify external methods  (use UML if helpful)

#Optional Extras (if have time)
* Android Client
* Windows Client
* Merging conflicts
* Edit locks

#Further Work (Not Implementing in this Dev Cycle)

* Web-visible files - hosted web pages and web links to files
* Web interface
* Version control for managed files
    - Likely to be implemented using Git
* GUI
    - stick to a daemon (and setup wizard?) with a config file for now
* Fine-grained file subscriptions
    - mirrors only host files they're interested in
    - Risk of low availability for undesirable/unpopular files - bad
* Merging conflicting file updates automatically (modification times, diffs, git?)
* Multiple networks, multiple folders in each
    * This could get complex for the user very quickly
* Encryption [^PGP] - this can be broken into 3 sub-areas; PGP is a good candidate for providing any of these.
	All of these, though especially the first two, are highly desirable, and may become core goals during the course of the project.
	- Transfer encryption - preventing digital wire taps from snooping on data being transferred is extremely important, especially given the use of a public network (the internet) as the transmission medium.
	- Authentication - prevent attackers from joining the network without permission.
	- Local storage encryption - Focus on interfacing with existing filesystem encryption technology here
* Human-readable anoynmous peer IDs
    - map IP address of a peer to a country,
    - then hash the peer's UUID to choose a name from a list specific for that country
