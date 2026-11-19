import { motion, AnimatePresence } from 'framer-motion';
import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getUserProfile } from '../api/client';
import {
  User, Mail, Shield, Calendar, FileText, AlertTriangle, ArrowRight, ExternalLink, CheckCircle2, Trash2, Loader2,
} from 'lucide-react';

function GoogleIcon({ className = 'w-4 h-4' }) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true">
      <path
        fill="#4285F4"
        d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
      />
      <path
        fill="#34A853"
        d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
      />
      <path
        fill="#FBBC05"
        d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
      />
      <path
        fill="#EA4335"
        d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
      />
    </svg>
  );
}

function GithubIcon({ className = 'w-4 h-4' }) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
      <path
        fillRule="evenodd"
        clipRule="evenodd"
        d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.53 1.032 1.53 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"
      />
    </svg>
  );
}

export default function ProfilePage() {
  const { userEmail, deleteAccount } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modal and deletion state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [confirmationInput, setConfirmationInput] = useState('');
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState(null);

  useEffect(() => {
    document.title = 'Account Profile - ResumeLens';
    let isMounted = true;

    getUserProfile()
      .then((data) => {
        if (!isMounted) return;
        setProfile(data);
        setLoading(false);
      })
      .catch((err) => {
        if (!isMounted) return;
        setError(err.message || 'Failed to load user profile.');
        setLoading(false);
      });

    return () => {
      isMounted = false;
    };
  }, []);

  const handleDeleteConfirm = async (e) => {
    e.preventDefault();
    if (confirmationInput.trim() !== 'DELETE') return;

    setDeleting(true);
    setDeleteError(null);

    try {
      await deleteAccount();
      navigate('/login?deleted=true');
    } catch (err) {
      setDeleteError(err.message || 'Failed to delete account. Please try again.');
      setDeleting(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      });
    } catch {
      return dateStr;
    }
  };

  const getInitial = () => {
    if (profile?.name && profile.name.trim()) {
      return profile.name.trim().charAt(0).toUpperCase();
    }
    if (userEmail && userEmail.trim()) {
      return userEmail.trim().charAt(0).toUpperCase();
    }
    return 'U';
  };

  const renderProviderBadge = () => {
    const provider = profile?.provider || 'LOCAL';
    if (provider === 'GOOGLE') {
      return (
        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-white/5 border border-white/10 text-white">
          <GoogleIcon className="w-3.5 h-3.5" />
          <span>Google Account</span>
        </span>
      );
    }
    if (provider === 'GITHUB') {
      return (
        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-white/5 border border-white/10 text-white">
          <GithubIcon className="w-3.5 h-3.5 text-white" />
          <span>GitHub Account</span>
        </span>
      );
    }
    return (
      <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-white/5 border border-white/10 text-neutral-200">
        <Shield className="w-3.5 h-3.5 text-[#E8602E]" />
        <span>Email & Password</span>
      </span>
    );
  };

  return (
    <div className="w-full max-w-6xl xl:max-w-[1200px] mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-10">
      <div className="space-y-6 sm:space-y-8">
        {/* Top Identity Header Card */}
        <motion.div
          initial={{ opacity: 0, y: -16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
          className="rounded-2xl sm:rounded-3xl bg-[rgba(14,14,18,0.78)] border border-white/10 p-6 sm:p-7 shadow-[0_20px_40px_rgba(0,0,0,0.5)]"
        >
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-5 sm:gap-6">
            {/* Left: Avatar & Identity Details */}
            <div className="flex items-start sm:items-center gap-4 sm:gap-5">
              <div className="relative shrink-0">
                <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-2xl bg-gradient-to-br from-[#E8602E] to-[#B3471D] p-[2px] shadow-lg shadow-[#E8602E]/20">
                  <div className="w-full h-full bg-[#121216] rounded-2xl flex items-center justify-center">
                    <span className="text-xl sm:text-2xl font-extrabold text-white font-heading">
                      {getInitial()}
                    </span>
                  </div>
                </div>
                <div className="absolute -bottom-1 -right-1 w-4 h-4 sm:w-5 sm:h-5 rounded-full bg-emerald-500 border-2 border-[#121216] flex items-center justify-center">
                  <CheckCircle2 className="w-2.5 h-2.5 sm:w-3 sm:h-3 text-black stroke-[3]" />
                </div>
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-[10px] font-mono uppercase tracking-widest text-emerald-400 font-semibold px-2 py-0.5 rounded bg-emerald-500/10 border border-emerald-500/25">
                    Verified User
                  </span>
                  {renderProviderBadge()}
                </div>
                <h1 className="text-xl sm:text-2xl font-extrabold text-white tracking-tight font-heading">
                  {profile?.name || userEmail?.split('@')[0] || 'ResumeLens User'}
                </h1>
                <p className="text-xs sm:text-sm font-mono text-neutral-400">
                  {profile?.email || userEmail}
                </p>
              </div>
            </div>

            {/* Right: Quick Navigation Actions */}
            <div className="flex items-center gap-3 pt-3 md:pt-0 border-t md:border-t-0 border-white/[0.08]">
              <Link
                to="/"
                className="px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold bg-[#E8602E] hover:bg-[#f06e3d] text-white active:scale-[0.99] transition-all duration-150 flex items-center gap-2 shadow-sm"
              >
                <span>New Scan</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
              <Link
                to="/history"
                className="px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold bg-white/5 hover:bg-white/10 text-neutral-200 hover:text-white border border-white/10 transition-all duration-150 flex items-center gap-2"
              >
                <span>History</span>
                <ExternalLink className="w-3.5 h-3.5 text-neutral-400" />
              </Link>
            </div>
          </div>
        </motion.div>

        {loading ? (
          <div className="rounded-2xl bg-[rgba(14,14,18,0.78)] border border-white/10 p-12 text-center text-sm text-neutral-400 flex flex-col items-center justify-center gap-3">
            <Loader2 className="w-6 h-6 animate-spin text-[#E8602E]" />
            <span>Loading account metrics...</span>
          </div>
        ) : error ? (
          <div className="rounded-2xl bg-red-500/10 border border-red-500/30 p-6 text-sm text-red-400 flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 shrink-0" />
            <span>{error}</span>
          </div>
        ) : (
          <>
            {/* 3 Metric Summary Cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 sm:gap-5">
              {/* Card 1: Scans */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.45, delay: 0.08, ease: [0.22, 1, 0.36, 1] }}
                whileHover={{ y: -3, transition: { duration: 0.2 } }}
                className="rounded-2xl bg-[rgba(14,14,18,0.78)] border border-white/10 p-5 shadow-sm"
              >
                <div className="flex items-center justify-between text-neutral-400 mb-2">
                  <span className="text-xs font-mono uppercase tracking-wider">Scans Completed</span>
                  <FileText className="w-4 h-4 text-[#E8602E]" />
                </div>
                <div className="text-2xl sm:text-3xl font-extrabold font-mono text-white">
                  {profile?.totalScans ?? 0}
                </div>
                <p className="text-xs text-neutral-400 mt-1">
                  Resumes scanned and saved in vault
                </p>
              </motion.div>

              {/* Card 2: Security & Provider */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.45, delay: 0.16, ease: [0.22, 1, 0.36, 1] }}
                whileHover={{ y: -3, transition: { duration: 0.2 } }}
                className="rounded-2xl bg-[rgba(14,14,18,0.78)] border border-white/10 p-5 shadow-sm"
              >
                <div className="flex items-center justify-between text-neutral-400 mb-2">
                  <span className="text-xs font-mono uppercase tracking-wider">Access Method</span>
                  <Shield className="w-4 h-4 text-emerald-400" />
                </div>
                <div className="text-lg sm:text-xl font-bold text-white uppercase font-mono mt-1">
                  {profile?.provider || 'LOCAL'}
                </div>
                <p className="text-xs text-neutral-400 mt-1">
                  Session protected by 24h JWT
                </p>
              </motion.div>

              {/* Card 3: Membership Date */}
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.45, delay: 0.24, ease: [0.22, 1, 0.36, 1] }}
                whileHover={{ y: -3, transition: { duration: 0.2 } }}
                className="rounded-2xl bg-[rgba(14,14,18,0.78)] border border-white/10 p-5 shadow-sm"
              >
                <div className="flex items-center justify-between text-neutral-400 mb-2">
                  <span className="text-xs font-mono uppercase tracking-wider">Member Since</span>
                  <Calendar className="w-4 h-4 text-cyan-400" />
                </div>
                <div className="text-lg sm:text-xl font-bold text-white mt-1">
                  {formatDate(profile?.createdAt)}
                </div>
                <p className="text-xs text-neutral-400 mt-1">
                  Active candidate profile
                </p>
              </motion.div>
            </div>

            {/* Account Specifications */}
            <motion.div
              initial={{ opacity: 0, y: 24 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2, ease: [0.22, 1, 0.36, 1] }}
              className="rounded-2xl sm:rounded-3xl bg-[rgba(14,14,18,0.78)] border border-white/10 p-6 sm:p-7 shadow-[0_20px_40px_rgba(0,0,0,0.5)]"
            >
              <div className="flex items-center justify-between border-b border-white/[0.08] pb-4 mb-5 sm:mb-6">
                <div>
                  <h2 className="text-base sm:text-lg font-bold text-white font-heading">
                    Account Specifications
                  </h2>
                  <p className="text-xs text-neutral-400 mt-0.5">
                    Metadata and security credentials associated with this account
                  </p>
                </div>
                <span className="text-xs font-mono text-neutral-500 uppercase">
                  Status: Active
                </span>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 sm:gap-5">
                <div className="p-4 rounded-xl bg-white/[0.02] border border-white/[0.06] space-y-1">
                  <div className="flex items-center gap-2 text-neutral-400 text-xs">
                    <User className="w-3.5 h-3.5 text-[#E8602E]" />
                    <span>Full Name</span>
                  </div>
                  <p className="text-sm font-semibold text-white pl-5.5">
                    {profile?.name || 'Not provided'}
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-white/[0.02] border border-white/[0.06] space-y-1">
                  <div className="flex items-center gap-2 text-neutral-400 text-xs">
                    <Mail className="w-3.5 h-3.5 text-[#E8602E]" />
                    <span>Registered Email</span>
                  </div>
                  <p className="text-sm font-mono text-white pl-5.5">
                    {profile?.email || userEmail}
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-white/[0.02] border border-white/[0.06] space-y-1">
                  <div className="flex items-center gap-2 text-neutral-400 text-xs">
                    <Shield className="w-3.5 h-3.5 text-emerald-400" />
                    <span>Authentication Method</span>
                  </div>
                  <p className="text-sm font-medium text-white pl-5.5">
                    {profile?.provider === 'GOOGLE'
                      ? 'Google OAuth 2.0'
                      : profile?.provider === 'GITHUB'
                        ? 'GitHub OAuth 2.0'
                        : 'Standard Email & Encrypted Password'}
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-white/[0.02] border border-white/[0.06] space-y-1">
                  <div className="flex items-center gap-2 text-neutral-400 text-xs">
                    <Calendar className="w-3.5 h-3.5 text-cyan-400" />
                    <span>Account Creation Date</span>
                  </div>
                  <p className="text-sm font-medium text-white pl-5.5">
                    {formatDate(profile?.createdAt)}
                  </p>
                </div>
              </div>
            </motion.div>

            {/* Danger Zone Card */}
            <motion.div
              initial={{ opacity: 0, y: 24 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.28, ease: [0.22, 1, 0.36, 1] }}
              className="rounded-2xl sm:rounded-3xl bg-red-950/20 border border-red-500/30 p-6 sm:p-7 shadow-[0_20px_40px_rgba(0,0,0,0.5)]"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-5 sm:gap-6">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <Trash2 className="w-4 h-4 text-red-400" />
                    <h3 className="text-base sm:text-lg font-bold text-red-400 font-heading">
                      Danger Zone
                    </h3>
                  </div>
                  <p className="text-xs sm:text-sm text-neutral-300 max-w-xl leading-relaxed">
                    Permanently delete your account, saved resume scans, section breakdown scores, and generated reports. All data is deleted across all storage tables. This action cannot be reversed.
                  </p>
                </div>

                <button
                  type="button"
                  onClick={() => {
                    setConfirmationInput('');
                    setDeleteError(null);
                    setIsModalOpen(true);
                  }}
                  className="self-start sm:self-center shrink-0 px-4 py-2.5 rounded-xl text-xs sm:text-sm font-semibold text-red-400 bg-red-500/10 hover:bg-red-500/20 border border-red-500/40 hover:border-red-500/60 transition-all cursor-pointer flex items-center gap-2"
                >
                  <Trash2 className="w-4 h-4" />
                  <span>Delete Account</span>
                </button>
              </div>
            </motion.div>
          </>
        )}
      </div>

      {/* Confirmation Modal with AnimatePresence & backdrop-blur-sm */}
      <AnimatePresence>
        {isModalOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-modal-heading"
            className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm"
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 12 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 12 }}
              transition={{ duration: 0.25, ease: [0.22, 1, 0.36, 1] }}
              className="w-full max-w-md rounded-2xl bg-[#121216] border border-white/10 p-6 sm:p-7 shadow-2xl space-y-4"
            >
            {/* Modal Header */}
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-red-500/10 border border-red-500/30 flex items-center justify-center text-red-400 shrink-0">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <div>
                <h3
                  id="delete-modal-heading"
                  className="text-base sm:text-lg font-bold text-white font-heading"
                >
                  Confirm Account Deletion
                </h3>
                <p className="text-xs text-neutral-400 mt-0.5">
                  This action is permanent and cannot be undone
                </p>
              </div>
            </div>

            {/* Description box */}
            <p className="text-xs text-neutral-300 leading-relaxed bg-white/[0.02] border border-white/[0.06] p-3.5 rounded-xl">
              Are you sure you want to delete your account? This will permanently erase your profile and cascade delete all resume scans, match scores, and analysis records.
            </p>

            {/* Form */}
            <form onSubmit={handleDeleteConfirm} className="space-y-4 pt-1">
              <div>
                <label
                  htmlFor="confirm-delete-input"
                  className="block text-xs font-medium text-neutral-300 mb-2"
                >
                  Type <span className="font-mono text-red-400 font-bold">DELETE</span> to confirm:
                </label>
                <input
                  id="confirm-delete-input"
                  type="text"
                  value={confirmationInput}
                  onChange={(e) => setConfirmationInput(e.target.value)}
                  placeholder="DELETE"
                  className="w-full px-3.5 py-2.5 bg-[#0a0a0c] border border-white/15 focus:border-red-500 focus:ring-1 focus:ring-red-500 rounded-xl text-sm text-white placeholder-neutral-500 font-mono tracking-wider focus:outline-none"
                  autoFocus
                />
              </div>

              {deleteError && (
                <div className="p-2.5 rounded-xl bg-red-500/10 border border-red-500/30 text-xs text-red-400 flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 shrink-0" />
                  <span>{deleteError}</span>
                </div>
              )}

              {/* Action buttons */}
              <div className="flex items-center justify-end gap-3 pt-3 border-t border-white/10">
                <button
                  type="button"
                  disabled={deleting}
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-neutral-300 hover:text-white bg-white/5 hover:bg-white/10 border border-white/10 transition-colors cursor-pointer"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={confirmationInput.trim() !== 'DELETE' || deleting}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-white bg-red-600 hover:bg-red-500 disabled:opacity-40 disabled:cursor-not-allowed transition-colors cursor-pointer flex items-center gap-2"
                >
                  {deleting && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
                  <span>{deleting ? 'Deleting account...' : 'Delete My Account'}</span>
                </button>
              </div>
            </form>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
