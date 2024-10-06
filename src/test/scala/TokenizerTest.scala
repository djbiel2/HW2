import org.scalatest.funsuite.AnyFunSuite

class TokenizerTest extends AnyFunSuite {

  test("Tokenizer test generate tokens") {
    val shardPath = "testResources/sample.txt"

    val tokenized = Tokenizer.tokenize_shard(shardPath)
    assert(tokenized.size() > 0, "Any non zero should return with tokens")
  }

  test("Empty token test") {
    val shardPath = "testResources/emptyShard.txt"

    val tokenized = Tokenizer.tokenize_shard(shardPath)
    assert(tokenized.isEmpty, "Empty token test")
  }
}
