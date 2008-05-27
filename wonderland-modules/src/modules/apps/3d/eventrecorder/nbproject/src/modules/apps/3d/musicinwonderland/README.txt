
MUSIC IN WONDERLAND
-------------------

Music in Wonderland (MIW) creates a space for collaboratively listening to music
using the Project Wonderland toolkit.  It is not a complete application,
but shows some of the interactive features you can build with the 
Wonderland toolkit.

INSTALLING MUSIC IN WONDERLAND
------------------------------

Music in Wonderland consists of two peices: the Music in Wonderland cell
and the music service.  The cell is a normal Wonderland cell that can be
placed in the world to display the MIW user interface.  The music service
provides information about the set of albums and tracks the system knows 
about.  It is implemented as a Darkstar service, and must be installed
separately before the MIW cell is installed.  Additionally, both the MIW
cell and the music service rely on an album database, which is an XML
file (described below) that gives information about what albums are
available.

To install Music in Wonderland, you will first need an album database.  We
do not provide a default database, so you will need to create one yourself.
See below for information on the album database format.  Once you have
an album database, you will need to put it somewhere web-accessible, so
both the Wonderland server and Wonderland clients can access it from a 
URL.

Next, you need to set up the music service in your Darkstar server.  To do
this, find the WonderlandMain.cfg file.  If you are using Wonderland from
a source workspace, the file will be in lg3d-wonderland/src/darkstar_config.
If you install Wonderland from binaries, this file will be in the 
<Wonderland install dir>/config.  Edit this file to add the following:

---
com.sun.sgs.app.services=com.sun.mpk20.voicelib.impl.service.voice.VoiceServiceImpl:com.sun.labs.miw.service.MusicServiceImpl
com.sun.sgs.app.managers=com.sun.mpk20.voicelib.impl.app.VoiceManagerImpl:com.sun.labs.miw.service.MusicManagerImpl

// Music in Wonderland
com.sun.labs.miw.service.musicDatabase=file:/home/jkaplan/miw/album_info.xml
---

You will need to set the location of the musicDatabase to point to an
actual album database file.  This can be a local URL (as shown here) or a 
regular http URL.

Finally, install the MIW cell by copying miw-wlc.xml from this directory
into your current Wonderland world.  By default, the current Wonderland
world is in the lg3d-wonderland/src/world/default-wfs directory if you are
building from source, or in <Wonderland install dir>/worlds/default-wfs
for a binary build.  You will then need to modify miw-wlc.xml to add the
following information:

---
<void property="albumListURL">
  <object class="java.net.URL">
    <string>http://localhost/miw/album_info.xml</string>
  </object>
</void>
---

Change the URL to the public URL of your album database file.  You may also
want to change the origin, so Music in Wonderland does not show up in the
middle of another world.

After all these changes, when you restart Wonderland you should see Music
in Wonderland.

ALBUM DATABASE FILE
-------------------

The album database contains information about which albums and tracks are
available to the music service.  The format is as follows:

---
<?xml version="1.0" encoding="ISO-8859-1" ?>
<AllAlbums>
  <Album>
    <artist>Sample Artist Name</artist>
    <cover_small>http://my.music.server/Sample%20Artist/Sample%20Album/cover.jpg</cover_small>
    <albumname>Sample Album</albumname>

    <Track>
      <trackname>Sample Track</trackname>
      <tracknum>1</tracknum>
      <seconds>49</seconds>
      <url>http://my.music.server/Sample%20Artist/Sample%20Album/Track-1.mp3</url>
    </Track>

    <Track>
      ...
    </Track>
  </Album>
  <Album>
    ...
  </Album>
</AllAlbums>
---

PLAYING MUSIC
-------------

Currently, Music in Wonderland does not actually play songs, it just shows
the MIW user interface.  The ability to playing songs from MP3 files will 
be added soon.
