Abstract
========

"Engineering is the art of compromise: you work with what is available to you to make the best solution to the problem."

----------------

~~You have 150 words - use them! (wisely)~~

Distribackup shall be a software system to back up and sync distributed copies of important data as efficiently as possible. The files will remain in possession of the user, providing extremely fault-tolerant without relying on any vulnerable external storage services.

distributed backup for the home user is an important area because ...

This report presents a proposal for a project in distributed backup solutions, with particular emphasis on 

* summary of aims ("The aims of this project are ....")
* summary of content (“The project will involve two phases ...")
* expected outcomes ("Results from the proposed project will be ...")

---------



Portions of the functionality come from existing solutions and algorithms:

1. Bit Torrent
2. Rsync
3. Kademlia
4. Inotify

network comms / port binding
node discovery
file differencing
p2p piece sending
file update watching

The project's main challenge will be to draw source code (or at least the algorithms used) from each of these open-source projects and put them together into a unified utility.

The program will aim to minimise setup and configuration; there is an extremely high barrier-to-entry in terms of technical knowledge required for many existing backup and  solutions, such as Git. This is by design in Git and Git-annex, so rather than go back on their intended design goals with a fork, this project will create something new with a minimal configuration requirement at its heart.

The experimental implementation (which is the focus of this project) will not be fully as user-friendly as the intended final version would be; this is to cut down time spent on tasks secondary to the core technical challenges, such as interface prototyping. If this project were to be taken further, then a full user interface (both graphical and commandline) could be implemented.


The program will aim to minimise setup and configuration; one of the reasons this project is a worthwhile endeavour is because of the extremely high barrier-to-entry in terms of technical knowledge required for many existing solutions, such as Git. This is by design in these other projects, so rather than compromise their intended design goals with a fork, I have decided to create something new with minimal configuration at its heart. The first, experimental implementation will not be fully as user-friendly as the final version is intended to be; this is to cut down time spent on tasks secondary to the core technical challenges, such as interface prototyping.

a raspi or other always-on computer with attached storage is connected to the internet: contains important backed up data

another machine elsehwere contains a full copy of the data

The copies are kept updated over the internet, and new copies can be set up by downloading from all existing nodes, thus reducing the setup time

network rtansfers will bee made as efficient as possible, using rsync algorithm and peer-to-peer file transfers.

Inter-node communication will be made as simple as possible to set up, using DHT (Kademlia) to connect peers to each other without any user intervention.

Can be thought of as a kind RAID over internet, with transfer optimisations 

Using Distributed Hash Tables may prove to be too complex for scope of this project; in that case, an alternative method for nodes to find each other will be devised. However, the alternative method will likely require more user intervention.


Introduction
============

* Set the scene
* Describe the proposed system and give an overview of its functionality
* Describe the main features of the project
* End with a section by section overview of the rest of the report

A software system which allows lay people to set up extremely robust backups of important documents that cann't be replaced
these docs have been digitised, and now they are often lost

Handing it over to a compnay which stores it on third-party server farms. These are vulnerable because they can not only suffer the same physical damage (floods, earthquakes, fires) as all data, but may also be actively targeted by the national government (FBI siezed all megaupload files during their raid, including files of legitimate users who had paid for storage)

Governments sieze data wholesale, stealing indiscriminately in the name of anti-copyright theft. This puts personal information stored in large centralised datacentres at risk.

The storage is run by a company, whose primary motivation is to make a profit; the agreement you sign may allow them to mine your data for information useful to them (eg using your photos as marketing material).



The digital age has meant that all family data and memories are stored on computer first, and hardcopies are made later. The lay user has little or no idea about where their data is stored, whether it is safe and whether their rights to it have been compromised by a heavy-handed TOS.

Whenever a particular piece of technology fails (mobile phone is lost, old PC breaks down or is replaced), often the user unwittingly throws away or loses many years of irreplaceable data such as family videos, photos, and even genealogy research.

This project aims to reduce the risk for families (and other groups with an interest in long-term data preservation) of data loss through lack of sufficient knowledge and funds for proper backup solutions, whilst allowing the user to remain free of subscriptions or agreements with any company that has ulterior motives for offering the service.

Existing backup solutions are offered commercially, and can come with very restrictive space limits, or require that the user allow the data to be used for marketing purposes (seeing photos from a birthday party 10 years ago being used in an advert).

Background
==========

The only people who have your files are people on your peer-access list

* A key section in setting the scene for the project
* Should contain a significant literature survey of related systems/ approaches
  - Key issue of scope
  - e.g. networking -> P2P -> file sharing software
  - May be sub-divided
  - e.g. methodologies vs. existing systems
* Should say something!
  - What are the implications for my project?


Technical expertise should not be a requirement for preserving family history

  
The Need for More Robust Backup for the Lay Person
--------------------------------------------------

Lost laptops and pen drives cause frequent important data loss
'Old' PCs being replaced cause families to throw away accumulated personal data
  - Often an assumption that all data is stored non-locally, ie on the internet
  - Lack of of understanding causes personal data loss upon hardware failure

Evaluation of Existing Solutions
--------------------------------

###Dropbox

Dropbox is useful for keeping files synced across multiple machines; but by using their service, Dropbox can stake a claim to your data. 

* You have no idea what they're doing with it
* You can't be sure they've really deleted something (hint: they haven't)
* If you made a commercially interesting piece of software (something that could make money), what would happen to your rights to the IP?
* The maximum file and storage sizes are (understandably, considering they personally store copies of everything you sync) very low, and this restricts its usefulness.

Dropbox can encumber your files with ownership and legal issues.
 
###Bit-Torrent 
collections cannot be updated after creation
unsecure by design (IP addresses and ISP hostnames are broadcast and used as identities)

###Git
git updates need to be synced by user, using commandline or seperate GUI
- Not automatic
- Very hands-on; cannot fire-and-forget

* Git is not visible to end-users: its intended audience are developers, and its steep learning curve (tens of commands to learn each with its own single-character options; a new mental model for manipulating 'staged' files which are 'indexed') would prevent its adoption by non-programmer power users
* Existing GUIs for git are unfinished, non-free, buggy or as confusing as the commandline interface, with none of the portability.
* Git is much more complex than is necessary for this task

###Commercial Solutions
commercial solutions (such Amazon S3) entrust critical private data with large hosting
* can be damaged
* hacked
* no guarantee of loss of owndership (all TOSes can change at any time)
 
None of the existing solutions are easy enough for all families to use


The Proposed Project
====================

The project will be focussed on achieving all the implementation goals first, then polishing later - or as future work

The software system will be comprised of the following components:-

* TCP/IP send and receive, ports binding
* file differencing
* file change event handler
* node discovery
* peer-to-peer transfer

The project will concentrate on fitting these disparate pieces together.

It shall use existing technology to provide this:-

* [Bit-torrent](http://www.bittorrent.org/beps/bep_0003.html): good at reducing bandwidth costs of a one-to-many download, by trading pieces of files among peers. Means the original syncing file only needs to be uploaded once by the originator, (like Dropbox, to its central servers).
* [rsync](http://rsync.samba.org/): robust file-transfer application, which uses differential transfer: only transfers new files, (or even parts of files, see differential transfer) that have changed. This would vastly improve sync times and 
* [Git](http://git-scm.com/): an existing solution for peers to exchange versions of files; I don't THINK I want version history in this program, but it could be added as an optional feature later.
* inotify
* [PGP](http://www.cryptography.org/getpgp.htm): some kind of encryption will be needed to prevent unauthorised peers from getting copies of your files. An authentication system would be good, possibly along with encrypted packet transmission, and/or encrypted storage. [Pretty Good Privacy][PGP] is a good candidate for any of these.

Possible Use Cases:
-------------------

wedding photo hosting/upload
family data archive (syncing geographically distant copies to maximise data resilience)
working documents for geographically distant Dev teams

Platform Support
----------------

I have chosen Linux as the platform to write my test implementation on, as its
filesystem update, network communication and  libraries are well-documented in the open source community,
and as linux iteself already runs on many hardware platforms, it will be relatively simple to port (ie to Raspberry Pi's armhf).



I have considered writing a web interface for the service, however I have decided
that this is too far outside the domain of the core project, and would also create too much extra work in the limited time.


I have chosen to write the project in Java. This is because I am most comfortable with Java for high-level networked applications; its popularity ensures there are plenty of examples and help for questions I may have. There are implementations of the bit-torrent protocol for me to compare. 

C was my other choice, however apart from being less familiar with it, I also found its lack of package encapsulation or object-oriented code to lack finesse. Clear structuring was not something I wished my code to lack. 

Java is also likely to run well on the target platforms, with minimal architecture-specific code (there will be some for Android, if I choose to support it in this initial implementation). Java has been designed to run on all my target platforms, which the work of maintaining seperate ports for each.

I am aware of the slight performance hit in using Java's Virtual Machine; I don't believe this will be an issue, even on the Raspberry Pi. My final reservation about using Java was initially its lack of support for the Raspberry Pi, whcih is the main platform I had in mind when I envisioned this project. This has recently been remedied, and there is now an Oracle-supplied Java Development Kit available in the Raspbian softwqare repositories, which uses the hardware-float feature, making it much faster than previous efforts.


I intend to write this for Windows, Linux and Android, which will affect library choices.
The backend can probably be written once (pending disk and network IO libraries); just the external UI will need separate versions for each platform.


raspberry pi
web interface
linux
(windows = later)


Aims and objectives
-------------------

* State clearly the overall aim of project
* State more specific objectives as bullet points
  - Subdivide into categories if necessary
* Are they measurable?
* Are they achievable?
* Do I know when I should stop?

---------------------------

User Interface
--------------

Users will choose:-
* a folder they wish to keep synced,
* and ONE UNIFIED list of peers that sync to this folder.
* a maximum size limit of data to sync (optionally no limit)

Theoretically users could choose different peer lists for different folders, but this could get complex for the user (and possibly for implementation) very quickly, so I will probably only let users sync 1 folder to 1 list of peers for now.

The Act of syncing a file
-------------------------

At the start, peers will have to get all the files from a filled folder,

later, peers will have to receive updates

A peer with new files will announce updates to all peers (push?)


~~To build a self-hosted cloud storage platform designed around lay person's archival data.~~

Methodology
-----------

* Software engineering approach
  - e.g. Use of rapid prototyping
* Planned evaluation strategy
  - Testing methods to be used and why
  - User evaluation? Performance evaluation?
  - Qualitative evaluation? Quantitative evaluation?

EVALUATION???
=============

---------------------

* Needs to be discussed with your supervisor
* Has to be tailored to the particular project and to the aims and objectives
* What aspects of your system can be evaluated?
  - Quantitative
    - Performance of the system
    - Software metrics, e.g. related to reusability
  - Qualitative
    - User experience
* What is realistic given the timescales?

* Think about the minimum number of users that will be required in order to produce ‘confident’ evaluation results.
* What ‘type’ of person is your system designed for – if ‘general’ then you need to plan to have ‘general’ users testing the system (not only fellow computer scientists).



•  Is a task based evaluation suitable?
    –  Use your system to perform the following steps.
      1. Send a picture to the display
      2. Receive a picture from the display
      3. etc. etc.
    –  Allows performance measures to be taken (if
       appropriate)
      - Time to complete tasks
      - Percentage tasks successfully completed etc.
•  Alternatively, is it reasonable to simply give the
   system to the user for a reasonable time for
   them to ‘play’ with it?

-----------------------------

Programme of Work
=================

-----------

* Start with overall timescale for the project
* Each task should have defined and measurable outcomes
  - sub-goals
  - short description
  - duration
  - milestones and deliverables
* Overall schedule:
  - use of appropriate diagrammatic notation
  
----------

Assuming that the deadline for this project is roughly the same as my individual research project last year, then I have 6 months to complete this project. Because this project is entirely self-derived, ie I came up with it without any outside advice on scale (and therefore it might prove to be too much work), I have attempted to keep the desired goals to a bare minimum, in order to give myself the best chance of finishing the core functionality, which is as follows:-

Stage | Time Given
------|-----------
Research | 1 month
early experimentation | 1 month

testing and cleanup | 1 month

Stage 1

###Major Technical Challenges:

This is a list of the core functionality that needs to be implemented

* Node discovery - given that nodes have dynamically assigned IP addresses and <100% uptime?
  - use DHTs eg Kademlia
  - is DHT node discovery too difficult to implement in the given time?
* Identifying/authenticating nodes - preventing man-in the middle attacks
  - use SSH authentication model
* Implementing p2p file transfer - à la bit torrent
  - downloading a file from multiple peers at once by splitting into file pieces
    - will be heavily related to rsync-derived algorithm 
* Sending file changes only
  - look at rsync algorithm, remote differential compression, diff, bsdiff, chromium's [Courgette](http://dev.chromium.org/developers/design-documents/software-updates-courgette)
  - Process file updates efficiently - eg renaming files, swapping file piece order
* Detecting changes rapidly
  - using inotify? (linux systems only)
* merging conflicting versions - just do what dropbox does and rename conflicts /create copies
* Working out which files and versions are most up to date -  propagating most up-to-date file
* Obtaining an open outgoing port - upnp?

###Possible Extensions (beyond initial scope):
* externally visible files - like hosted webpages and weblinks to files - distributed content hosting
* client for Windows
* Web interface 
* Android client?
* Version control for managed files
  - Likely to be implemented using Git
* fine-grained folder watch lists - just watch one folder for now
* GUI - stick to a daemon (and setup wizard?) with config file for now
* fine-grained file subscriptions
  - nodes only mirroring files they're interested in
  - Risk of low availability for undesirable/unpopular files - bad
* merging conflicting file updates automatically (modification times, diffs, git?)



Gantt Chart
-----------

Need to build time into the project plan to try out different libraries and software, 
and a cutoff time after which no more library changes can be made

1. Define research method and question during discussion with supervisor
2. Define provisional algorithm for each major technical challenge
3. Implement using blueprint from 1, refining as necessary

Resources Required
==================

* Specify hardware and software resources required by the project
* Justify why they are required
* Confirm they are available

Need several (>2) sets of the following:

* raspberry pi
* ethernet cable
* sd card
* power supply
* micro USB lead
* external hard drive
* external hard drive power supply

power source (~12W per node)
internet access via ethernet

References
=========

* Be complete
* Use a well recognised style
  - E.g. formatting style recommended for your short
report
  - Web resources change, so put revision/accessed
dates

Related Software
----------------

https://tahoe-lafs.org/trac/tahoe-lafs
http://code.google.com/p/mogilefs/
http://ceph.com/
http://sparkleshare.org/
https://git-annex.branchable.com/

Protocols and Libraries
-----------------------
[rsync]:(http://rsync.samba.org/tech underscore report)

 
step 1
get rsync delta algorithm
get bit-torrent distributed piece trading (p2p file chunks transfers) algorithm
use inotify(?) to listen for file updates
use lz4 or lzo for fast file compression
* need a way to check that data is compressible
