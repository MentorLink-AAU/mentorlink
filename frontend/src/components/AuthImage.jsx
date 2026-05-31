/** Fetches auth-protected images with JWT; shows placeholder on error. */
import { useState, useEffect } from 'react';

export function AuthImage({ src, alt, className }) {
  const [objectUrl, setObjectUrl] = useState(null);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!src || !src.includes('/api/')) {
      const id = requestAnimationFrame(() => {
        setObjectUrl(src);
        setError(false);
      });
      return () => cancelAnimationFrame(id);
    }
    const token = localStorage.getItem('mentorlink_token');
    if (!token) {
      const id = requestAnimationFrame(() => setError(true));
      return () => cancelAnimationFrame(id);
    }
    let url = null;
    fetch(src, { headers: { Authorization: `Bearer ${token}` } })
      .then((r) => {
        if (!r.ok) throw new Error();
        return r.blob();
      })
      .then((blob) => {
        url = URL.createObjectURL(blob);
        setObjectUrl(url);
      })
      .catch(() => setError(true));
    return () => {
      if (url) URL.revokeObjectURL(url);
    };
  }, [src]);

  if (error || !objectUrl) {
    return (
      <div className={`bg-blue-100 flex items-center justify-center ${className}`}>
        <span className="text-blue-600 text-2xl">?</span>
      </div>
    );
  }
  return <img src={objectUrl} alt={alt || ''} className={className} />;
}
