package com.melikenurozun.webtoapp.data.remote.model

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Attribute

@Root(name = "rss", strict = false)
data class RssFeed @JvmOverloads constructor(
    @field:Element(name = "channel")
    var channel: RssChannel = RssChannel()
)

@Root(name = "channel", strict = false)
data class RssChannel @JvmOverloads constructor(
    @field:Element(name = "title", required = false)
    var title: String? = null,

    @field:Element(name = "description", required = false)
    var description: String? = null,

    @field:ElementList(name = "item", inline = true, required = false)
    var items: List<RssItem>? = null
)

@Root(name = "item", strict = false)
data class RssItem @JvmOverloads constructor(
    @field:Element(name = "title", required = false)
    var title: String? = null,

    @field:Element(name = "description", required = false)
    var description: String? = null,

    @field:Element(name = "link", required = false)
    var link: String? = null,

    @field:Element(name = "pubDate", required = false)
    var pubDate: String? = null,

    @field:Element(name = "thumbnail", required = false)
    var thumbnail: RssImage? = null
)

@Root(name = "thumbnail", strict = false)
data class RssImage @JvmOverloads constructor(
    @field:Attribute(name = "url", required = false)
    var url: String? = null
)
