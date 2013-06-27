r-a-dio-android
===============


***Current Features/Behavior/Progress is listed with made up categories. Screenshot is the current progress with the layout.***
<img src="http://i.imgur.com/eKDCCPg.png"
 alt="screenshot" title="Current Look" align="right" />
 
<img src="http://i.imgur.com/tqgS8j6.png"
 alt="screenshot" title="Current Look" align="right" />

> * Notifications Display
  - Now playing song 
  - DJ   
* Main Application Activity Display (vertical scroll)
  - DJ
  - Progress Bar
  - Now Playing Song
  - Queue
  - Last Played
  - Song Length
  - Listener Count
* Home Screen Widget
  - Updates and displays progressbar, songinfo, and song length. 
  - Currently the update code is just awful. (Calls r/a/dio API every second and etc.) I feel like I barfed the widget
  ontop of the RadioService class and stapled the two together with a staplegun only to find out later that the
  staplegun was a nailgun and the staples were nail.
* Behaviors Include
  - Playback upon opening application.
  - Updates information and progressbar in the background every 10 seconds with the r/a/dio API.
  - Progressbar and song length in Main Application Activity Display updates every second with estimation.  
* Interactions Include
  - Play button
  - Pause button


