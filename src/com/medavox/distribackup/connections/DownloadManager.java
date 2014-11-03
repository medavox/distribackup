import java.io.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import org.jsoup.*;
import java.io.PrintStream;
import java.net.*;
import java.util.regex.*;
import java.util.*;
import javax.swing.*;
/*TODO
separate and sensible notifcations for begin and end of large downloads, eg videos
possibly a max connections limit, plus a download queue?
*/
public class DownloadManager
{
	//final String downloadFolder = "blogs";
	private PrintStream o = System.out;
    int imagesDownloaded = 0;
    final int RETRY_LIMIT = 3;
    final int MAX__CONSECUTIVE_FRUITLESS_PAGES = 20;
    final int MIN_POSTS_PER_PAGE = 5;
    int consecutiveFruitlessPages = 0;
    Map<String, String> localImages = new Hashtable<String, String>();
    String outputFolderName;

    

	/**Looks through all the image URLs found, and weeds out any smaller dupes
	@return A list of URLs to be downloaded*/
	private String[] dupFinder(String[][] imageURLs)
	{
		Map<String, String> entries = new Hashtable<String, String>();
		for(int i = 0; i < imageURLs.length; i++)
		{
            if(imageURLs[i] == null)
                continue;//failed post-page retrievals cause null entries
            for(int j = 0; j < imageURLs[i].length; j++)
			{
				String imgUrl = imageURLs[i][j];
				String hash = getImageHash(imgUrl);
				if((!entries.containsKey(hash)//if there's no image with this "name",
				|| getImageSize(entries.get(hash)) < getImageSize(imgUrl))//or if this version is bigger,
                && !localImages.containsKey(hash))//or there's no local copy
				{//add this one to the table
					entries.put(hash, imgUrl);
				}//otherwise, leave it out!
			}
		}
		//get a string array from the hashtable
		Collection<String> out1 =  entries.values();
		String[] results = new String[out1.size()];
		results = out1.toArray(results);
		o.println("unique image URLs: "+results.length);
		return results;
	}
	/**Gets unique(?) alphanumeric string from image's URL*/
	String getImageHash(String url)
	{
		Pattern patrick = Pattern.compile("tumblr_([a-zA-Z0-9]{17,19})");
		Matcher cherche = patrick.matcher(url);
		if(cherche.find())//if we found something
		{
			String match = url.substring(cherche.start(), cherche.end());
			return cherche.group(1);
		}
		return "";//elsewise return an empty string
	}
	
	int getImageSize(String url) throws NumberFormatException
	{
		Pattern patrick = Pattern.compile("tumblr_[a-zA-Z0-9]{17,19}_([0-9]+)");
		Matcher cherche = patrick.matcher(url);
		if(cherche.find())//if we found something
		{
			String match = url.substring(cherche.start(), cherche.end());
			return Integer.parseInt(cherche.group(1));
		}
		return -1;//default to fail
	}

	/*This is the hardest part to make reliable. Tumblr is a lot less internally consistent than it looks.*/
	/**Finds one or more URLs in the supplied document, according to supplied regex.*/
	String[] findURLsInDoc(String page, Pattern reggie)
	{
		Set<String> matches = new HashSet<String>();//store in a set to remove exact duplicates
		
		Matcher cherche = reggie.matcher(page);
		int i = 0;
		while(cherche.find())//if we found something
		{
			String match = page.substring(cherche.start(), cherche.end());
			
			matches.add(match);
			i++;
		}
		//cast the set to an array, and return
		String[] retval = new String[matches.size()];
		return matches.toArray(retval);
	}
	/**Hooray for the Lazy Exception Handler! Even its acronym sounds half-arsed!*/
	static void lazyExceptionHandler(Exception e)
    {
        System.err.println("ERROR! meh, just dump it and exit.");
        e.printStackTrace();
        System.exit(1);
    }
	/**Get the actual HTTP error code int from the stupidly verbose IOException message
	@param ioe The exception we can't be bothered dealing with
	@return nothing really, as this method exits the program with status code 1.*/

    /**Downloads files.
     * @param src The URL to download
     * @param pageMode if true, URL points to an html page; return its contents as a string.
     * If false, save the file at the URL to disk.
     * @return in pageMode, returns a the contents of the html page at the URL. otherwise, returns "success"*/
    String download(String src, boolean pageMode)
    {
		int retries = 0;
		int indexname = src.lastIndexOf("/");
		String name = src.substring(indexname+1, src.length());

		File destFile = new File(outputFolderName + File.separator + name);
		if(!pageMode)
		{//print out where the image will be saved
			System.out.println(outputFolderName + File.separator + name);
		}
	retry:
		while(true)//keep trying to download the page until successful,
		{//retrying on SocketTimeoutException or IOException
			try
			{
				if(pageMode)
				{
					return Jsoup.connect(src).get().toString();
				}
				else
				{
					URL url = new URL(src);//Open URL Stream
					InputStream in = url.openStream();
					OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile));
					//new chunk downloading seems faster
					byte[] chunk = new byte[4096];//chunk size - currently 4KB
					int bytesRead = 0;
					while ((bytesRead = in.read(chunk)) != -1)
					{
						out.write(chunk, 0, bytesRead);
					}
					in.close();
					out.close();
					localImages.put(getImageHash(name), outputFolderName + File.separator + name);
					return "success";
				}
			}
			catch(SocketTimeoutException stoe)
			{
				System.err.println("TIMEOUT getting "+src+"; trying again...");
			}
            catch(Exception e)//IOExceptions may be http-related, but also may not
            {
				lazyExceptionHandler(ioe);
            }
		}
		return "error";
	}
}
/**Downloads an image from the supplied URL, and saves it to disk, in the supplied named folder.
 * @param outputFolderName the name for the folder to save images to.
 *     Program will halt if there is a file with that name.
 * @param src URL of image to download. Downloaded image will have this filename.
 * @return true if image with the derived filename already exists, false otherwise */
class Downloader extends Thread
{
	String src;
	String outputFolderName;
	TumblRaidr tr;
	public Downloader(String outputFolderName, String src, TumblRaidr tr)
	{
		this.outputFolderName = outputFolderName;
		this.src = src;
		this.tr = tr;
	}
	
	public void run()
	{
		tr.download(src, false);
	}
}

/**Looks for media links in a post page.
 * Gets the URLs of all images on a post page
	 * Now handles videos too!*/
class PostPageProcessor extends Thread
{
	String postURL;
	String[] links;
	TumblRaidr tr;
	int i;
	public PostPageProcessor(String postURL, String[] links, int i, TumblRaidr tr)
	{
		this.postURL = postURL;
		this.links = links;
		this.tr = tr;
		this.i = i;
	}
	public String[] getLinks()
	{
		return links;
	}
	public void run()
	{
		String postPage = tr.download(postURL, true);
		if(postPage.equals("error"))//failed to retrieve post page,
		{//and other error catching failed
			//System.err.println("ERROR: failed to download "+postURL);
			return;//move on
		}
		
		Pattern imgPat = Pattern.compile("http://\\d*\\.?media\\.tumblr\\.com/[a-z0-9]{0,32}[/]?tumblr_(inline)?.{17,19}(_(200|250|400|500|700|1280))?\\.(jpg|png|gif)");
		String[] imgLinks = tr.findURLsInDoc(postPage, imgPat);//get image links for post
		links = imgLinks;//if there are videos, this is overwritten

		
		//totalURLsFound += mediaLinks[i].length;
		int il = imgLinks.length;
		String equiWidth = ("    " + i).substring((""+i).length());
		//if there's some vid and/or img links, print how many, iff not, print "files: 0"
		System.out.println("post "+equiWidth+" ("+postURL+") "+(il > 0 ? "images: "+il : "")+vidPrint+(vl <1 && il < 1 ? "files:  0" : ""));
	}
}
