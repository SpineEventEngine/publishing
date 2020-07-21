package io.spine.publishing.github

import java.net.URL

data class RepositoryName(val value: String)
data class Branch(val name: String)
data class RemoteRepository(val url: URL, val name: RepositoryName) {

}
