#Lazer exporter
A utility for importing and exporting beatmap list, as well as extracting songs and beatmaps installed in osu!Lazer
##Disclaimer
The author of this application is affiliated with ppy Pty Ltd.

"osu!" and "ppy" are trademarks of ppy Pty Ltd.

###Login requirement

The download beatmap feature requires logging in using your account. Please note that the application does not store any of your credentials and they will only be sent to [osu.ppy.sh](https://osu.ppy.sh).

If you are not comfortable of using your account on a third party application, the application provides official links to the beatmap sets as an alternative.

###Exporting songs

This program only copies, and depending on the settings made by the user, converts and modifies the ID3 tags of songs that are already stored the user's (The individual operating this program) computer. The copied and/or modified files are stored in the user's device only. These songs are intended for personal use only but can be manipulated by the user in any way they wish. The author of this program cannot prevent the user from performing any copyright infringing actions with the files produced by the program. Therefore, in no event shall the author be liable for any copyright infringement claims. The user should only use these files under the confines of copyright laws.

In short,

Do not use this program for distributing songs illegally. The creator of this program is not responsible for such actions performed by the user of the program.

##Features
* Import and export beatmap list
* Download beatmaps directly from osu server after importing a list (Requires osu account login)
* Export installed beatmaps back to .osz format
* Export songs that are installed in the game
    * Proper file renaming (Romanji/Unicode/Rename after the beatmap ID)
    * Adding MP3 tags based on beatmap info
    * Filter practice beatmaps
    * Filter duplicates based on song length
    * Conversion from ogg to mp3 (Done through FFmpeg)
    
##Building
The artifact can be built through standard Intellij tools. Additional libraries that are not in maven are stored in /lib. Please check the README.md in /lib for more information.

##Downloads
Go to the release tab

##License
```
Copyright © 2018 Ringosham
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 ```

##Dependencies
[JAVE](http://www.sauronsoftware.it/projects/jave/index.php) - Java Audio Video Encoder, also a wrapper for FFmpeg - Under GNU GPL v3 license

[mp3agic](https://github.com/mpatric/mp3agic) - Java library for reading/manipulating ID3 tags - Under MIT License

[SQLite JDBC](https://xerial.org/) - SQLite JDBC library - Under Apache 2.0 license