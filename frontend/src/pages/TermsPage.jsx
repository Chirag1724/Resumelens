import { Link } from 'react-router-dom';
import { ArrowLeft, FileText, CheckCircle2, AlertCircle, Scale, ShieldAlert } from 'lucide-react';

export default function TermsPage() {
  return (
    <div className="max-w-7xl xl:max-w-[1360px] mx-auto px-4 sm:px-6 lg:px-8 py-8 sm:py-12">
      {/* Back button */}
      <Link
        to="/"
        className="inline-flex items-center gap-2 text-xs font-medium text-[var(--text-tertiary)] hover:text-[var(--text)] transition-colors mb-8 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] rounded px-1.5 py-1"
      >
        <ArrowLeft className="w-3.5 h-3.5" />
        <span>Back to Home</span>
      </Link>

      {/* Header */}
      <header className="mb-10 pb-8 border-b border-[var(--border)]">
        <div className="flex items-center gap-2 mb-3">
          <span className="inline-flex items-center gap-1.5 text-[11px] font-mono uppercase tracking-[0.08em] px-2.5 py-1 rounded-[6px] bg-[var(--bg-elevated)] border border-[var(--border)] text-[var(--brand-500)]">
            <Scale className="w-3.5 h-3.5" />
            Legal Terms
          </span>
          <span className="text-xs font-mono text-[var(--text-tertiary)]">
            Effective: October 30, 2026
          </span>
        </div>
        <h1 className="text-3xl sm:text-4xl font-extrabold font-heading text-[var(--text)] tracking-tight">
          Terms of Service
        </h1>
        <p className="text-sm sm:text-base text-[var(--text-secondary)] mt-3 leading-relaxed max-w-3xl font-body">
          These terms govern your access to and use of ResumeLens. Distributed freely under the MIT License, our service is designed to help job seekers understand ATS parsing rules with transparent mathematical evaluation.
        </p>
      </header>

      {/* Quick Summary Grid */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5 mb-12">
        <div className="rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-6 flex flex-col gap-3">
          <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--brand-500)]">
            <CheckCircle2 className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-heading font-semibold text-sm text-[var(--text)] mb-1">
              100% User Ownership
            </h3>
            <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
              You retain all copyright, ownership, and rights to any resume content submitted to the system.
            </p>
          </div>
        </div>

        <div className="rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-5 flex flex-col gap-3">
          <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--brand-500)]">
            <AlertCircle className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-heading font-semibold text-sm text-[var(--text)] mb-1">
              Educational & Diagnostic
            </h3>
            <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
              Scores are algorithmic indicators. They do not guarantee job interviews, offers, or hiring decisions.
            </p>
          </div>
        </div>

        <div className="rounded-[12px] bg-[var(--bg-card)] border border-[var(--border)] p-5 flex flex-col gap-3">
          <div className="w-8 h-8 rounded-[8px] bg-[var(--bg-elevated)] border border-[var(--border)] flex items-center justify-center text-[var(--brand-500)]">
            <ShieldAlert className="w-4 h-4" />
          </div>
          <div>
            <h3 className="font-heading font-semibold text-sm text-[var(--text)] mb-1">
              MIT License ("As Is")
            </h3>
            <p className="text-xs text-[var(--text-secondary)] leading-relaxed">
              Software is provided free and open source without warranty of any kind, as expressed in the MIT License.
            </p>
          </div>
        </div>
      </div>

      {/* Terms Content Sections */}
      <div className="space-y-10 text-sm text-[var(--text-secondary)] leading-relaxed font-body">
        {/* 1. Acceptance */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">01.</span>
            Acceptance of Terms
          </h2>
          <p>
            By accessing or using ResumeLens (hosted web application, API endpoints, or open-source codebase), you agree to be bound by these Terms of Service and our{' '}
            <Link to="/privacy" className="text-[var(--brand-500)] hover:underline font-medium">
              Privacy Policy
            </Link>. If you do not agree to these terms, please do not use the application.
          </p>
        </section>

        {/* 2. Ownership & IP */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">02.</span>
            Intellectual Property & Content Ownership
          </h2>
          <p>
            You retain 100% full ownership, title, copyright, and all intellectual property rights in and to any documents, resumes, cover letters, and text you provide.
          </p>
          <p>
            You grant ResumeLens only the temporary, non-exclusive license strictly required to parse text in memory, compute TF-IDF terms, generate keyword gap analysis, and output the report back to you. ResumeLens claims zero rights or ownership over your professional materials.
          </p>
        </section>

        {/* 3. Educational Tool Disclaimer */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">03.</span>
            ATS Compatibility Disclaimer & No Hiring Guarantee
          </h2>
          <p>
            ResumeLens is an informational, educational, and diagnostic tool created to assist candidates in identifying common automated screening pitfalls (such as non-standard section headers, multi-column parsing failure, and keyword deficiencies).
          </p>
          <ul className="space-y-2 text-xs sm:text-sm pl-4 list-disc marker:text-[var(--brand-500)]">
            <li>
              <strong className="text-[var(--text)]">No Guarantee:</strong> A high score on ResumeLens does not guarantee an interview invitation, job offer, or hiring milestone from any employer.
            </li>
            <li>
              <strong className="text-[var(--text)]">Algorithmic Variations:</strong> Commercial Applicant Tracking Systems (e.g., Workday, Greenhouse, Taleo, Lever) use differing, proprietary, and employer-configured logic. ResumeLens scores provide an objective benchmark, not a definitive prediction.
            </li>
            <li>
              <strong className="text-[var(--text)]">Not Legal or Career Advice:</strong> Recommendations and AI fix prompts are suggestions only. Candidates remain solely responsible for the truthfulness, accuracy, and representation of their resumes.
            </li>
          </ul>
        </section>

        {/* 4. Acceptable Use */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">04.</span>
            Acceptable Use Policy
          </h2>
          <p>
            When utilizing ResumeLens, you agree to refrain from:
          </p>
          <ul className="space-y-2 text-xs sm:text-sm pl-4 list-disc marker:text-[var(--brand-500)]">
            <li>
              Submitting files containing malicious code, exploit payloads, zip bombs, or viruses intended to disrupt Apache PDFBox or backend services.
            </li>
            <li>
              Attempting denial-of-service attacks, automated API floods, or unauthorized penetration tests against hosted environments.
            </li>
            <li>
              Submitting content that infringes upon the intellectual property, privacy, or legal rights of third parties.
            </li>
          </ul>
        </section>

        {/* 5. Nominative Trademark Use */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">05.</span>
            Third-Party Trademarks & Nominative Use
          </h2>
          <p>
            Any references to third-party ATS platforms or software systems (including Workday, Greenhouse, Lever, Taleo, or LaTeX) are made solely for technical compatibility descriptions, educational reference, and nominative fair use. ResumeLens is an independent open-source tool and is not endorsed by, sponsored by, or affiliated with any third-party ATS vendor.
          </p>
        </section>

        {/* 6. Limitation of Liability */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">06.</span>
            Limitation of Liability
          </h2>
          <p className="uppercase text-xs font-mono text-[var(--text-tertiary)]">
            As set forth in the MIT License:
          </p>
          <p className="text-xs sm:text-sm italic border-l-2 border-[var(--border-strong)] pl-4 text-[var(--text-secondary)]">
            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
          </p>
        </section>

        {/* 7. Contact */}
        <section className="rounded-[18px] bg-[var(--bg-card)] border border-[var(--border)] p-6 sm:p-8 space-y-4">
          <h2 className="text-lg font-heading font-bold text-[var(--text)] flex items-center gap-2">
            <span className="font-mono text-xs text-[var(--brand-500)]">07.</span>
            Contact & Repository
          </h2>
          <p>
            Questions regarding these Terms of Service can be directed to the repository maintainer:
            <br />
            Maintainer: <span className="text-[var(--text)] font-medium">Chirag Dwivedi</span>
            <br />
            Email:{' '}
            <a
              href="mailto:chiragdwivediji@gmail.com"
              className="text-[var(--brand-500)] hover:underline font-medium"
            >
              chiragdwivediji@gmail.com
            </a>
            <br />
            Source Code:{' '}
            <a
              href="https://github.com/Chirag1724/Resumelens"
              target="_blank"
              rel="noopener noreferrer"
              className="text-[var(--brand-500)] hover:underline font-medium"
            >
              github.com/Chirag1724/Resumelens
            </a>
          </p>
        </section>
      </div>
    </div>
  );
}
