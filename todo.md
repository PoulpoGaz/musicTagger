* update "Music" metadata after downloading
* REFUSED: don't use yt-dlp to write metadata
  * yt-dlp handles properly metadata with a trick (see YTDLP#setMetadata)
* better internal template handling (saving internal template without having to rename it)
* DONE: template: allow creation of new metadata key based on yt-dlp expression
* ui:
  * TemplateTable: disable menu items when clicking outside the table
  * DONE: disable menu items when right+click on template tab
* DONE: reduce memory consumption by loading only thumbnails or loading images lazily
  * DONE: reuse OggPage and MetadataPicture data