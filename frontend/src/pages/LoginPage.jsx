import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Mail, Lock, User, AlertCircle, ArrowRight } from 'lucide-react';

export default function LoginPage() {
  const [tab, setTab] = useState('login');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');

  const [touched, setTouched] = useState({});
  const [serverError, setServerError] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    document.title = tab === 'login' ? 'Login - ResumeLens' : 'Register - ResumeLens';
  }, [tab]);

  const { login, register } = useAuth();
  const navigate = useNavigate();

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

  const errors = {
    email: validateEmail(email),
    password: validatePassword(password),
  };

  const isFormValid = !errors.email && !errors.password;

  const handleBlur = (field) => {
    setTouched((prev) => ({ ...prev, [field]: true }));
  };

  const handleTabChange = (nextTab) => {
    setTab(nextTab);
    setServerError(null);
    setTouched({});
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setTouched({ email: true, password: true, name: true });
    setServerError(null);

    if (!isFormValid) {
      return;
    }

    setLoading(true);
    try {
      if (tab === 'login') {
        await login({ email: email.trim(), password });
      } else {
        await register({
          email: email.trim(),
          password,
          name: name.trim() || undefined,
        });
      }
      navigate('/');
    } catch (err) {
      setServerError(err.message || 'Authentication failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleGuestContinue = () => {
    navigate('/');
  };

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-8rem)] px-4 py-8">
      <div className="w-full max-w-[420px] bg-[var(--bg-card)] border border-[var(--border)] rounded-[12px] p-6 sm:p-8 hover:shadow-[var(--shadow-card-hover)] transition-shadow duration-300">
        <div className="text-center mb-6">
          <h1 className="text-[22px] font-bold font-heading text-[var(--text)] tracking-tight">
            {tab === 'login' ? 'Welcome back' : 'Create an account'}
          </h1>
          <p className="text-sm text-[var(--text-secondary)] mt-1.5">
            {tab === 'login'
              ? 'Sign in to access your saved resume analysis history.'
              : 'Sign up to track your resume score progression over time.'}
          </p>
        </div>

        <div className="flex border-b border-[var(--border)] mb-6">
          <button
            type="button"
            onClick={() => handleTabChange('login')}
            className={`flex-1 py-2 text-sm font-semibold text-center border-b-2 transition-colors duration-150 cursor-pointer ${
              tab === 'login'
                ? 'border-[var(--brand-500)] text-[var(--brand-500)]'
                : 'border-transparent text-[var(--text-secondary)] hover:text-[var(--text)]'
            }`}
          >
            Log in
          </button>
          <button
            type="button"
            onClick={() => handleTabChange('signup')}
            className={`flex-1 py-2 text-sm font-semibold text-center border-b-2 transition-colors duration-150 cursor-pointer ${
              tab === 'signup'
                ? 'border-[var(--brand-500)] text-[var(--brand-500)]'
                : 'border-transparent text-[var(--text-secondary)] hover:text-[var(--text)]'
            }`}
          >
            Create account
          </button>
        </div>

        {serverError && (
          <div
            role="alert"
            className="mb-5 p-3 rounded-[12px] bg-[rgba(239,68,68,0.12)] border border-[var(--danger)] text-[var(--text)] text-sm flex items-start gap-2.5"
          >
            <AlertCircle className="w-4 h-4 text-[var(--danger)] shrink-0 mt-0.5" />
            <span className="leading-tight">{serverError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate className="space-y-4">
          {tab === 'signup' && (
            <div>
              <label
                htmlFor="signup-name"
                className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5"
              >
                Full name <span className="text-[var(--text-tertiary)]">(optional)</span>
              </label>
              <div className="relative">
                <User className="w-4 h-4 text-[var(--text-tertiary)] absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
                <input
                  id="signup-name"
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Jane Doe"
                  className="w-full pl-9 pr-3 py-2 bg-[var(--bg-elevated)] border border-[var(--border)] focus:border-[var(--border-strong)] rounded-[9px] text-sm text-[var(--text)] placeholder-[var(--text-tertiary)] focus:outline-none focus:ring-2 focus:ring-[var(--brand-400)] focus:ring-offset-2 focus:ring-offset-[var(--bg-card)] transition-colors duration-150"
                />
              </div>
            </div>
          )}

          <div>
            <label
              htmlFor="auth-email"
              className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5"
            >
              Email address
            </label>
            <div className="relative">
              <Mail className="w-4 h-4 text-[var(--text-tertiary)] absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                id="auth-email"
                type="email"
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (serverError) setServerError(null);
                }}
                onBlur={() => handleBlur('email')}
                placeholder="user@example.com"
                className={`w-full pl-9 pr-3 py-2 bg-[var(--bg-elevated)] border rounded-[9px] text-sm text-[var(--text)] placeholder-[var(--text-tertiary)] focus:outline-none transition-colors duration-150 ${
                  touched.email && errors.email
                    ? 'border-[var(--danger)] focus:border-[var(--danger)] focus:ring-2 focus:ring-[var(--danger)] focus:ring-offset-2 focus:ring-offset-[var(--bg-card)]'
                    : 'border-[var(--border)] focus:border-[var(--border-strong)] focus:ring-2 focus:ring-[var(--brand-400)] focus:ring-offset-2 focus:ring-offset-[var(--bg-card)]'
                }`}
              />
            </div>
            {touched.email && errors.email && (
              <p className="text-xs text-[var(--danger)] mt-1.5">{errors.email}</p>
            )}
          </div>

          <div>
            <label
              htmlFor="auth-password"
              className="block text-xs font-medium text-[var(--text-secondary)] mb-1.5"
            >
              Password
            </label>
            <div className="relative">
              <Lock className="w-4 h-4 text-[var(--text-tertiary)] absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                id="auth-password"
                type="password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  if (serverError) setServerError(null);
                }}
                onBlur={() => handleBlur('password')}
                placeholder="At least 8 characters"
                className={`w-full pl-9 pr-3 py-2 bg-[var(--bg-elevated)] border rounded-[9px] text-sm text-[var(--text)] placeholder-[var(--text-tertiary)] focus:outline-none transition-colors duration-150 ${
                  touched.password && errors.password
                    ? 'border-[var(--danger)] focus:border-[var(--danger)] focus:ring-2 focus:ring-[var(--danger)] focus:ring-offset-2 focus:ring-offset-[var(--bg-card)]'
                    : 'border-[var(--border)] focus:border-[var(--border-strong)] focus:ring-2 focus:ring-[var(--brand-400)] focus:ring-offset-2 focus:ring-offset-[var(--bg-card)]'
                }`}
              />
            </div>
            {touched.password && errors.password && (
              <p className="text-xs text-[var(--danger)] mt-1.5">{errors.password}</p>
            )}
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full mt-2 py-2.5 px-4 bg-[var(--brand-500)] hover:bg-[var(--brand-600)] text-[var(--on-brand)] font-semibold text-sm rounded-[9px] transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-[var(--brand-400)] focus:ring-offset-2 focus:ring-offset-[var(--bg-card)] hover:shadow-[0_0_50px_rgba(232,96,46,0.20)] disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
          >
            {loading
              ? tab === 'login'
                ? 'Signing in...'
                : 'Creating account...'
              : tab === 'login'
                ? 'Sign in'
                : 'Create account'}
          </button>
        </form>

        <div className="relative my-6">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-[var(--border)]"></div>
          </div>
          <div className="relative flex justify-center text-xs">
            <span className="bg-[var(--bg-card)] px-3 text-[var(--text-secondary)]">
              Do not have an account?
            </span>
          </div>
        </div>

        <button
          type="button"
          onClick={handleGuestContinue}
          className="w-full py-2.5 px-4 bg-[var(--bg-elevated)] hover:bg-[var(--bg-card)] border border-[var(--border)] hover:border-[var(--border-strong)] text-[var(--text)] font-medium text-sm rounded-[9px] transition-colors duration-150 flex items-center justify-center gap-2 cursor-pointer focus:outline-none focus:ring-2 focus:ring-[var(--brand-400)] focus:ring-offset-2 focus:ring-offset-[var(--bg-card)]"
        >
          <span>Continue as guest</span>
          <ArrowRight className="w-4 h-4 text-[var(--text-secondary)]" />
        </button>
      </div>
    </div>
  );
}
