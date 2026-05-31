/** Profile page: view/edit profile, upload photo; role-specific fields. */
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getProfile, updateProfile, uploadProfilePhoto } from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { User, Camera, Loader2, Users, GraduationCap } from 'lucide-react';
import { AuthImage } from '../components/AuthImage';
import { PageHeader } from '../components/ui/PageHeader';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Textarea } from '../components/ui/Textarea';
import { Alert } from '../components/ui/Alert';
import { Loader } from '../components/ui/Loader';

export function Profile() {
  const { user, refreshUser, isStudent, isFaculty } = useAuth();
  const { toast } = useToast();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploadingPhoto, setUploadingPhoto] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    fullName: '',
    contactNumber: '',
    bio: '',
    skills: '',
    interests: '',
    rollNumber: '',
    department: '',
    yearOfStudy: '',
    expertise: '',
    phoneNumber: '',
  });

  useEffect(() => {
    getProfile()
      .then((res) => {
        const p = res.data?.data;
        setProfile(p);
        setForm({
          fullName: p?.fullName || user?.fullName || '',
          contactNumber: p?.contactNumber || user?.contactNumber || '',
          bio: p?.bio || '',
          skills: (p?.skills || []).join(', '),
          interests: (p?.interests || []).join(', '),
          rollNumber: p?.rollNumber || '',
          department: p?.department || '',
          yearOfStudy: p?.yearOfStudy != null ? String(p.yearOfStudy) : '',
          expertise: p?.expertise || '',
          phoneNumber: p?.phoneNumber || '',
        });
      })
      .catch(() => setProfile(null))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps -- mount-only load; API response drives form
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        fullName: form.fullName?.trim() || undefined,
        contactNumber: form.contactNumber?.trim() || undefined,
        bio: form.bio?.trim() || undefined,
        skills: form.skills ? form.skills.split(',').map((s) => s.trim()).filter(Boolean) : undefined,
        interests: form.interests ? form.interests.split(',').map((s) => s.trim()).filter(Boolean) : undefined,
        rollNumber: form.rollNumber?.trim() || undefined,
        department: form.department?.trim() || undefined,
        yearOfStudy: form.yearOfStudy ? parseInt(form.yearOfStudy, 10) : undefined,
        expertise: form.expertise?.trim() || undefined,
        phoneNumber: form.phoneNumber?.trim() || undefined,
      };
      await updateProfile(payload);
      await refreshUser();
      const res = await getProfile();
      setProfile(res.data?.data);
      toast.success('Profile updated successfully');
    } catch (e) {
      const msg = e.response?.data?.error?.message || 'Failed to update profile';
      setError(msg);
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  const handlePhotoUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploadingPhoto(true);
    setError('');
    try {
      await uploadProfilePhoto(file);
      await refreshUser();
      const res = await getProfile();
      setProfile(res.data?.data);
      toast.success('Photo updated');
    } catch {
      const msg = 'Failed to upload photo';
      setError(msg);
      toast.error(msg);
    } finally {
      setUploadingPhoto(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader size="lg" />
      </div>
    );
  }

  const photoUrl = profile?.profilePictureUrl;
  const baseUrl = import.meta.env.VITE_API_URL || '';

  return (
    <div className="mx-auto max-w-2xl space-y-8">
      <PageHeader title="Profile" description="Manage your account details and visibility." />

      {error && <Alert variant="error">{error}</Alert>}

      <Card>
        <CardContent className="flex flex-col items-center gap-6 py-6 sm:flex-row">
          <div className="relative">
            <div className="flex h-28 w-28 items-center justify-center overflow-hidden rounded-full bg-mentor-surface">
              {photoUrl ? (
                <AuthImage src={baseUrl + photoUrl} alt="" className="h-full w-full object-cover" />
              ) : (
                <User className="h-14 w-14 text-mentor-primary" />
              )}
            </div>
            <label className="absolute bottom-0 right-0 cursor-pointer rounded-full bg-mentor-primary p-2 text-white shadow-md hover:bg-mentor-primary-dark disabled:opacity-50">
              <input
                type="file"
                accept="image/*"
                className="hidden"
                onChange={handlePhotoUpload}
                disabled={uploadingPhoto}
              />
              {uploadingPhoto ? <Loader2 className="h-4 w-4 animate-spin" /> : <Camera className="h-4 w-4" />}
            </label>
          </div>
          <div className="flex-1 text-center sm:text-left">
            <h2 className="text-xl font-semibold text-mentor-text">{profile?.fullName || user?.fullName || '—'}</h2>
            <p className="text-mentor-muted">{profile?.email || user?.email}</p>
            <p className="mt-1 text-sm text-mentor-primary">{profile?.department || user?.department || '—'}</p>
            {isFaculty && profile?.maxGroups != null && (
              <p className="mt-2 text-sm text-mentor-muted">
                Load: {profile.currentLoad ?? 0} / {profile.maxGroups} groups
              </p>
            )}
          </div>
        </CardContent>
      </Card>

      {isStudent && (profile?.group || profile?.assignedMentor) && (
        <div className="grid gap-4 sm:grid-cols-2">
          {profile?.group && (
            <Link to={`/groups/${profile.group.groupId}`}>
              <Card className="h-full transition hover:border-mentor-primary/40 hover:shadow-md">
                <CardContent className="py-5">
                  <div className="mb-1 flex items-center gap-2 font-medium text-mentor-text">
                    <Users className="h-5 w-5 text-mentor-primary" />
                    My group
                  </div>
                  <p className="text-sm text-mentor-muted">{profile.group.name}</p>
                  <p className="mt-1 text-xs text-mentor-primary">
                    {profile.group.projectTitle} • {profile.group.memberCount} members
                  </p>
                </CardContent>
              </Card>
            </Link>
          )}
          {profile?.assignedMentor && (
            <Card>
              <CardContent className="py-5">
                <div className="mb-1 flex items-center gap-2 font-medium text-mentor-text">
                  <GraduationCap className="h-5 w-5 text-mentor-primary" />
                  Assigned mentor
                </div>
                <p className="text-sm font-medium text-mentor-text">{profile.assignedMentor.name}</p>
                <p className="text-xs text-mentor-muted">{profile.assignedMentor.department}</p>
                <p className="mt-1 text-xs text-mentor-primary">{profile.assignedMentor.expertise}</p>
              </CardContent>
            </Card>
          )}
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Edit profile</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              type="text"
              label="Full name"
              value={form.fullName || ''}
              onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))}
            />
            <Input
              type="text"
              label="Contact number"
              value={form.contactNumber || ''}
              onChange={(e) => setForm((f) => ({ ...f, contactNumber: e.target.value }))}
              placeholder="+1 234 567 8900"
            />

            {(isStudent || isFaculty) && (
              <Input
                type="text"
                label="Department"
                value={form.department || ''}
                onChange={(e) => setForm((f) => ({ ...f, department: e.target.value }))}
                placeholder="Computer Science"
              />
            )}

            {isStudent && (
              <>
                <Input
                  type="text"
                  label="Roll number"
                  value={form.rollNumber || ''}
                  onChange={(e) => setForm((f) => ({ ...f, rollNumber: e.target.value }))}
                  placeholder="e.g. 21BCS001"
                />
                <Input
                  type="text"
                  label="Year of study"
                  value={form.yearOfStudy || ''}
                  onChange={(e) => setForm((f) => ({ ...f, yearOfStudy: e.target.value }))}
                  placeholder="1, 2, 3, or 4"
                />
              </>
            )}

            {isFaculty && (
              <>
                <Input
                  type="text"
                  label="Expertise"
                  value={form.expertise || ''}
                  onChange={(e) => setForm((f) => ({ ...f, expertise: e.target.value }))}
                  placeholder="Machine Learning, Web Dev"
                />
                <Input
                  type="text"
                  label="Phone number"
                  value={form.phoneNumber || ''}
                  onChange={(e) => setForm((f) => ({ ...f, phoneNumber: e.target.value }))}
                  placeholder="Faculty contact"
                />
              </>
            )}

            <Textarea
              label="Bio"
              value={form.bio || ''}
              onChange={(e) => setForm((f) => ({ ...f, bio: e.target.value }))}
              rows={3}
              placeholder="Brief introduction…"
            />

            {isStudent && (
              <>
                <Input
                  type="text"
                  label="Skills (comma separated)"
                  value={form.skills || ''}
                  onChange={(e) => setForm((f) => ({ ...f, skills: e.target.value }))}
                  placeholder="React, Python, NLP"
                />
                <Input
                  type="text"
                  label="Interests (comma separated)"
                  value={form.interests || ''}
                  onChange={(e) => setForm((f) => ({ ...f, interests: e.target.value }))}
                  placeholder="AI, Web development"
                />
              </>
            )}

            <Button type="submit" variant="primary" disabled={saving}>
              {saving ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Saving…
                </>
              ) : (
                'Save changes'
              )}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
