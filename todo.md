* DONE: update "Music" metadata after downloading
* REFUSED: don't use yt-dlp to write metadata
  * yt-dlp handles properly metadata with a trick (see YTDLP#setMetadata)
  * COUNTER: it's not possible with yt-dlp to write multiple values to the same field
* DONE: better internal template handling (saving internal template without having to rename it)
* DONE: template: allow creation of new metadata key based on yt-dlp expression
* ui:
  * TemplateTable
    * DONE: disable menu items when clicking outside the table
    * add select all
    * DONE: add mass move of metadata -> move/swap all values associated with metadata 'M1' to 'M2'
  * DONE: disable menu items when right+click on template tab
* DONE: reduce memory consumption by loading only thumbnails or loading images lazily
  * DONE: reuse OggPage and MetadataPicture data
* DONE: save to json
  * DONE: with sub menu to only save musics that aren't downloaded
* DONE: load from json
* DONE: save metadata of already downloaded musics
* better error handling
  * create OpusException/OggException
  * show error to user
* allow user to add more options to yt-dlp
* mass move of musics
* add tests -_-
* filenaming:
  * more functions
  * check function semantics
  * if should probably a keyword
    * if may not always return a value
    * having a special FOp may be useful for only evaluating the condition than the appropriate expression