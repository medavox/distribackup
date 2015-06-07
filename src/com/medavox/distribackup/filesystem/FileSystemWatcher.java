package com.medavox.distribackup.filesystem;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.medavox.distribackup.peers.*;

/**Watches the supplied directory tree for changes to files.*/
public class FileSystemWatcher extends Thread
{
    Peer owner;
	private final WatchService watcher;
	private final Map<WatchKey,Path> keys;
	public ConcurrentSkipListSet<String> ignoreList = new ConcurrentSkipListSet<String>();
	private boolean trace = false;

	/**Creates a WatchService and registers the given directory*/
	public FileSystemWatcher(Path dir, Peer owner) throws IOException
	{
        this.owner = owner;
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new ConcurrentHashMap<WatchKey,Path>();

		System.out.format("Watching %s\n", dir);
		registerAll(dir);
		//System.out.println("Done.");

		//enable trace after initial registration
		this.trace = true;
	}

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event)
	{
		return (WatchEvent<T>)event;
	}

	/**Register the given directory with the WatchService*/
	public void register(Path dir) throws IOException
	{
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		if (trace)
		{
			Path prev = keys.get(key);
			if (prev == null)
			{
				System.out.format("register: %s\n", dir);
			}
			else
			{
				if (!dir.equals(prev))
				{
					System.out.format("update: %s -> %s\n", prev, dir);
				}
			}
		}
		keys.put(key, dir);
	}

	/**Recursively register the directory with the WatchService.*/
	private void registerAll(Path start) throws IOException
    {
		//register directory and sub-directories
		Files.walkFileTree(start, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
			{
				register(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**Process all events for keys queued to the watcher
	 */
	public void run()
	{
		while(owner.threadsEnabled)
		{
			//wait for key to be signalled
			WatchKey key;
			try
			{
				key = watcher.take();//this blocks until a key is available
			}
			catch (InterruptedException x)
			{
				x.printStackTrace();
				return;
			}

			Path dir = keys.get(key);
			if (dir == null)
			{
				System.err.println("WatchKey not recognised!");
				continue;
			}

			for (WatchEvent<?> event: key.pollEvents())
			{
				//we have no idea what to do in the case of an overflow
				if (event.kind() == OVERFLOW)
				{//ignore it and carry on
					continue;
				}

				//Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);
				
				Path rel = owner.root.relativize(child);
				//System.out.print("ignoreList length:"+ignoreList.size());
				//System.out.println("ignoreList tested path:"+rel.toString());
				
				//the good news is that this actually worked
				//the bad news is that >1 event is fired off during receiveFileDataChunk
				String entry = event.kind().name()+":"+rel.toString();
				//System.out.println("ignore List contains file:"+ignoreList.contains(entry));
				if(ignoreList.contains(entry))//TODO: make it work
				{//let it off this once
					//System.out.println("ignored "+event.kind().name()+" for \""+child+"\".");
					ignoreList.remove(rel.toString());
				}
				else
				{
					//print out event
					System.out.format("%s: %s\n", event.kind().name(), child);
	                //System.out.println("name:"+name);
	                
	                //callback to main program thread, letting it know about this
	                owner.fileChanged(child, event.kind().name());
				}
                
				//if a directory is created, then register it recursively too
				if (event.kind() == ENTRY_CREATE)
				{
					try 
					{
						if (Files.isDirectory(child, NOFOLLOW_LINKS))
						{
							registerAll(child);
						}
					} 
					catch (IOException ioe)
					{//TODO
                        ioe.printStackTrace();
                        System.exit(1);
					}
				}
			}

			//reset key
            //remove it from the map if its directory is no longer accessible
			boolean valid = key.reset();
			if (!valid)
			{
				keys.remove(key);
				//all directories are inaccessible
				if (keys.isEmpty())
				{
                    System.err.println("ERROR: All directories are inaccessible!");
                    System.exit(1);
					break;
				}
			}
		}
	}
}
