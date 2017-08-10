#What to do in every major scenario

##Definitions

P
:	the publisher
	
S
:	a new subscriber

Common Scenarios
================

##Peer Starts Program Up (Not First Time) AKA Old Subscriber Joins Network

1. Peer checks integrity of file tree by checking against stored values of size, name, checksum for every file
2.      If there are discrepancies:
            and we're a Subscriber:
                see Subscriber Loses Some Or All of Local Copy.
            or if we're the Publisher:
                see Publisher Has Updates
        else no discrepancies:
            If we're a Subscriber:
                check with Publisher about new peers and file updates
            Or if we're the Publisher:
                check if any new Peers have joined via DHT or otherwise

##New Subscriber Joins Network

1. New subscriber S connects to publisher P on P's listening port
2. P and S exchange version numbers; if they don't match, disconnect and warn users at P and S
2. P and S exchange UUIDs. These will later serve as keys, mapping to PeerInfo objects.
3. S sends P PeerInfo of itself, containing relevant info (see message binary spec)
3. P sends S a File Tree Status object, informing S of the latest versions
4. P sends S a PeerInfo List of the peers it knows about (hopefully all of them)
5. S Greets peers it heard about from P, sends File Requests for files/versions it doesn't have
6. Peers send back list of requested pieces they have/are able to send
6. S chooses best peer to download each file or piece from, based on speed or other metrics
8. Some kind of sanity check is performed to make sure everything went OK

##Publisher Has Updates to Push to All Subscribers / Publisher Adds Files

1. P announces to all known peers the changed file revision numbers (partial fileTree state info)
1. If P has any data about bandwidths of various subscribers, the files are sent to the fastest first, to mazimise the number of peers that can supply the file. If not, a random subscriber is chosen
2. Every time a subscriber finishes downloading a file, it announces to peers it hasn't heard the same announcement from.
3. For every subscriber still downloading the file, requests are made for that file to those who have it.
3. Data is gathered by every peer about speeds during the transfer

##Subscriber Leaves Network Gracefully

1. Exit announcement is made to all known peers
2. Any transfers this subscriber has in progress are finished, or if they'll take too long (which is how long?), cancelled
3. Sockets etc are closed
4. Peers set their record for this Peer as 'offline'

##Subscriber is in the Middle of Downloading a FileID, When a New Version is Announced

##Publisher Leaves Network Gracefully

the publisher may intend for the files to remain static, so a new publisher won't be elected unless (good_reason).
any new peer which joins by contacting a peer will be brought up to date by the network
(version check, file list exchange, sync files with p2p)
once (if) it's implemented, peers may also find the network by DHT.

##Subscriber Disappears Without Saying Goodbye

1. Subscriber fails heartbeats on each peer individually; each per individually marks it as offline in its local peer list.

##Publisher Disappears Without Saying Goodbye

##Subscriber loses some or all of local copy, subscriber erroneously edits local copy

1. The user is warned that this is a bad idea: any changes will be overwritten. If they want to edit the files, then they should make a copy
2. Procedure for 1 out-of-date peer is followed
3. Request more peers and files from all known peers
1. Subscriber announces its loss 

##publisher loses its files

###publisher loses its files while network has differing versions unresolved

situation is resolved by using file revision numbers (every update or transaction if given a number, so with conflicting updates, the later number will take priority) to bring all peers, including the Publisher, up-to-date

1. Peers exchange information about the latest updates the've seen, until they are all in agreement.
2. updates which conflict are weeded out, and a list of updates which need applying to the network is created
3. Peers which have part or all of this update send what they have to everyone else

Complex states with no intuitive solution
=========================================

##a token, key or password is used by a peer to become the new publisher (P2)

###Another peer becomes the publisher P2 while the old publisher (P1) is propagating updates

##subscriber checks (with publisher or publisher-checked peers) that files are up-to-date



##Publisher can't access a subscriber, but others can

* From the perspective of this weakly-connected peer S, the publisher is down
* From the publisher's perspective, the peer has disappeared without saying goodbye (timed out)

-------

There's a common procedure to follow for some of these events

there are general cases:

##Generalisation: One Out-Of-Date Peer

For example:

* new peer joins network
* peer loses its files (they are deleted, disk corrupt etc)
* subscriber erroneously edits files

###Procedure:

1. Out of date Peer P asks the publisher for the current revision number for every file in the tree, along with their checksums, size, and names
1. P asks all known peers for more peers (in case its local peer list has been partially or wholly corrupted), and files (filtering any which aren't as high 
