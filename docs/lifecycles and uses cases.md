#What to do in every major scenario

#Common Scenarios

##Definitions

P
:	the publisher
	
S
:	a new subscriber

##New Subscriber Joins Network

1. New subcriber S connects to publisher P on P's listening port
2. P and S exchange version numbers; if they don't match, disconnect and warn users at P and S
2. P and S exchange UUIDs. THese will later serve as keys, mapping to PeerInfo objects.
3. S sends P serialised Peer object, containing relevant info (see peer struct.txt)
4. P sends S a list of other known peers
5. P sends existing subscribers the Peer file for P
6. S sends recursive list of local files it has, and their checksums
7. P sends serialised FileData objects for any missing or failed-checksum files
8. Some kind of sanity check is performed to make sure everything went OK

##Publisher has updates to push to all subscribers

1. If P has any data about bandwidths of various subscribers, the files are sent to the fastest first, to mazimise the number of peers that can supply the file. If not, a random subscriber is chosen
2. Every time a subscriber finishes downloading a file, it announces to peers it hasn't heard the same announcement from.
3. For every subscriber still downloading the file, requests are made for that file to those who have it.
3. Data is gathered by every peer about speeds during the transfer

##Publisher adds files

same as above



##subscriber says goodbye, then leaves

1. Any transfers this subscriber has in progress are finished
2. 

##publisher says goodbye, then leaves

the publisher may intend for the files to remain static, so a new publisher won't be elected unless (good_reason).
any new peer which joins by contacting a peer will be brought up to date by the network
(version check, file list exchange, sync files with p2p)
once (if) it's implemented, peers may also find the network by DHT.

##subscriber disappears without saying goodbye

##publisher disappears without saying goodbye

##subscriber loses its files

##publisher loses its files

###publisher loses its files while network has differing versions unresolved

situation is resolved by using update numbers (every update or transaction if given a number, so with conflicting updates, the later number will take priority) to bring all peers, including the Publisher, up-to-date

1. Peers exchange information about the latest updates the've seen, until they are all in agreement.
2. updates which conflict are weeded out, and a list of updates which need applying to the network is created
3. Peers which have part or all of this update send what they have to everyone else

##a token, key or password is used by a peer to become the new publisher (P2)

###Another peer becomes the publisher P2 while the old publisher (P1) is propagating updates

##subscriber checks (with publisher or publisher-checked peers) that files are up-to-date

##publisher can't access a subscriber, but others can

##subscriber erroneously edits local copy of files

1. The user is warned that this is a bad idea: any changes will be overwritten. If they want to edit the files, then they should make a copy
2. Procedure for 1 out-of-date peer is followed


###subscriber's local changes are overwritten

Well, we did warn them.

-------

There's a common procedure to follow for some of these events

there are general cases:


#Procedures

##Handshake

##One Out-Of-Date Peer

examples:

* new peer joins network
* peer loses its files (they are deleted, disk corrupt etc)
* subscriber erroneously edits files

###Procedure:
