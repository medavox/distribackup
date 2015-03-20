%Distribackup: distributed backup syncing for the lay user
%Adam Howard
%27th June 2014

Abstract
========

"Engineering is the art of compromise: you work with what is available to you to make the best solution to the problem."

"jdb" on Raspberry Pi blog[^engquote]

Technical expertise should not be a requirement for preserving family history.
Distribackup shall be a system to sync distributed backups of important data as efficiently as possible. The user will host the data on domestic machines, remaining in control of their files.

Distributed backup for the home user is an important area because lay people currently don't have access to secure and robust backup for their personal data which is easy to use and maintain, without relying on commercial cloud-hosting companies whose motivations are not entirely aligned with theirs.

This report proposes a project to create such a backup solution, with emphasis on the technical aspects of implementing robust, efficient transfer between distributed mirrors that can work efficiently for large collections of binary data.

This will be achieved by using co-opting technology from existing work in other areas in order to provide an efficient mirroring system.


Introduction
============

Computers have made it convenient to store all our important data digitally. Family photos, videos, genealogy records, old family recipes; they all take less physical space on a disk, and can be copied easily for anyone interested. However, digital data is at risk of loss (flooding, fire and theft being the most often mentioned), and most homes don't have the infrastructure in place to preserve their family's unique data long-term. Lay users often have little or no idea about where or how their data is stored (on their computer or on the Internet), whether it is safe and whether their rights to it have been compromised by a Terms Of Service agreement (TOS).

Unique data collections such as photos are often stored on a physical medium which is vulnerable to damage or corruption. CDs are damaged by heat, light and wear and tear. When this storage fails (mobile phones are lost, old PCs break down or are replaced due to age, without sufficiently diligent data transferral), often the user unwittingly throws away or loses many years of irreplaceable data.

Lost laptops and pen drives cause frequent important data loss. Old PCs being replaced cause families to throw away accumulated personal data, without realising what is stored locally rather than on the Internet -- lack of understanding causes further personal data loss upon hardware failure.

The backup strategy often lauded as the most prudent follows the 3-2-1 strategy: 3 copies, on 2 different storage medium, including 1 offsite backup.[^backup321] For everyone but, this kind of robustness is extremely difficult to set up and maintain. For instance:

* How does one set up backups in different locations?
* How does one sync updates to each offsite copy?

This project aims to answer these questions with a software solution, reducing the risk of data loss for families (and other groups with an interest in long-term data preservation) due to lack of sufficient knowledge and/or funds for more thorough backup solutions, without relying on a third party service that has ulterior motives.

<!-- Describe the main features of the project -->

Distribackup will watch the contents of a directory, and keep its contents synced with identical directories on other computers. Because there is no authoritative server broadcasting updates to parties, syncing will use a distributed model to propagate changes. In order to expedite large file transfers (which are likely in the primary use case), a differencing algorithm will be used to only send pieces of files that have changed with an update.

This report will describe why this software is needed, comparing it to related work, and showing how this project can build on this work. It will then describe the proposed programme of work to be undertaken in order to complete the bare-bones implementation offered here; continuing by laying out the sub-components of the software, their purposes, and how they fit together to complete the end goal. The methodology will then be explained, describing what software development techniques will be used during the project. This will lead into the proposed evaluation methodology, and how research will be carried out.

The expected timeline for this work will then be described in detail, accompanied by a Gantt chart showing this visually. The report will then finish by listing the resources required in order for it to be completed, and a list of references used.

Background
==========

More than ever before there is a need for long-term backup software which is accessible for everyone. There are now years' worth of data stored digitally which record milestones in peoples' lives, all stored on machines they don't fully understand.

Evaluation of Existing Solutions
--------------------------------

This is by no means an exhaustive list.

### Bit-Torrent  ###
* Collection contents cannot be changed after torrent creation
* Unsecure by design (IP addresses and ISP hostnames are broadcast and used as identities)
* Nowadays highly stigmatised; associated with illegal activity (copyright theft) in public consciousness

### Git ###
updates require user intervention
git updates need to be synced by user, using command line or separate GUI
- Not automatic
- Very hands-on; cannot fire-and-forget

* Git is not visible to end-users: its intended audience are developers, and its steep learning curve (tens of commands to learn each with its own single-character options; a new mental model for manipulating 'staged' files which are 'indexed') would prevent its adoption by non-programmer power users
* Existing GUIs for git are unfinished, non-free, buggy or as confusing as the command line interface, with none of the portability.
* Git is much more complex than is necessary for this task

###Git-Annex###

Git-Annex, and its GUI front end git-annex assistant allow the user to manage collections of large files using git, without checking the file contents into git.[^git-annex]

Git-annex is aimed towards a more technically literate user. Also, as with Sparkleshare, a central server is needed to manage and distribute changes between different storage nodes.

###Ceph###

Ceph is a distributed file system. Ceph is aimed more at technically proficient users and industry professionals.

###Tahoe-LAFS###

Tahoes-LAFS (Least Authority File system) is an open source distributed file system, focused on providing self-hosted cloud storage that is resistant to attack.[^tahoe1page] This, again, is aimed much more at system administrators and other professionals with an understanding of the area.

###Sparkleshare###

Sparkleshare[sparkleshare] is also an open-source cloud syncing solution with the intention of providing an alternative to DropBox. 

Sparkleshare is backed by Git and SSH, and is well suited to managing a collection of many regularly-changing small (mostly text) files which are edited by a group, such as in a software development team.[^sparklegood] However, by its own admission Sparkleshare is not well-suited to full-computer backups, or for storing large archives of binary data such as photos and videos.[^sparklebad] Sparkleshare also relies on a centralised server to manage backups, which introduces an infrastructure overhead (including setup time and maintenance) which this project aims to avoid.

###MogileFS###
Complex conceptual structure
Multiple types of mirror nodes

### Dropbox ###

Dropbox is an extremely popular solution for accessing data across multiple machines, and sharing files easily with small groups of people. As convenient as Dropbox is, there are downsides:


Dropbox is useful for keeping files synced across multiple machines; but by using their service, Dropbox can stake a claim to your data.

* Their encryption model is not published, has been proven to be unsecure in the past[^dropbox-secpaper]
* They have repeatedly leaked sensitive data, only remedying the problem after being notified by third parties[^dropbox-security1] [^dropbox-security2] [^dropbox-leak]
* Data may be used for undisclosed purposes
* You can't be sure they've really deleted something[^dropbox-hoard]
* If you stored the source code to a commercially interesting piece of software (something that could make money), Dropbox could feasibly contend the Intellectual Property rights
* The maximum capacity and file sizes are very low, which restricts its usefulness.
* Dropbox are required by law to hand over data to governmental bodies (including overseas agencies such as US intelligence). In an age of controversial laws (legislate first, ask questions later)[^prism], this is problematic
* Dropbox also reserves the right to share certain personal information with other companies, whose own security may be insufficient[^dropbox-privacy]

### Other Commercial Solutions ###

Dropbox has been examined individually due to its popularity; however some general disadvantages apply to all third-party commercial solutions:-

Other commercial solutions (such Amazon S3) entrust critical private data within large hosting data-centres. These data-centres are large targets for attack. They:

* can be damaged or hacked
* carry a risk of loss of ownership or Intellectual Property rights (Terms Of Service agreements often can change at any time, with or without notice)
* hand over information to governmental bodies with dubious jurisdiction[^ms-dublin-usgov-handover]
 
None of the existing storage solutions are robust enough to use for long-term archival data.

Existing backup solutions are commercially available, and can come with very restrictive space limits, or require that the user allow the data to be used for marketing purposes (eg photos from a birthday party used in advertising without explicit permission). A software system which allows lay people to set up extremely robust backups of data that can't be replaced.

Data is stored on third-party server farms. These are vulnerable because they can not only suffer the same physical damage (floods, earthquakes, fires) as all data, but may also be actively targeted by governmental groups (FBI seized all MegaUpload files during their raid, including files of legitimate users who had paid for storage)

Governments seize data wholesale from sites (like MegaUpload), stealing both pirated material and legitimate data indiscriminately in the name of anti-copyright theft. This means that any personal information which happens to be stored on the same site as  in large centralised data centres is at risk.

The storage is run by a company, whose primary motivation is to make a profit; the agreement you sign may allow them to mine your data for information useful to them (eg. using your photos as marketing material).

The Proposed Project
====================


Aims and objectives
-------------------

The aim of this project is to create a fault-tolerant backup system which is easy enough for anyone with limited computer knowledge to set up.

One of the main goals of the project is to offload as much work as possible from the user, to make creating strong backup systems much easier. To this end, there will be no need for a central server, eliminating the need for an always-on machine (which costs money in dedicated hardware and electricity).

This decentralisation will be achieved by using an implementation of Distributed Hash Tables to provide an overlay network, allowing mirrors to find each other over the internet without knowing each other's IP addresses in advance. This is necessary, due to the dynamic nature of IP addresses assigned in domestic environments.

Node discovery using DHT can be slow; it can take up to 30 minutes for the overlay network to fully populate. This is one reason why the project is not intended for use as in real-time as a collaboration management system, such as for a widely geographically distributed development team.

Mirrors joining an overlay network will be told which overlay network they are joining with a special key, possibly using SSH authentication. This key will be a long, minimum-length (eg 32+ bytes) string of human-readable characters. The key may be provided by the DHT algorithm.

Network performance may mean that changes between files do not propagate throughout the network quickly (domestic internet upload speeds are often relatively low). The system is designed to mitigate this with differential transfer, using the Rsync algorithm.[^rsync-tech]

The project will be focused on completing these technical goals first, then polishing later - or as future work (See section "Interface"). These technical goals will involve implementing 5 pieces of functionality:-

* TCP/IP send and receive, ports binding
* Peer-to-peer transfer
* file differencing
* file change event handler
* mirror discovery

It shall build upon existing technology to provide this:-

* Bit-torrent[bt-protocol] : Peer-to-peer file transfer is good at reducing bandwidth costs of a one-to-many download, by trading pieces of files among peers. Means the original syncing file only needs to be uploaded once by the originator, (like Dropbox, to its central servers). Bit-Torrent is only one possible model to follow.
* Rsync[^rsync] : robust file-transfer application, which uses differential transfer: only transfers new files, (or even parts of files, see differential transfer) that have changed. This would vastly improve sync times. 
* Git[^git] : an existing solution for peers to exchange versions of files; I don't THINK I want version history in this program, but it could be added as an optional feature later.
* Inotify[^inotify] : file system update notification system implemented in the Linux kernel. Avoids having to implement costly scans of watched files. Available on Linux desktops, and on Android using the FileObserver wrapper.[^FileObserver]


###Pseudocode

The following is a loose pseudocode demonstration of the program's algorithm.

	Upon startup:
	node discovers others using DHT as overlay network

	Upon a file change:
	Inotify alerts of file change
	
	if a file has been deleted:
		if file is confirmed to exist on other nodes
		and is identical on other nodes,
			then a deletion announcement is sent through overlay network to other nodes

	if a file is added:
		rsync is used to find which nodes don't have the file
		file is sent via p2p implementation to any nodes which don't have the file (or a piece of it)
		
	if a file is updated:
		rsync diffs these changes against copies on other nodes
		differing chunks are sent via p2p implementation

Intelligent handling of renaming will depend on what information Inotify (and the intermediate library) provides for manipulation. Detection of renames may or may not need to take place inside the program.


The program will aim to minimise set-up and configuration; there is a high barrier-to-entry in terms of technical knowledge required for many existing backup and  solutions, such as Git. This is by design in Git, so rather than work against their intended design with a fork, this project will create something new with minimal configuration requirements at its heart.

* File transfers will use peer-to-peer techniques to reduce transfer times and individual upload bandwidth usage
* Transfers will also use Rsync algorithm to only transfer differences - again minimising bandwidth usage
* Inter-mirror communication will require no set up, using DHT (Kademlia) to allow peers to find each other without user intervention
* Can be thought of as a kind RAID over Internet, with transfer optimisations 

Intended Use Case:
-------------------

This project's main goal is to provide a long-term backup solution for lay users. To this end, it will focus on the following areas:

The system will not require an always-on archive machine - archive copies will be on every computer that is subscribed to the overlay network, and the purpose of this project is provide infrastructure which will reliably and efficiently sync them.

* Not intended for real-time sync
* The system will work best with infrequent updates among sometimes-on machines that form a network with common uptime (ie each mirror is on at the same time as at least one other machine, to pass updates)
    - a Raspberry Pi can be used to bolster this, but should not be necessary.


Platform Support
----------------

Linux will be the primary implementation platform, as its file system update notification system (Inotify) and network communication libraries are well-documented, and as Linux itself already runs on many hardware platforms, Java code which relies on Linux-specific features (such as Inotify) will be relatively simple to port to other hardware running linux - ie. to Raspberry Pi, or even to Android.

The project will be written in Java. This is because I am most comfortable with Java for high-level networked applications; its popularity ensures that there is plenty of support, and that there are plenty of libraries to provide common functionality (such as network IO, and the Bit Torrent protocol). There are also implementations of the Bit Torrent protocol for to use as reference when developing the peer-to-peer file transfer component.

C was another choice, however aside from less familiarity with it than Java, its lack of package encapsulation or object-oriented code lacked finesse. Clear structuring was not desirable for a project of this undertaking. 

Java is also likely to run well on the target platforms, with minimal architecture-specific code (Android will require some kind of GUI, and may have a different file-access API to normal Java). Java runs on all the target platforms, which removes the work of maintaining (or at least cross-compiling) a separate port for each.

The Java Virtual Machine still runs slightly slower than native C code. As a language, it tends to use a lot of memory for a given task (compared to the same task in another language); this may become an issue on certain target platforms with little RAM, namely the Model A Raspberry Pi, which only has 256MB of RAM.

Java was not initially considered as a viable language for this project, due to its lack of (or poor) support for the Raspberry Pi, which is one of the target platforms for this project. This has been remedied since the project's initial conception[^raspi-java]; there is now an Oracle-supplied Java Development Kit implemented in hardware, making it much faster than previous versions for the Pi.


Interface
===========

There is an inherent conflict between designing a system that is easy to use, and designing a system that is easy to implement. Due to the ambitious nature of the work being proposed, the project will err on the side of caution, and a programme of work will be specified which prioritises the technical achievements, rather than development unrelated to the technical challenges, such as porting to Windows, iteratively refining a GUI or other usability refinements.

Although the end goal is intended ease of use, the technical work is original and challenging, and conservative estimates must be made as to the duration of development. This being said, no design choices will be made which would later affect usability, given further time to polish the application. This first phase will measure the feasibility of the software design, and ultimately develop the backend (which is nonetheless usable on its own) for a complete, hands-off distributed backup system.

Windows support would greatly broaden the availability of this software to lay users, as Windows' dominance in the home desktop PC market is still relatively assured. However, Windows does not make use of many (if any) of the standard Unix-style conventions or services, such as Inotify, which Linux and Android both use. Even Apple's OS X makes use of Unix-style paths. Therefore, aside from large portions of common code (due to development in Java), development of a fully Windows-compatible version would require significant further development time, to account for the differences in Windows.

Also, a GUI would help greatly with usability, but would introduce a development phase which is unrelated to the rest of the work to be undertaken, and may take significant resources to achieve successfully.

The experimental implementation (which is the focus of this project) will not be as fully user-friendly as the intended final version; this is because this project will concentrate on implementing and testing the back-end core functionality. This is to cut down on time spent on tasks not critical to the technical challenges, such as interface prototyping. If this project were to be taken further, or if there is significant extra time in the project plan after core implementation, then a full user interface (both graphical and command line) would be implemented.

As a bare minimum of configurability (all of which will be optional, and will default to sane defaults) users will be given choices for the following options:-

* a folder they wish to keep synced
    - Creating a new network with this folder, or adding it to an existing network
        * Joining an existing network will require some kind of network identity key as a minimum. See Aims and Objectives.
* a maximum size limit of data to sync (optionally no limit)

Gathering user input may be implemented using some kind of rudimentary GUI, such as a wizard.

Evaluation
==========

Evaluation will focus on a number of factors.

As this project does not intend to provide a completely user-friendly system (specifically, GUI development is not a core goal), then little testing will be able to be performed using its audience, namely lay computers users. Therefore, an alternative intended audience is proposed for testing the results of this project: technical or semi-technical computers users (who use Linux) with no prior knowledge of setting up or using the system.

Some evaluation questions:-

* Were all technical tasks completed?
* Does the program perform the intended goal?
    - Without error?

* Are mirrors able to find each other easily? (test of DHT mirror discovery)
* Does the average household's collection of computers form a connected enough network that updates can be propagated between them?
* Can mirrors successfully keep in sync with low uptime? (test of transient mirrors)
* What is the minimum percentage uptime needed as an average per mirror to keep the mirrors synced?
* Does the system run without requiring user intervention? (successful no-maintenance)

Programme of Work
=================

Working under the assumption that the deadline for this project is roughly the same as last year's Final Year Project deadline, then there are 6 months from the start of term in October, to the deadline in late March. Because this project is self-derived, ie it was devised without outside appraisal of project scale (and therefore it might prove to be too much work), core goals have been kept to a bare minimum number, to ensure the best chance of finishing the core functionality.

Overview of Time-Scale
----------------------

A breakdown of the major project stages is as follows:-


Phase                 | Estimated Duration
----------------------|-----------
Research			  | 2-3 weeks
Early Experimentation | 5-6 weeks
Testing and clean-up  | 1 month

Using Distributed Hash Tables may prove to be too complex for the scope of this project; in that case, an alternative method for mirrors to find each other will have to be devised. However, the alternative method will likely require more user intervention.

The project plan needs adequate safety time built into each stage, to provide a fall-back for unforeseen difficulties. Any extra time that is recouped can be put towards an further work, such as GUI development.


The development work is broken into 3 major stages, after each of which there will be a functioning program. Because of this, early-completion stages have been devised, which provide functional subsets of the full program.

Stage   | Functionality | Implemented components
--------|---------------|---------------------
Stage 1 | syncing between 2 peers, with advance knowledge of each other's IP addresses | Inotify, network comms, rsync
Stage 2 | syncing between multiple peers, still with advance knowledge | peer-to-peer file transfers
Stage 3 | syncing between multiple peers without advance knowledge of addresses | mirror discovery using DHT overlay network

1. Research
	* Distributed Hash Tables:
		- Compare DHT algorithms
		- Choose a DHT algorithm to use for the overlay network
	* Peer-to-Peer File Transfer
		- Compare existin p2p transfer algorithms and techniques
		- Choose one to use, either as an API or as a fork
	* Research Inotify API further
		- investigate possibility of equivalent in Windows, including Java bindings
	* Research Rsync algorithm further
		- make notes to help when porting to Java
	* Summarise how Bit-Torrent (or other p2p algorithm) concept of file pieces can be integrated with Rsync algorithm concept of differing file pieces
	* finalise exactly which implementation/algorithm version are to be used for:
		- Peer-to-Peer
		- Distributed Hash Table Overlay Network
		
2. Design
	* Write interface specifications for all major components to lay groundwork for modularity. Specific cases:
		- Write file update interface, which will be implemented differently on each target platform
			- eg Jnotify for Linux, FileObserver for Android, something else for Windows
		- Encapsulate node access subcomponent, to allow for using simple pre-supplied IP addresses during development
		- Create unified definition of a file 'piece' which can be exchanged between remote differencing and transfer subcomponents
		- Standardise mirror/node functionality
	* Create flowcharts detailing intended operation in all use cases

3. Implement
	* Write Linux-specific and Android-specific implementations of file update interface
	* Port Rsync algorithm to Java
	* Create p2p transfer algorithm (based on existing algorithm decided on in 1.)
		- Emphasising efficiency of transfer

4. Evaluate
	* Quantitative analysis of questions
		- Rigorous testing of system using network simulation, and real networks where possible
	* Qualitative analysis of system by members of testing group  (See "Evaluation")
5. Write up
	- Collate findings into report

###  Major Technical Tasks ###

These subcomponents need to be implemented in order for the software to achieve its primary function.

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

###  Possible Extensions (Beyond Initial Scope) ###

These features are good areas for further work, but are not essential in the first iteration of development. These will *not* be implemented during this project, unless the stated goals change, or there is significant extra time after after completing the core goals.

* Externally visible files - like hosted web pages and web links to files - distributed content hosting
* Client for Windows
* Web interface
* Android client?
* Version control for managed files
    - Likely to be implemented using Git
* A GUI
    - stick to a daemon (and setup wizard?) with a config file for now
* Fine-grained file subscriptions
    - mirrors only hosting files they're interested in
    - Risk of low availability for undesirable/unpopular files - bad
* Merging conflicting file updates automatically (modification times, diffs, git?)
* Multiple networks, multiple folders in each
    * This could get complex for the user very quickly
* Encryption [^PGP] - this can be broken into 3 sub-areas; Pretty Good Privacy is a good candidate for any of these.
	All of these, though especially the first two, are highly desirable, and may become core goals during the course of the project.
	- Transfer encryption - preventing digital wire taps from snooping on data being transferred is extremely important, especially given the use of a public network (the internet) as the transmission medium.
	- Authentication - An authentication system would prevent attackers from joining the network without permisson.
	- Local storage encryption - can be provided by the filesystem, or existing  disk encryption solutions. Interfacing with these would be the focus here.


Resources Required
==================

Hardware
--------

In order to set up a test network with multiple mirrors that is easy to configure, it would be beneficial (though not strictly necessary) to have at least 2 of the following sets:

* Raspberry Pi - to run the backup system on. In a real-world version of the system, backup mirror architecture will vary
* Ethernet cable - needs to be long enough to reach network access point
* SD card - For the Raspberry Pi to boot from. 8GB should suffice
* USB power supply (>=1.0A at 5V, to allow for a mouse and keyboard)
* Micro USB lead (Plugging Pi into power supply)
* External hard drive - for data storage. Preferably from a "green" branded product range to minimise power consumption when idle
* External hard drive power supply - needs a socket to plug into, and about 24 watts (2.0A at 12V)

AC power source (approx. 30 watts per mirror)
Internet access via Ethernet ports

These resources will be needed in order to test the software system in a realistic environment more easily. Virtualbox could be used to simulate multiple mirrors on a single machine (with an internal "LAN"), however this would not adequately test the unpredictable nature of the real-world Internet.

Software
--------

The following libraries and APIs will be built upon:

* Rsync source
    - used for rsync algorithm reference
* Bit-Torrent protocol specification for reference
* Bit-Torrent example implementation (preferably in Java)
    - For reference
* A Distributed Hash Tables algorithm, and API implementation
    - One of the following:
        * Kademlia
        * Chord
        * CAN
        * Pastry
        * Tapestry

Some of these sources may be subject to change (particularly the DHT algorithm), depending on judgement of their suitability after further research.

Conclusion
==========

Using the methods described by this report, Distribackup will provide a backup system that is easy enough for the lay user, and robust enough for long-term data survival. It uses existing technologies to ensure a successful and relevant implementation of this type of project.

References
=========

[^engquote]: "Engineering Quote" (http://www.raspberrypi.org/a-birthday-present-from-broadcom/#comment-493992) (retrieved 23/06/2014)
[^backup321]: "Backups with the 3-2-1 rule" (http://blog.wisefaq.com/2010/01/05/backups-with-the-3-2-1-rule/) (accessed 25/06/2014)
[^wang13]: Liang Wang and Jussi Kangasharju, "Measuring Large-Scale Distributed Systems:
Case of Bit Torrent Mainline DHT", 13-th IEEE International Conference on Peer-to-Peer Computing
(http://www.cs.helsinki.fi/u/lxwang/publications/P2P2013_13.pdf) (accessed 19/06/14)
<!--_-->

[^tahoe]: http://tahoe-lafs.org/trac/tahoe-lafs (accessed 24/06/2014)
[^tahoe1page]:"Tahoe one-page summary" (https://tahoe-lafs.org/trac/tahoe-lafs/browser/trunk/docs/about.rst) (accessed 24/06/2014)
[^MogileFS]: http://code.google.com/p/mogilefs/ (accessed 25/06/2014)
[^ceph]:http://ceph.com/(accessed 25/06/2014)
[^sparkleshare]:http://sparkleshare.org/ (accessed 25/06/2014)
[^sparklegood]:http://sparkleshare.org/#good (accessed 25/06/2014)
[^sparklebad]:http://sparkleshare.org/#bad (accessed 25/06/2014)
[^git-annex]:Joey Hess, "Git Annex" (http://git-annex.branchable.com/) (Accessed 25/06/2014)
[^dropbox-secpaper]: Dhiru Kholia and Przemysław Wegrzyn, "Looking inside the (Drop) box", 7th USENIX Workshop on Offensive Technologies (http://0b4af6cdc2f0c5998459-c0245c5c937c5dedcca3f1764ecc9b2f.r43.cf2.rackcdn.com/12058-woot13-kholia.pdf) (accessed 24/0/2014)
[^dropbox-security1]: Matt Marshall, "Dropbox has become 'problem child' of cloud security" (http://venturebeat.com/2012/08/01/dropbox-has-become-problem-child-of-cloud-security/)(accessed 25/06/2014)
[^dropbox-security2]:Derek Newton, "Dropbox authentication: insecure by design" (http://dereknewton.com/2011/04/dropbox-authentication-static-host-ids/)(Accessed 25/06/2014)
[^dropbox-leak]: Graham Cluley, "Dropbox users leak tax returns, mortgage applications and more" (http://grahamcluley.com/2014/05/dropbox-box-leak/)(accessed 25/06/2014)
[^dropbox-hoard]: User 'IsThisTheRealLife', "DropBox is keeping 'permanently deleted' files for longer than the 30 day recovery limit.", (http://www.reddit.com/r/privacy/comments/1m60yp/dropbox_is_keeping_permanently_deleted_files_for/)(accessed 26/06/2014)
[^prism]:The Guardian, "The NSA Files" (http://www.theguardian.com/world/the-nsa-files)(accessed 25/06/2014)
[^dropbox-privacy]:"Dropbox Privacy Policy", (https://www.dropbox.com/privacy)(accessed 25/06/2014)

[^ms-dublin-usgov-handover]:Margi Murphy, "Microsoft must hand over customer data held in Dublin to US government" (http://www.computerworlduk.com/news/security/3514076/microsoft-must-hand-over-customer-data-held-in-dublin-us-government/)(accessed 25/06/2014)

[^raspi-java]: Eben Upton, "Oracle Java on Raspberry Pi" (http://www.raspberrypi.org/oracle-java-on-raspberry-pi) (accessed 24/06/2014)
[^git]:"Git Website" (http://git-scm.com/) (accessed 24/06/2014)
[^rsync-tech]:"The rsync algorithm" (http://rsync.samba.org/tech_report) (accessed 24/06/2014)
[^rsync]:"Rsync" (http://rsync.samba.org/) (accessed 24/06/2014)
[^bt-protocol]: "The BitTorrent Protocol Specification" (http://www.bittorrent.org/beps/bep_0003.html) (accessed 24/06/2014)
[^courgette]:http://dev.chromium.org/developers/design-documents/software-updates-courgette
[^inotify]: "inotify(7) - Linux man page" (http://linux.die.net/man/7/inotify)(alternatively try "man 7 inotify" on linux systems)
[^FileObserver]:Android API Reference, "FileObserver Class" (https://developer.android.com/reference/android/os/FileObserver.html) (accessed 24/06/2014)
[^PGP]:"Pretty Good Privacy" (http://www.cryptography.org/getpgp.htm) (accessed 24/06/2014)
