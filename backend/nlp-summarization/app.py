"""
Flask API for NLP Summarization Service.
Accepts PDF files and returns structured summaries.
"""

import os
import tempfile
from pathlib import Path

from flask import Flask, request, jsonify
from flask_cors import CORS

from pipeline import summarize_pdf

app = Flask(__name__)
CORS(app)

UPLOAD_FOLDER = tempfile.gettempdir()
app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER
app.config["MAX_CONTENT_LENGTH"] = 50 * 1024 * 1024  # 50 MB


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok", "service": "nlp-summarization"})


@app.route("/summarize", methods=["POST"])
def summarize():
    """
    Accept PDF file (multipart/form-data, key: file) or file path (JSON: {"pdf_path": "..."}).
    Returns {"summary": "..."} or {"error": "..."}
    """
    pdf_path = None

    if request.is_json:
        data = request.get_json()
        pdf_path = data.get("pdf_path")
        if not pdf_path:
            return jsonify({"error": "Missing pdf_path in JSON body"}), 400
        if not Path(pdf_path).exists():
            return jsonify({"error": f"File not found: {pdf_path}"}), 404
    else:
        if "file" not in request.files:
            return jsonify({"error": "No file provided. Use 'file' form field or send JSON with pdf_path."}), 400
        file = request.files["file"]
        if file.filename == "":
            return jsonify({"error": "No file selected"}), 400
        if not file.filename.lower().endswith(".pdf"):
            return jsonify({"error": "File must be a PDF"}), 400

        # Save to temp and use path
        suffix = Path(file.filename).suffix or ".pdf"
        fd, pdf_path = tempfile.mkstemp(suffix=suffix)
        try:
            os.close(fd)
            file.save(pdf_path)
            summary_text = summarize_pdf(pdf_path)
            return jsonify({"summary": summary_text})
        finally:
            try:
                os.unlink(pdf_path)
            except OSError:
                pass

    try:
        summary_text = summarize_pdf(pdf_path)
        return jsonify({"summary": summary_text})
    except FileNotFoundError as e:
        return jsonify({"error": str(e)}), 404
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5001))
    app.run(host="0.0.0.0", port=port, debug=False)
