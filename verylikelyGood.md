Abstract
========

"Engineering is the art of compromise: you work with what is available to you to make the best solution to the problem."

"jdb" on Raspberry Pi blog[^engquote]

<!--You have 150 words - use them! (wisely)-->

Technical expertise should not be a requirement for preserving family history.
Distribackup shall be a software system to sync distributed backups of important data as efficiently as possible. The user will remain in control of the files, while providing an hardware fault-tolerant service without relying on any vulnerable external storage services.

* distributed backup for the home user is an important area because ...

This report proposes a project to create such a distributed backup solution, with particular emphasis on the technical aspects of implementing a robust, efficient transfer infrastructure to 

* summary of aims ("The aims of this project are ...")
* summary of content ("The project will involve two phases ...")
* expected outcomes ("Results from the proposed project will be ...")


Some of the algorithms and techniques used will come from existing solutions and algorithms:

1. Bit Torrent
2. Rsync
3. Kademlia
4. Inotify

The project's main aim will be to apply the methods used in these open-source applications from each of these open-source projects, combining them in novel ways into a unified utility.


Introduction
============

Computers have made it convenient to store all our important data digitally. Family photos, videos, genealogy records, old family recipes; They all take less physical space on a disk, and can be copied easily for anyone interested. However, all data is at risk of loss (flooding, fire and theft being the most often-cited), and most homes don't have the infrastructure in place to preserve their family's unique data long-term. Lay users often have little or no idea about where or how their data is stored (on their computer or on the internet), whether it is safe and whether their rights to it have been compromised by a heavy-handed Terms Of Service agreement (TOS).

Unique data such as photos are often stored locally on a physical medium which is vulnerable to damage or corruption. CDs are damaged by heat, light and wear and tear. When this storage fails (mobile phones are lost, old PCs break down or are just replaced due to age, without sufficiently diligent data transferral), often the user unwittingly throws away or loses many years of irreplaceable.

Lost laptops and pen drives cause frequent important data loss. Old PCs being replaced cause families to throw away accumulated personal data, without realising what is stored locally rather than on the internet
    - Lack of of understanding causes further personal data loss upon hardware failure

* Describe the proposed system and give an overview of its functionality



This project aims to reduce the risk of data loss for families (and other groups with an interest in long-term data preservation) due to lack of sufficient knowledge and/or funds for proper backup solutions, without relying on a third party service that has ulterior motives.

* Describe the main features of the project

Distribackup will watch the contents of a directory, and keep its contents synced with identical directories on other computers. Because there is no authoritative server serving updates to parties, syncing will rely on peer-to-peer file transfers to propagate changes. In order to expediate large file transfers (which are likely in the primary use case), a differencing algorithm will be used to only send pieces of files that have changed with an update.

This report will describe why this software is needed, comparing it to related work, and showing how this project can build on this work; it will then describe the proposed programme of work to be undertaken in order to complete the bare-bones implementation offered here; continuing by laying out the subcomponents of the software, their purposes, and how they fit together to complete the end goal. The methodology will then be explained, describing what software development techniques will be used during the project. This will lead into the proposed evaluation methodology, and how research will be carried out.

The expected timeline for this work will then described in detail, accompanied by a Gantt chart. The report will then finish by listing the resources required in order for it to be completed, and a list of references.

Background
==========
<!--
* A key section in setting the scene for the project
* Should contain a significant literature survey of related systems/ approaches
    - Key issue of scope
    - e.g. networking -> P2P -> file sharing software
    - May be sub-divided
    - e.g. methodologies vs. existing systems
* Should say something!
    - What are the implications for my project?
-->

More than ever before there is a need for thorough backup software which is accessible for everyone. There are now years' worth of milestones in peoples' lives, all stored on a machine they don't fully understand.

Evaluation of Existing Solutions
--------------------------------

<!-- TODO: improve this
	more words, less bullets 
	also cover similar software:
	ceph
	sparkleshare
	git-annex
	-->

### Dropbox ###

Dropbox is an extremely popular solution for accessing data across multiple machines, and sharing files easily with small groups of people. As convenient as Dropbox is, there are downsides:

encryption model is not published, has been proven to be unsecure in the past

Dropbox is useful for keeping files synced across multiple machines; but by using their service, Dropbox can stake a claim to your data.

* You have no idea what they're doing with it (marketing?)
* You can't be sure they've really deleted something (hint: they haven't)
* If you made a commercially interesting piece of software (something that could make money), what would happen to your rights to the IP?
* The maximum file and storage sizes are (understandably, considering they personally store copies of everything you sync) very low, and this restricts its usefulness.

Dropbox can encumber your files with ownership and legal issues.
 
### Bit-Torrent  ###
* Collection contents cannot be changed after torrent creation
* Unsecure by design (IP addresses and ISP hostnames are broadcast and used as identities)
  * Therefore vulnerable to poisoning
* Nowadays highly stigmatised; associated with illegal activity (copyright theft) in public consciousness

### Git ###
updates require user intervention
git updates need to be synced by user, using commandline or seperate GUI
- Not automatic
- Very hands-on; cannot fire-and-forget

* Git is not visible to end-users: its intended audience are developers, and its steep learning curve (tens of commands to learn each with its own single-character options; a new mental model for manipulating 'staged' files which are 'indexed') would prevent its adoption by non-programmer power users
* Existing GUIs for git are unfinished, non-free, buggy or as confusing as the commandline interface, with none of the portability.
* Git is much more complex than is necessary for this task

### Commercial Solutions ###

Commercial solutions (such Amazon S3) entrust critical private data with large hosting

* can be damaged or hacked
* potential loss of ownership of Intellectual Property rights (Terms Of Service agreements can often change at any time)
 
None of the existing solutions are easy enough to use

Existing backup solutions are commercially available, and can come with very restrictive space limits, or require that the user allow the data to be used for marketing purposes (seeing photos from a birthday party 10 years ago being used in an advert). A software system which allows lay people to set up extremely robust backups of data that can't be replaced.

Handing it over to a company which stores it on third-party server farms. These are vulnerable because they can not only suffer the same physical damage (floods, earthquakes, fires) as all data, but may also be actively targeted by governmental groups (FBI seized all megaupload files during their raid, including files of legitimate users who had paid for storage)

Governments seize data wholesale, stealing indiscriminately in the name of anti-copyright theft. This puts personal information stored in large centralised datacentres at risk.

The storage is run by a company, whose primary motivation is to make a profit; the agreement you sign may allow them to mine your data for information useful to them (eg using your photos as marketing material).



The Proposed Project
====================


Aims and objectives
-------------------
<!--
* State clearly the overall aim of project
* State more specific objectives as bullet points
    - Subdivide into categories if necessary
* Are they measurable?
* Are they achievable?
* Do I know when I should stop?
-->

The aim of this project is to create a robust, fault-tolerant backup system which is easy enough for anyone with limited computer knowledge to set up.

One of the main goals of the project is to offload as much work as possible from the user, to make creating formidable backup plans less intimidating. 

The bulk of the project will involve implementing 5 major pieces of functionality

The project will be focused on achieving all the implementation goals first, then polishing later - or as future work

The software system will be comprised of the following components:-

* TCP/IP send and receive, ports binding
* file differencing
* file change event handler
* node discovery
* peer-to-peer transfer

network comms / port binding
node discovery
file differencing
p2p piece sending
file update watching

The project will concentrate on fitting these disparate pieces together.

The program will aim to minimise setup and configuration; there is an extremely high barrier-to-entry in terms of technical knowledge required for many existing backup and  solutions, such as Git. This is by design in Git and Git-annex, so rather than go back on their intended design goals with a fork, this project will create something new with a minimal configuration requirement at its heart.

The experimental implementation (which is the focus of this project) will not be as fully user-friendly as the intended final version; this is because the project will concentrate on implementing the backend, and testing this core functionality. This is to cut down on time spent on tasks not critical to the technical challenges, such as interface prototyping. If this project were to be taken further, or if there is significant extra time in the project plan after core implementation, then a full user interface (both graphical and commandline) would be implemented.

* A Raspberry Pi or other always-on computer with attached storage is connected to the internet: contains important backed up data
* another machine elsehwere contains a full copy of the data
* The copies are kept updated over the internet, and new copies can be set up by downloading from all existing nodes, thus reducing the setup time
* network transfers will bee made as efficient as possible, using rsync algorithm and peer-to-peer file transfers.
* Inter-node communication will be made as simple as possible to set up, using DHT (Kademlia) to connect peers to each other without any user intervention.
* Can be thought of as a kind RAID over internet, with transfer optimisations 

Using Distributed Hash Tables may prove to be too complex for scope of this project; in that case, an alternative method for nodes to find each other will be devised. However, the alternative method will likely require more user intervention.

It shall use existing technology to provide this:-

* Bit-torrent[bt-protocol] : good at reducing bandwidth costs of a one-to-many download, by trading pieces of files among peers. Means the original syncing file only needs to be uploaded once by the originator, (like Dropbox, to its central servers).
* Rsync[^rsync] : robust file-transfer application, which uses differential transfer: only transfers new files, (or even parts of files, see differential transfer) that have changed. This would vastly improve sync times. 
* Git[^git] : an existing solution for peers to exchange versions of files; I don't THINK I want version history in this program, but it could be added as an optional feature later.
* inotify[^inotify] : filesystem update notification system implemented in the linux kernel. Avoids having to implement costly scans of watched files. Available on linux desktops, and on Android using the FileObserver wrapper.[^FileObserver]
* [PGP](http://www.cryptography.org/getpgp.htm): some kind of encryption will be needed to prevent unauthorised peers from getting copies of your files. An authentication system would be good, possibly along with encrypted packet transmission, and/or encrypted storage. [Pretty Good Privacy][PGP] is a good candidate for any of these.

Possible Use Cases:
-------------------

wedding photo hosting/upload
family data archive (syncing geographically distant copies to maximise data resilience)
working documents for geographically distant Dev teams

Platform Support
----------------

Linux will be the primary implementation platform, as its filesystem update notification system (inotify) and network communication  libraries are well-documented, and as linux itself already runs on many hardware platforms, linux-specific code (such as inotify) will be relatively simple to port to other hardware running linux - ie to Raspberry Pi, or to Android.

The project will be written in Java. This is because I am most comfortable with Java for high-level networked applications; its popularity ensures that there is plenty of support, and that there are plenty of libraries to provide common functionality (such as network IO, and the BitTorrent protocol). There are also implementations of the BitTorrent protocol for to use as reference when developing the peer-to-peer file transfer component.

C was another choice, however aside from less familiarity with it than Java, its lack of package encapsulation or object-oriented code lacked finesse. Clear structuring was not desirable for a project of this underaking. 

Java is also likely to run well on the target platforms, with minimal architecture-specific code (Android will require some kind of GUI, an may have a different file-access API to normal Java). Java runs on all the target platforms, which removes the work of maintaining (or at least cross-compiling) a seperate port for each.

Java and its Virtual Machine still runs slightly slower than native C code. As a language, it tends to use a lot of memory for a given task (compared to the same task in another language); this may be an issue on Model A raspberry Pis, which only have 256MB of RAM. My initial reservation about using Java was its lack of support for the Raspberry Pi, whcih is the main platform I had in mind when I envisioned this project. This has recently been remedied[^raspi-java], and there is now an Oracle-supplied Java Development Kit available in the Raspbian softwqare repositories, which uses the hardware-float feature, making it much faster than previous efforts.



Interaction
===========

Users will choose:-

* a folder they wish to keep synced,
* and ONE UNIFIED list of peers that sync to this folder.
* a maximum size limit of data to sync (optionally no limit)



At the start, peers will have to get all the files from a filled folder,

later, peers will have to receive updates

A peer with new files will announce updates to all peers (push?)


~~To build a self-hosted cloud storage platform designed around lay person's archival data.~~

Methodology
-----------
<!--
* Software engineering approach
    - e.g. Use of rapid prototyping
* Planned evaluation strategy
    - Testing methods to be used and why
    - User evaluation? Performance evaluation?
    - Qualitative evaluation? Quantitative evaluation?
-->

Evaluation???
=============

<!--
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

* Is a task based evaluation suitable?
  –  Use your system to perform the following steps.
    1. Send a picture to the display
    2. Receive a picture from the display
    3. etc. etc.
  –  Allows performance measures to be taken (if appropriate)
    - Time to complete tasks
    - Percentage tasks successfully completed etc.
* Alternatively, is it reasonable to simply give the system to the user for a reasonable time for them to ‘play’ with it?
-->

Programme of Work
=================

<!--
* Start with overall timescale for the project
* Each task should have defined and measurable outcomes
    - sub-goals
    - short description
    - duration
    - milestones and deliverables
* Overall schedule:
    - use of appropriate diagrammatic notation
-->


Working under the assumption that the deadline for this project is roughly the same as last year's Final Year Project deadline, then there are 6 months from the start of term in October, to the deadline in late march. March to complete this project. Because this project is self-derived, ie I came up with it without any outside advice on scale (and therefore it might prove to be too much work), I have attempted to keep the desired goals to a bare minimum, in order to give myself the best chance of finishing the core functionality, which is as follows:-


Stage | Time Given
------|-----------
Research | 2-3 weeks
early experimentation | 5-6 weeks
testing and cleanup | 1 month


Phase 1 (weeks 0-12): Blah
-------------
### Task 1 ###
During this time the project developer will chew on a stick, preferably a liquorice root, and twirl his moustache

Phase 2 (weeks 12-24): blah blah
--------------------------------
Further beard growth will be made available to intersted parties at this time, inluding a one-time fire-and-forget german-model fritzenstoppe

###  Major Technical Tasks ###

These subcomponents need to be implemented in order for the software to achieve its primary function.

* Node discovery - given that nodes have dynamically assigned IP addresses and <100% uptime?
    - use DHTs eg Kademlia
        - is DHT node discovery too difficult to implement in the given time?
* Identifying/authenticating nodes - preventing man-in the middle attacks
    - use SSH authentication model
* Implementing p2p file transfer - à la bit torrent
    - downloading a file from multiple peers at once by splitting into file pieces
        - will be heavily related to rsync-derived algorithm 
* Sending file changes only
    - look at rsync algorithm, remote differential compression, diff, bsdiff, chromium's Courgette[^courgette]
    - Process edge-case file updates efficiently - eg renaming a file, swapping file piece order
* Detecting changes rapidly
    - using inotify? (linux systems only)
* merging conflicting versions - just do what dropbox does and rename conflicts /create copies
* Working out which files and versions are most up to date -  propagating most up-to-date file
* Obtaining an open outgoing port - UPnP?

###  Possible Extensions (Beyond Initial Scope) ###

These features are good areas for further work, but are not essential in the first iteration of development.

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
* Merging conflicting file updates automatically (modification times, diffs, git?)
* Multiple peer lists, multiple folders
  * This could get complex for the user very quickly

Gantt Chart
-----------

I need to build time into the project plan to try out different approaches to each subtask, and a cutoff time after which no more changes can be made.

1. Define research method and question during discussion with supervisor
2. Define provisional algorithm for each major technical challenge
3. Implement using blueprint from 1, refining as necessary

Resources Required
==================
<!--
* Justify why they are required
* Confirm they are available
-->

Hardware
--------

In order to set up a test network with multiple nodes that is easy to configure, it would be beneficial (though not strictly necessary) to have at least 2 of the following sets:

* Raspberry Pi - to run the backup system on. Real-life backup network may also use other machines
* Ethernet cable - needs to be long enough to reach network access point
* SD card - For the Raspberry Pi to boot from. 8GB should suffice
* USB power supply (>=1.0A at 5V, so as to allow for a mouse an keyboard)
* Micro USB lead (Plugging Pi into power supply)
* External hard drive - for data storage. Preferably from a "green" range to minimise power consumption when idle
* External hard drive power supply - needs a socket to plug into, and about 24 watts (2.0A at 12V)

AC power source (approx. 30 watts per node)
Internet access via ethernet ports

These resources are needed in order to test the software system in a realistic environment. Virtualbox could be used to simulate multiple nodes on a single machine (with an internal "LAN"), however this would not adequately test the unpredictable nature of the real-world internet.


Software
--------

The following libraries and APIs will be necessary to develop from:

* Rsync source - algorithm reference
* Bit-Torrent example implementation
  * Also a protocol specification for reference 

References
=========
<!--
* Be complete
* Use a well recognised style
    - E.g. formatting style recommended for your short report
    - Web resources change, so put revision/accessed dates
-->

[^engquote]:http://www.raspberrypi.org/a-birthday-present-from-broadcom/#comment-493992-------------------------------
[^wang13]: Liang Wang and Jussi Kangasharju, "Measuring Large-Scale Distributed Systems:
Case of BitTorrent Mainline DHT", 13-th IEEE International Conference on Peer-to-Peer Computing
(http://www.cs.helsinki.fi/u/lxwang/publications/P2P2013_13.pdf) (accessed 19/06/14)
<!--_-->



Related Software
----------------

[^Tahoe]: http://tahoe-lafs.org/trac/tahoe-lafs
[^MogileFS]: http://code.google.com/p/mogilefs/
[^ceph]:http://ceph.com/
[^sparkleshare]:http://sparkleshare.org/
[^git-annex]:http://git-annex.branchable.com/

Protocols and Libraries
-----------------------

[^raspi-java]: http://www.raspberrypi.org/oracle-java-on-raspberry-pi
[^git]:http://git-scm.com/
[^rsync]:http://rsync.samba.org/tech_report
[^bt-protocol]:http://www.bittorrent.org/beps/bep_0003.html
[^courgette]:http://dev.chromium.org/developers/design-documents/software-updates-courgette
[^FileObserver]:https://developer.android.com/reference/android/os/FileObserver.html


* get BitTorrent distributed piece trading (p2p file chunks transfers) algorithm
* use lz4 or lzo for fast file compression
  * need a way to check that data is compressible
