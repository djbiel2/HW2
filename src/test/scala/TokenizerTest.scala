import org.scalatest.funsuite.AnyFunSuite

class TokenizerTest extends AnyFunSuite {

  test("Tokenizer should generate tokens for a given text") {
    val shardPath = "testResources/sampleShard.txt"

    val tokenized = Tokenizer.tokenizeShard(shardPath)
    assert(tokenized.size() > 0, "Tokenizer should generate tokens for non-empty text.")
  }

  test("Tokenizer should handle empty shard file gracefully") {
    val shardPath = "testResources/emptyShard.txt"

    val tokenized = Tokenizer.tokenizeShard(shardPath)
    assert(tokenized.isEmpty, "Tokenizer should return an empty token list for an empty shard.")
  }
}
