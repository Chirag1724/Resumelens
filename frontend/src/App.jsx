import { useState } from 'react';
import { Routes, Route, Link, Navigate, useNavigate, useLocation } from 'react-router-dom';
import { Menu, X } from 'lucide-react';
import { AuthProvider, useAuth } from './context/AuthContext';
import PageBackground from './components/PageBackground';
import ErrorBoundary from './components/ErrorBoundary';
import LoginPage from './pages/LoginPage';
import LandingPage from './pages/LandingPage';
import ReportPage from './pages/ReportPage';
import HistoryPage from './pages/HistoryPage';
import { NotFoundPage } from './pages/Placeholders';

function TopBar() {
  const { isAuthenticated, userEmail, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const isAuth = location.pathname === '/login';
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleHistoryClick = (e) => {
    if (!isAuthenticated) {
      e.preventDefault();
      navigate('/login');
    }
  };

  return (
    <header className="fixed top-0 left-0 right-0 z-50 px-4 sm:px-8 lg:px-12 w-full max-w-[1536px] mx-auto pt-3 sm:pt-5 pointer-events-none">
      <nav className="pointer-events-auto rounded-2xl sm:rounded-full bg-[rgba(14,14,18,0.75)] backdrop-blur-xl border border-white/10 px-5 sm:px-8 lg:px-10 py-3 sm:py-3.5 flex items-center justify-between shadow-[0_20px_50px_rgba(0,0,0,0.5),0_0_30px_rgba(232,96,46,0.06)]">
        {/* Left: Brand Logo & Wordmark */}
        <Link
          to="/"
          className="flex items-center gap-2.5 group focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#E8602E] rounded-xl"
          aria-label="ResumeLens Home"
        >
          <img
            src="/favicon.svg"
            alt="ResumeLens Logo"
            className="w-6 h-6 sm:w-7 sm:h-7 object-contain rounded-md transition-transform duration-150 group-hover:scale-105"
          />
          <span className="font-heading font-bold text-base sm:text-lg tracking-tight text-[var(--text)]">
            ResumeLens
          </span>
        </Link>

        {/* Center: Desktop Navigation Links */}
        <div className="hidden md:flex items-center gap-8 lg:gap-10 text-[15px] sm:text-base font-semibold">
          <Link
            to="/"
            className="text-neutral-300 hover:text-white transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#E8602E] rounded-md py-1"
          >
            Home
          </Link>
          <Link
            to="/history"
            onClick={handleHistoryClick}
            className="text-neutral-300 hover:text-white transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#E8602E] rounded-md py-1"
          >
            History
          </Link>
        </div>

        {/* Right: User Email / Auth Controls */}
        <div className="hidden md:flex items-center gap-4 text-sm">
          {isAuthenticated ? (
            <>
              <span className="text-xs sm:text-sm text-neutral-300 font-mono px-3.5 py-1.5 rounded-xl bg-white/5 border border-white/10 max-w-[220px] truncate">
                {userEmail}
              </span>
              <button
                type="button"
                onClick={logout}
                className="px-4 py-2 rounded-xl text-xs sm:text-sm font-semibold text-neutral-300 hover:text-red-400 hover:bg-red-500/10 border border-white/10 transition-colors duration-150 cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#E8602E]"
              >
                Logout
              </button>
            </>
          ) : isAuth ? (
            <Link
              to="/"
              className="px-5 sm:px-6 py-2 sm:py-2.5 rounded-full text-xs sm:text-sm font-semibold text-neutral-300 hover:text-white bg-white/5 hover:bg-white/10 border border-white/10 hover:border-white/20 transition-all duration-200"
            >
              Scanner
            </Link>
          ) : (
            <Link
              to="/login"
              className="px-6 sm:px-7 py-2.5 sm:py-3 rounded-full text-sm sm:text-[15px] font-bold bg-[#E8602E] hover:bg-[#f06e3d] text-white transition-all duration-200 transform hover:-translate-y-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#E8602E]"
            >
              Login
            </Link>
          )}
        </div>

        {/* Mobile menu toggle button */}
        <div className="md:hidden flex items-center">
          <button
            type="button"
            onClick={() => setMobileMenuOpen((prev) => !prev)}
            className="p-2 rounded-xl text-neutral-300 hover:text-white hover:bg-white/10 transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#E8602E] cursor-pointer"
            aria-label="Toggle navigation menu"
            aria-expanded={mobileMenuOpen}
          >
            {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </nav>

      {/* Mobile menu drawer */}
      {mobileMenuOpen && (
        <div className="pointer-events-auto md:hidden mt-3 rounded-2xl bg-[rgba(14,14,18,0.95)] backdrop-blur-xl border border-white/10 p-5 shadow-2xl flex flex-col gap-3">
          <Link
            to="/"
            onClick={() => setMobileMenuOpen(false)}
            className="text-base font-semibold text-neutral-300 hover:text-white transition-colors duration-150 py-2"
          >
            Home
          </Link>
          <Link
            to="/history"
            onClick={(e) => {
              setMobileMenuOpen(false);
              handleHistoryClick(e);
            }}
            className="text-base font-semibold text-neutral-300 hover:text-white transition-colors duration-150 py-2"
          >
            History
          </Link>
          {isAuthenticated ? (
            <div className="pt-3 border-t border-white/10 flex flex-col gap-2.5">
              <span className="text-xs text-neutral-400 font-mono truncate">
                {userEmail}
              </span>
              <button
                type="button"
                onClick={() => {
                  setMobileMenuOpen(false);
                  logout();
                }}
                className="text-left text-sm font-semibold text-red-400 hover:underline py-1 cursor-pointer"
              >
                Logout
              </button>
            </div>
          ) : isAuth ? (
            <Link
              to="/"
              onClick={() => setMobileMenuOpen(false)}
              className="text-center font-bold text-white bg-white/10 py-3 rounded-xl mt-2 transition-transform active:scale-95"
            >
              Scanner
            </Link>
          ) : (
            <Link
              to="/login"
              onClick={() => setMobileMenuOpen(false)}
              className="text-center font-bold text-white bg-[#E8602E] hover:bg-[#f06e3d] py-3 rounded-xl mt-2 transition-transform active:scale-95"
            >
              Login
            </Link>
          )}
        </div>
      )}
    </header>
  );
}

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export default function App() {
  const location = useLocation();
  const isHome = location.pathname === '/';
  const isAuth = location.pathname === '/login';

  return (
    <AuthProvider>
      <PageBackground isAuth={isAuth}>
        <TopBar />
        <main
          className={`flex-1 w-full ${
            isHome || isAuth ? '' : 'pt-24 sm:pt-28'
          } ${isAuth ? 'h-full max-h-screen overflow-hidden' : ''}`}
        >
          <Routes>
            <Route
              path="/"
              element={
                <ErrorBoundary pageName="Home">
                  <LandingPage />
                </ErrorBoundary>
              }
            />
            <Route
              path="/login"
              element={
                <ErrorBoundary pageName="Login">
                  <LoginPage />
                </ErrorBoundary>
              }
            />
            <Route
              path="/report/:id"
              element={
                <ErrorBoundary pageName="Report">
                  <ReportPage />
                </ErrorBoundary>
              }
            />
            <Route
              path="/history"
              element={
                <ProtectedRoute>
                  <ErrorBoundary pageName="History">
                    <HistoryPage />
                  </ErrorBoundary>
                </ProtectedRoute>
              }
            />
            <Route
              path="*"
              element={
                <ErrorBoundary pageName="Not Found">
                  <NotFoundPage />
                </ErrorBoundary>
              }
            />
          </Routes>
        </main>
      </PageBackground>
    </AuthProvider>
  );
}
