"""
Step 3: Text Cleaning.
Remove extra whitespace, normalize line breaks, merge broken sentences,
remove repeated spaces, trim formatting artifacts.
"""

import re


def clean_text(text: str) -> str:
    """
    Clean extracted text:
    - Remove extra whitespace
    - Normalize line breaks
    - Merge broken sentences (e.g. "word\nword" -> "word word" when no period)
    - Remove repeated spaces
    - Trim formatting artifacts
    """
    if not text or not text.strip():
        return ""

    # Replace multiple spaces with single space
    text = re.sub(r" +", " ", text)

    # Replace multiple newlines with double newline (paragraph break)
    text = re.sub(r"\n{3,}", "\n\n", text)

    # Merge broken sentences: "word\nword" where no period before newline
    # Split into paragraphs first
    paragraphs = text.split("\n\n")
    cleaned_paragraphs = []

    for para in paragraphs:
        # Within paragraph, replace single newlines with space (merge lines)
        lines = para.split("\n")
        merged = []
        for line in lines:
            line = line.strip()
            if not line:
                continue
            # If previous line didn't end with sentence-ending punctuation, merge
            if merged and merged[-1] and merged[-1][-1] not in ".!?":
                merged[-1] = merged[-1] + " " + line
            else:
                merged.append(line)
        cleaned_para = " ".join(merged)
        # Normalize spaces again
        cleaned_para = re.sub(r" +", " ", cleaned_para).strip()
        if cleaned_para:
            cleaned_paragraphs.append(cleaned_para)

    # Join paragraphs
    result = "\n\n".join(cleaned_paragraphs)

    # Remove any remaining excessive spaces
    result = re.sub(r" +", " ", result)
    result = re.sub(r"\n ", "\n", result)
    result = result.strip()

    return result
