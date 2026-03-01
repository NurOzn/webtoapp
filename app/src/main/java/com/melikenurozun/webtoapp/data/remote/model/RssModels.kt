package com.melikenurozun.webtoapp.data.remote.model

data class RssFeed(
    var channel: RssChannel = RssChannel()
)

data class RssChannel(
    var title: String? = null,
    var description: String? = null,
    var items: List<RssItem> = emptyList()
)

data class RssItem(
    var title: String? = null,
    var description: String? = null,
    var link: String? = null,
    var pubDate: String? = null,
    var thumbnail: RssImage? = null
)

data class RssImage(
    var url: String? = null
)
