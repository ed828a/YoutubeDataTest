package com.dew.edward.youtubedatatest.model

class SearchVideoResponse(val prevPageToken: String,
                          val nextPageToken: String,
                          val pageInfo: PageInfo,
                          var items: List<Item>) {
    class Item(val id: ID,
               val snippet: Snippet) {
        class ID(val kind: String,
                 val videoId: String)

        class Snippet(val publishedAt: String,
                      val title: String,
                      val thumbnails: Thumbnails) {
            class Thumbnails(val high: High) {
                class High(val url: String)
            }
        }
    }
    class PageInfo(val totalResults: String,
                   val resultsPerPage: String)
}