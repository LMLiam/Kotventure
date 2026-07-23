package io.github.lmliam.kotventure.core.virtual;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.VirtualComponentRenderer;

final class NullVirtualComponentRenderer implements VirtualComponentRenderer<Viewer> {
  @Override
  public ComponentLike apply(final Viewer context) {
    return null;
  }
}
