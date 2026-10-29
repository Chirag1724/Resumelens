import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Mail,
  Lock,
  User,
  AlertCircle,
  Eye,
  EyeOff,
  Loader2,
  FileText,
  CheckCircle2,
} from 'lucide-react';
import HeroBackground from '../components/HeroBackground';
import DocumentMockup from '../components/DocumentMockup';
import ScoreGauge from '../components/ScoreGauge';

const PREVIEW_DIMENSIONS = [
  { label: 'ATS Compatibility', score: 96, color: 'bg-emerald-500' },
  { label: 'Skills Alignment', score: 88, color: 'bg-[#E8602E]' },
  { label: 'Quantified Impact', score: 82, color: 'bg-[#ff7a45]' },
  { label: 'Format Safety', score: 100, color: 'bg-emerald-500' },
];

const PREVIEW_CHECKS = [
  'Single-column parse-safe layout verified',
  'Standard section headers detected',
  '36 of 42 target keywords matched',
];

const PREVIEW_KEYWORDS = ['Java 17', 'Spring Boot', 'REST APIs', 'Docker', 'SQL'];

export default function LoginPage() {
  const [tab, setTab] = useState('login'); // 'login' | 'signup'

  // Login form state
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  const [loginTouched, setLoginTouched] = useState({});
  const [showLoginPassword, setShowLoginPassword] = useState(false);

  // Signup form state
  const [signupName, setSignupName] = useState('');
  const [signupEmail, setSignupEmail] = useState('');
  const [signupPassword, setSignupPassword] = useState('');
  const [signupTouched, setSignupTouched] = useState({});
  const [showSignupPassword, setShowSignupPassword] = useState(false);

  const [serverError, setServerError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { login, register } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    document.title = tab === 'login' ? 'Sign In - ResumeLens' : 'Create Account - ResumeLens';
  }, [tab]);

  const validateEmail = (val) => {
    if (!val || !val.trim()) return 'Email is required';
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(val.trim())) return 'Please enter a valid email address';
    return null;
  };

  const validatePassword = (val) => {
    if (!val) return 'Password is required';
    if (val.length < 8) return 'Password must be at least 8 characters';
    return null;
  };

  const loginErrors = {
    email: validateEmail(loginEmail),
    password: validatePassword(loginPassword),
  };

  const signupErrors = {
    email: validateEmail(signupEmail),
    password: validatePassword(signupPassword),
  };

  const handleTabFlip = (targetTab) => {
    if (tab === targetTab) return;
    setTab(targetTab);
    setServerError(null);
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setLoginTouched({ email: true, password: true });
    setServerError(null);

    if (loginErrors.email || loginErrors.password) {
      return;
    }

    setLoading(true);
    try {
      await login({ email: loginEmail.trim(), password: loginPassword });
      navigate('/');
    } catch (err) {
      setServerError(err.message || 'Authentication failed. Please verify your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleSignupSubmit = async (e) => {
    e.preventDefault();
    setSignupTouched({ email: true, password: true });
    setServerError(null);

    if (signupErrors.email || signupErrors.password) {
      return;
    }

    setLoading(true);
    try {
      await register({
        email: signupEmail.trim(),
        password: signupPassword,
        name: signupName.trim() || undefined,
      });
      navigate('/');
    } catch (err) {
      setServerError(err.message || 'Account registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-[100dvh] w-full flex flex-col justify-center items-center px-4 sm:px-6 lg:px-8 pt-20 pb-12 sm:pt-24 sm:pb-16 overflow-x-hidden">
      {/* Background with dot matrix and atmospheric ambient glow */}
      <HeroBackground />

      <div className="relative z-10 w-full max-w-7xl xl:max-w-[1320px] mx-auto grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-10 xl:gap-14 items-center">
        {/* Left Column: Live Resume Scan and ATS Score Preview (visible on lg+) */}
        <div className="hidden lg:flex lg:col-span-7 flex-col gap-4">
          <div className="space-y-1 mb-1">
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              <span className="text-xs font-mono uppercase tracking-wider text-neutral-300">
                Live Scanner Preview
              </span>
            </div>
            <h2 className="text-2xl xl:text-3xl font-extrabold text-white tracking-tight font-heading">
              Real-time ATS parsing and scoring.
            </h2>
          </div>

          <div className="relative w-full">
            {/* Ambient back-glow behind the preview card */}
            <div
              className="pointer-events-none absolute -inset-1 rounded-[2.5rem] bg-gradient-to-br from-[#E8602E]/20 via-[#E8602E]/5 to-transparent blur-2xl opacity-60"
              aria-hidden="true"
            />

            <div className="relative w-full rounded-2xl sm:rounded-3xl bg-[rgba(14,14,18,0.78)] backdrop-blur-2xl border border-white/10 p-6 xl:p-7 shadow-[0_24px_50px_rgba(0,0,0,0.6)]">
              {/* Top meta status bar */}
              <div className="flex items-center justify-between border-b border-white/[0.08] pb-4 mb-6">
                <div className="flex items-center gap-2.5">
                  <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                  <span className="text-xs font-mono text-neutral-300 font-medium">
                    SCANNING ACTIVE
                  </span>
                </div>
                <span className="text-[11px] font-mono text-neutral-400 uppercase tracking-wider">
                  Profile: Software Engineer
                </span>
              </div>

              {/* Grid: Document Scan on left, Score and Dimensions on right */}
              <div className="grid grid-cols-1 sm:grid-cols-12 gap-6 xl:gap-8 items-center">
                {/* Left: Document Mockup with active scan line */}
                <div className="sm:col-span-5 flex justify-center">
                  <div className="w-full max-w-[220px] sm:max-w-[230px]">
                    <DocumentMockup scanning={true} />
                  </div>
                </div>

                {/* Right: ATS Score and Dimensions */}
                <div className="sm:col-span-7 flex flex-col gap-4">
                  {/* Score gauge readout */}
                  <div className="flex items-center gap-4 xl:gap-5">
                    <ScoreGauge score={88} tierLabel="Strong Match" size={135} showLabel={false} />
                    <div>
                      <span className="text-[11px] font-mono uppercase tracking-wider text-emerald-400 font-semibold px-2 py-0.5 rounded bg-emerald-500/10 border border-emerald-500/25">
                        Strong Match
                      </span>
                      <h3 className="text-lg font-bold text-white mt-1.5 font-heading">
                        ATS Score: 88/100
                      </h3>
                      <p className="text-xs text-neutral-400 mt-0.5">
                        Enterprise recruiter screening ready
                      </p>
                    </div>
                  </div>

                  {/* Dimension progress bars */}
                  <div className="space-y-2 pt-1">
                    {PREVIEW_DIMENSIONS.map((dim, idx) => (
                      <div key={idx} className="space-y-1">
                        <div className="flex justify-between text-xs">
                          <span className="text-neutral-300 font-medium text-[11px]">{dim.label}</span>
                          <span className="font-mono text-neutral-400 text-[11px]">{dim.score}%</span>
                        </div>
                        <div className="h-1.5 w-full bg-white/[0.06] rounded-full overflow-hidden">
                          <div
                            className={`h-full ${dim.color} rounded-full transition-all duration-700`}
                            style={{ width: `${dim.score}%` }}
                          />
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Verified safety checks */}
                  <div className="space-y-1.5 pt-2 border-t border-white/[0.08]">
                    {PREVIEW_CHECKS.map((text, idx) => (
                      <div key={idx} className="flex items-center gap-2 text-xs text-neutral-300">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400 shrink-0" />
                        <span>{text}</span>
                      </div>
                    ))}
                  </div>

                  {/* Matched keyword chips */}
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {PREVIEW_KEYWORDS.map((kw, idx) => (
                      <span
                        key={idx}
                        className="text-[10px] font-mono px-2 py-0.5 rounded bg-white/[0.04] border border-white/10 text-neutral-300"
                      >
                        +{kw}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Clean Auth Card */}
        <div className="lg:col-span-5 w-full flex justify-center lg:justify-end">
          <div className="relative w-full max-w-[420px]">
            {/* Subtle ambient back-glow behind the card */}
            <div
              className="pointer-events-none absolute -inset-1 rounded-[2rem] bg-gradient-to-b from-[#E8602E]/15 via-[#E8602E]/5 to-transparent blur-2xl opacity-60"
              aria-hidden="true"
            />

            {/* Clean, minimal glassmorphic auth card */}
            <div className="relative w-full rounded-2xl sm:rounded-3xl bg-[rgba(14,14,18,0.78)] backdrop-blur-2xl border border-white/10 p-6 sm:p-8 shadow-[0_24px_50px_rgba(0,0,0,0.6)]">
              {/* Top minimal brand icon */}
              <div className="flex justify-center mb-4">
                <div className="w-10 h-10 rounded-xl bg-white/[0.04] border border-white/10 flex items-center justify-center p-2 shadow-sm">
                  <img src="/favicon.svg" alt="ResumeLens Logo" className="w-6 h-6 object-contain" />
                </div>
              </div>

              {/* Header text */}
              <div className="text-center mb-6">
                <h1 className="text-xl sm:text-2xl font-bold text-white tracking-tight font-heading">
                  {tab === 'login' ? 'Welcome back' : 'Create an account'}
                </h1>
                <p className="text-xs text-neutral-400 mt-1.5">
                  {tab === 'login'
                    ? 'Sign in to access your saved resume scans'
                    : 'Start analyzing your resumes with ATS precision'}
                </p>
              </div>

              {/* Understated segmented tab control */}
              <div className="flex rounded-xl bg-white/[0.03] p-1 border border-white/[0.08] mb-5">
                <button
                  type="button"
                  onClick={() => handleTabFlip('login')}
                  className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all duration-200 cursor-pointer ${
                    tab === 'login'
                      ? 'bg-[#1e1e24] text-white shadow-sm border border-white/10'
                      : 'text-neutral-400 hover:text-white'
                  }`}
                >
                  Sign In
                </button>
                <button
                  type="button"
                  onClick={() => handleTabFlip('signup')}
                  className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all duration-200 cursor-pointer ${
                    tab === 'signup'
                      ? 'bg-[#1e1e24] text-white shadow-sm border border-white/10'
                      : 'text-neutral-400 hover:text-white'
                  }`}
                >
                  Create Account
                </button>
              </div>

              {/* Server error alert */}
              {serverError && (
                <div
                  role="alert"
                  className="mb-4 p-2.5 rounded-xl bg-red-500/10 border border-red-500/30 text-xs text-red-400 flex items-start gap-2 leading-relaxed"
                >
                  <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
                  <span>{serverError}</span>
                </div>
              )}

              {/* Tab 1: Sign In */}
              {tab === 'login' ? (
                <form onSubmit={handleLoginSubmit} noValidate className="space-y-4">
                  <div>
                    <label
                      htmlFor="login-email"
                      className="block text-xs font-medium text-neutral-300 mb-1.5"
                    >
                      Email address
                    </label>
                    <div className="relative">
                      <Mail className="w-4 h-4 text-neutral-500 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="login-email"
                        type="email"
                        value={loginEmail}
                        onChange={(e) => {
                          setLoginEmail(e.target.value);
                          if (serverError) setServerError(null);
                        }}
                        onBlur={() => setLoginTouched((prev) => ({ ...prev, email: true }))}
                        placeholder="you@example.com"
                        className={`w-full pl-10 pr-4 py-2.5 bg-[#101014] border rounded-xl text-sm text-white placeholder-neutral-500 focus:outline-none transition-all ${
                          loginTouched.email && loginErrors.email
                            ? 'border-red-500/60 focus:border-red-500 focus:ring-1 focus:ring-red-500'
                            : 'border-white/10 hover:border-white/20 focus:border-[#E8602E]/80 focus:ring-1 focus:ring-[#E8602E]/40'
                        }`}
                      />
                    </div>
                    {loginTouched.email && loginErrors.email && (
                      <p className="text-[11px] text-red-400 mt-1 font-medium">{loginErrors.email}</p>
                    )}
                  </div>

                  <div>
                    <label
                      htmlFor="login-password"
                      className="block text-xs font-medium text-neutral-300 mb-1.5"
                    >
                      Password
                    </label>
                    <div className="relative">
                      <Lock className="w-4 h-4 text-neutral-500 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="login-password"
                        type={showLoginPassword ? 'text' : 'password'}
                        value={loginPassword}
                        onChange={(e) => {
                          setLoginPassword(e.target.value);
                          if (serverError) setServerError(null);
                        }}
                        onBlur={() => setLoginTouched((prev) => ({ ...prev, password: true }))}
                        placeholder="Enter your password"
                        className={`w-full pl-10 pr-10 py-2.5 bg-[#101014] border rounded-xl text-sm text-white placeholder-neutral-500 focus:outline-none transition-all ${
                          loginTouched.password && loginErrors.password
                            ? 'border-red-500/60 focus:border-red-500 focus:ring-1 focus:ring-red-500'
                            : 'border-white/10 hover:border-white/20 focus:border-[#E8602E]/80 focus:ring-1 focus:ring-[#E8602E]/40'
                        }`}
                      />
                      <button
                        type="button"
                        onClick={() => setShowLoginPassword((prev) => !prev)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-neutral-500 hover:text-white transition-colors cursor-pointer p-1"
                        aria-label={showLoginPassword ? 'Hide password' : 'Show password'}
                      >
                        {showLoginPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                      </button>
                    </div>
                    {loginTouched.password && loginErrors.password && (
                      <p className="text-[11px] text-red-400 mt-1 font-medium">{loginErrors.password}</p>
                    )}
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full py-2.5 px-4 rounded-xl text-sm font-semibold bg-[#E8602E] hover:bg-[#f06e3d] text-white active:scale-[0.99] transition-all duration-150 cursor-pointer flex items-center justify-center gap-2 mt-2 disabled:opacity-50"
                  >
                    {loading ? (
                      <>
                        <Loader2 className="w-4 h-4 animate-spin text-white" />
                        <span>Signing in...</span>
                      </>
                    ) : (
                      <span>Sign in</span>
                    )}
                  </button>
                </form>
              ) : (
                /* Tab 2: Sign Up */
                <form onSubmit={handleSignupSubmit} noValidate className="space-y-4">
                  <div>
                    <div className="flex items-center justify-between mb-1.5">
                      <label
                        htmlFor="signup-name"
                        className="block text-xs font-medium text-neutral-300"
                      >
                        Full name
                      </label>
                      <span className="text-[10px] font-mono text-neutral-500 uppercase tracking-wider">
                        Optional
                      </span>
                    </div>
                    <div className="relative">
                      <User className="w-4 h-4 text-neutral-500 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="signup-name"
                        type="text"
                        value={signupName}
                        onChange={(e) => setSignupName(e.target.value)}
                        placeholder="Your name"
                        className="w-full pl-10 pr-4 py-2.5 bg-[#101014] border border-white/10 hover:border-white/20 focus:border-[#E8602E]/80 focus:ring-1 focus:ring-[#E8602E]/40 rounded-xl text-sm text-white placeholder-neutral-500 focus:outline-none transition-all"
                      />
                    </div>
                  </div>

                  <div>
                    <label
                      htmlFor="signup-email"
                      className="block text-xs font-medium text-neutral-300 mb-1.5"
                    >
                      Email address
                    </label>
                    <div className="relative">
                      <Mail className="w-4 h-4 text-neutral-500 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="signup-email"
                        type="email"
                        value={signupEmail}
                        onChange={(e) => {
                          setSignupEmail(e.target.value);
                          if (serverError) setServerError(null);
                        }}
                        onBlur={() => setSignupTouched((prev) => ({ ...prev, email: true }))}
                        placeholder="you@example.com"
                        className={`w-full pl-10 pr-4 py-2.5 bg-[#101014] border rounded-xl text-sm text-white placeholder-neutral-500 focus:outline-none transition-all ${
                          signupTouched.email && signupErrors.email
                            ? 'border-red-500/60 focus:border-red-500 focus:ring-1 focus:ring-red-500'
                            : 'border-white/10 hover:border-white/20 focus:border-[#E8602E]/80 focus:ring-1 focus:ring-[#E8602E]/40'
                        }`}
                      />
                    </div>
                    {signupTouched.email && signupErrors.email && (
                      <p className="text-[11px] text-red-400 mt-1 font-medium">{signupErrors.email}</p>
                    )}
                  </div>

                  <div>
                    <label
                      htmlFor="signup-password"
                      className="block text-xs font-medium text-neutral-300 mb-1.5"
                    >
                      Password
                    </label>
                    <div className="relative">
                      <Lock className="w-4 h-4 text-neutral-500 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                      <input
                        id="signup-password"
                        type={showSignupPassword ? 'text' : 'password'}
                        value={signupPassword}
                        onChange={(e) => {
                          setSignupPassword(e.target.value);
                          if (serverError) setServerError(null);
                        }}
                        onBlur={() => setSignupTouched((prev) => ({ ...prev, password: true }))}
                        placeholder="At least 8 characters"
                        className={`w-full pl-10 pr-10 py-2.5 bg-[#101014] border rounded-xl text-sm text-white placeholder-neutral-500 focus:outline-none transition-all ${
                          signupTouched.password && signupErrors.password
                            ? 'border-red-500/60 focus:border-red-500 focus:ring-1 focus:ring-red-500'
                            : 'border-white/10 hover:border-white/20 focus:border-[#E8602E]/80 focus:ring-1 focus:ring-[#E8602E]/40'
                        }`}
                      />
                      <button
                        type="button"
                        onClick={() => setShowSignupPassword((prev) => !prev)}
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-neutral-500 hover:text-white transition-colors cursor-pointer p-1"
                        aria-label={showSignupPassword ? 'Hide password' : 'Show password'}
                      >
                        {showSignupPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                      </button>
                    </div>
                    {signupTouched.password && signupErrors.password && (
                      <p className="text-[11px] text-red-400 mt-1 font-medium">{signupErrors.password}</p>
                    )}
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full py-2.5 px-4 rounded-xl text-sm font-semibold bg-[#E8602E] hover:bg-[#f06e3d] text-white active:scale-[0.99] transition-all duration-150 cursor-pointer flex items-center justify-center gap-2 mt-2 disabled:opacity-50"
                  >
                    {loading ? (
                      <>
                        <Loader2 className="w-4 h-4 animate-spin text-white" />
                        <span>Creating account...</span>
                      </>
                    ) : (
                      <span>Create account</span>
                    )}
                  </button>
                </form>
              )}

              {/* Footer actions */}
              <div className="pt-5 border-t border-white/[0.08] mt-5 text-center space-y-3">
                <p className="text-xs text-neutral-400">
                  {tab === 'login' ? (
                    <>
                      Do not have an account?{' '}
                      <button
                        type="button"
                        onClick={() => handleTabFlip('signup')}
                        className="font-medium text-[#E8602E] hover:text-[#ff7a45] transition-colors cursor-pointer"
                      >
                        Sign up
                      </button>
                    </>
                  ) : (
                    <>
                      Already have an account?{' '}
                      <button
                        type="button"
                        onClick={() => handleTabFlip('login')}
                        className="font-medium text-[#E8602E] hover:text-[#ff7a45] transition-colors cursor-pointer"
                      >
                        Sign in
                      </button>
                    </>
                  )}
                </p>

                <div>
                  <button
                    type="button"
                    onClick={() => navigate('/')}
                    className="text-xs text-neutral-400 hover:text-white transition-colors cursor-pointer"
                  >
                    Continue as guest
                  </button>
                </div>

                <div className="pt-2 border-t border-white/[0.06] text-center">
                  <p className="text-[11px] text-neutral-500 leading-relaxed">
                    By continuing, you agree to ResumeLens's{' '}
                    <Link to="/terms" className="text-neutral-400 hover:text-white underline">
                      Terms of Service
                    </Link>{' '}
                    and{' '}
                    <Link to="/privacy" className="text-neutral-400 hover:text-white underline">
                      Privacy Policy
                    </Link>.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
