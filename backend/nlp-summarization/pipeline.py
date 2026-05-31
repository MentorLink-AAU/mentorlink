"""
Full NLP Pipeline: PDF -> Structured Summary.
Orchestrates extraction, filtering, cleaning, chunking, summarization, post-processing.
"""

from pathlib import Path
from typing import Optional

from pdf_extractor import extract_text_from_pdf
from section_filter import filter_irrelevant_sections
from text_cleaner import clean_text
from chunker import chunk_text
from summarizer import run_full_summarization
from post_processor import post_process_summary


def summarize_pdf(pdf_path: str, chunk_size: int = 1200) -> str:
    """
    Full pipeline:
    1. Extract text from PDF
    2. Filter irrelevant sections
    3. Clean text
    4. Chunk document
    5. Summarize chunks + final pass
    6. Post-process
    """
    # Step 1: Extract
    raw_text = extract_text_from_pdf(pdf_path)
    if not raw_text or len(raw_text.strip()) < 100:
        return "Insufficient text extracted from PDF. The document may be image-based or empty."

    # Step 2: Filter
    filtered = filter_irrelevant_sections(raw_text)
    if not filtered.strip():
        filtered = raw_text  # Fallback if too much was filtered

    # Step 3: Clean
    cleaned = clean_text(filtered)

    # Step 4: Chunk
    chunks = chunk_text(cleaned, chunk_size=chunk_size)
    if not chunks:
        return "No content remained after processing."

    # Steps 5-10: Summarize
    summary = run_full_summarization(chunks)

    # Step 11: Post-process
    summary = post_process_summary(summary)

    return summary
