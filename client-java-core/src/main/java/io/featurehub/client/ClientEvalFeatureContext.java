package io.featurehub.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * This class is ONLY used when we are doing client side evaluation. So the edge service stays the same.
 */
class ClientEvalFeatureContext extends BaseClientContext {
  private final EdgeService edgeService;
  private final boolean weOwnRepositoryAndEdge;

  public ClientEvalFeatureContext(FeatureHubConfig config, FeatureRepositoryContext repository,
                                  EdgeService edgeService) {
    super(repository, config);

    this.edgeService = edgeService;
    this.weOwnRepositoryAndEdge = false;
  }

  // this doesn't matter for client eval
  @Override
  public Future<ClientContext> build() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        edgeService.contextChange(null).get();
      } catch (InterruptedException|ExecutionException ignored) {
      }

      return this;
    });
  }

  @Override
  public FeatureState feature(String name) {
    return repository.getFeatureState(name).withContext(this);
  }

  @Override
  public EdgeService getEdgeService() {
    return edgeService;
  }

  /**
   * We only close if we created the edge and repository
   */
  @Override
  public void close() {
    if (weOwnRepositoryAndEdge) {
      repository.close();
    }

    edgeService.close();
  }
}
