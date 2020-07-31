package io.spine.publishing

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import io.spine.publishing.given.PipelineTestEnv.CollectingOperation
import io.spine.publishing.given.PipelineTestEnv.ErroringOperation
import io.spine.publishing.given.PipelineTestEnv.ThrowingOperation
import io.spine.publishing.given.PipelineTestEnv.sampleLibrary
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("`PublishingPipeline` should")
class PublishingPipelineTest {

    @Test
    @DisplayName("terminate if one of the operations throws an exception")
    fun endOnException() {
        val pipeline = pipeline(CollectingOperation(), ThrowingOperation)
        val result = pipeline.eval()
        assertThat(result).isInstanceOf(Error::class)
        assertThat((result as Error).exception!!).isInstanceOf(IllegalStateException::class)
    }

    @Test
    @DisplayName("terminate if one of the operations returns an error")
    fun endOnError() {
        val pipeline = pipeline(CollectingOperation(), ErroringOperation)
        val result = pipeline.eval()
        assertThat(result).isInstanceOf(Error::class)
        assertThat((result as Error).exception).isNull()
    }

    @Test
    @DisplayName("end with `Ok` if none of the operations throw")
    fun ok() {
        val firstCollecting = CollectingOperation()
        val secondCollecting = CollectingOperation()

        val result = pipeline(firstCollecting, secondCollecting).eval()
        assertThat(result).isInstanceOf(Ok::class)
        assertThat(firstCollecting.seenLibraries()).containsOnly(sampleLibrary)
        assertThat(secondCollecting.seenLibraries()).containsOnly(sampleLibrary)
    }

    private fun pipeline(vararg operations: PipelineOperation) =

            PublishingPipeline(setOf(sampleLibrary), operations.toList())
}
