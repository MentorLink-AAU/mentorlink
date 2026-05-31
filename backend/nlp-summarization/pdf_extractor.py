"""
Step 1: PDF Text Extraction using PyMuPDF.
Extracts text from PDF, removes page numbers and header/footer artifacts,
preserves paragraph structure.
"""

import re
from pathlib import Path
from typing import Optional

try:
    import fitz  # PyMuPDF
except ImportError:
    fitz = None


def extract_text_from_pdf(pdf_path: str) -> str:
    """
    Open PDF, read all pages, extract text, merge into single string.
    Removes page numbers and common header/footer patterns.
    """
    if fitz is None:
        raise ImportError("PyMuPDF (fitz) is required. Install with: pip install pymupdf")

    path = Path(pdf_path)
    if not path.exists():
        raise FileNotFoundError(f"PDF not found: {pdf_path}")

    doc = fitz.open(str(path))
    pages_text = []

    for page_num in range(len(doc)):
        page = doc[page_num]
        text = page.get_text()

        # Remove common page number patterns (e.g., "1", "Page 1 of 10", "- 1 -")
        text = re.sub(r"\n\s*[-]?\s*\d+\s*[-]?\s*\n", "\n", text)
        text = re.sub(r"Page\s+\d+\s+of\s+\d+", "", text, flags=re.IGNORECASE)
        text = re.sub(r"^\d+\s*$", "", text, flags=re.MULTILINE)

        pages_text.append(text)

    doc.close()

    return "\n\n".join(pages_text)
