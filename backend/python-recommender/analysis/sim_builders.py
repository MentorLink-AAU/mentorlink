"""
Matrix Factorization-only similarity builder.
"""

from __future__ import annotations

import numpy as np
from sklearn.decomposition import NMF

from common_io import build_skill_matrix, cosine_similarity_matrix


def matrix_factorization_similarity(students, _faculty, latent_dim: int = 8):
    """Build student similarity using NMF latent vectors + cosine similarity."""
    x = build_skill_matrix(students)
    if x.size == 0:
        return np.zeros((0, 0), dtype=float)

    n, d = x.shape
    components = max(1, min(latent_dim, n, d))

    try:
        model = NMF(n_components=components, init="nndsvda", random_state=42, max_iter=600)
        w = model.fit_transform(np.maximum(x, 0.0))
        if w.shape[1] == 0:
            w = x
    except Exception:
        w = x
    return cosine_similarity_matrix(w)
