"""
Step 4: Hierarchical Chunking.
Split long documents into chunks of ~1200 words for transformer processing.
"""

from typing import List


def chunk_text(text: str, chunk_size: int = 1200, overlap: int = 0) -> List[str]:
    """
    Split document into chunks of approximately chunk_size words.
    chunk_size: target words per chunk (default 1200)
    overlap: optional overlap words between chunks (default 0)
    """
    if not text or not text.strip():
        return []

    words = text.split()
    chunks = []
    start = 0

    while start < len(words):
        end = min(start + chunk_size, len(words))
        chunk_words = words[start:end]
        chunk = " ".join(chunk_words)
        chunks.append(chunk)

        if overlap > 0 and end < len(words):
            start = end - overlap
        else:
            start = end

    return chunks
