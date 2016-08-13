Need to find an acceptable compromise between

1. Sending updates immediately upon file changes, which wastes bandwidth & processing on "incomplete" changes (publishing user hasn't finished editing),
but ensures file are always fresh
2. Sending updates periodically on a timer (during user activity), 
which reduces bandwidth wasted on unnecessary transfers
(after the publishing user goes back on that big change),
but files becomes out-of-date for subscribers

How about a cooldown timer?

An update is packaged and announced 2 minutes (for instance) after the last file modification.

Say, after each file change, a timer is reset to 2 minutes.

If that timer reaches 0, the user is considered to have finished editing for now,
and a network update announcement is triggered.
